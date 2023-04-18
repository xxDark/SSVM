package dev.xdark.ssvm.util;

import lombok.experimental.UtilityClass;

/**
 * CRC32 utilities.
 *
 * @see <a href="https://github.com/ymnk/jzlib">https://github.com/ymnk/jzlib</a>
 */
@UtilityClass
public class CrcUtil {

	private final int[] TABLE;

	static {
		int[] table = new int[256];
		for (int n = 0; n < 256; n++) {
			int c = n;
			for (int k = 8; k-- != 0; ) {
				if ((c & 1) != 0) {
					c = 0xedb88320 ^ (c >>> 1);
				} else {
					c = c >>> 1;
				}
			}
			table[n] = c;
		}
		TABLE = table;
	}

	/**
	 * Updates CRC32 value.
	 *
	 * @param v Initial value.
	 * @param b Byte to update the checksum with.
	 * @return updated value.
	 */
	public int update(int v, byte b) {
		int c = ~v;
		c = TABLE[(c ^ b) & 0xff] ^ (c >>> 8);
		return ~c;
	}
}
