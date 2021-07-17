@file:JvmName("AnnotationParser")

package io.github.alexbeggs.kspannotationparser

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

/** TODO **/

fun <T : Annotation> KSAnnotated.getAnnotation(annotationKClass: KClass<T>): T? {
    return this.annotations.firstOrNull { annotationKClass.qualifiedName == it.annotationType.resolve().declaration.qualifiedName?.asString() }
        ?.toAnnotation(annotationKClass)
}

/**
 * Test for inherited annotations
 */
fun KSAnnotated.getAnnotations(): Sequence<Annotation> {
    return this.annotations.map { it.toAnnotation(Annotation::class) }
}

fun KSAnnotated.getDirectAnnotations(): Sequence<Annotation> {
    return this.annotations.map { it.toAnnotation(Annotation::class) }
}

internal fun <T : Annotation> KSAnnotation.toAnnotation(annotationKClass: KClass<T>): T {
    val clazz = annotationKClass.java
    @Suppress("UNCHECKED_CAST")
    return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), createInvocationHandler(clazz)) as T
}

val lruCacheMap = object : LinkedHashMap<Any, Proxy>() {
    val MAX = 10
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Any, Proxy>?): Boolean {
        return size > MAX;
    }
}

@Suppress("TooGenericExceptionCaught")
private fun KSAnnotation.createInvocationHandler(clazz: Class<*>): InvocationHandler {
    val cache = mutableMapOf<Any, Any>()
    return InvocationHandler { _, method, _ ->
        if (method.name == "toString" && arguments.none { it.name?.asString() == "toString" }) {
            // toStringProvider()
            "${clazz.canonicalName}@${Integer.toHexString(arguments.hashCode())}"
        } else {
            val argument = try {
                arguments.first { it.name?.asString() == method.name }
            } catch (e: NullPointerException) {
                throw IllegalArgumentException("This is a bug using the default KClass for an annotation", e)
            }
            when (val result = argument.value ?: method.defaultValue) {
                is Proxy -> result
                // TODO verify this works w/ list of annotations
                is ArrayList<*> -> {
                    val defaultValue = { result.asArray(method) }
                    cache.getOrPut(result, defaultValue)
                }
                else -> {
                    when {
                        method.returnType.isEnum -> {
                            val defaultValue = { result.asEnum(method.returnType) }
                            cache.getOrPut(result, defaultValue)
                        }
                        method.returnType.isAnnotation -> {
                            val defaultValue = { (result as KSAnnotation).asAnnotation(method.returnType) }
                            cache.getOrPut(result, defaultValue)
                        }
                        method.returnType.name == "java.lang.Class" -> {
                            val defaultValue = { result.asClass() }
                            cache.getOrPut(result, defaultValue)
                        }
                        method.returnType.name == "byte" -> {
                            val defaultValue = { result.asByte() }
                            cache.getOrPut(result, defaultValue)
                        }
                        else -> result // original value
                    }
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun KSAnnotation.asAnnotation(
    annotationInterface: Class<*>
): Any {
    // return lruCacheMap.getOrPut(this) {

    // }
    return Proxy.newProxyInstance(
        this.javaClass.classLoader, arrayOf(annotationInterface),
        this.createInvocationHandler(annotationInterface)
    ) as Proxy
}

@Suppress("UNCHECKED_CAST")
private fun <T> Any.asEnum(returnType: Class<T>): T =
    returnType.getDeclaredMethod("valueOf", String::class.java).invoke(null, this.toString()) as T

private fun Any.asByte(): Byte = if (this is Int) this.toByte() else this as Byte

private fun Any.asClass() =
    Class.forName((this as KSType).declaration.qualifiedName!!.asString())

@Suppress("UNCHECKED_CAST")
private fun ArrayList<*>.asArray(method: Method) =
    when (method.returnType.componentType.name) {
        "boolean" -> (this as ArrayList<Boolean>).toBooleanArray()
        "byte" -> (this as ArrayList<Byte>).toByteArray()
        "char" -> (this as ArrayList<Char>).toCharArray()
        "double" -> (this as ArrayList<Double>).toDoubleArray()
        "float" -> (this as ArrayList<Float>).toFloatArray()
        "int" -> (this as ArrayList<Int>).toIntArray()
        "long" -> (this as ArrayList<Long>).toLongArray()
        "java.lang.Class" -> (this as ArrayList<KSType>).map {
            Class.forName(it.declaration.qualifiedName!!.asString())
        }.toTypedArray()
        "java.lang.String" -> (this as ArrayList<String>).toTypedArray()
        else -> { // arrays of enums or annotations
            when {
                method.returnType.componentType.isEnum -> {
                    val array: Array<Any> = java.lang.reflect.Array.newInstance(
                        method.returnType.componentType,
                        this.size
                    ) as Array<Any>
                    for (r in 0 until this.size) {
                        array[r] = method.returnType.componentType.valueOf(this[r].toString())
                    }
                    array
                }
                method.returnType.componentType.isAnnotation -> {
                    val array: Array<Any> = java.lang.reflect.Array.newInstance(
                        method.returnType.componentType,
                        this.size
                    ) as Array<Any>
                    for (r in 0 until this.size) {
                        val ksAnnotation = this[r] as KSAnnotation
                        val j = ksAnnotation.asAnnotation(method.returnType.componentType)
                        array[r] = j
                    }
                    array
                }
                else -> {
                    throw IllegalStateException(
                        "Unable to process type ${method.returnType.componentType.name}"
                    )
                }
            }
        }
    }

private fun <T> Class<T>.valueOf(value: String): T {
    if (this.isEnum) {
        val valueOfMethod = this.getDeclaredMethod("valueOf", String::class.java)
        @Suppress("UNCHECKED_CAST")
        return valueOfMethod.invoke(null, value) as T
    } else {
        throw IllegalArgumentException("$this is not an Enum type")
    }
}
