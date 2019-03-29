package edu.lu.uni.serval.buildtool.maven;

import static org.junit.Assert.*;
import java.util.Map;
import org.junit.Test;

public class MavenExecutorTest
{

	@Test
	public void testBuildCommand()
	{
		//int exitcode = MavenExecutor.buildProject("/Users/darkrsw/git/commons-math", "tmp/mavenRepo");
		/*MavenExecutor.buildProject("/Users/darkrsw/git/commons-math", 
				"tmp/mavenRepo", 
				false, // no test jar 
				true,  // skip test run
				false, // don't skip test compile
				false, // no fail-never action
				false // verbose
				);*/
		//System.out.println(ck);
	}

	@Test
	public void testCopyDependencyCommand()
	{
		//MavenExecutor.copyDependencies("/Users/darkrsw/git/commons-math");
	}
}
