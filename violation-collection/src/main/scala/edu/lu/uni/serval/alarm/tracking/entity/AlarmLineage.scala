package edu.lu.uni.serval.alarm.tracking.entity

import com.typesafe.scalalogging.LazyLogging
import scala.collection.mutable._

class AlarmLineage(origin: AlarmEntity) extends LazyLogging 
{
  val alarmGraph = Map[AlarmEntity, Set[AlarmEntity]]()
  alarmGraph += (origin -> Set[AlarmEntity]()) // initialization.
  
  // NOTE: this adds additional child to a parent 
  def attachAfter(parent: AlarmEntity, child: AlarmEntity): Boolean =
  {  	
  	val childCheck = alarmGraph.get(child) match
  	{
  		case None => true 
  		case Some(children) => false // circular? already exists?
  	}
  	
  	if(!childCheck)
  	{
  		logger.error("Something wrong with terminal tacking: new child already exists!") 
  		return childCheck
  	}
  	
  	val childSet = alarmGraph.get(parent)
  	val parentCheck = childSet match
  	{
  		case None => false // parent should be here
  		case Some(_) => true
  	}
  	
  	if(!parentCheck)
  	{
  		logger.error("Something wrong with terminal tacking: parent does not exist!") 
  		return parentCheck
  	}
  	
  	childSet match
  	{
  		case None => Set[AlarmEntity](child) // can happen?
  		case Some(children) => { children += child; 
  														 alarmGraph += (child -> Set[AlarmEntity]()); }
  	}
  	
  	return true;  	 
  }
  
  // get origin's alarm signature
  def getAlarmSignature() = { }
  
  def contains(a: AlarmEntity): Boolean =
  {
  	alarmGraph.contains(a)
  }
}