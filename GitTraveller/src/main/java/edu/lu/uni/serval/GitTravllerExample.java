package edu.lu.uni.serval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.lu.uni.serval.git.exception.GitRepositoryNotFoundException;
import edu.lu.uni.serval.git.exception.NotValidGitRepositoryException;
import edu.lu.uni.serval.git.travel.CommitDiffEntry;
import edu.lu.uni.serval.git.travel.GitRepository;
import edu.lu.uni.serval.git.travel.MyDiffEntry;
import edu.lu.uni.serval.utils.FileHelper;

public class GitTravllerExample {
	
	private static final String DATASET_FILE_PATH = "OUTPUT/";
	private static final String REVISION_FILE_PATH = "/revisionFiles/";
	private static final String PREVIOUS_FILE_PATH = "/prev/";
	private static final String GIT_REPOSITORY_PATH = "../dataset/lucene-solr/.git";
	
	public static void main(String[] args) throws GitRepositoryNotFoundException, NotValidGitRepositoryException, IOException, NoHeadException, GitAPIException {
		String revisedFilesPath = DATASET_FILE_PATH + getRepositoryName(GIT_REPOSITORY_PATH) + REVISION_FILE_PATH;
		String previousFilesPath = DATASET_FILE_PATH + getRepositoryName(GIT_REPOSITORY_PATH) + PREVIOUS_FILE_PATH;
		FileHelper.createDirectory(revisedFilesPath);
		FileHelper.createDirectory(previousFilesPath);
		
		GitRepository gitRepo = new GitRepository(GIT_REPOSITORY_PATH, revisedFilesPath, previousFilesPath);
		gitRepo.open();
		List<RevCommit> commits = gitRepo.getAllCommits(true);
//		System.out.println("Selected Commits: " + commits.size());
//		gitRepo.createFilesOfAllCommits(commits);// Create non-test java files in all commits. 
		// select bug-related commits by commit messages
//		List<RevCommit> selectedCommits = gitRepo.filterCommits(commits);
//		List<String> commitIds = new ArrayList<>();//TODO: a list of selected commit IDs.
		List<RevCommit> selectedCommits = readRevCommit(gitRepo, commits);
		List<CommitDiffEntry> commitDiffentries = gitRepo.getCommitDiffEntries(selectedCommits);
		
//		List<MyDiffEntry> myDiffEntries = gitRepo.getMyDiffEntries(commitDiffentries);
		List<MyDiffEntry> myDiffEntries = gitRepo.getMyDiffEntriesWithContext(commitDiffentries);
		System.out.println("DiffEntries: " + myDiffEntries.size());
		
		// get the revised java file and previous java file.
		gitRepo.createFilesForGumTree(commitDiffentries);
		gitRepo.close();
		
	}
	
	private static List<RevCommit> readRevCommit(GitRepository gitRepo, List<RevCommit> commitIds) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
		List<RevCommit> commits = new ArrayList<>();
		for (RevCommit commitId : commitIds) {
//			commits.add(gitRepo.getRevCommitById(commitId));
			if (commitId.getId().getName().startsWith("9c5be23")) {
				commits.add(commitId);
				break;
			}
		}
		return commits;
	}

	private static String getRepositoryName(String gitRepositoryPath) {
		// ../../git/commons-math/.git
		String gitRepositoryName = FileHelper.getFileName(FileHelper.getFileParentPath(gitRepositoryPath));

		return gitRepositoryName;
	}

}
