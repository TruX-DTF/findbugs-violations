package edu.lu.uni.serval.parsing.javaparsing

import edu.lu.uni.serval.parsing.javaparsing.visitor.{AnnotationVisitor}
import org.eclipse.jdt.core.dom.{ASTVisitor, PackageDeclaration, TypeDeclaration}
import org.junit.Test
import java.io.File

/**
  * Created by darkrsw on 2016/July/09.
  */
class InMemoryParserTest
{
  @Test
  def testParsingString(): Unit =
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

    val cu = InMemoryParser.parse(source)
    val packageVisitor = new ASTVisitor() {

      override def visit(node: PackageDeclaration): Boolean =
      {
        val name = node.getName
        Console.println("Package: " + name)

        return super.visit(node)
      }

      override def visit(node: TypeDeclaration): Boolean =
      {
        val name = node.getName
        Console.println("Class: " + name)

        return super.visit(node)
      }
    }

    cu.accept(packageVisitor)
  }

  @Test
  def testBindingParser(): Unit =
  {
    //val source = Source.fromFile("src/test/java/edu/lu/uni/serval/parsing/javaparsing/TestClass4Parser.java").mkString
    val paths = Array[String]("src/test/java/edu/lu/uni/serval/parsing/javaparsing/TestClass4Parser.java")
    val visitor = new AnnotationVisitor()
    val depenRoot = new File("target/dependency/")
    val classPath = depenRoot.listFiles().filter(_.getName.endsWith(".jar"))
    val classList = for(path <- classPath) yield path.getCanonicalPath

    //Array[String]("target/dependency/*", "target/classes", "target/test-classes")

    InMemoryParser.parseJavaFileWithBinding(paths, classList ++ Array("target/classes", "target/test-classes"), visitor)
  }
}

