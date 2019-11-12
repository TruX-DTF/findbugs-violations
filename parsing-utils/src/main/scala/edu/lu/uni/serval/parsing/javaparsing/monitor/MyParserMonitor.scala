package edu.lu.uni.serval.parsing.javaparsing.monitor

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.core.runtime.NullProgressMonitor

/**
  * Created by darkrsw on 2016/August/15.
  */
class MyParserMonitor extends NullProgressMonitor with LazyLogging
{
  override def beginTask(name: String, totalWork: Int): Unit =
  {
    logger.debug("Task: " + name)
    super.beginTask(name, totalWork);
  }
}