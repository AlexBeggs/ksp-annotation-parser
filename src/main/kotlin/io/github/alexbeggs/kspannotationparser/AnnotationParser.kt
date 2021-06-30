@file:JvmName("AnnotationParser")
package io.github.alexbeggs.kspannotationparser

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

fun <T : Annotation> KSAnnotation.getAnnotation(annotationKClass: KClass<T>): T {
    val clazz = annotationKClass.java
    if (this.annotationType.resolve().declaration.qualifiedName?.asString() != clazz.canonicalName) {
        throw IllegalArgumentException(
            "Class ${this.annotationType.resolve().declaration.qualifiedName?.asString()} " +
                "cannot be cast to ${clazz.canonicalName}"
        )
    }
    @Suppress("UNCHECKED_CAST")
    return Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), createInvocationHandler(clazz)) as T
}

@Suppress("TooGenericExceptionCaught")
private fun <T : Annotation> KSAnnotation.createInvocationHandler(annotationClass: Class<T>) =
    InvocationHandler { _, method, _ ->
        if (method.name == "toString" && arguments.none { it.name?.asString() == "toString" }) {
            "${annotationClass.canonicalName}@${Integer.toHexString(arguments.hashCode())}"
        } else {
            val argument = try {
                arguments.first { it.name?.asString() == method.name }
            } catch (e: NullPointerException) {
                throw IllegalArgumentException("This is a bug using the default KClass for an annotation", e)
            }
            val result = argument.value ?: method.defaultValue
            if (result is ArrayList<*>) {
                result.toArray(method)
            } else {
                when {
                    method.returnType.isEnum -> Class.forName(method.returnType.name).valueOf(result.toString())
                    method.returnType.name == "java.lang.Class" ->
                        // Class.forName(method.clazz.name)
                        Class.forName((result as KSType).declaration.qualifiedName!!.asString())
                    method.returnType.name == "byte" -> if (result is Int) result.toByte() else result
                    else -> result // original value
                }
            }
        }
    }

@Suppress("UNCHECKED_CAST")
private fun ArrayList<*>.toArray(method: Method) =
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
        else -> { // enums
            if (method.returnType.componentType.isEnum) {
                val array: Array<Any> = java.lang.reflect.Array.newInstance(
                    method.returnType.componentType,
                    this.size
                ) as Array<Any>
                for (r in 0 until this.size) {
                    array[r] = method.returnType.componentType.valueOf(this[r].toString())
                }
                array
            } else {
                throw IllegalStateException(
                    "Unable to process type ${method.returnType.componentType.name}"
                )
            }
        }
    }

private fun <T> Class<T>.valueOf(value: String): T {
    if (this.isEnum) {
        val valueOfMethod = this.methods.first { m -> m.name == "valueOf" }
        @Suppress("UNCHECKED_CAST")
        return valueOfMethod.invoke(null, value) as T
    } else {
        throw IllegalArgumentException("$this is not an Enum type")
    }
}
