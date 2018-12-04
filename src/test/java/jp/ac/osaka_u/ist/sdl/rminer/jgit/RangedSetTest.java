package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RangedSetTest {

	@Test
	public void testAdd() {
		RangedSet<Integer> set = new RangedSet<>(a -> a + 1);
		assertThat(set.size()).isEqualTo(0);
		assertThat(set.contains(2)).isFalse();
		set.addRange(1, 3);
		assertThat(set.size()).isEqualTo(2);
		assertThat(set.contains(1)).isTrue();
		assertThat(set.contains(2)).isTrue();
		assertThat(set.contains(3)).isFalse();
	}

	@Test
	public void testAddRemove() {
		RangedSet<Integer> set = new RangedSet<>(a -> a + 1);
		assertThat(set.size()).isEqualTo(0);
		assertThat(set.contains(2)).isFalse();
		set.addRange(1, 3);
		assertThat(set.size()).isEqualTo(2);
		assertThat(set.contains(1)).isTrue();
		assertThat(set.contains(2)).isTrue();
		assertThat(set.contains(3)).isFalse();
		set.removeRange(2, 3);
		assertThat(set.size()).isEqualTo(1);
		assertThat(set.contains(1)).isTrue();
		assertThat(set.contains(2)).isFalse();
		assertThat(set.contains(3)).isFalse();
	}

	@Test
	public void testRemoveInRange() {
		RangedSet<Integer> set = new RangedSet<>(a -> a + 1);
		set.addRange(0, 10);
		set.removeRange(3, 5);
		assertThat(set.size()).isEqualTo(8);
		assertThat(set).containsExactly(0, 1, 2, 5, 6, 7, 8, 9);

	}

	@Test
	public void testAddMultiple() {
		RangedSet<Integer> set = new RangedSet<>(a -> a + 1);
		set.addRange(1, 5);
		set.addRange(3, 7);
		set.addRange(10, 12);

		assertThat(set.size()).isEqualTo(8);
		assertThat(set).containsExactly(1, 2, 3, 4, 5, 6, 10, 11);
	}

	@Test
	public void testIntersect() {
		RangedSet<Integer> set1 = new RangedSet<>(a -> a + 1);
		set1.addRange(1, 5);
		set1.addRange(8, 9);
		set1.addRange(11, 13);
		set1.addRange(18, 20);

		RangedSet<Integer> set2 = new RangedSet<>(a -> a + 1);

		set2.addRange(3, 12);
		set2.addRange(20, 25);

		assertThat(set1.intersect(set2)).containsExactly(3, 4, 8, 11);
		assertThat(set1.intersect(set2)
			.size()).isEqualTo(4);
		assertThat(set2.intersect(set1)).containsExactly(3, 4, 8, 11);
		assertThat(set2.intersect(set1)
			.size()).isEqualTo(4);

	}
}
