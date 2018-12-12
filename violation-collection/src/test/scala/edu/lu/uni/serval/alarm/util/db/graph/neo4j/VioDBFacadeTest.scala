package edu.lu.uni.serval.alarm.util.db.graph.neo4j

import org.junit.Test

class VioDBFacadeTest 
{
	@Test
	def testCreateOrigin()
	{
		VioDBFacade.init()
		// (id: String, project: String, commit: String, 
																//vtype: String, category: String, sLine: Int, eLine: Int)
		VioDBFacade.addNewOriginViolation("new:commons-math:type", "commons-math", 
				"a142341324324", "NPE", "category1", 213, 214)
		VioDBFacade.close()
	}
	
	@Test
	def testConnect2Parent()
	{
		VioDBFacade.init()
		//id: String, pid: String, commit: String, sLine: Int, eLine: Int
		VioDBFacade.connect2Parent(
				"new:commons-math:type", "child:commons-math:child", "hash", "b142341324324", 213, 214)
		VioDBFacade.close()
	}
	
	@Test
	def testSetTerminal()
	{
		VioDBFacade.init()
		VioDBFacade.setTerminal("child:commons-math:child", "fixed", "child:commit")
		VioDBFacade.close()
	}
}