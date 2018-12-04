package jp.ac.osaka_u.ist.sdl.rminer.jgit.blame;

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
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.RawText;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jp.ac.osaka_u.ist.sdl.rminer.jgit.Commit;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.ComparatorAlgorithms;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitFile;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.GitRepository;
import jp.ac.osaka_u.ist.sdl.rminer.jgit.blame.BlameCommand.ExecutorType;

public class MyBlameResultTest {
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
	public void test() throws IOException, GitAPIException {
		GitRepository repository = new GitRepository(repositoryDirectory);
		Commit commit = repository.getCommit("HEAD");
		DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(SupportedAlgorithm.HISTOGRAM);
		RawTextComparator textComparator = RawTextComparator.DEFAULT;
		for(GitFile file : commit.retrieveFiles()){
			BlameResult callJGit = new BlameCommand(repository)
				.setFilePath(file.getPath())
				.setStartCommit(commit.getId())
				.setFollowFileRenames(true)
				.setDiffAlgorithm(diffAlgorithm)
				.setTextComparator(textComparator)
				.setExecutorType(ExecutorType.JGIT)
				.call();
			
			BlameResult callCustom = new BlameCommand(repository)
				.setFilePath(file.getPath())
				.setStartCommit(commit.getId())
				.setFollowFileRenames(true)
				.setAlgorithms(ComparatorAlgorithms.of(diffAlgorithm, textComparator, RawText::new))
				.setExecutorType(ExecutorType.CUSTOM)
				.call();
			
			assertThat(callCustom.getResultSize()).isEqualTo(callJGit.getResultSize());
			for(int i = 0; i < callCustom.getResultSize(); i++) {
				assertThat(callCustom.getSourceCommit(i)).isEqualTo(callJGit.getSourceCommit(i));
				assertThat(callCustom.getSourceLine(i)).isEqualTo(callJGit.getSourceLine(i));
				assertThat(callCustom.getSourcePath(i)).isEqualTo(callJGit.getSourcePath(i));
			}
		}
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
