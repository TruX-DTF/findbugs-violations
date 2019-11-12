package edu.lu.uni.serval.parsing.javaparsing.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by darkrsw on 2016/August/15.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE) //on class level
public @interface MyClassAnnotation {

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    Priority priority() default Priority.MEDIUM;

    String[] tags() default "";

    String createdBy() default "Mkyong";

    String lastModified() default "03/01/2014";

}