package edu.lu.uni.serval.util

import java.io.File
import com.github.tototoshi.csv.CSVReader
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import edu.lu.uni.serval.scm.util.RepoUtils

/**
 * @author darkrsw
 */
object RepoStringUtils 
{
	def getRepoOwner(url: String): String =
  {
  	val matchRegex = """(?<=http:\/\/github.com\/)(.*)(?=\/+)""".r
  	
  	val result = matchRegex findFirstIn url
  	return result.getOrElse("UNKNOWN")
  }
	
  def getRepoOwnerFromAPIURL(url: String): String =
  {
  	val matchRegex = """(?<=https:\/\/api.github.com\/repos\/)(.*)(?=\/+)""".r
  	
  	val result = matchRegex findFirstIn url
  	return result.getOrElse("UNKNOWN")
  }
  
  // Temporary function: read csv (repo list from ghtorrent) and make a list of git locations
  def main(args: Array[String]): Unit =
  {
  	val util = new RepoUtils
  	val gitLocTemplate = "http://github.com/%s/%s.git"
  	
  	val inFile = new File(args(0))
  	
  	val reader = CSVReader.open(inFile)
  	val repoRows = reader.all()
  	
  	Console.println("read completed: " + args(0))
  	
  	val outString = new StringBuilder
  	
  	for( row <- repoRows )
  	{
  		val url = row(1).replaceAll("\'", "")
  		val repoName = takeRepoName(url)
  		val repoOwner = getRepoOwnerFromAPIURL(url)
  		
  		outString ++= gitLocTemplate.format(repoOwner, repoName) + "\n"
  		
  		// check pom.xml ==> only for java projects
  		/*if(util.checkPomExists(repoOwner, repoName))
  		{
  			Console.println("%s/%s has pom.xml".format(repoOwner, repoName))
  			outString ++= gitLocTemplate.format(repoOwner, repoName) + "\n"
  		}
  		else
  		{
  			Console.println("%s/%s has *NO* pom.xml".format(repoOwner, repoName))
  		}*/
  	}
  	
  	reader.close
  	
  	FileUtils.write(new File(args(1)), outString.toString)
  }
  
  def takeRepoName(url: String): String =
  {
  	val fileName = FilenameUtils.getBaseName(url);

    fileName
  }
}