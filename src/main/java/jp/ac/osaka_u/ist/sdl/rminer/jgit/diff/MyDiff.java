package jp.ac.osaka_u.ist.sdl.rminer.jgit.diff;

import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;

public class MyDiff {
	public void aaa(Git git) throws Exception{
		DiffCommand diffCommand = git.diff();
		
		diffCommand.call();
	}
}
