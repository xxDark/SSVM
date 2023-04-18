package dev.xdark.ssvm.mirror.member.area;

import dev.xdark.ssvm.mirror.member.JavaMember;
import dev.xdark.ssvm.mirror.member.MemberIdentifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple class area.
 *
 * @author xDark
 */
public final class SimpleClassArea<T extends JavaMember> implements ClassArea<T> {
	private final Map<MemberIdentifier, T> byIdentifier;
	private final T[] slotArray;
	private final int slotOffset;
	private List<T> listView;

	/**
	 * @param members    All members.
	 * @param slotOffset Slot offset.
	 */
	public SimpleClassArea(List<T> members, int slotOffset) {
		T[] slotArray = (T[]) members.toArray(new JavaMember[0]); // Generic type is lost at runtime, safe to cast
		this.slotArray = slotArray;
		this.slotOffset = slotOffset;
		byIdentifier = Arrays.stream(slotArray)
			.collect(Collectors.toMap(JavaMember::getIdentifier, Function.identity()));
	}

	/**
	 * @param members All members.
	 */
	public SimpleClassArea(List<T> members) {
		this(members, 0);
	}

	@Override
	public T get(int slot) {
		T[] slotArray;
		if (slot < 0 || (slot -= slotOffset) >= (slotArray = this.slotArray).length) {
			return null;
		}
		return slotArray[slot];
	}

	@Override
	public T get(MemberIdentifier identifier) {
		return byIdentifier.get(identifier);
	}

	@Override
	public Stream<T> stream() {
		return Arrays.stream(slotArray);
	}

	@Override
	public List<T> list() {
		List<T> listView = this.listView;
		if (listView == null) {
			return this.listView = Collections.unmodifiableList(Arrays.asList(slotArray));
		}
		return listView;
	}
}
