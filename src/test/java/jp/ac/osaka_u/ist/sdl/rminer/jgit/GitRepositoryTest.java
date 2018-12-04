package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GitRepositoryTest {

	@Mock
	private Repository repository;

	@Mock
	private ObjectReader reader;

	private GitRepository gitRepository;

	@Before
	public void setUp() {
		gitRepository = new GitRepository(null, repository, reader);
	}

	@Test
	public void testGetCommit() throws IOException {
		Commit commit = new Commit(gitRepository, ObjectId.zeroId());
		when(repository.resolve("12345")).thenReturn(ObjectId.zeroId());

		assertThat(gitRepository.getCommit("12345")).isEqualTo(commit);

		verify(repository).resolve(any());

	}

	@Test
	public void testGetCommitNotFound() throws IOException {
		when(repository.resolve("12345")).thenReturn(null);

		assertThat(gitRepository.getCommit("12345")).isEqualTo(null);

		verify(repository).resolve(any());

	}

}
