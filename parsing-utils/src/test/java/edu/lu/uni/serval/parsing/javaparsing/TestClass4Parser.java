package edu.lu.uni.serval.parsing.javaparsing;


import edu.lu.uni.serval.parsing.javaparsing.annotation.MyClassAnnotation;
import edu.lu.uni.serval.parsing.javaparsing.annotation.MyCompileAnnotation;
import edu.lu.uni.serval.parsing.javaparsing.annotation.MyRuntimeAnnotation;
import org.junit.Ignore;

/**
 * Created by darkrsw on 2016/August/15.
 */

@MyCompileAnnotation
//@MyClassAnnotation
//@MyRuntimeAnnotation
public class TestClass4Parser
{
    @Ignore
    public void samplemethod1(int arg1)
    {
        System.out.println("test method1 " + arg1);
    }
}
