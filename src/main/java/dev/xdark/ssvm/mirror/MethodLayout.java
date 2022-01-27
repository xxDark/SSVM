package dev.xdark.ssvm.mirror;

import java.util.Collection;
import java.util.Map;

/**
 * Represents method class layout.
 *
 * @author xDark
 */
public final class MethodLayout {

	private final Map<MemberKey, JavaMethod> methods;

	/**
	 * @param methods
	 * 		Map containing all methods from hierarchy.
	 */
	public MethodLayout(Map<MemberKey, JavaMethod> methods) {
		this.methods = methods;
	}

	/**
	 * Returns a map containing methods info.
	 *
	 * @return map containing methods info.
	 */
	public Map<MemberKey, JavaMethod> getMethods() {
		return methods;
	}

	/**
	 * Shortcut for {@code getMethods().values()}.
	 *
	 * @return all methods.
	 */
	public Collection<JavaMethod> getAll() {
		return methods.values();
	}
}
