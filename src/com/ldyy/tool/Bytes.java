package com.ldyy.tool;

import java.io.UnsupportedEncodingException;

public abstract class Bytes {
	public static int indexOf(byte[] source, String target) {
		return indexOf(source, 0, source.length, target);
	}

	public static int indexOf(byte[] source, int sourceOffset, int sourceCount, String target) {
		try {
			return indexOf(source, sourceOffset, sourceCount, target.getBytes("ASCII"), 0);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static int indexOf(byte[] source, int sourceOffset, int sourceCount, byte[] target, int fromIndex) {
		return indexOf(source, sourceOffset, sourceCount, target, 0, target.length, fromIndex);
	}

	/**
	 * The source is the bytes array being searched, and the target is the bytes
	 * being searched for.
	 *
	 * @param source
	 *            the bytes being searched.
	 * @param sourceOffset
	 *            offset of the source string.
	 * @param sourceCount
	 *            count of the source string.
	 * @param target
	 *            the bytes being searched for.
	 * @param targetOffset
	 *            offset of the target string.
	 * @param targetCount
	 *            count of the target string.
	 * @param fromIndex
	 *            the index to begin searching from.
	 */
	static int indexOf(byte[] source, int sourceOffset, int sourceCount, byte[] target, int targetOffset,
			int targetCount, int fromIndex) {
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}

		byte first = target[targetOffset];
		int max = sourceOffset + (sourceCount - targetCount);

		for (int i = sourceOffset + fromIndex; i <= max; i++) {
			/* Look for first character. */
			if (source[i] != first) {
				while (++i <= max && source[i] != first)
					;
			}

			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++)
					;

				if (j == end) {
					/* Found whole string. */
					return i;
				}
			}
		}
		return -1;
	}
}
