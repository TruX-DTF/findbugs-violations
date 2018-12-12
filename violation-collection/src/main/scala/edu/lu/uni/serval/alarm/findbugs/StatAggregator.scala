package edu.lu.uni.serval.alarm.findbugs

import java.io.File
import edu.lu.uni.serval.alarm.util.AlarmFileUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils



/**
 * @author darkrsw
 */
object StatAggregator 
{
	def main(args:Array[String]): Unit = 
	{
		
	}
	
	def doTask(rootPath: String): Unit =
	{
		// TODO incomplete
		val reports = AlarmFileUtils.getListOfCSVFiles(rootPath)
		for( reportFile <- reports)
		{
			val listOfViolations = FileUtils.readLines(reportFile)
			val sha1 = FilenameUtils.getBaseName(reportFile.getName)
			
			
		}
	}
}