package jp.ac.osaka_u.ist.sdl.rminer.jgit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RangedSet<T> implements Iterable<T> {

	public class Range implements Comparable<Range>, Iterable<T> {
		T begin;
		T end;

		public Range(T begin, T end) {
			this.begin = begin;
			this.end = end;
		}

		@Override
		public int compareTo(Range o) {
			return compare(begin, o.begin);
		}

		@Override
		public Iterator<T> iterator() {
			if(begin == null || end == null) {
				return Collections.emptyIterator();
			}

			return new Iterator<T>() {
				T current = begin;

				@Override
				public boolean hasNext() {
					return current != null;
				}

				@Override
				public T next() {
					if(!hasNext()) {
						throw new NoSuchElementException();
					}
					T ret = current;
					T val = successor.apply(current);
					if(lowerElement(val, end)) {
						current = val;
					} else {
						current = null;
					}
					return ret;
				}

			};
		}

		public Stream<T> stream() {
			return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.NONNULL), false);
		}

		public Range intersect(Range r) {
			Range front;
			Range back;
			if(compareTo(r) < 0) {
				front = this;
				back = r;
			} else {
				front = r;
				back = this;
			}

			if(lowerElement(front.end, back.begin)) {
				return new Range(front.end, front.end);
			}
			return new Range(max(front.begin, back.begin), min(front.end, back.end));
		}

		public boolean isEmpty() {
			return !lowerElement(begin, end);
		}

		public int size() {
			if(this == first || this == last) return 0;

			int size = 0;
			T current = begin;
			while(lowerElement(current, end)) {
				size++;
				current = successor.apply(current);

			}
			return size;
		}
	}

	public static RangedSet<Integer> create() {
		return new RangedSet<>(a -> a + 1);
	}

	private Comparator<? super T> comparator;
	private Function<? super T, ? extends T> successor;
	private TreeSet<Range> tree;
	private final Range first = new Range(null, null);
	private final Range last = new Range(null, null);

	public RangedSet(Comparator<? super T> comparator, Function<? super T, ? extends T> successor) {
		this(successor);
		this.comparator = comparator;
	}

	public RangedSet(Function<? super T, ? extends T> successor) {
		this.successor = successor;

		tree = new TreeSet<>((o1, o2) -> {
			if(o1 == o2) return 0;
			if(o1 == first || o2 == last) return -1;
			if(o1 == last || o2 == first) return 1;
			return o1.compareTo(o2);
		});
		tree.add(first);
		tree.add(last);
	}

	public void addRange(T begin, T end) {
		Range range = new Range(begin, end);
		Range floorRange = tree.floor(range);
		if(floorRange == first || lowerElement(floorRange.end, range.begin)) {

		} else {
			tree.remove(floorRange);
			range = new Range(floorRange.begin, max(floorRange.end, range.end));
		}

		NavigableSet<Range> tailSet = tree.tailSet(range, false);
		while(addRangeContinueCheck(tailSet, range)) {
			Range pollFirst = tailSet.pollFirst();
			if(higherElement(pollFirst.end, range.end)) {
				range = new Range(range.begin, pollFirst.end);
			}
		}

		tree.add(range);
	}

	public void add(T value) {
		this.addRange(value, successor.apply(value));
	}

	public void addAllRange(RangedSet<T> target) {
		for(Range range : target.tree) {
			if(range == target.first || range == target.last) continue;
			this.addRange(range.begin, range.end);
		}
	}

	public boolean contains(T element) {
		Range range = new Range(element, element);
		Range floor = tree.floor(range);
		if(floor == first) return false;
		return lowerElement(element, floor.end);
	}

	public void removeRange(T begin, T end) {
		Range range = new Range(begin, end);
		Range floorRange = tree.floor(range);
		if(floorRange == first || lowerElement(floorRange.end, range.begin)) {

		} else {
			tree.remove(floorRange);
			T min = min(floorRange.end, range.begin);
			if(!equalsElement(floorRange.begin, min)) {
				tree.add(new Range(floorRange.begin, min));
			}

			if(lowerElement(range.end, floorRange.end)) {
				tree.add(new Range(range.end, floorRange.end));
			}
		}

		NavigableSet<Range> tailSet = tree.tailSet(range, false);
		while(addRangeContinueCheck(tailSet, range)) {
			Range pollFirst = tailSet.pollFirst();
			if(higherElement(pollFirst.end, range.end)) {
				range = new Range(range.end, pollFirst.end);
				tree.add(range);
			}
		}
	}

	public RangedSet<T> intersect(RangedSet<T> target) {
		RangedSet<T> result = new RangedSet<>(comparator, successor);
		for(Range range : target.tree) {
			if(range == target.first || range == target.last) continue;
			result.tree.addAll(intersect(range));
		}
		return result;
	}

	private List<Range> intersect(Range range) {
		List<Range> result = new ArrayList<>();

		Range floor = tree.floor(range);
		if(floor != first && higherElement(floor.end, range.begin)) {
			Range r = new Range(range.begin, min(floor.end, range.end));
			if(!r.isEmpty()) {
				result.add(r);
			}
		}

		NavigableSet<Range> tailSet = tree.tailSet(range, false);
		for(Range r : tailSet) {
			if(r == last || lowerElement(range.end, r.begin)) break;
			Range r2 = range.intersect(r);
			if(!r2.isEmpty()) {
				result.add(r2);
			}
		}
		return result;
	}

	@Override
	public Iterator<T> iterator() {
		return stream().iterator();
	}

	public Stream<T> stream() {
		return tree.stream()
			.flatMap(Range::stream);
	}

	public int size() {
		return tree.stream()
			.mapToInt(Range::size)
			.sum();
	}

	@SuppressWarnings("unchecked")
	private int compare(T o1, T o2) {
		if(comparator != null) {
			return comparator.compare(o1, o2);
		} else {
			return ((Comparable<? super T>)o1).compareTo(o2);
		}
	}

	private boolean lowerElement(T o1, T o2) {
		return compare(o1, o2) < 0;
	}

	private boolean equalsElement(T o1, T o2) {
		return compare(o1, o2) == 0;
	}

	private boolean higherElement(T o1, T o2) {
		return compare(o1, o2) > 0;
	}

	private T max(T o1, T o2) {
		if(higherElement(o2, o1)) {
			return o2;
		} else {
			return o1;
		}
	}

	private T min(T o1, T o2) {
		if(lowerElement(o2, o1)) {
			return o2;
		} else {
			return o1;
		}
	}

	private boolean addRangeContinueCheck(NavigableSet<Range> tailSet, Range range) {
		if(tailSet.isEmpty()) return false;
		Range head = tailSet.first();
		if(head == last) return false;
		return lowerElement(head.begin, range.end);
	}
}
