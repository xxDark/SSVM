package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * VM intrinsics.
 *
 * @author xDark
 */
@UtilityClass
public class IntrinsicsNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		mathIntrinsics(vm);
		objectIntrinsics(vm);
		stringIntrinsics(vm);
		characterIntrinsics(vm);
		intIntrinsics(vm);
		longIntrinsics(vm);
		arrayIntrinsics(vm);
	}

	private void mathIntrinsics(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val jc = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Math");
		vmi.setInvoker(jc, "min", "(II)I", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(IntValue.of(Math.min(locals.load(0).asInt(), locals.load(1).asInt())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "min", "(JJ)J", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(LongValue.of(Math.min(locals.load(0).asLong(), locals.load(2).asLong())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "min", "(FF)F", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new FloatValue(Math.min(locals.load(0).asFloat(), locals.load(1).asFloat())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "min", "(DD)D", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.min(locals.load(0).asDouble(), locals.load(2).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "max", "(II)I", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(IntValue.of(Math.max(locals.load(0).asInt(), locals.load(1).asInt())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "max", "(JJ)J", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(LongValue.of(Math.max(locals.load(0).asLong(), locals.load(2).asLong())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "max", "(FF)F", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new FloatValue(Math.max(locals.load(0).asFloat(), locals.load(1).asFloat())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "max", "(DD)D", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.max(locals.load(0).asDouble(), locals.load(2).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "abs", "(I)I", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(IntValue.of(Math.abs(locals.load(0).asInt())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "abs", "(J)J", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(LongValue.of(Math.abs(locals.load(0).asLong())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "abs", "(F)F", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new FloatValue(Math.abs(locals.load(0).asFloat())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "abs", "(D)D", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.abs(locals.load(0).asDouble())));
			return Result.ABORT;
		});
	}

	private static void objectIntrinsics(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val jc = vm.getSymbols().java_lang_Object;
		vmi.setInvoker(jc, "equals", "(Ljava/lang/Object;)Z", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(locals.load(0) == locals.load(1) ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
	}

	private static void stringIntrinsics(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val jc = vm.getSymbols().java_lang_String;
		// This will only work on JDK 8, sadly.
		if (jc.hasVirtualField("value", "[C")) {
			val memoryManager = vm.getMemoryManager();
			val offset = memoryManager.valueBaseOffset(jc);
			val lengthOffset = offset + jc.getFieldOffset("value", "[C");
			vmi.setInvoker(jc, "length", "()I", ctx -> {
				val chars = (ArrayValue) memoryManager.readValue(ctx.getLocals().<InstanceValue>load(0), lengthOffset);
				ctx.setResult(IntValue.of(chars.getLength()));
				return Result.ABORT;
			});
			val hashOffset = offset + jc.getFieldOffset("hash", "I");
			val valueOffset = offset + jc.getFieldOffset("value", "[C");
			vmi.setInvoker(jc, "hashCode", "()I", ctx -> {
				val _this = ctx.getLocals().<InstanceValue>load(0);
				int hc = memoryManager.readInt(_this, hashOffset);
				if (hc == 0) {
					val value = (ArrayValue) memoryManager.readValue(_this, valueOffset);
					for (int i = 0, j = value.getLength(); i < j; i++) {
						hc = 31 * hc + value.getChar(i);
					}
					memoryManager.writeInt(_this, hashOffset, hc);
				}
				ctx.setResult(IntValue.of(hc));
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "lastIndexOf", "(II)I", ctx -> {
				val locals = ctx.getLocals();
				val _this = locals.<InstanceValue>load(0);
				val chars = (ArrayValue) memoryManager.readValue(_this, valueOffset);
				int ch = locals.load(1).asInt();
				int fromIndex = locals.load(2).asInt();
				ctx.setResult(IntValue.of(lastIndexOf(chars, ch, fromIndex)));
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "indexOf", "([CII[CIII)I", ctx -> {
				val locals = ctx.getLocals();
				val source = locals.<ArrayValue>load(0);
				int sourceOffset = locals.load(1).asInt();
				int sourceCount = locals.load(2).asInt();
				val target = locals.<ArrayValue>load(3);
				int targetOffset = locals.load(4).asInt();
				int targetCount = locals.load(5).asInt();
				int fromIndex = locals.load(6).asInt();
				ctx.setResult(IntValue.of(indexOf(source, sourceOffset, sourceCount, target, targetOffset, targetCount, fromIndex)));
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "indexOf", "(II)I", ctx -> {
				val locals = ctx.getLocals();
				val _this = locals.<InstanceValue>load(0);
				val chars = (ArrayValue) memoryManager.readValue(_this, valueOffset);
				int ch = locals.load(1).asInt();
				int fromIndex = locals.load(2).asInt();
				ctx.setResult(IntValue.of(indexOf(chars, ch, fromIndex)));
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "equals", "(Ljava/lang/Object;)Z", ctx -> {
				ret:
				{
					val locals = ctx.getLocals();
					val other = locals.<ObjectValue>load(1);
					if (other.getJavaClass() != jc) {
						ctx.setResult(IntValue.ZERO);
					} else {
						val _this = locals.<InstanceValue>load(0);
						val chars = (ArrayValue) memoryManager.readValue(_this, valueOffset);
						val chars2 = (ArrayValue) memoryManager.readValue(other, valueOffset);
						int len = chars.getLength();
						if (len != chars2.getLength()) {
							ctx.setResult(IntValue.ZERO);
						} else {
							while (len-- != 0) {
								if (chars.getChar(len) != chars2.getChar(len)) {
									ctx.setResult(IntValue.ZERO);
									break ret;
								}
							}
							ctx.setResult(IntValue.ONE);
						}
					}
				}
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "startsWith", "(Ljava/lang/String;I)Z", ctx -> {
				val locals = ctx.getLocals();
				val helper = vm.getHelper();
				val prefix = (ArrayValue) memoryManager.readValue(helper.<InstanceValue>checkNotNull(locals.load(1)), valueOffset);
				val _this = (ArrayValue) memoryManager.readValue(locals.<InstanceValue>load(0), valueOffset);
				int toOffset = locals.load(2).asInt();
				ctx.setResult(startsWith(_this, prefix, toOffset) ? IntValue.ONE : IntValue.ZERO);
				return Result.ABORT;
			});
			val charPrimitive = vm.getPrimitives().charPrimitive;
			vmi.setInvoker(jc, "replace", "(CC)Ljava/lang/String;", ctx -> {
				val locals = ctx.getLocals();
				val oldChar = locals.load(1).asChar();
				val newChar = locals.load(2).asChar();
				val _this = locals.<InstanceValue>load(0);
				if (oldChar == newChar) {
					ctx.setResult(_this);
				} else {
					val helper = vm.getHelper();
					val value = (ArrayValue) memoryManager.readValue(_this, valueOffset);
					int len = value.getLength();
					int i = -1;
					while (++i < len) {
						if (value.getChar(i) == oldChar) {
							break;
						}
					}
					if (i < len) {
						val buf = helper.newArray(charPrimitive, len);
						for (int j = 0; j < i; j++) {
							buf.setChar(j, value.getChar(j));
						}
						while (i < len) {
							val c = value.getChar(i);
							buf.setChar(i++, (c == oldChar) ? newChar : c);
						}
						ctx.setResult(helper.newUtf8(buf));
					} else {
						ctx.setResult(_this);
					}
				}
				return Result.ABORT;
			});
		}
	}

	private int lastIndexOf(ArrayValue value, int ch, int fromIndex) {
		if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
			int i = Math.min(fromIndex, value.getLength() - 1);
			for (; i >= 0; i--) {
				if (value.getChar(i) == ch) {
					return i;
				}
			}
			return -1;
		} else {
			return lastIndexOfSupplementary(value, ch, fromIndex);
		}
	}

	private int lastIndexOfSupplementary(ArrayValue value, int ch, int fromIndex) {
		if (Character.isValidCodePoint(ch)) {
			char hi = Character.highSurrogate(ch);
			char lo = Character.lowSurrogate(ch);
			int i = Math.min(fromIndex, value.getLength() - 2);
			for (; i >= 0; i--) {
				if (value.getChar(i) == hi && value.getChar(i + 1) == lo) {
					return i;
				}
			}
		}
		return -1;
	}

	private int indexOf(ArrayValue source, int sourceOffset, int sourceCount,
						ArrayValue target, int targetOffset, int targetCount,
						int fromIndex) {
		if (fromIndex >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		if (targetCount == 0) {
			return fromIndex;
		}

		char first = target.getChar(targetOffset);
		int max = sourceOffset + (sourceCount - targetCount);

		for (int i = sourceOffset + fromIndex; i <= max; i++) {
			if (source.getChar(i) != first) {
				while (++i <= max && source.getChar(i) != first) ;
			}

			if (i <= max) {
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end && source.getChar(j)
						== target.getChar(k); j++, k++)
					;

				if (j == end) {
					return i - sourceOffset;
				}
			}
		}
		return -1;
	}

	private int indexOf(ArrayValue value, int ch, int fromIndex) {
		int max = value.getLength();
		if (fromIndex < 0) {
			fromIndex = 0;
		} else if (fromIndex >= max) {
			return -1;
		}

		if (ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
			for (int i = fromIndex; i < max; i++) {
				if (value.getChar(i) == ch) {
					return i;
				}
			}
			return -1;
		} else {
			return indexOfSupplementary(value, ch, fromIndex);
		}
	}

	private int indexOfSupplementary(ArrayValue value, int ch, int fromIndex) {
		if (Character.isValidCodePoint(ch)) {
			char hi = Character.highSurrogate(ch);
			char lo = Character.lowSurrogate(ch);
			int max = value.getLength() - 1;
			for (int i = fromIndex; i < max; i++) {
				if (value.getChar(i) == hi && value.getChar(i + 1) == lo) {
					return i;
				}
			}
		}
		return -1;
	}

	private boolean startsWith(ArrayValue value, ArrayValue pa, int toffset) {
		int to = toffset;
		int po = 0;
		int pc = pa.getLength();
		if ((toffset < 0) || (toffset > value.getLength() - pc)) {
			return false;
		}
		while (--pc >= 0) {
			if (value.getChar(to++) != pa.getChar(po++)) {
				return false;
			}
		}
		return true;
	}

	private void characterIntrinsics(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val jc = vm.getSymbols().java_lang_Character;
		val toLowerCase = (MethodInvoker) ctx -> {
			ctx.setResult(IntValue.of(Character.toLowerCase(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		};
		vmi.setInvoker(jc, "toLowerCase", "(I)I", toLowerCase);
		vmi.setInvoker(jc, "toLowerCase", "(C)C", toLowerCase);
		val toUpperCase = (MethodInvoker) ctx -> {
			ctx.setResult(IntValue.of(Character.toUpperCase(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		};
		vmi.setInvoker(jc, "toUpperCase", "(I)I", toUpperCase);
		vmi.setInvoker(jc, "toUpperCase", "(C)C", toUpperCase);
		val digit = (MethodInvoker) ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(IntValue.of(Character.digit(locals.load(0).asInt(), locals.load(1).asInt())));
			return Result.ABORT;
		};
		vmi.setInvoker(jc, "digit", "(II)I", digit);
		vmi.setInvoker(jc, "digit", "(CI)I", digit);
		vmi.setInvoker(jc, "forDigit", "(II)C", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(IntValue.of(Character.forDigit(locals.load(0).asInt(), locals.load(1).asInt())));
			return Result.ABORT;
		});
	}

	private void intIntrinsics(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val jc = vm.getSymbols().java_lang_Integer;
		vmi.setInvoker(jc, "hashCode", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.hashCode(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "highestOneBit", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.highestOneBit(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "numberOfLeadingZeros", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.numberOfLeadingZeros(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "numberOfTrailingZeros", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.numberOfTrailingZeros(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "bitCount", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.bitCount(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "reverse", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.reverse(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "reverseBytes", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.reverseBytes(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "toString", "(I)Ljava/lang/String;", ctx -> {
			val locals = ctx.getLocals();
			int value = locals.load(0).asInt();
			ctx.setResult(vm.getHelper().newUtf8(Integer.toString(value)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "toHexString", "(I)Ljava/lang/String;", ctx -> {
			val locals = ctx.getLocals();
			int value = locals.load(0).asInt();
			ctx.setResult(vm.getHelper().newUtf8(Integer.toHexString(value)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "toOctalString", "(I)Ljava/lang/String;", ctx -> {
			val locals = ctx.getLocals();
			int value = locals.load(0).asInt();
			ctx.setResult(vm.getHelper().newUtf8(Integer.toOctalString(value)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "toString", "(II)Ljava/lang/String;", ctx -> {
			val locals = ctx.getLocals();
			int value = locals.load(0).asInt();
			int radix = locals.load(1).asInt();
			ctx.setResult(vm.getHelper().newUtf8(Integer.toString(value, radix)));
			return Result.ABORT;
		});
	}

	private void longIntrinsics(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val jc = vm.getSymbols().java_lang_Long;
		vmi.setInvoker(jc, "hashCode", "(J)I", ctx -> {
			ctx.setResult(IntValue.of(Long.hashCode(ctx.getLocals().load(0).asLong())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "highestOneBit", "(J)J", ctx -> {
			ctx.setResult(LongValue.of(Long.highestOneBit(ctx.getLocals().load(0).asLong())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "numberOfLeadingZeros", "(J)I", ctx -> {
			ctx.setResult(IntValue.of(Long.numberOfLeadingZeros(ctx.getLocals().load(0).asLong())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "numberOfTrailingZeros", "(J)I", ctx -> {
			ctx.setResult(IntValue.of(Long.numberOfTrailingZeros(ctx.getLocals().load(0).asLong())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "bitCount", "(J)I", ctx -> {
			ctx.setResult(IntValue.of(Long.bitCount(ctx.getLocals().load(0).asLong())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "reverse", "(J)J", ctx -> {
			ctx.setResult(LongValue.of(Long.reverse(ctx.getLocals().load(0).asLong())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "reverseBytes", "(J)J", ctx -> {
			ctx.setResult(LongValue.of(Long.reverseBytes(ctx.getLocals().load(0).asLong())));
			return Result.ABORT;
		});
	}

	private void arrayIntrinsics(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val jc = (InstanceJavaClass) vm.findBootstrapClass("java/util/Arrays");
		vmi.setInvoker(jc, "hashCode", "([J)I", ctx -> {
			val arr = ctx.getLocals().<ObjectValue>load(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Long.hashCode(array.getLong(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([D)I", ctx -> {
			val arr = ctx.getLocals().<ObjectValue>load(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Double.hashCode(array.getDouble(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([I)I", ctx -> {
			val arr = ctx.getLocals().<ObjectValue>load(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Integer.hashCode(array.getInt(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([F)I", ctx -> {
			val arr = ctx.getLocals().<ObjectValue>load(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Float.hashCode(array.getFloat(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([C)I", ctx -> {
			val arr = ctx.getLocals().<ObjectValue>load(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Character.hashCode(array.getChar(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([S)I", ctx -> {
			val arr = ctx.getLocals().<ObjectValue>load(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Short.hashCode(array.getShort(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([B)I", ctx -> {
			val arr = ctx.getLocals().<ObjectValue>load(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Byte.hashCode(array.getByte(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([Z)I", ctx -> {
			val arr = ctx.getLocals().<ObjectValue>load(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Boolean.hashCode(array.getBoolean(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([JJ)V", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val arr = helper.checkNotNullArray(locals.load(0));
			val v = locals.load(1).asLong();
			for (int j = arr.getLength(); j != 0; ) {
				arr.setLong(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([DD)V", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val arr = helper.checkNotNullArray(locals.load(0));
			val v = locals.load(1).asDouble();
			for (int j = arr.getLength(); j != 0; ) {
				arr.setDouble(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([II)V", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val arr = helper.checkNotNullArray(locals.load(0));
			val v = locals.load(1).asInt();
			for (int j = arr.getLength(); j != 0; ) {
				arr.setInt(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([FF)V", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val arr = helper.checkNotNullArray(locals.load(0));
			val v = locals.load(1).asFloat();
			for (int j = arr.getLength(); j != 0; ) {
				arr.setFloat(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([CC)V", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val arr = helper.checkNotNullArray(locals.load(0));
			val v = locals.load(1).asChar();
			for (int j = arr.getLength(); j != 0; ) {
				arr.setChar(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([SS)V", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val arr = helper.checkNotNullArray(locals.load(0));
			val v = locals.load(1).asShort();
			for (int j = arr.getLength(); j != 0; ) {
				arr.setShort(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([BB)V", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val arr = helper.checkNotNullArray(locals.load(0));
			val v = locals.load(1).asByte();
			for (int j = arr.getLength(); j != 0; ) {
				arr.setByte(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([ZZ)V", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val arr = helper.checkNotNullArray(locals.load(0));
			val v = locals.load(1).asBoolean();
			for (int j = arr.getLength(); j != 0; ) {
				arr.setBoolean(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([J[J)Z", ctx -> {
			val locals = ctx.getLocals();
			val $a = locals.load(0);
			val $a2 = locals.load(1);
			done:
			if ($a == $a2) {
				ctx.setResult(IntValue.ONE);
			} else if ($a.isNull() || $a2.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val a = (ArrayValue) $a;
				val a2 = (ArrayValue) $a2;
				int j = a.getLength();
				if (j != a2.getLength()) {
					ctx.setResult(IntValue.ZERO);
				} else {
					while (j-- != 0) {
						if (a.getLong(j) != a2.getLong(j)) {
							ctx.setResult(IntValue.ZERO);
							break done;
						}
					}
					ctx.setResult(IntValue.ONE);
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([D[D)Z", ctx -> {
			val locals = ctx.getLocals();
			val $a = locals.load(0);
			val $a2 = locals.load(1);
			done:
			if ($a == $a2) {
				ctx.setResult(IntValue.ONE);
			} else if ($a.isNull() || $a2.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val a = (ArrayValue) $a;
				val a2 = (ArrayValue) $a2;
				int j = a.getLength();
				if (j != a2.getLength()) {
					ctx.setResult(IntValue.ZERO);
				} else {
					while (j-- != 0) {
						if (a.getDouble(j) != a2.getDouble(j)) {
							ctx.setResult(IntValue.ZERO);
							break done;
						}
					}
					ctx.setResult(IntValue.ONE);
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([I[I)Z", ctx -> {
			val locals = ctx.getLocals();
			val $a = locals.load(0);
			val $a2 = locals.load(1);
			done:
			if ($a == $a2) {
				ctx.setResult(IntValue.ONE);
			} else if ($a.isNull() || $a2.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val a = (ArrayValue) $a;
				val a2 = (ArrayValue) $a2;
				int j = a.getLength();
				if (j != a2.getLength()) {
					ctx.setResult(IntValue.ZERO);
				} else {
					while (j-- != 0) {
						if (a.getInt(j) != a2.getInt(j)) {
							ctx.setResult(IntValue.ZERO);
							break done;
						}
					}
					ctx.setResult(IntValue.ONE);
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([F[F)Z", ctx -> {
			val locals = ctx.getLocals();
			val $a = locals.load(0);
			val $a2 = locals.load(1);
			done:
			if ($a == $a2) {
				ctx.setResult(IntValue.ONE);
			} else if ($a.isNull() || $a2.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val a = (ArrayValue) $a;
				val a2 = (ArrayValue) $a2;
				int j = a.getLength();
				if (j != a2.getLength()) {
					ctx.setResult(IntValue.ZERO);
				} else {
					while (j-- != 0) {
						if (a.getFloat(j) != a2.getFloat(j)) {
							ctx.setResult(IntValue.ZERO);
							break done;
						}
					}
					ctx.setResult(IntValue.ONE);
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([C[C)Z", ctx -> {
			val locals = ctx.getLocals();
			val $a = locals.load(0);
			val $a2 = locals.load(1);
			done:
			if ($a == $a2) {
				ctx.setResult(IntValue.ONE);
			} else if ($a.isNull() || $a2.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val a = (ArrayValue) $a;
				val a2 = (ArrayValue) $a2;
				int j = a.getLength();
				if (j != a2.getLength()) {
					ctx.setResult(IntValue.ZERO);
				} else {
					while (j-- != 0) {
						if (a.getChar(j) != a2.getChar(j)) {
							ctx.setResult(IntValue.ZERO);
							break done;
						}
					}
					ctx.setResult(IntValue.ONE);
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([S[S)Z", ctx -> {
			val locals = ctx.getLocals();
			val $a = locals.load(0);
			val $a2 = locals.load(1);
			done:
			if ($a == $a2) {
				ctx.setResult(IntValue.ONE);
			} else if ($a.isNull() || $a2.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val a = (ArrayValue) $a;
				val a2 = (ArrayValue) $a2;
				int j = a.getLength();
				if (j != a2.getLength()) {
					ctx.setResult(IntValue.ZERO);
				} else {
					while (j-- != 0) {
						if (a.getShort(j) != a2.getShort(j)) {
							ctx.setResult(IntValue.ZERO);
							break done;
						}
					}
					ctx.setResult(IntValue.ONE);
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([B[B)Z", ctx -> {
			val locals = ctx.getLocals();
			val $a = locals.load(0);
			val $a2 = locals.load(1);
			done:
			if ($a == $a2) {
				ctx.setResult(IntValue.ONE);
			} else if ($a.isNull() || $a2.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val a = (ArrayValue) $a;
				val a2 = (ArrayValue) $a2;
				int j = a.getLength();
				if (j != a2.getLength()) {
					ctx.setResult(IntValue.ZERO);
				} else {
					while (j-- != 0) {
						if (a.getByte(j) != a2.getByte(j)) {
							ctx.setResult(IntValue.ZERO);
							break done;
						}
					}
					ctx.setResult(IntValue.ONE);
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([Z[Z)Z", ctx -> {
			val locals = ctx.getLocals();
			val $a = locals.load(0);
			val $a2 = locals.load(1);
			done:
			if ($a == $a2) {
				ctx.setResult(IntValue.ONE);
			} else if ($a.isNull() || $a2.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val a = (ArrayValue) $a;
				val a2 = (ArrayValue) $a2;
				int j = a.getLength();
				if (j != a2.getLength()) {
					ctx.setResult(IntValue.ZERO);
				} else {
					while (j-- != 0) {
						if (a.getBoolean(j) != a2.getBoolean(j)) {
							ctx.setResult(IntValue.ZERO);
							break done;
						}
					}
					ctx.setResult(IntValue.ONE);
				}
			}
			return Result.ABORT;
		});
	}

	private boolean nativeFillAvailable(ArrayValue value, int from, int to) {
		return from >= 0 && from <= to && to <= value.getLength();
	}
}
