package edu.lu.uni.serval.alarm.findbugs

import java.io.File

import scala.collection.JavaConversions
import scala.collection.mutable.HashSet
import scala.collection.mutable.ListBuffer

import com.github.tototoshi.csv.CSVWriter
import com.github.tototoshi.csv.DefaultCSVFormat
import com.github.tototoshi.csv.QUOTE_ALL
import com.github.tototoshi.csv.Quoting

import edu.umd.cs.findbugs.BugCollection
import edu.umd.cs.findbugs.BugInstance
import edu.umd.cs.findbugs.DetectorFactoryCollection
import edu.umd.cs.findbugs.Priorities
import edu.umd.cs.findbugs.TextUIBugReporter
import edu.umd.cs.findbugs.classfile.ClassDescriptor


/**
 * @author darkrsw
 */
class CsvBugReporter(outputPath: String) extends TextUIBugReporter 
{
	val OTHER_CATEGORY_ABBREV = "X";
	
  val seenAlready = new HashSet[BugInstance]
	val alarmList = new ListBuffer[List[Any]]
  
	def doReportBug(ins: BugInstance): Unit = 
  {
    if (seenAlready.add(ins)) 
    {
    	//Console.println("\n" + ins.toString())    	
    	recordBugInstance(ins);
      notifyObservers(ins);
    }
  }

  def finish(): Unit = 
  {
    val outFile = new File(outputPath)
    val writer = CSVWriter.open(outFile)(MyCSVFormat)
    
    writer.writeAll(this.alarmList)
    writer.flush()
    writer.close()
  }

  def getBugCollection(): BugCollection = {
    // TODO nothing to do currently.
  	return null;
  }

  def observeClass(cs: ClassDescriptor): Unit = {
    // TODO do we need to compute statistics?
  }

  def recordBugInstance(bugInstance: BugInstance): Unit = 
  {
    val prioirty = bugInstance.getPriority() match  
    {
        case Priorities.EXP_PRIORITY => "E"
        case Priorities.LOW_PRIORITY => "L"
        case Priorities.NORMAL_PRIORITY => "M"
        case Priorities.HIGH_PRIORITY => "H"
        case _ => "false";
    }
    
    val pattern = bugInstance.getBugPattern();
    var categoryAbbrev: String = "X"
    
    if( pattern != null )
    {
    	val bcat = DetectorFactoryCollection.instance().getBugCategory(pattern.getCategory());
      if (bcat != null) 
      {
      	/*categoryAbbrev = if(getUseLongBugCodes){ bcat.getCategory }
      																		else { bcat.getCategory }*/
      	categoryAbbrev = bcat.getAbbrev
      }
    }
    
    val bugAbbv = if(pattern != null) 
    {
    	if(getUseLongBugCodes) { pattern.getType }
    										else { pattern.getAbbrev }
    } else
    {
    	""
    }
    
    
    val annotationList = JavaConversions.asScalaBuffer(bugInstance.getAnnotations)
    
    //annotationList.foreach(x=>Console.println(x.getClass))
    
    val aClass = bugInstance.getPrimaryClass
    val aField = bugInstance.getPrimaryField
    val aMethod = bugInstance.getPrimaryMethod
    val aSourceLine = bugInstance.getPrimarySourceLineAnnotation
    
    val sClass = aClass match
    {
    	case null => "NO_CLASS"
    	case _ => aClass.getClassName
    }
    
    val sField = aField match
    {
    	case null => "NO_FIELD"
    	case _ => aField.getFieldName
    }
    
    val sMethod = aMethod match
    {
    	case null => "NO_METHOD"
    	case _ => aMethod.getMethodName + aMethod.getMethodSignature
    }
    
    val pSourceLine = aSourceLine match
    {
    	case null => (-1, -1)
    	case _ => (aSourceLine.getStartLine, aSourceLine.getEndLine)
    }
    
    //bugInstance.getPrimarySourceLineAnnotation.getSourcePath
    /*Console.println(
    		(sClass, sField, sMethod, pSourceLine)
    		)*/
    
    val bugRow = List(prioirty, categoryAbbrev, bugAbbv, sClass, sField, sMethod, pSourceLine._1, pSourceLine._2)
    alarmList += bugRow
  }
}

object MyCSVFormat extends DefaultCSVFormat
{
	override val quoting = QUOTE_ALL
}