package dev.xdark.ssvm.mirror.member.area;

import dev.xdark.ssvm.mirror.member.JavaMember;
import dev.xdark.ssvm.mirror.member.MemberIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Empty class area.
 *
 * @apiNote xDark
 */
public final class EmptyClassArea<T extends JavaMember> implements ClassArea<T> {
	private static final ClassArea<?> EMPTY = new EmptyClassArea<>();

	private EmptyClassArea() {
	}

	@Override
	public T get(int slot) {
		return null;
	}

	@Override
	public T get(MemberIdentifier identifier) {
		return null;
	}

	@Override
	public Stream<T> stream() {
		return Stream.empty();
	}

	@Override
	public List<T> list() {
		return Collections.emptyList();
	}

	public static <T extends JavaMember> ClassArea<T> create() {
		return (ClassArea<T>) EMPTY;
	}
}
