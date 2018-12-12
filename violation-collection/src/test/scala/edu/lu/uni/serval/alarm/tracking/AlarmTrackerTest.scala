package edu.lu.uni.serval.alarm.tracking

import org.junit.Test
import scala.io.Source
import com.github.gumtreediff.actions.model._

import edu.lu.uni.serval.alarm.tracking.TrackingUtils;

class AlarmTrackerTest 
{
	@Test
	def testGumTree() =
	{
		val oldSource = Source.fromFile("tmp/input/TestFilterFileSystem.java").mkString
		val newSource = Source.fromFile("tmp/input/TestFilterFileSystem2.java").mkString
		val actions = TrackingUtils.getGumTreeDiff(oldSource, newSource)
		
		Console.println(actions.size)
		actions.foreach {
			case x: Insert =>
				Console.println("Insert: " + x.getNode.getType + ":" + x.getNode.getLabel + "(" + x.getParent.getType + ")" )
			case x: Update => Console.println("Update: " + x.getNode.getLabel + "(" + x.getNode.getPos + ")" + ":" + x.getValue)
			case x: Delete => Console.println("Delete: " + x.getNode.getLabel)
			case x: Move => Console.println("Move: " + x.getNode.getPos + "-" + x.getNode.getLength + "-" + x.getNode.getEndPos + ":" + x.getPosition)
			case x: Addition => Console.println("Add: " + x.getNode.getLabel)
		}
	}
	
	@Test
	def testMatching() =
	{
		
	}
}