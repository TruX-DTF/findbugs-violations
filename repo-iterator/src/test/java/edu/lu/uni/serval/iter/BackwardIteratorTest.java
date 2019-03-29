package edu.lu.uni.serval.iter;

import static org.junit.Assert.fail;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Ignore;
import org.junit.Test;

import edu.lu.uni.serval.iter.filter.CommitFilter;
import edu.lu.uni.serval.scm.git.GitProxy;

public class BackwardIteratorTest
{
	@Ignore
	@Test
	public void test()
	{
		GitProxy proxy = new GitProxy();
		proxy.setURI("/Users/darkrsw/git/findbugs/.git/");
		
		if(!proxy.connect()) fail();
		
		BackwardIterator iter = new BackwardIterator(proxy);
		
		iter.setPreFilter( new CommitFilter() {

			@Override
			public boolean filterCommit(RevCommit commit)
			{
				if(commit.getParentCount() > 1 || commit.getShortMessage().startsWith("Merge ")
						|| commit.getShortMessage().startsWith("Merged ") ) return false;
				else
					return true;
			} });
		
		iter.setTask(new CommitTask() {

			@Override
			public boolean doTask(RevCommit commit)
			{
				String hash = commit.getId().toString();
				String nParents = ""+commit.getParentCount();
				System.out.println("("+nParents+") "+hash+ ": " + commit.getShortMessage());
				
				return true;
			}

			@Override
			public String getResultMsg()
			{
				return "";
			}
			
		});
		
		iter.iterateBackward();
	}

}