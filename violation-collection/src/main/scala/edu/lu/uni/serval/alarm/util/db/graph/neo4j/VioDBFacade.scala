package edu.lu.uni.serval.alarm.util.db.graph.neo4j

import org.neo4j.driver.v1._
import org.neo4j.driver.v1.Values._
import scala.collection.mutable._
import scala.collection.JavaConverters._
import com.google.gson.Gson
import com.google.gson.JsonObject

object VioDBFacade 
{
	var viodbURI = "bolt://empise.uni.lux:7687"
	var user = "task"
	var passwd = "ssel1008"
	var driver: Driver = _
	var session: Session = _
	
	def init() =
	{
		driver = GraphDatabase.driver( viodbURI, AuthTokens.basic( user, passwd ) )
		session = driver.session()
	}
	
	def findFixedWithoutFixer(): List[Record] =
	{
		val results = session.run(
				"""MATCH (f:Violation {resolution: 'fixed'})
					 WHERE NOT exists(f.fixer)
					 RETURN f 
				""") 
		
		results.list().asScala.toList
	}
	
	def setFixer2Node(id: String, fixerCommit: String) =
	{
		val result = session.run( """MATCH (n:Violation {id: {id} } )
										SET n.fixer = {fixer}
										RETURN n""",
				    value( Map( "id" -> id, 
				    						"fixer" -> fixerCommit).asJava )
				    )
				    
		val rlist = result.list()
		
		if(rlist.size() > 0)
		{
			print(".")
		}
		else
		{
			println()
			System.out.println( "CQL could not set %s as terminal.".format(id) )
		}
	}
	
	def searchFixedAlarms(): List[Record] =
	{
		val results = session.run(
				"""MATCH (f:Violation {resolution: 'fixed'})
					 WHERE exists(f.fixer)
					 RETURN f 
				""")
		
		results.list().asScala.toList
	}
  
	def addNewOriginViolation(id: String, project: String, commit: String, 
																vtype: String, category: String, sLine: Int, eLine: Int) =
	{
		val result = session.run( 
				"""MERGE (a:Violation {id: {id}})
					 ON CREATE SET a.oid = {id},
												 a.project = {project}, 
												 a.commit = {commit}, 
												 a.class = {class},
												 a.vtype = {vtype}, 
												 a.category = {category}, 
												 a.sLine = {sLine}, 
												 a.eLine = {eLine}
						RETURN a""",
				value( 
						Map("id" -> id, 
								"project" -> project, 
								"commit" -> commit,
								"class" -> "origin", 
								"vtype"-> vtype, 
								"category" -> category,
								"sLine" -> sLine,
								"eLine" -> eLine
								).asJava
					)
				)
				
		val rlist = result.list()
		
		if(rlist.size() > 0)
		{
			rlist.asScala.foreach(x => {
				print(".")
				//println( "A new origin added: %s.".format(id) )
				//println( x.toString() )
			})
		}
		else
		{
			System.out.println( "CQL could not add a node for %s.".format(id) )
		}
	}
	
	def connect2Parent( pid: String, id: String, commit: String, matched: String, sLine: Int, eLine: Int) =
	{
		val result = session.run( """MATCH (p:Violation { id: {pid} } )
															MERGE (c:Violation { id: {id} } )
															ON CREATE SET c.pid = {pid},
																		 c.oid = p.oid,
																		 c.project = p.project, 
																		 c.commit = {commit},
																		 c.vtype = p.vtype,
																		 c.category = p.category,
																		 c.matched = {matched},
																		 c.class = {class}, 
																		 c.sLine = {sLine}, 
																		 c.eLine = {eLine}
										MERGE (p)-[:CHILD]->(c)
  									MERGE (c)-[:PARENT]->(p)
										RETURN p, c
								""",
				value(
						Map("id" -> id, 
								"pid" -> pid, 
								"class" -> "child", 
								"commit"-> commit,
								"matched" -> matched,
								"sLine" -> sLine,
								"eLine" -> eLine
								).asJava
						)
				)
				
		val rlist = result.list()
		
		if(rlist.size() > 0)
		{
			rlist.asScala.foreach(x => {
				print("*")
				//System.out.println( x.toString() )
			})
		}
		else
		{
			System.out.println( "CQL could not found any match for %s -> %s.ㅇ".format(pid, id) )
		}
	}
	
	def setTerminal( id: String, resolution: String, fixerCommit: String ) =
	{
		val result = session.run( """MATCH (n:Violation {id: {id} } )
										SET n.resolution = {resolution}, n.fixer = {fixer}
										RETURN n""",
				    value( Map( "id" -> id, 
				    		"resolution" -> resolution, 
				    		"fixer" -> fixerCommit).asJava )
				    )
				    
		val rlist = result.list()
		
		if(rlist.size() > 0)
		{
			rlist.asScala.foreach(x => {
				println( "Terminal: %s by %s.".format(id, fixerCommit) ) 
				println( x.toString() )
			})
		}
		else
		{
			System.out.println( "CQL could not set %s as terminal.".format(id) )
		}
	}
	
	def close() =
	{
		session.close()
		driver.close()
	}
}