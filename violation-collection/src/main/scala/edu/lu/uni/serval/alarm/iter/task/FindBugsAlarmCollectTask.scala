package edu.lu.uni.serval.alarm.iter.task

import edu.lu.uni.serval.iter.CommitTask
import org.eclipse.jgit.revwalk.RevCommit
import edu.lu.uni.serval.scm.git.GitCommands
import java.io.File
import com.typesafe.scalalogging.LazyLogging
import edu.lu.uni.serval.buildtool.maven.ModuleIterator
import edu.lu.uni.serval.buildtool.maven.MavenExecutor
import edu.lu.uni.serval.alarm.findbugs.FindBugsRunner
import scala.collection.mutable.ListBuffer
import edu.lu.uni.serval.alarm.findbugs.CsvBugReporter
import edu.lu.uni.serval.alarm.util.AlarmFileUtils
import edu.umd.cs.findbugs.TextUIBugReporter
import scala.collection.mutable.Buffer
import org.apache.commons.io.FileUtils



/**
 * @author darkrsw
 */
class FindBugsAlarmCollectTask extends CommitTask with LazyLogging 
{
	var repoDir: String = "";
	var mvnRepo: String = "";
	var reportsRoot: String = "";
	var releaseName: String = "";
	var projectName: String = "";
	var resultMsg: String = "";
	var statLogger: StringBuffer = null; // not in use yet.
	var filters: Buffer[String] = null;
	var logDir: File = null;
	
	def setStatLogger(p: StringBuffer) =	{ this.statLogger = p;}
	def setRepoDir(p: String) = {this.repoDir = p;}
	def setMavenTempRepoDir(p: String) = {this.mvnRepo = p;}
	def setReportsRoot(p: String) = {reportsRoot = p;}
	def setReleaseName(p: String) = { releaseName = p;}
	def setProjectName(p: String) = {projectName = p;}
	def setFilters(p: Buffer[String]) = { filters = p; }
	def setLogDir(p: File) = { logDir = p; }
	
	def logBuildResult(logFile: File, msg: String): Unit =
		{	FileUtils.write(logFile, msg, true); }
		
  def doTask(commit: RevCommit): Boolean =
  {
  	val sha1 = commit.getName
  	logger.debug("Processing: " + sha1)
  	
  	val reportPath = reportsRoot+"/"+sha1+".csv"
  	
  	
  	GitCommands.reset(repoDir, sha1, true)
  	
  	val mavenPomFile = new File(repoDir + "/pom.xml")
  	if(!mavenPomFile.exists())
  	{
  		// No maven pom.xml defined.
  		val r = "pom.xml not found in root."
  		logger.error(r)
  		this.resultMsg = r;
  		return false;
  	}
  	
  	val logFile = new File(logDir, sha1+".log")
  	
  	val buildResult = MavenExecutor.buildProject(this.repoDir, this.mvnRepo, quiet=false, skipTestCompile=false);
  	logBuildResult(logFile, buildResult._2.toString())
  	
  	if(! buildResult._1)
  	{
  		// Maven build error
  		val r = "mvn build failed."
  		logger.error(r)
  		this.resultMsg = r;
  		return false;
  	}
  	
  	val buildResult2 = MavenExecutor.copyDependencies(this.repoDir)
  	logBuildResult(logFile, buildResult._2.toString())
  	
  	if(!buildResult2._1)
  	{
  		// copy dependencies error
  		val r = "Error occurred in copy dependencies."
  		logger.error(r)
  		this.resultMsg = r;
  		return false;
  	}
  	
  	val reporter = new CsvBugReporter(reportPath)
  	
  	val modules = ModuleIterator.getModulesFromPom(repoDir)
  	
  	for( module <- modules )
  	{
  		// check "target" exists
  		val targetPath = module + "/target"
  		val targetDir = new File(targetPath)
  		if(targetDir.exists())
  		{
  			val targetJars = AlarmFileUtils.getListOfJarFilePaths(targetPath);
  			
  			val depPath = targetPath + "/dependency"
  			val depDir = new File(depPath)
  			
  			val depJars = 
  				if(depDir.exists())
  						{	AlarmFileUtils.getListOfJarFilePaths(depPath);	}
  				else
  				{	new ListBuffer[String] }
  			
  			try {
  				FindBugsRunner.runFindBugs(targetJars, depJars, filters, reporter, releaseName, projectName)
  			}	catch
  			{
  				// code base can be empty (since a module has not java files to analyze).
  				case e: Exception => logger.error("Error occurred in processing $module --- " + e.getMessage)
  			}
  		}
  	}
  	
  	this.resultMsg = "no error"
  	return true;
  }

  def getResultMsg(): String = {
	  this.resultMsg
	}
}