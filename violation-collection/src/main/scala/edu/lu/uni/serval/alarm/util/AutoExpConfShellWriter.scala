package edu.lu.uni.serval.alarm.util

import java.io.File
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.FileUtils
import scala.sys.process._

/**
 * @author darkrsw
 */
object AutoExpConfShellWriter 
{
  def main(args: Array[String]): Unit =
  {
  	if(args.length < 2)
  	{
  		Console.err.println("Usage: $java -cp \"$LIBS/*\" THIS.class$ [repoRootDir] [workingDir]")
  		return
  	}
  	
  	val reposDir = new File(args(0))
  	val reposPath = reposDir.getCanonicalPath
  	
  	val workingDir = new File(args(1))
  	val workingDirPath = workingDir.getCanonicalPath
  	
  	val tmpDir = "/tmp/dkim"
  	val javaRT = "/home/users/dkim/sbin/java/bin/java"
  	val libDir = "/scratch/users/dkim/exp/violation/lib"
  	
  	
  	val confDir = new File(workingDir + "/conf")
  	val shellDir = new File(workingDir + "/shell")
  	
  	val confDirPath = confDir.getCanonicalPath
		val shellDirPath = shellDir.getCanonicalPath
  	
  	val rootDir = reposDir

		if (rootDir.exists && rootDir.isDirectory) 
		{
			val repos = rootDir.listFiles.filter(_.isDirectory).toBuffer
			val sGitLocation = s"git.location = $tmpDir/git/%s\n"
			val sFindbugsExcludeFilter = "exclude.filter = ${git.location}/findbugs-exclude-filter.xml\n"
			val sMavenLocalRepo = s"maven.temp.repo = $tmpDir/mvn\n"
			val sReleaseName = "release.name = all\n"
			val sProjectName = "project.name = %s\n"
			val sExpRoot = "exp.root = "+ workingDirPath + "\n"
			val sReportRoot = "report.root = ${exp.root}/reports/%s\n"
			val sLogRoot = "log.root = ${exp.root}/log/%s\n"
			
			val oarsubScript = new StringBuilder
			oarsubScript ++= """#!/bin/bash -l"""
			oarsubScript ++= "\n"
			
			for(r <- repos)
			{
				val repoName = r.getName
				val confStrings = new StringBuilder
				
				//build conf string
				confStrings ++= sGitLocation.format(repoName)
				
				val findbugExcludeFile = new File(r, "findbugs-exclude-filter.xml")
				if(findbugExcludeFile.exists())
				{
					confStrings ++= sFindbugsExcludeFilter
				}
					
				confStrings ++= sMavenLocalRepo
				confStrings ++= sReleaseName
				confStrings ++= sProjectName.format(repoName)
				confStrings ++= sExpRoot
				confStrings ++= sReportRoot.format(repoName)
				confStrings ++= sLogRoot.format(repoName)
				
				
				//build shell string
				val shellStrings = new StringBuilder
				
				shellStrings ++= "#!/bin/bash -l\n"
				shellStrings ++= s"mkdir -p $tmpDir/git\n"
				shellStrings ++= s"cp -R $reposPath/%s $tmpDir/git/\n".format(repoName)
				shellStrings ++= s"mkdir -p $tmpDir/mvn\n"
				
				
				shellStrings ++= "%s -cp \"%s/*\" ".format(javaRT, libDir)+
												"edu.lu.uni.serval.alarm.findbugs.exp.AlarmExpExecutor %s/%s.conf\n".
														format(confDirPath, repoName)
				
				
				//write conf and shell file
				FileUtils.write(new File(confDir, "%s.conf".format(repoName)), confStrings.toString())
				FileUtils.write(new File(shellDir, "%s.sh".format(repoName)), shellStrings.toString())
			
				
				/*
				oarsubScript ++= "oarsub -n vio-%s ".format(repoName)
				oarsubScript ++= s"""-O $shellDirPath/shell-log/OAR.%jobid%."""
				oarsubScript ++= "%s.stdout ".format(repoName)
				oarsubScript ++= s"""-E $shellDirPath/shell-log/OAR.%jobid%."""
				oarsubScript ++= "%s.stderr ".format(repoName)
				oarsubScript ++= "-l nodes=1/core=2,walltime=120:00:00 --notify \"mail:darkrsw@gmail.com\" "
				oarsubScript ++= "%s.sh\n\n".format(repoName)
				*/
			}
			
			//val chmod = "chmod 755 *.sh"
			//val exitCode = Process(chmod, shellDir).!
			//Console.err.println("chmod exitcode = " + exitCode)
			
			
			//FileUtils.write(new File(shellDir, "job-submit.sh"), oarsubScript.toString)
		} else {
			Console.err.println("Repo root (" + reposPath + ") does not exists");
			return;
		}
  	
  	
  }
}