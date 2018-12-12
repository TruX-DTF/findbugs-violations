package edu.lu.uni.serval.alarm.tracking

import com.typesafe.scalalogging.LazyLogging
import java.io.File
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.FileUtils
import sys.process._

object TrackingExecutorByPack extends LazyLogging
{
	def main(args: Array[String]): Unit =
	{
		doTask(args(0), args(1), args(2))
		
		Runtime.getRuntime.exit(0)
	}
  
	def doTask(reportRootPath: String, projectRootPath: String, logPath: String) =
	{
		logger.info("Starting pack: "+reportRootPath)
		
		val reportRootDir = new File(reportRootPath)
		val prjList = reportRootDir.listFiles().filter(_.isDirectory()).map(x=>FilenameUtils.getBaseName(x.getCanonicalPath))
		
		val logFile = new File(logPath)
		
		prjList.foreach( prj =>
			{
				var result = ""
				
				try {
					// check existence
					val reportDir = new File(reportRootPath + File.separator + prj)
					val repoDir = new File(projectRootPath + File.separator + prj)
					
					val doesExist = (reportDir.exists() && reportDir.isDirectory() &&
													repoDir.exists() && repoDir.isDirectory() &&
													prj != "Talend-tcommon-studio-se"  // this is just testing.
													)
					
					val isSuccessful: Boolean = if( ! doesExist )
					{
						println(s"$prj does not exists!")
						false
					}
					else
					{
						// count alarm files.
						val reportFiles = reportDir.listFiles().filter( x => x.getName.endsWith(".csv") )
						
						if( reportFiles.size < 2 )
						{
							println(s"$prj does not have enough number of alarm files!")
							false
						}
						else
						{
							logger.info(s"Starting tracking of $prj...")
							val results = s"./project-wise-tracking.sh $prj $reportRootPath $projectRootPath".!
							//TrackingExecutorByProject.internal(prj, reportRootPath, projectRootPath)
							
							true
						}
					}
						
					if(isSuccessful)
						result = "Success"
					else 		
						result = "Fail"
				} 
				catch
				{
					case e: Throwable => result = "Fail with " + e.getMessage
				}
				finally
				{
					FileUtils.write(logFile, s"$prj=>$result\n", "UTF-8", true)	
				}
			}
			)
		
		logger.info("Finished pack: "+reportRootPath)
	}
}