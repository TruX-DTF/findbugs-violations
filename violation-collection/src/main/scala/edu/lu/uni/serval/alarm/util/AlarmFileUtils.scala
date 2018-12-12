package edu.lu.uni.serval.alarm.util

import java.io.File
import org.apache.commons.io.FilenameUtils
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

/**
 * @author darkrsw
 */
object AlarmFileUtils 
{
	def getListOfJarFiles(dir: String): Buffer[File] = 
	{
		getListOfFilesByExtension(dir, "jar")
	}
	
	def getListOfCSVFiles(dir: String): Buffer[File] =
	{
		getListOfFilesByExtension(dir, "csv")
	}
	
	def getListOfFilesByExtension(dir: String, ext: String ): Buffer[File] =
	{
		val d = new File(dir)

		if (d.exists && d.isDirectory) {
			d.listFiles.filter(_.isFile).filter(x => FilenameUtils.getExtension(x.getName) == ext).toBuffer
		} else {
			Buffer[File]()
		}
	}
	
	def getListOfJarFilePaths(dir: String): Buffer[String] =
	{
			val Jars = getListOfJarFiles(dir)
			
			val JarPaths = new ListBuffer[String]; 
			Jars.foreach( x => JarPaths.append(x.getAbsolutePath))
			
			return JarPaths;
	}
}