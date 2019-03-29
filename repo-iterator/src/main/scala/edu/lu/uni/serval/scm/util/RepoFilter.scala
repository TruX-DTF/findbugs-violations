package edu.lu.uni.serval.scm.util

import java.io.File
import org.apache.commons.io.FileUtils

/**
 * @author darkrsw
 */
@deprecated
object RepoFilter {
  def filterByFile(args: Array[String]) = {// repo collection root
    val repoRootPath = args(0)
    
    val repoRoot = new File(repoRootPath)
    val repos = repoRoot.listFiles.filter(_.isDirectory).toBuffer
    
    // for all repos
    for( repo <- repos )
    {
    	val repoPath = repo.getAbsolutePath
    	if(!RepoUtils.checkPomExists(repoPath))
    	{
    		// delete repo dir
    		Console.println("no pom.xml in " + repo.getName)
    		FileUtils.deleteDirectory(repo)
    	}
    }
  }
}