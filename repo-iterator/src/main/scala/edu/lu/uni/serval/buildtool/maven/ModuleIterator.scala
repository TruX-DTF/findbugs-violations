package edu.lu.uni.serval.buildtool.maven

import org.apache.maven.model.building.DefaultModelBuildingRequest
import org.apache.maven.model.building.ModelBuildingException
import org.apache.maven.model.building.DefaultModelBuilder
import org.apache.maven.model.Model
import java.io.File
import org.apache.maven.model.building.ModelBuildingRequest
import com.typesafe.scalalogging.LazyLogging
import scala.collection.mutable.Stack
import scala.collection.JavaConversions
import org.apache.maven.model.building.DefaultModelBuilderFactory
import scala.xml._
import scala.collection.mutable.ListBuffer

/**
 * @author darkrsw
 */
object ModuleIterator extends LazyLogging
{
	def getModulesFromPom(rootPath: String): Iterable[String] = 
	{
		val modules = Stack[String]()
		
		def addModuleRecursive(pomPath: String): Unit =
  	{
  		val pomxml = XML.loadFile(pomPath+"/pom.xml")
		  
  		if( pomxml == null )
  		{
  			// Actually, this is error;
  			logger.error("Invalid pom.xml path: "+ pomPath)
  			return;
  		}
  		else
  		{
  			modules.push(pomPath)
  		}
  		
  		val moduleNodes = (pomxml \ "modules" \ "module")
			if(moduleNodes.isEmpty)
  		{
  			return;
  		}
  		else
  		{
  			moduleNodes.foreach(x => addModuleRecursive(pomPath + "/" + x.text))
  		}
  	}
  	
  	addModuleRecursive(rootPath)
		
		return modules
	}
	
	@Deprecated
  private def readPOM(targetBasePath: String) : Model =
  {
    val pomPath = new File(targetBasePath+"/pom.xml");
    
    if(!pomPath.exists())
      return null;
    
    val req =
         new DefaultModelBuildingRequest().setProcessPlugins( false ).setPomFile( pomPath ).
         setTwoPhaseBuilding( false ).setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );
    
    //req.setModelResolver( new RepositoryModelResolver( basedir, pathTranslator ) );
    req.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );

    val builder = new DefaultModelBuilderFactory().newInstance();
    var model: Model =null;
    
    try
    {
        model = builder.build( req ).getEffectiveModel();
    }
    catch
    {
      case e: ModelBuildingException => logger.error("Invalid pom.xml at " + pomPath); e.printStackTrace();
      model = null;
    }
    
    return model;
  }
  
	@Deprecated
  def getAllModules(rootPomPath: String): Iterable[String] = // returns module (containing pom.xml) paths
  {
  	// takes path containing "pom.xml" (not pom.xml itself).
  	
  	val modules = Stack[String]()
  	
  	def addModuleRecursive(pomPath: String): Unit =
  	{
  		val model = readPOM(pomPath)
  		if( model == null )
  		{
  			// Actually, this is error;
  			logger.error("Invalid pom.xml path: "+ pomPath)
  			return;
  		}
  		else
  		{
  			modules.push(pomPath)
  		}
  		
  		val moduleList = JavaConversions.asScalaBuffer(model.getModules)
  		if(moduleList.isEmpty)
  		{
  			return;
  		}
  		else
  		{
  			moduleList.foreach(x => addModuleRecursive(pomPath + "/" + x))
  		}
  	}
  	
  	addModuleRecursive(rootPomPath)
  	
  	return modules
  }
}