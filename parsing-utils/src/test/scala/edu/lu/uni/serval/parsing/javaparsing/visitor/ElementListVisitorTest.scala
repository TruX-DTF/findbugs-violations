package edu.lu.uni.serval.parsing.javaparsing.visitor

import edu.lu.uni.serval.parsing.javaparsing.InMemoryParser
import edu.lu.uni.serval.parsing.javaparsing.util.ASTUtils
import org.junit.Test

/**
  * Created by darkrsw on 2016/August/04.
  */
class ElementListVisitorTest
{
  val source =
    """
      |package edu.lu.uni.serval.parsing.javaparsing
      |
      |import org.junit.Test
      |
      |public class SampleTest {
      | @Test
      | public void testandtest()
      | {
      |   return;
      | }
      |}
      |
      |class AnotherClass {
      | public void anotherMethod()
      | {
      |   return;
      | }
      |}        |
    """.stripMargin

  @Test
  def testElementCollection() =
  {
    val cu = InMemoryParser.parse(source)
    val visitor = new ElementListVisitor

    cu.accept(visitor)

    visitor.classTypes.foreach(println)

    visitor.classTypes.foreach{
      case (name, (start, end)) => Console.println(ASTUtils.getLineRange(cu, (start, end)))
    }
  }
}
