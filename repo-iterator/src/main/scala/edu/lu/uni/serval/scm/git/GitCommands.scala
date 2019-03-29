package edu.lu.uni.serval.scm.git

import java.io.File

import scala.sys.process._
import scala.util.control.Breaks._

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.scalalogging.Logger

object GitCommands extends LazyLogging
{
  val nRETRY = 3;

  def archiveCommit(gitBase: String, outputDir: String, hash: String): Boolean =
  {
  	val targetDir = new File(gitBase)
  	
    // archive == export
    //"base.dir"+"/"+hash+".zip", hash
    val archiveCmd = "git archive --format zip --output %s %s".format(outputDir + "/" + hash + ".zip", hash);
    var exitCode = Process(archiveCmd, targetDir).!;
    
    if(exitCode != 0)
    {
      logger.error("git archiving for %s has failed.".format(hash));
      return false;
    }
    
    logger.info("git archiving: %s".format(hash));
    
    
    // unzip
    // "base.dir"+"/"+revName+".zip" , 
    // "base.dir"+"/"+ revName));
    val unzipCmd = "unzip -q -o -U %s -d %s".format(outputDir + "/" + hash + ".zip", outputDir + "/" + hash );
    exitCode = Process(unzipCmd, targetDir).!;
    
    if(exitCode != 0)
    {
      logger.error("Unable to unzip %s.".format(hash))
      return false;
    }
    
    logger.info("unzip: %s".format(hash))
    
    return true;
  }
  
  def cloneWithRetry(url: String, path: String, workingDir: String): Boolean =
  {
    var success = false;
    
    for(i <- 1 to nRETRY)
    {
      val result = clone(url, path, workingDir)
      if(result == 0)
      {
        success = true
        return success;
      }
    }
    
    return success;
  }
  
  def clone(url: String, path: String, workingDir: String): Int =
  {
  	val targetDir = new File(workingDir)
  	
    val cmd = "git clone %s %s".format(url, path);
    
  	//Console.println(cmd)
  	
    val success = Process(cmd, targetDir).!;
    
    logger.info("cloning result: " + success);
    
    return success;
    // caller needs to retry when the return value is false;
  }

  def reset(workingDir: String, hash: String, hard: Boolean): Int =
  {
  	val targetDir = new File(workingDir)
  	val cmd1 = "git reset "
  	val cmd2 = if(hard) { cmd1 + "--hard"} else {cmd1}
  	val cmd = cmd2 + " " + hash
  	
  	val success = Process(cmd, targetDir).!;
  	
  	return success;
  }
  
  def checkExists(owner: String, repoName: String): Boolean = 
  {
  	val url = "https://github.com/%s/%s".format(owner, repoName)
  	val cmd = """curl -s -o /dev/null -I -w %{http_code} """ + url
  	val status = cmd.!!
  	
  	val result = status.trim match
  		{
  			case "200" => true
  			//case "301" => checkRedirect(url)
  			case _ => false
  		}
  	
  	return result
  }
  
  def checkRedirect(url: String): Boolean =
  {
  	// What if redirected again? What if redirected one is private?
  	val cmd = """curl -s -o /dev/null -I -w %{redirect_url} """ + url
  	
  	val rurl = cmd.!!.trim()
  	val truncated = url.replaceAll(".git", "")
  
  	return if (url == rurl || truncated == rurl) false else true 
  			
  }
}