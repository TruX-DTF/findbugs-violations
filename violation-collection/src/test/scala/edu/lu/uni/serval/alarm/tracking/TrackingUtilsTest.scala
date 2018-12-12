package edu.lu.uni.serval.alarm.tracking

import org.junit.Test
import org.junit.Assert._

import edu.lu.uni.serval.alarm.tracking.TrackingUtils;

class TrackingUtilsTest 
{
	@Test
	def testTakeClassName() =
	{
		val input = "org.apache.hadoop.fs.TestFileUtil$MyFile"

		val className = TrackingUtils.takeClassName(input)
		Console.println(className)
		assertEquals(className, "TestFileUtil")
	}
	
	@Test
	def testTakeFileName() =
	{
		val input = "false-alarm-study/src/test/scala/edu/lu/uni/serval/alarm/util/db/graph/neo4j/VioDBFacadeTest.scala"
		
		val filename = TrackingUtils.takeFileName(input)
		println(filename)
		assertEquals("VioDBFacadeTest", filename)
	}
	
	@Test
	def testTakeLineRange() =
	{
		val input = "This is the first line.\n" + 
						"This is the second line.\n" +
						"This is the third line\n" +
						"This is the fourth line.\n" +
						"This is the fifth line."
						
		val expected = "This is the second line.\n" +
						"This is the third line\n" +
						"This is the fourth line.\n"
						
		assertEquals(expected, TrackingUtils.takeLineRange(2, 4, input) )
	}
	
	@Test
	def testHashTokens()
	{
		val input = """In order to reside and work on the territory lawfully, you need to apply for a “temporary stay authorization”.
								|	The University of Luxembourg offers to be the intermediate between the Ministry of Foreign Affairs (MAE) and the employee for the request of the “temporary stay authorization. Your HR contact will help you to prepare your file (and for your family members, if applicable) and send it to the MAE. 
								|	We hope that this information will help you to understand the different steps before arriving and working in Luxembourg.
								|	Step 1: Gather all the required documents (as listed on p.3/4) and send them by post to your HR contact person at the University of Luxembourg. 
								|	Step 2:  Wait for the decision of the MAE. The application needs to be favorably advised before your entry on the Luxembourg territory. Processing the authorization request takes more or less 3 months. If granted we’ll send you the “temporary stay authorization” per registered mail.  If it is not granted or if more documents are requested we will inform you immediately.
								|	Step 3: The “temporary stay authorization” document will indicate if you need to request a Visa.
								|		If you need a visa, you must request it at the Embassy within 90 days following the issuance of the “temporary stay authorization”. Without a visa, you are not allowed to enter the territory of Luxembourg.
								|		If you don’t need a visa, you must enter the Luxembourg territory within 90 days following the issuance of the “temporary stay authorization”. 
								|	
								|	Step 4: After your arrival in Luxembourg, you’ll have 3 working-days to register at the local municipality (“Administration Communale”) of your place of residence which means that you need an address in Luxembourg. You will receive a declaration of arrival. This declaration and the “temporary stay authorization” provides proof of the legality of your stay until the biometric residence permit has been issued. 
									"""
		
		val tokens = input.split("\\s+")
		
		val head = TrackingUtils.takeFirstTokens(TrackingUtils.HASH_SIZE, tokens)
		println(head)
		
		val tail = TrackingUtils.takeLastTokens(TrackingUtils.HASH_SIZE, tokens)
		println(tail)
		
		val headHash = TrackingUtils.hashFirstTokens(TrackingUtils.HASH_SIZE, tokens)
		println("\n"+headHash)
		
		assertEquals("e0678060881020f0cd7a14f5f9b20154b43d330c", headHash)
		
		val tailHash = TrackingUtils.hashLastTokens(TrackingUtils.HASH_SIZE, tokens)
		println("\n"+tailHash)
		
		assertEquals("53166ab9a48f8866175290d907030a18e131dda9", headHash)
	}
}