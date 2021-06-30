package io.github.alexbeggs.kspannotationparser

import com.google.common.truth.Truth.assertThat
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.reflect.KClass

class AnnotationParserTest {

    @ParametersTestAnnotation(kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testDefaultValues() {
        setupProcessor("testDefaultValues", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.booleanValue).isFalse()
            assertThat(annotation.byteValue).isEqualTo(2)
            assertThat(annotation.charValue).isEqualTo('b')
            assertThat(annotation.doubleValue).isEqualTo(3.0)
            assertThat(annotation.floatValue).isEqualTo(4.0f)
            assertThat(annotation.intValue).isEqualTo(5)
            assertThat(annotation.longValue).isEqualTo(6L)
            assertThat(annotation.stringValue).isEqualTo("emptystring")
            assertThat(annotation.kClassValue).isEqualTo(ParametersTestAnnotation::class)
            assertThat(annotation.enumValue).isEqualTo(TestEnum.NONE)
        }
    }

    @ParametersTestWithNegativeDefaultsAnnotation
    @Test
    fun testDefaultWithNegativeValues() {
        setupProcessor(
            "testDefaultWithNegativeValues",
            ParametersTestWithNegativeDefaultsAnnotation::class
        ) { annotation ->
            assertThat(annotation.byteValue).isEqualTo(-2)
            assertThat(annotation.doubleValue).isEqualTo(-3.0)
            assertThat(annotation.floatValue).isEqualTo(-4.0f)
            assertThat(annotation.intValue).isEqualTo(-5)
            assertThat(annotation.longValue).isEqualTo(-6L)
        }
    }

    @ParametersTestAnnotation
    @Test
    @Disabled
    fun testDefaultKClassValueNPEBug() {
        setupProcessor("testDefaultKClassValueNPEBug", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.kClassValue).isEqualTo(ParametersTestAnnotation::class)
        }
    }

    @ParametersTestAnnotation(booleanValue = true, kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testBooleanValue() {
        setupProcessor("testBooleanValue", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.booleanValue).isTrue()
        }
    }

    @ParametersTestAnnotation(byteValue = 23, kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testByteValue() {
        setupProcessor("testByteValue", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.byteValue).isEqualTo(23)
        }
    }

    @ParametersTestAnnotation(charValue = 'a', kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testCharValue() {
        setupProcessor("testCharValue", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.charValue).isEqualTo('a')
        }
    }

    @ParametersTestAnnotation(doubleValue = -5.1231, kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testDoubleValueWithNegativeValue() {
        setupProcessor("testDoubleValueWithNegativeValue", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.doubleValue).isEqualTo(-5.1231)
        }
    }

    @ParametersTestAnnotation(doubleValue = 5.1231, kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testDoubleValue() {
        setupProcessor("testDoubleValue", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.doubleValue).isEqualTo(5.1231)
        }
    }

    @ParametersTestAnnotation(floatValue = 5.12f, kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testFloatValue() {
        setupProcessor("testFloatValue", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.floatValue).isEqualTo(5.12f)
        }
    }

    @ParametersTestAnnotation(intValue = 5, kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testIntegerValue() {
        setupProcessor("testIntegerValue", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.intValue).isEqualTo(5)
        }
    }

    @ParametersTestAnnotation(longValue = 5L, kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testLongValue() {
        setupProcessor("testLongValue", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.longValue).isEqualTo(5L)
        }
    }

    @ParametersTestAnnotation(stringValue = "somevalue", kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testStringValue() {
        setupProcessor("testStringValue", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.stringValue).isEqualTo("somevalue")
        }
    }

    @ParametersTestAnnotation(kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testKClassValue() {
        setupProcessor("testKClassValue", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.kClassValue).isEqualTo(ParametersTestAnnotation::class)
        }
    }

    @ParametersTestAnnotation(enumValue = TestEnum.VALUE2, kClassValue = ParametersTestAnnotation::class)
    @Test
    fun testEnumValue() {
        setupProcessor("testEnumValue", ParametersTestAnnotation::class) { annotation ->
            assertThat(annotation.enumValue).isEqualTo(TestEnum.VALUE2)
        }
    }

    @ParameterArraysTestAnnotation
    @Test
    fun testDefaultArrayValues() {
        setupProcessor("testDefaultArrayValues", ParameterArraysTestAnnotation::class) { annotation ->
            assertThat(annotation.booleanArrayValue).isEmpty()
            assertThat(annotation.byteArrayValue).isEmpty()
            assertThat(annotation.charArrayValue).isEmpty()
            assertThat(annotation.doubleArrayValue).isEmpty()
            assertThat(annotation.floatArrayValue).isEmpty()
            assertThat(annotation.intArrayValue).isEmpty()
            assertThat(annotation.longArrayValue).isEmpty()
            assertThat(annotation.stringArrayValue).isEmpty()
            assertThat(annotation.kClassArrayValue).isEmpty()
            assertThat(annotation.enumArrayValue).isEmpty()
        }
    }

    @ParameterArraysTestAnnotation(booleanArrayValue = [true, false])
    @Test
    fun testBooleanArrayValue() {
        setupProcessor("testBooleanArrayValue", ParameterArraysTestAnnotation::class) { annotation ->
            assertThat(annotation.booleanArrayValue).isEqualTo(booleanArrayOf(true, false))
        }
    }

    @ParameterArraysTestAnnotation(byteArrayValue = [1, 2, 3])
    @Test
    fun testByteArrayValue() {
        setupProcessor("testByteArrayValue", ParameterArraysTestAnnotation::class) { annotation ->
            assertThat(annotation.byteArrayValue).isEqualTo(byteArrayOf(1, 2, 3))
        }
    }

    @ParameterArraysTestAnnotation(charArrayValue = ['a', 'b', 'c'])
    @Test
    fun testCharArrayValue() {
        setupProcessor("testCharArrayValue", ParameterArraysTestAnnotation::class) { annotation ->
            assertThat(annotation.charArrayValue).isEqualTo(charArrayOf('a', 'b', 'c'))
        }
    }

    @ParameterArraysTestAnnotation(doubleArrayValue = [1.0, 2.0, 3.1, 4.4, 5.3])
    @Test
    fun testDoubleArrayValue() {
        setupProcessor("testDoubleArrayValue", ParameterArraysTestAnnotation::class) { annotation ->
            assertThat(annotation.doubleArrayValue).isEqualTo(
                doubleArrayOf(
                    1.0,
                    2.0,
                    3.1,
                    4.4,
                    5.3
                )
            )
        }
    }

    @ParameterArraysTestAnnotation(floatArrayValue = [1.1f, 2.12f, 3.34f, 4f, 5.1f])
    @Test
    fun testFloatArrayValue() {
        setupProcessor("testFloatArrayValue", ParameterArraysTestAnnotation::class) { annotation ->
            assertThat(annotation.floatArrayValue).isEqualTo(
                floatArrayOf(
                    1.1f,
                    2.12f,
                    3.34f,
                    4f,
                    5.1f
                )
            )
        }
    }

    @ParameterArraysTestAnnotation(intArrayValue = [1, 2, 3, 4, 5])
    @Test
    fun testIntegerArrayValue() {
        setupProcessor("testIntegerArrayValue", ParameterArraysTestAnnotation::class) { annotation ->
            assertThat(annotation.intArrayValue).isEqualTo(intArrayOf(1, 2, 3, 4, 5))
        }
    }

    @ParameterArraysTestAnnotation(stringArrayValue = ["a", "b", "c", "d", "e"])
    @Test
    fun testStringArrayValue() {
        setupProcessor("testStringArrayValue", ParameterArraysTestAnnotation::class) { annotation ->
            assertThat(annotation.stringArrayValue).isEqualTo(arrayOf("a", "b", "c", "d", "e"))
        }
    }

    @ParameterArraysTestAnnotation(kClassArrayValue = [TestEnum::class])
    @Test
    fun testKClassArrayValue() {
        setupProcessor("testKClassArrayValue", ParameterArraysTestAnnotation::class) { annotation ->
            assertThat(annotation.kClassArrayValue).isEqualTo(arrayOf(TestEnum::class))
        }
    }

    @ParameterArraysTestAnnotation(enumArrayValue = [TestEnum.VALUE1, TestEnum.VALUE2])
    @Test
    fun testEnumArrayValue() {
        setupProcessor("testEnumArrayValue", ParameterArraysTestAnnotation::class) { annotation ->
            assertThat(annotation.enumArrayValue).isEqualTo(arrayOf(TestEnum.VALUE1, TestEnum.VALUE2))
        }
    }

    @ParameterArraysTestAnnotation(longArrayValue = [1L, 5L, 10L])
    @Test
    fun testLongArrayValue() {
        setupProcessor("testLongArrayValue", ParameterArraysTestAnnotation::class) { annotation ->
            assertThat(annotation.longArrayValue).isEqualTo(longArrayOf(1L, 5L, 10L))
        }
    }

    // ************************************************************
    //                      Java Annotations
    // ************************************************************

    @JavaTestAnnotations.JavaParametersTestAnnotation(classValue = AnnotationParserTest::class)
    @Test
    fun testJavaParametersTestAnnotationDefaults() {
        setupProcessor(
            "testJavaParametersTestAnnotationDefaults",
            JavaTestAnnotations.JavaParametersTestAnnotation::class
        ) { annotation ->
            assertThat(annotation.booleanValue).isTrue()
            assertThat(annotation.byteValue).isEqualTo(-2)
            assertThat(annotation.charValue).isEqualTo('b')
            assertThat(annotation.doubleValue).isEqualTo(-3.0)
            assertThat(annotation.floatValue).isEqualTo(-4.0f)
            assertThat(annotation.intValue).isEqualTo(-5)
            assertThat(annotation.longValue).isEqualTo(-6L)
            assertThat(annotation.stringValue).isEqualTo("emptystring")
            assertThat(annotation.classValue).isEqualTo(AnnotationParserTest::class)
            assertThat(annotation.enumValue).isEqualTo(JavaTestAnnotations.JavaTestEnum.NONE)
        }
    }

    @JavaTestAnnotations.JavaParameterArraysTestAnnotation
    @Test
    fun testJavaParameterArraysTestAnnotationDefaults() {
        setupProcessor(
            "testJavaParameterArraysTestAnnotationDefaults",
            JavaTestAnnotations.JavaParameterArraysTestAnnotation::class
        ) { annotation ->
            assertThat(annotation.booleanArrayValue).isEqualTo(booleanArrayOf(true, false))
            assertThat(annotation.byteArrayValue).isEmpty()
            assertThat(annotation.charArrayValue).isEmpty()
            assertThat(annotation.doubleArrayValue).isEmpty()
            assertThat(annotation.floatArrayValue).isEmpty()
            assertThat(annotation.intArrayValue).isEmpty()
            assertThat(annotation.longArrayValue).isEmpty()
            assertThat(annotation.stringArrayValue).isEmpty()
            assertThat(annotation.classArrayValue).isEmpty()
            assertThat(annotation.enumArrayValue).isEmpty()
        }
    }

    companion object {

        @JvmStatic
        fun <T : Annotation> setupProcessor(
            methodName: String,
            annotationKClass: KClass<T>,
            processor: (annotation: T) -> Unit,
        ): KotlinCompilation.Result {
            val symbolProcessorProvider = object : SymbolProcessorProvider {
                override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
                    return object : SymbolProcessor {
                        override fun process(resolver: Resolver): List<KSAnnotated> {
                            val annotation =
                                resolver.getSymbolsWithAnnotation(annotationKClass.qualifiedName!!)
                                    .filter { s -> s.toString() == methodName }
                                    .map { it.annotations.first() }
                                    .map { it.getAnnotation(annotationKClass) }
                                    .first()
                            processor.invoke(annotation)
                            return emptyList()
                        }
                    }
                }
            }
            val compilation = KotlinCompilation().apply {
                val path = "src/test/kotlin/io/github/alexbeggs/kspannotationparser"
                sources = listOf(
                    SourceFile.fromPath(File("$path/AnnotationParserTest.kt")),
                    SourceFile.fromPath(File("$path/JavaTestAnnotations.java")),
                )
                symbolProcessorProviders = listOf(symbolProcessorProvider)
            }
            val result = compilation.compile()
            if (result.exitCode != KotlinCompilation.ExitCode.OK) {
                throw IllegalStateException("Failed $result")
            }
            return result
        }
    }
}

@Suppress("LongParameterList")
annotation class ParametersTestAnnotation(
    val booleanValue: Boolean = false,
    val byteValue: Byte = 2,
    val charValue: Char = 'b',
    val doubleValue: Double = 3.0,
    val floatValue: Float = 4.0f,
    val intValue: Int = 5,
    val longValue: Long = 6L,
    val stringValue: String = "emptystring",
    // fails on getting the arguments from the KSAnnotation when no value is set for the kClassValue in
    // the declaration. Throws an NPE with using a default value
    val kClassValue: KClass<*> = ParametersTestAnnotation::class,
    val enumValue: TestEnum = TestEnum.NONE,
)

@Suppress("LongParameterList")
annotation class ParameterArraysTestAnnotation(
    val booleanArrayValue: BooleanArray = booleanArrayOf(),
    val byteArrayValue: ByteArray = byteArrayOf(),
    val charArrayValue: CharArray = charArrayOf(),
    val doubleArrayValue: DoubleArray = doubleArrayOf(),
    val floatArrayValue: FloatArray = floatArrayOf(),
    val intArrayValue: IntArray = intArrayOf(),
    val longArrayValue: LongArray = longArrayOf(),
    val stringArrayValue: Array<String> = emptyArray(),
    val kClassArrayValue: Array<KClass<*>> = emptyArray(),
    val enumArrayValue: Array<TestEnum> = emptyArray(),
)

annotation class ParametersTestWithNegativeDefaultsAnnotation(
    val byteValue: Byte = -2,
    val doubleValue: Double = -3.0,
    val floatValue: Float = -4.0f,
    val intValue: Int = -5,
    val longValue: Long = -6L,
)

enum class TestEnum {
    NONE, VALUE1, VALUE2
}
