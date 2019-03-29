package edu.lu.uni.serval.scm.git.util

import scala.collection.mutable.Buffer
import java.nio.file.Path
import java.nio.file.Paths
import java.io.File
import edu.lu.uni.serval.scm.git.GitCommands
import org.apache.commons.io.FileUtils
import scala.collection.JavaConversions
import org.apache.commons.io.FilenameUtils
import edu.lu.uni.serval.util.RepoStringUtils

/**
 * @author darkrsw
 */
object BatchGitCloner 
{
	def main(args: Array[String]): Unit =
	{
		val listFilePath = args(0)
		val workingDirPath = args(1)
		
		Console.println("Repo list input: "+listFilePath)
		Console.println("Working dir: "+workingDirPath)
		
		val listFile = new File(listFilePath)
		val workDir = new File(workingDirPath)
		workDir.mkdirs
		
		val repoList = JavaConversions.asScalaBuffer( FileUtils.readLines(listFile) )
		Console.println("# of repos: " + repoList.size)
		
		val result = collectReposWithIdempotentTrial(repoList, workingDirPath)
		
		FileUtils.write(new File(workingDirPath + "/gitClone.results"), result)
	}
	
	
	// Direct download
	// directory structure: group/owner/reponame
	
  def collectReposWithIdempotentTrial(urls: Buffer[String], workingDirPath: String): String =
  {
  	val workingDir = new File(workingDirPath)
  	val sb = new StringBuilder
  	
  	for(url <- urls)
  	{
  		val repoName = RepoStringUtils.takeRepoName(url)
  		val repoOwner = RepoStringUtils.getRepoOwner(url)
  		
  		val newURL = "https://github.com/%s/%s.git".format(repoOwner, repoName)
  		Console.println("working on %s/%s".format(repoOwner, repoName))
  		//val repoPath = repoOwner+"-"+repoName
  		
  		// check if exists
	  	val repoOwnerDir = new File(workingDir, repoOwner)
  		repoOwnerDir.mkdirs()
  		
  		val repoDir = new File(repoOwnerDir, repoName)
	  			
  		val ck = if(!repoDir.exists())
  		{	
				val exists = GitCommands.checkExists(repoOwner, repoName)
				if(exists)
				{	
					val ownerPath = repoOwnerDir.getCanonicalPath
  				val cloned = GitCommands.cloneWithRetry(newURL, repoName, ownerPath)
  				
  				if(cloned)
  					"SUCCESS"
  				else
  					"FAILED"
  			} 
  			else { 
  				Console.println(url + " does not exists.");
  				//sb.append(url + " " + "false" +"\n")
	  			"NO_REMOTE_REPO"
	  		}
	  	}
  		else
  		{
  			Console.println(url + " already cloned.")
  			"ALREADY_CLONED"
  		}
  		sb.append(url + " " + ck+"\n")
  		
  	}
  	
  	return sb.toString()
  }
}

