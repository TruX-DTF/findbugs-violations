package edu.lu.uni.serval.parsing.javaparsing

import com.typesafe.scalalogging.LazyLogging
import edu.lu.uni.serval.parsing.javaparsing.monitor.MyParserMonitor
import edu.lu.uni.serval.parsing.javaparsing.requestor.MyParserRequestor
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom._

/**
  * Created by darkrsw on 2016/July/09.
  */
object InMemoryParser extends AbstractJavaParser with LazyLogging
{
  // This method parses a Java file only. No compilation and binding at all.
  def parse(source: String): CompilationUnit =
  {
    val parser = ASTParser.newParser(AST.JLS8)

    parser.setSource(source.toCharArray)

    val options = JavaCore.getOptions
    JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options)
    parser.setCompilerOptions(options)

    parser.setKind(ASTParser.K_COMPILATION_UNIT)
    val cu: CompilationUnit = parser.createAST(null).asInstanceOf[CompilationUnit]

    return cu
  }

  def parseType(source: String): ASTNode =
  {
    val parser = ASTParser.newParser(AST.JLS8)

    parser.setSource(source.toCharArray)
    val options = JavaCore.getOptions
    JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options)
    parser.setCompilerOptions(options)

    parser.setKind(ASTParser.K_STATEMENTS)
    parser.createAST(null)
  }


  /* TODO Implement later (ISSUE: How to create dummy IJavaProject?)
  def parseSingleJavaSourceWithBinding(source: String, classPaths: Array[String],
                                       visitor: ASTVisitor): Unit =
  {
    val cu = parse(source)
    val parser = ASTParser.newParser(AST.JLS8)
    parser.setEnvironment(classPaths, Array[String](), null, false)

    val options = JavaCore.getOptions()
    JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options)
    parser.setCompilerOptions(options)

    /*val name = "MyProject"
    val root = ResourcesPlugin.getWorkspace().getRoot()
    val project= root.getProject(name)
    project.create(null)
    project.open(null)

    val desc = project.getDescription()
    desc.setNatureIds(Array[String](JavaCore.NATURE_ID))
    project.setDescription(desc, null)
    val javaProj = JavaCore.create(project)*/

    val javaProject = JavaCore.create(project);

    parser.setProject(javaProj)

    parser.setKind(ASTParser.K_COMPILATION_UNIT)
    parser.setResolveBindings(true)
    parser.setBindingsRecovery(true)
    parser.setStatementsRecovery(true)

    parser.createASTs(Array[ICompilationUnit](cu.getJavaElement.asInstanceOf[ICompilationUnit]),
       Array[String](), new MyParserRequestor(visitor), new MyParserMonitor())
  }
  */

  def parseJavaFileWithBinding(paths: Array[String], classPaths: Array[String],
                                     visitor: ASTVisitor): Unit =
  {
    //val cu = parse(source)
    val parser = ASTParser.newParser(AST.JLS8)
    parser.setEnvironment(classPaths, Array[String](), null, true)

    val options = JavaCore.getOptions()
    JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options)
    parser.setCompilerOptions(options)

    parser.setKind(ASTParser.K_COMPILATION_UNIT)
    parser.setResolveBindings(true)
    parser.setBindingsRecovery(true)
    parser.setStatementsRecovery(true)

    parser.createASTs(paths, null, Array[String](), new MyParserRequestor(visitor), new MyParserMonitor())
  }
}
