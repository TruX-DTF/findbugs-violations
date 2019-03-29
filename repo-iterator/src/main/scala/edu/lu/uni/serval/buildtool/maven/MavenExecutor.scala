package edu.lu.uni.serval.buildtool.maven

import java.io.File
import scala.sys.process._
import com.typesafe.scalalogging._
import org.slf4j.LoggerFactory


object MavenExecutor extends LazyLogging
{
	// baseDirPath: path to (root) pom.xml
  def buildProject(baseDirPath: String, 
  								mavenTempRepoDir: String,
  								testJar: Boolean = false,
  								skipTestRun: Boolean = true,
  								skipTestCompile: Boolean = false,
  								failNever: Boolean = false,
  								quiet: Boolean = true
  																				): (Boolean, StringBuilder) =
  {
    val targetDir = new File(baseDirPath)
    
    if (!skipTestRun && skipTestCompile)
    {
    	logger.error("maven install error (invalid options: never skip test run but skip test compile): %s".format(baseDirPath))
      return (false, new StringBuilder);
    }
    
    val sTestJar = if (testJar) "jar:test-jar" else "";
    val sSkipTest = if (skipTestRun) "-DskipTests=true" else "";
    val sSkipTestCompile = if (skipTestCompile) "-Dmaven.test.skip=true" else "";
    val sFailNever = if (failNever) "-fn" else "";
    val sQuiet = if (quiet) "-q" else "";
    
    
    // FIXED: maven does not support concurrent build correctly yet --- especially for multimodule projects
    // maven 3.3 now supports parallel build for multimodule projects.
    val mavenCmd = "mvn -T 12 clean install %s -U %s %s %s %s -Dmaven.javadoc.skip=true -Dmaven.site.skip=true -Dmaven.repo.local=%s\n".
    		format(sTestJar, sSkipTest, sSkipTestCompile, sFailNever, sQuiet, mavenTempRepoDir)
    
    val errout = new StringBuilder
    
    val recorder = ProcessLogger(
    			(o: String) => errout.append(o+"\n"),
    			(e: String) => errout.append(e+"\n")
    		); 
    		
    val exitCode = Process(mavenCmd, targetDir) ! recorder    
        
    if(exitCode != 0)
    {
      logger.error("maven install error (%d): %s".format(exitCode, baseDirPath))
      return (false, errout);
    }
    
    return (true, errout);
  }
  
  def copyDependencies(baseDirPath: String): (Boolean, StringBuilder) = 
  {
  	val targetDir = new File(baseDirPath)
  	
  	val mavenDepCmd = "mvn -q dependency:copy-dependencies";
  	
  	val errout = new StringBuilder
    
    val recorder = ProcessLogger(
    			(o: String) => errout.append(o+"\n"),
    			(e: String) => errout.append(e+"\n")
    		);
  	
    val exitCode = Process(mavenDepCmd, targetDir) ! recorder;
    
    if(exitCode != 0)
    {
      logger.error("maven copy-dependencies error (%d): %s".format(exitCode, baseDirPath))
      return (false, errout);
    }
    
    //logger.info("commit %s is ready.".format(baseDirPath))
    return (true, errout);
  }
  
  @deprecated
  def buildProjectWithoutTestJar(baseDirPath: String, mavenTempRepoDir: String): Boolean =
  {
    val targetDir = new File(baseDirPath)
    
    // FIXED: maven does not support concurrent build correctly yet --- especially for multimodule projects
    // maven 3.3 now supports parallel build for multimodule projects.
    val mavenCmd = "mvn -T 12 clean install -U -q -fn -DskipTests=true -Dmaven.javadoc.skip=true -Dmaven.site.skip=true -Dmaven.repo.local=%s\n".format(mavenTempRepoDir)
    
    //val mavenCmd = "mvn -T 12 clean install jar:test-jar -U -fn -DskipTests=true -Dmaven.javadoc.skip=true -Dmaven.site.skip=true -Dmaven.repo.local=%s\n".format(mavenTempRepoDir)
    
    val exitCode = Process(mavenCmd, targetDir).!    
    
    // TODO test
    Console.println("exit code: %d".format(exitCode))
    
    if(exitCode != 0)
    {
      logger.error("maven install error (%d): %s".format(exitCode, baseDirPath))
      return false;
    }
    
    return true;
  }
  
  @deprecated
  def buildProjectSkippingTests(baseDirPath: String, mavenTempRepoDir: String): Boolean =
  {
    val targetDir = new File(baseDirPath)
    
    // FIXED: maven does not support concurrent build correctly yet --- especially for multimodule projects
    // maven 3.3 now supports parallel build for multimodule projects.
    val mavenCmd = "mvn -T 12 clean install -U -q -fn -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Dmaven.site.skip=true -Dmaven.repo.local=%s\n".format(mavenTempRepoDir)
    
    //val mavenCmd = "mvn -T 12 clean install jar:test-jar -U -fn -DskipTests=true -Dmaven.javadoc.skip=true -Dmaven.site.skip=true -Dmaven.repo.local=%s\n".format(mavenTempRepoDir)
    
    val exitCode = Process(mavenCmd, targetDir).!    
        
    if(exitCode != 0)
    {
      logger.error("maven install error (%d): %s".format(exitCode, baseDirPath))
      return false;
    }
    
    return true;
  }
}