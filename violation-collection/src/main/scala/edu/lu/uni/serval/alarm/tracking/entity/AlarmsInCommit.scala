package edu.lu.uni.serval.alarm.tracking.entity

import java.io.File
import scala.collection.mutable._

import org.apache.commons.io.FilenameUtils

import com.github.tototoshi.csv.CSVReader
import com.typesafe.scalalogging.LazyLogging

class AlarmsInCommit(h: String, rawList: List[List[String]]) extends LazyLogging  
{
	val commitHash = h;
	val parents = Set[AlarmsInCommit]()			// begin with empty set
	val children = Set[AlarmsInCommit]()		// begin with empty set
	
	val alarms = parseAll(rawList)
		//for(x <- rawList) yield parseAlarm(x)

	def parseAll(rawList: List[List[String]]): List[AlarmEntity] =
	{
		val aSet = Set[String]()
		val aList = ListBuffer[AlarmEntity]()
		
		rawList.map( x =>
			{
				val stringfied = x.mkString(",")
				if( ! aSet.contains(stringfied) )
				{
					aList += parseAlarm(x)
					aSet.add(stringfied)
				}
				else
				{
					// TODO just for logging and testing
					//println("DUPLICATE: " + stringfied)
				}
			})
			
		return aList.toList
	}
	
  def parseAlarm(x: List[String]): AlarmEntity = 
  {
	  // x = a record of 8 columns
		val asArray = x.toArray
		
		new AlarmEntity(
				x(0), // priority
				x(1), // category
				x(2), // violation type
				x(3), // class
				x(4), // field
				x(5), // method
				x(6), // start line
				x(7),  // end line
				this
		);
	}
}


// This main method is just for an instant test.
object AlarmsInCommit extends LazyLogging
{
	def main(args: Array[String]) =
	{
		val in = new File(args(0))
		val csvreader = CSVReader.open(in)
		val rawList = csvreader.all()
		
		val commitHash = FilenameUtils.getBaseName(in.getName)
  		
		val aCommit = new AlarmsInCommit(commitHash, rawList)
		
		Console.println("First Hash: " + aCommit.commitHash) 
		Console.println("First alarm ==> " + aCommit.alarms.head.toString())
	
		Console.println("Collected alarms #: " + aCommit.alarms.size)
	}
}