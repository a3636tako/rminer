package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.blame.BlameCommand;

public class StableCommitsTest {

	private static Path repositoryDirectory;

	@BeforeClass
	public static void setup() throws IOException, GitAPIException {
		String uri = Paths.get("example-repository")
			.toAbsolutePath()
			.toUri()
			.toString();
		repositoryDirectory = Files.createTempDirectory(null);
		Git git = Git.cloneRepository()
			.setURI(uri)
			.setBranch("stableCommitsTest")
			.setDirectory(repositoryDirectory.toFile())
			.call();

		git.close();
	}

	@Test
	public void test() throws Exception {
		GitRepository repository = new GitRepository(repositoryDirectory);
		StableCommits commits = new StableCommits(repository.getCommit("stableCommitsTest"));
		assertThat(commits.getStableCommits()).extracting(Commit::getCommitMessage)
			.containsOnly("C1\n", "C2\n", "C5\n");
	}

	@Test
	public void testNative() throws Exception {
		GitRepository repository = new GitRepository(repositoryDirectory);
		StableCommits commits = new StableCommits(repository
			.getCommit("stableCommitsTest"), BlameCommand.ExecutorType.NATIVE, ComparatorAlgorithms.DEFAULT);
		assertThat(commits.getStableCommits()).extracting(Commit::getCommitMessage)
			.containsOnly("C1\n", "C2\n", "C5\n");
	}

	@Test
	public void testCustom() throws Exception {
		GitRepository repository = new GitRepository(repositoryDirectory);
		StableCommits commits = new StableCommits(repository
			.getCommit("stableCommitsTest"), BlameCommand.ExecutorType.CUSTOM, ComparatorAlgorithms.of(DiffAlgorithm
				.getAlgorithm(SupportedAlgorithm.HISTOGRAM), new JavaTextComparator(), new JavaTextLoader()));
		assertThat(commits.getStableCommits()).extracting(Commit::getCommitMessage)
			.containsOnly("C1\n", "C2\n", "C5\n");
	}

	@AfterClass
	public static void teardown() throws IOException, GitAPIException {
		Files.walk(repositoryDirectory)
			.forEach(path -> {
				try {
					Files.setPosixFilePermissions(path, EnumSet
						.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
				} catch(IOException e) {}
			});

		Files.walkFileTree(repositoryDirectory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

}
