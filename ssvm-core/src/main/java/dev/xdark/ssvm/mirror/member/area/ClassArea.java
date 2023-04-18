package dev.xdark.ssvm.mirror.member.area;

import dev.xdark.ssvm.mirror.member.JavaMember;
import dev.xdark.ssvm.mirror.member.MemberIdentifier;

import java.util.List;
import java.util.stream.Stream;

/**
 * Class area. Can hold any member type.
 *
 * @param <T> Member type.
 * @author xDark
 */
public interface ClassArea<T extends JavaMember> {

	/**
	 * @param slot Member slot.
	 * @return Member or {@code null},
	 * if slot is out of bounds.
	 */
	T get(int slot);

	/**
	 * Member by it's identifier.
	 *
	 * @param identifier Member identifier.
	 * @return Member or {@code null},
	 * if not found.
	 */
	T get(MemberIdentifier identifier);

	/**
	 * Member by it's name and descriptor.
	 *
	 * @param name Member name.
	 * @param desc Member descriptor.
	 * @return Member or {@code null},
	 * if not found.
	 */
	default T get(String name, String desc) {
		return get(MemberIdentifier.of(name, desc));
	}

	/**
	 * @return A stream of members.
	 */
	Stream<T> stream();

	/**
	 * @return A list of members.
	 */
	List<T> list();
}
