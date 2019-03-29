package edu.lu.uni.serval.scm.git.util

import org.apache.commons.io.FileUtils
import scala.collection.mutable.Buffer
import java.io.File
import scala.collection.JavaConversions

class RemoteGitCloner 
{
	/*def main(args: Array[String]): Unit =
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
		
		//val result = collectReposWithIdempotentTrial(repoList, workingDirPath)
		
		FileUtils.write(new File(workingDirPath + "/gitClone.results"), result)
	}
	
	// 2. clone and transfer
  def collectReposWithIdempotentTrialAndTransfer
  				(urls: Buffer[String], workingDirPath: String): String =
  {
  	
  }  */
}