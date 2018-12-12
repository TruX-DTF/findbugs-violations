package edu.lu.uni.serval.alarm.tracking

import java.io.File

import scala.collection.JavaConverters._

import com.typesafe.scalalogging.LazyLogging
import scala.annotation.tailrec
import scala.io.Source
import com.github.tototoshi.csv.CSVReader
import org.apache.commons.io.FilenameUtils
import edu.lu.uni.serval.alarm.tracking.entity.AlarmsInCommit

class AlarmDataReader extends LazyLogging 
{
  def readAlarmList4AllCommits(reportRootPath: String): List[AlarmsInCommit] =
  {
  	val reportRootDir = new File(reportRootPath)
  	val reportFiles = reportRootDir.listFiles().filter( x => x.getName.endsWith(".csv") )
  	
  	@tailrec
  	def collectAlarmsRec(files: List[File], aList: List[AlarmsInCommit]): List[AlarmsInCommit] = 
  	{
  		files match {
  			case Nil => aList
  			case x :: tail => collectAlarmsRec(tail, readAlarmsOfCommit(x) :: aList)
  		}
  	}
  	
  	def readAlarmsOfCommit(in: File): AlarmsInCommit =
  	{
  		//val lines = Source.fromFile(in).getLines()
  		val csvreader = CSVReader.open(in)
  		val rawList = csvreader.all()
  		
  		val commitHash = FilenameUtils.getBaseName(in.getName)
  		
  		val aCommit = new AlarmsInCommit(commitHash, rawList)
  		return aCommit
  	}
  	
  	collectAlarmsRec(reportFiles.toList, List[AlarmsInCommit]())
  }
}


// This main method is just for an instant test.
object AlarmDataReader extends LazyLogging
{
	def main(args: Array[String]): Unit =
	{
		val projectRootPath = args(0)
		
		val reader = new AlarmDataReader
		val list = reader.readAlarmList4AllCommits(projectRootPath)
		
		Console.println("# of commits: " + list.size)
		
		val totalAlarms = list.map(_.alarms.size).sum
		Console.println(s"# of total alarms: $totalAlarms")
	}
}