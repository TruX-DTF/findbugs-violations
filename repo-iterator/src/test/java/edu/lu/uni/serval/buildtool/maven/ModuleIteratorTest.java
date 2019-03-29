package edu.lu.uni.serval.buildtool.maven;

import static org.junit.Assert.*;

import org.junit.Test;
import scala.collection.JavaConversions;

public class ModuleIteratorTest
{
	@Test
	public void testModuleIterator1()
	{
		Iterable<String> modules = JavaConversions.asJavaIterable(ModuleIterator.getModulesFromPom("/Users/darkrsw/git/commons-math"));
		
		for( String path: modules )
		{
			System.out.println(path);
		}
	}
	
	@Test
	public void testModuleIterator2()
	{
		Iterable<String> modules = JavaConversions.asJavaIterable(ModuleIterator.getModulesFromPom("/Users/darkrsw/git/guava"));
		
		for( String path: modules )
		{
			System.out.println(path);
		}
	}
	
	@Test
	public void testModuleIterator3()
	{
		Iterable<String> modules = JavaConversions.asJavaIterable(ModuleIterator.getModulesFromPom("/Users/darkrsw/git/hadoop"));
		
		for( String path: modules )
		{
			System.out.println(path);
		}
	}
}
