package dev.xdark.ssvm.dummy;

@SuppressWarnings("all")
public class StringCoderLength {
	public static void coderCmp() {
		// In Java 9+ the strings are compact where possible.
		// Strings with chars beyond the byte bounds cannot be compact.
		//
		// Comparing the length of compact and non-compact strings should not yield different lengths,
		// even though the backing arrays are different sizes.
		String s1 = "abc";
		String s2 = "\uFFFF\uFFFE\u7FFF";
		if (s1.length() != s2.length())
			throw new IllegalStateException("s1 and s2 lengths should be the same");
	}
}
