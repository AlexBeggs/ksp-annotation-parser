package org.abeggs.kspannotations;

public class JavaTestAnnotations {

    public @interface JavaParametersTestAnnotation {
        boolean booleanValue() default true;

        byte byteValue() default -2;

        char charValue() default 'b';

        double doubleValue() default -3.0;

        float floatValue() default -4.0f;

        int intValue() default -5;

        long longValue() default -6L;

        String stringValue() default "emptystring";

        Class<?> classValue();

        JavaTestEnum enumValue() default JavaTestEnum.NONE;
    }

    public @interface JavaParameterArraysTestAnnotation {
        boolean[] booleanArrayValue()  default {true, false};

        byte[] byteArrayValue() default {};

        char[] charArrayValue() default {};

        double[] doubleArrayValue() default {};

        float[] floatArrayValue() default {};

        int[] intArrayValue() default {};

        long[] longArrayValue() default {};

        String[] stringArrayValue() default {};

        Class[] classArrayValue() default {};

        JavaTestEnum[] enumArrayValue() default {};
    }

//    public @interface ParametersTestWithNegativeDefaultsAnnotation(
//            val byteValue:Byte=-2,
//            val doubleValue:Double=-3.0,
//            val floatValue:Float=-4.0f,
//            val intValue:Integer=-5,
//            val longValue:Long=-6L,
//}

   public enum JavaTestEnum {
        NONE, VALUE1, VALUE2
    }
}