package edu.lu.uni.serval.alarm.tracking.entity

import com.typesafe.scalalogging.LazyLogging
import edu.umd.cs.findbugs.Priorities._

class AlarmEntity(
									p: String, // priority
									c: String, // category
									v: String, // violation type
									aClass: String, // class
									aField: String, // field
									aMethod: String, // method
									sLine: String, // start line
									eLine: String, // end line
									commit: AlarmsInCommit // where this belongs to
									) extends LazyLogging								
{
	//////////////////////////////////////
	// Basic constructor begin
	
	val priority = p match {
		case "E" => EXP_PRIORITY
		case "L" => LOW_PRIORITY
		case "M" => NORMAL_PRIORITY
		case "H" => HIGH_PRIORITY
		case _   => IGNORE_PRIORITY
	}
	
	val category = c 
	val vType = v
	val className = aClass
	val fieldName = aField
	val methodName = aMethod
	val startLine = sLine.toInt
	val endLine = eLine.toInt
	val baseCommit = commit
	// End of basic constructor
	///////////////////////////////////////
	
	override def toString(): String =
	{
		val sb = new StringBuilder()
		
		sb ++= priorityToString + ";"
		sb ++= category + ";   "
		sb ++= vType + "@" + className + ";" + fieldName + "#" + methodName + ":"
		sb ++= s"$startLine-$endLine"
		sb.toString() 
	}
	
	def priorityToString() =
	{
		priority match {
			case EXP_PRIORITY => "EXP_PRIORITY"
			case LOW_PRIORITY => "LOW_PRIORITY"
			case NORMAL_PRIORITY => "NORMAL_PRIORITY"
			case HIGH_PRIORITY => "HIGH_PRIORITY"
			case _   => "IGNORE_PRIORITY" 
		}
	}
}