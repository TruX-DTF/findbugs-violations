package edu.lu.uni.serval.scm.git

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import java.io.File
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.errors.AmbiguousObjectException
import org.eclipse.jgit.errors.IncorrectObjectTypeException
import org.eclipse.jgit.errors.RevisionSyntaxException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.util.io.DisabledOutputStream
import scala.collection.JavaConversions
import scala.collection.mutable.Buffer
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import scala.collection.JavaConverters._
import org.eclipse.jgit.diff.Edit

class GitProxy extends LazyLogging
{
  // base uri
  private var uri : String = "";

  // check connected. Don't connect again to another repository.
  // For a new repository, just create a new one.
  private var isConnected = false

  // repository handler
  private var repository: Repository = null;
  
  private var df = new DiffFormatter(DisabledOutputStream.INSTANCE)
	
  def connect(): Boolean = 
  {
    if (this.isConnected)
    {
      logger.error("This proxy is already connected. Create a new one.");
      return false;
    }

    if (this.uri == null)
    {
      logger.error("Repository URI is not set.");
      return false;
    }

    val builder = new FileRepositoryBuilder();
    repository = builder.setGitDir(new File(this.uri)).readEnvironment() // scan
                                        // environment
                                        // GIT_*
                                        // variables
        .findGitDir() // scan up the file system tree
        .build();

    if (this.testConnection())
    {
      logger.info("Repository connected: " + repository.getDirectory());
      this.isConnected = true;
      df.setRepository(getRepository())
  
      return true;
    } else
    {
      logger.error("Wrong repository location");
      this.isConnected = false;
      return false;
    }
  }
  
  def getLogAll(): Iterable[RevCommit] = 
  {
    //val head = repository.resolve(Constants.HEAD);

    return JavaConversions.iterableAsScalaIterable(new Git(repository).log().all().call())
  }
  
  def getCommitByHash(hash: String): RevCommit =
  {
    val obj = repository.resolve(hash);
    val walk = new RevWalk(this.repository);
    val commit = walk.parseCommit(obj);
    
    return commit;
  }
  
  def getFileContent(commit: RevCommit, path: String): String =
  {
  	val tree = commit.getTree
  	
  	val treeWalk = new TreeWalk(this.repository)
  	treeWalk.addTree(tree)
  	treeWalk.setRecursive(true)
  	treeWalk.setFilter(PathFilter.create(path))
  	
  	if(!treeWalk.next())
  	{
  		logger.error(s"Can't find file: $path in " + commit.getName)
  		return null
  	}
  	
  	val resultingPath = treeWalk.getPathString
  	if( path != resultingPath )
  	{
  		logger.info("Resulting path is different from requested one: " + resultingPath)
  		return null
  	}
  	
  	val objectID = treeWalk.getObjectId(0)
  	val loader = this.repository.open(objectID)
  	
  	val content = new String(loader.getBytes)
  	
  	return content
  }
  
  def getFileContent(hash: String, path: String): String = getFileContent(getCommitByHash(hash), path);
  
  
  def getChangedFiles(current: RevCommit, parent: RevCommit): Buffer[DiffEntry] = 
  {
    val df = new DiffFormatter(DisabledOutputStream.INSTANCE);
    df.setRepository(this.repository);
    df.setDiffComparator(RawTextComparator.DEFAULT);
    df.setDetectRenames(true);
    val diffs = df.scan(parent.getTree(), current.getTree());
    
    JavaConversions.asScalaBuffer(diffs);
  }
  
  def getEditList(d: DiffEntry): List[Edit] =
  {
  	val header = df.toFileHeader(d)
		header.toEditList().asScala.toList
  }
  
  def testConnection(): Boolean =
  {
    if (this.repository == null)
    {
      logger.error("Repository object is null");
      return false;
    }

    val head = this.repository.resolve(Constants.HEAD);

    if (head == null)
    {
      logger.error("Wrong repository location or incorrect options");
      return false;
    }

    return true;
  }
  
  def setURI(in: String): Unit =
  {
    uri = in;
  }
  
  def getURI() = uri;
  
  def disconnect(): Unit = 
  {
    this.repository.close()
  }
  
  def getRepository() = this.repository
}