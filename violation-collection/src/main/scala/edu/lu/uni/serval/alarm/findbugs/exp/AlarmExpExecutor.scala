package edu.lu.uni.serval.alarm.findbugs.exp

import java.io.File
import scala.collection.JavaConversions
import scala.collection.mutable.Buffer
import scala.collection.mutable.HashSet
import scala.collection.mutable.ListBuffer
import org.apache.commons.configuration.FileConfiguration
import org.apache.commons.io.FileUtils
import edu.lu.uni.serval.alarm.iter.filter.FindBugsPreFilter
import edu.lu.uni.serval.alarm.iter.task.FindBugsAlarmCollectTask
import edu.lu.uni.serval.iter.BackwardIterator
import edu.lu.uni.serval.scm.git.GitProxy
import org.apache.commons.configuration.PropertiesConfiguration
import org.eclipse.jgit.revwalk.RevCommit
import edu.lu.uni.serval.iter.filter.CommitFilter

/**
 * @author darkrsw
 */
class AlarmExpExecutor 
{
  def executeCollection(
  		repoRoot: String, 
  		tmpMavenRepo: String,
  		releaseName:String, 
  		projectName: String,
  		reportsRoot:String,
  		logRoot: String,
  		bugFilters: Buffer[String]
  											): Boolean =
  {
  	val proxy = new GitProxy();
		proxy.setURI(repoRoot+"/.git");
		
		if(!proxy.connect()) return false;
		
		val iter = new BackwardIterator(proxy);
		
		val logDir = new File(logRoot)
		logDir.mkdirs()
		
		val resultSummaryLogFile = new File(logDir, ""+projectName+"-summary.log")
		iter.setLogWriter(resultSummaryLogFile)
		
		val resultMap = new HashSet[String]
		
		val resultSummary = if(resultSummaryLogFile.exists()) 
				JavaConversions.asScalaBuffer(FileUtils.readLines(resultSummaryLogFile))
			else
				new ListBuffer[String]
		
		for(line <- resultSummary)
		{
			resultMap+=line.split("\\s+")(0).trim()
		}
		
		
		iter.setPreFilter( new FindBugsPreFilter(resultMap) );
		
		iter.setResultMap(resultMap) // NOTE: might be necessary to refactor.
		
		val task = new FindBugsAlarmCollectTask
		task.setRepoDir(repoRoot)
		
		val tmpMvnRepo = new File(tmpMavenRepo);
		tmpMvnRepo.mkdirs()
		task.setMavenTempRepoDir(tmpMavenRepo);
		
		task.setProjectName(projectName)
		task.setReleaseName(releaseName)
		task.setFilters(bugFilters)
		
		val reportsDir = new File(reportsRoot)
		reportsDir.mkdirs()
		task.setReportsRoot(reportsRoot)
		
		task.setLogDir(logDir)
		
		iter.setTask(task);
		val ck = iter.iterateBackward();
		
		//FileUtils.write(expLogFile, logWriter.toString())
		//Console.println(logWriter.toString())
		
		FileUtils.write(resultSummaryLogFile, "Job done.\n", true)
		
		ck;
		
		// NOTE just for test
		/*iter.setPreFilter( new CommitFilter() {
			var count = 0;
			val another = new FindBugsPreFilter(resultMap)	
			
			def filterCommit(commit: RevCommit): Boolean =
			{
				count += 1;
				
				if(commit.getName != "d3911464f59982f3a93a2d44f627d968dca9bd04" || another.filterCommit(commit))
				//if(count >= 20 || another.filterCommit(commit))
					return true;
				else
				{
					return false;
				}
			}
		})*/
  }
}


object AlarmExpExecutor
{
	def main(args: Array[String]): Unit =
	{
		val obj = new AlarmExpExecutor
		val confFilename = args(0)
		val confFile = new File(confFilename)
		
		if(!confFile.exists())
		{
			Console.println(confFilename + " does not exist.")
			return;
		}
		
		val conf = new PropertiesConfiguration(confFile)
		val filters = new ListBuffer[String]
		
		if(conf.containsKey("exclude.filter"))
		{
			val filterList = JavaConversions.asScalaBuffer(conf.getList("exclude.filter"))
			for( item <- filterList )
			{
				val filterFilePath = item.asInstanceOf[String]
				val filterFile = new File(filterFilePath)
				if(filterFile.exists())
					filters+=filterFilePath
			}
		} 
		
		val gitLocation = conf.getString("git.location")
	  val mavenTmpDir = conf.getString("maven.temp.repo")
		val realeaseName = conf.getString("release.name")
		val projectName = conf.getString("project.name")
		val reportRootDir = conf.getString("report.root")
		val logRootDir = conf.getString("log.root")
		
		obj.executeCollection(
				gitLocation, mavenTmpDir, realeaseName, projectName,
				reportRootDir, logRootDir, filters) 
	}
}