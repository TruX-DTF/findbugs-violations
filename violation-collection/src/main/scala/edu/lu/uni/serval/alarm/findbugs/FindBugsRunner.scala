package edu.lu.uni.serval.alarm.findbugs

import edu.umd.cs.findbugs.FindBugs2
import edu.umd.cs.findbugs.Project
import scala.collection.mutable.Buffer
import edu.umd.cs.findbugs.PrintingBugReporter
import java.io.File
import org.apache.commons.io.FilenameUtils
import scala.collection.mutable.ListBuffer
import edu.umd.cs.findbugs.DetectorFactoryCollection
import edu.umd.cs.findbugs.Detector
import edu.umd.cs.findbugs.Priorities
import edu.umd.cs.findbugs.SystemProperties
import edu.umd.cs.findbugs.BugRanker
import edu.umd.cs.findbugs.ClassScreener
import edu.umd.cs.findbugs.FindBugs
import edu.umd.cs.findbugs.TextUIBugReporter
import edu.lu.uni.serval.alarm.util.AlarmFileUtils
import edu.lu.uni.serval.alarm.util.AlarmFileUtils


object FindBugsRunner {
	def runFindBugs(targetFiles: Buffer[String], 
			libFiles: Buffer[String],
			filterFiles: Buffer[String],
			reporter: TextUIBugReporter,
			releaseName: String,
			projectName: String) =
		{
			val findbugs = new FindBugs2();
			val project = new Project();
			
			findbugs.setDetectorFactoryCollection(DetectorFactoryCollection.instance())
			
			
			targetFiles.foreach { x => project.addFile(x) }
			libFiles.foreach { x => project.addAuxClasspathEntry(x) }
			
			//val reporter = new CsvBugReporter("tmp/commons-math/head.csv")
			//val reporter = new PrintingBugReporter
			
			//reporter.setPriorityThreshold(Priorities.HIGH_PRIORITY)
			reporter.setPriorityThreshold(Priorities.LOW_PRIORITY)
			val rankThreshold = SystemProperties.getInt("findbugs.maxRank", BugRanker.VISIBLE_RANK_MAX)
			reporter.setRankThreshold(rankThreshold)
			reporter.setUseLongBugCodes(true)
			reporter.setReportHistory(false)
			
			findbugs.setRankThreshold(rankThreshold)
			findbugs.setBugReporter(reporter)
			findbugs.setProject(project)
			findbugs.setUserPreferences(project.getConfiguration)
			findbugs.setClassScreener(new ClassScreener)
			findbugs.setRelaxedReportingMode(false)
			findbugs.setAbridgedMessages(false)
			findbugs.setAnalysisFeatureSettings(FindBugs.DEFAULT_EFFORT)
			findbugs.setMergeSimilarWarnings(true)
			findbugs.setReleaseName(releaseName)
			findbugs.setProjectName(projectName)
			findbugs.setScanNestedArchives(true)
			findbugs.setNoClassOk(false)
			filterFiles.foreach { x => findbugs.addFilter(x, false)}
			//findbugs.setBugReporterDecorators(explicitlyEnabled, explicitlyDisabled)

			findbugs.finishSettings;

			findbugs.execute;
		}

	def main(args: Array[String]): Unit =
		{
			// TODO this is just test.
			val targetJarPaths = AlarmFileUtils.getListOfJarFilePaths("/Users/darkrsw/git/commons-math/target")
			
			//targetJarPaths.foreach(Console.println(_))
			
			//runFindBugs(targetJarPaths, new ListBuffer[String]);		
		}
}