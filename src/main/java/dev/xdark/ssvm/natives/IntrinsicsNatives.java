package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.mirror.PrimitiveClass;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;

/**
 * VM intrinsics.
 *
 * @author xDark
 */
@UtilityClass
public class IntrinsicsNatives {

	/**
	 * @param vm VM instance.
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
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass jc = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Math");
		vmi.setInvoker(jc, "min", "(II)I", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(IntValue.of(Math.min(locals.loadInt(0), locals.loadInt(1))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "min", "(JJ)J", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(LongValue.of(Math.min(locals.loadLong(0), locals.loadLong(2))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "min", "(FF)F", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(new FloatValue(Math.min(locals.loadFloat(0), locals.loadFloat(1))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "min", "(DD)D", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.min(locals.loadDouble(0), locals.loadDouble(2))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "max", "(II)I", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(IntValue.of(Math.max(locals.loadInt(0), locals.loadInt(1))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "max", "(JJ)J", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(LongValue.of(Math.max(locals.loadLong(0), locals.loadLong(2))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "max", "(FF)F", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(new FloatValue(Math.max(locals.loadFloat(0), locals.loadFloat(1))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "max", "(DD)D", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.max(locals.loadDouble(0), locals.loadDouble(2))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "abs", "(I)I", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(IntValue.of(Math.abs(locals.loadInt(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "abs", "(J)J", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(LongValue.of(Math.abs(locals.loadLong(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "abs", "(F)F", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(new FloatValue(Math.abs(locals.loadFloat(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "abs", "(D)D", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.abs(locals.loadDouble(0))));
			return Result.ABORT;
		});
	}

	private static void objectIntrinsics(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass jc = vm.getSymbols().java_lang_Object();
		vmi.setInvoker(jc, "equals", "(Ljava/lang/Object;)Z", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(locals.loadReference(0) == locals.loadReference(1) ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
	}

	private static void stringIntrinsics(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass jc = vm.getSymbols().java_lang_String();
		// This will only work on JDK 8, sadly.
		if (jc.hasVirtualField("value", "[C")) {
			MemoryManager memoryManager = vm.getMemoryManager();
			int offset = memoryManager.valueBaseOffset(jc);
			long lengthOffset = offset + jc.getVirtualFieldOffset("value", "[C");
			vmi.setInvoker(jc, "length", "()I", ctx -> {
				ArrayValue chars = (ArrayValue) memoryManager.readValue(ctx.getLocals().loadReference(0), lengthOffset);
				ctx.setResult(IntValue.of(chars.getLength()));
				return Result.ABORT;
			});
			long hashOffset = offset + jc.getVirtualFieldOffset("hash", "I");
			long valueOffset = offset + jc.getVirtualFieldOffset("value", "[C");
			vmi.setInvoker(jc, "hashCode", "()I", ctx -> {
				InstanceValue _this = ctx.getLocals().loadReference(0);
				MemoryData data = _this.getData();
				int hc = data.readInt(hashOffset);
				if (hc == 0) {
					ArrayValue value = (ArrayValue) memoryManager.readValue(_this, valueOffset);
					for (int i = 0, j = value.getLength(); i < j; i++) {
						hc = 31 * hc + value.getChar(i);
					}
					data.writeInt(hashOffset, hc);
				}
				ctx.setResult(IntValue.of(hc));
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "lastIndexOf", "(II)I", ctx -> {
				Locals locals = ctx.getLocals();
				InstanceValue _this = locals.loadReference(0);
				ArrayValue chars = (ArrayValue) memoryManager.readValue(_this, valueOffset);
				int ch = locals.loadInt(1);
				int fromIndex = locals.loadInt(2);
				ctx.setResult(IntValue.of(lastIndexOf(chars, ch, fromIndex)));
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "indexOf", "([CII[CIII)I", ctx -> {
				Locals locals = ctx.getLocals();
				ArrayValue source = locals.loadReference(0);
				int sourceOffset = locals.loadInt(1);
				int sourceCount = locals.loadInt(2);
				ArrayValue target = locals.loadReference(3);
				int targetOffset = locals.loadInt(4);
				int targetCount = locals.loadInt(5);
				int fromIndex = locals.loadInt(6);
				ctx.setResult(IntValue.of(indexOf(source, sourceOffset, sourceCount, target, targetOffset, targetCount, fromIndex)));
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "indexOf", "(II)I", ctx -> {
				Locals locals = ctx.getLocals();
				InstanceValue _this = locals.loadReference(0);
				ArrayValue chars = (ArrayValue) memoryManager.readValue(_this, valueOffset);
				int ch = locals.loadInt(1);
				int fromIndex = locals.loadInt(2);
				ctx.setResult(IntValue.of(indexOf(chars, ch, fromIndex)));
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "indexOf", "(I)I", ctx -> {
				Locals locals = ctx.getLocals();
				InstanceValue _this = locals.loadReference(0);
				ArrayValue chars = (ArrayValue) memoryManager.readValue(_this, valueOffset);
				int ch = locals.loadInt(1);
				ctx.setResult(IntValue.of(indexOf(chars, ch, 0)));
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "equals", "(Ljava/lang/Object;)Z", ctx -> {
				ret:
				{
					Locals locals = ctx.getLocals();
					ObjectValue other = locals.loadReference(1);
					if (other.isNull() || other.getJavaClass() != jc) {
						ctx.setResult(IntValue.ZERO);
					} else {
						InstanceValue _this = locals.loadReference(0);
						ArrayValue chars = (ArrayValue) memoryManager.readValue(_this, valueOffset);
						ArrayValue chars2 = (ArrayValue) memoryManager.readValue(other, valueOffset);
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
				Locals locals = ctx.getLocals();
				VMHelper helper = vm.getHelper();
				ArrayValue prefix = (ArrayValue) memoryManager.readValue(helper.<InstanceValue>checkNotNull(locals.loadReference(1)), valueOffset);
				ArrayValue _this = (ArrayValue) memoryManager.readValue(locals.<InstanceValue>loadReference(0), valueOffset);
				int toOffset = locals.loadInt(2);
				ctx.setResult(startsWith(_this, prefix, toOffset) ? IntValue.ONE : IntValue.ZERO);
				return Result.ABORT;
			});
			PrimitiveClass charPrimitive = vm.getPrimitives().charPrimitive();
			vmi.setInvoker(jc, "replace", "(CC)Ljava/lang/String;", ctx -> {
				Locals locals = ctx.getLocals();
				char oldChar = (char) locals.loadInt(1);
				char newChar = (char) locals.loadInt(2);
				InstanceValue _this = locals.loadReference(0);
				if (oldChar == newChar) {
					ctx.setResult(_this);
				} else {
					VMHelper helper = vm.getHelper();
					ArrayValue value = (ArrayValue) memoryManager.readValue(_this, valueOffset);
					int len = value.getLength();
					int i = -1;
					while (++i < len) {
						if (value.getChar(i) == oldChar) {
							break;
						}
					}
					if (i < len) {
						ArrayValue buf = helper.newArray(charPrimitive, len);
						for (int j = 0; j < i; j++) {
							buf.setChar(j, value.getChar(j));
						}
						while (i < len) {
							char c = value.getChar(i);
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
				while (++i <= max && source.getChar(i) != first) {
					;
				}
			}

			if (i <= max) {
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end && source.getChar(j)
					== target.getChar(k); j++, k++) {
					;
				}

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
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass jc = vm.getSymbols().java_lang_Character();
		vmi.setInvoker(jc, "toLowerCase", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Character.toLowerCase(ctx.getLocals().loadInt(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "toLowerCase", "(C)C", ctx -> {
			ctx.setResult(IntValue.of(Character.toLowerCase((char) ctx.getLocals().loadInt(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "toUpperCase", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Character.toUpperCase(ctx.getLocals().loadInt(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "toUpperCase", "(C)C", ctx -> {
			ctx.setResult(IntValue.of(Character.toUpperCase((char) ctx.getLocals().loadInt(0))));
			return Result.ABORT;
		});
		MethodInvoker digit = ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(IntValue.of(Character.digit(locals.loadInt(0), locals.loadInt(1))));
			return Result.ABORT;
		};
		vmi.setInvoker(jc, "digit", "(II)I", digit);
		vmi.setInvoker(jc, "digit", "(CI)I", digit);
		vmi.setInvoker(jc, "forDigit", "(II)C", ctx -> {
			Locals locals = ctx.getLocals();
			ctx.setResult(IntValue.of(Character.forDigit(locals.loadInt(0), locals.loadInt(1))));
			return Result.ABORT;
		});
	}

	private void intIntrinsics(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass jc = vm.getSymbols().java_lang_Integer();
		vmi.setInvoker(jc, "hashCode", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.hashCode(ctx.getLocals().loadInt(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "highestOneBit", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.highestOneBit(ctx.getLocals().loadInt(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "numberOfLeadingZeros", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.numberOfLeadingZeros(ctx.getLocals().loadInt(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "numberOfTrailingZeros", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.numberOfTrailingZeros(ctx.getLocals().loadInt(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "bitCount", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.bitCount(ctx.getLocals().loadInt(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "reverse", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.reverse(ctx.getLocals().loadInt(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "reverseBytes", "(I)I", ctx -> {
			ctx.setResult(IntValue.of(Integer.reverseBytes(ctx.getLocals().loadInt(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "toString", "(I)Ljava/lang/String;", ctx -> {
			Locals locals = ctx.getLocals();
			int value = locals.loadInt(0);
			ctx.setResult(vm.getHelper().newUtf8(Integer.toString(value)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "toHexString", "(I)Ljava/lang/String;", ctx -> {
			Locals locals = ctx.getLocals();
			int value = locals.loadInt(0);
			ctx.setResult(vm.getHelper().newUtf8(Integer.toHexString(value)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "toOctalString", "(I)Ljava/lang/String;", ctx -> {
			Locals locals = ctx.getLocals();
			int value = locals.loadInt(0);
			ctx.setResult(vm.getHelper().newUtf8(Integer.toOctalString(value)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "toString", "(II)Ljava/lang/String;", ctx -> {
			Locals locals = ctx.getLocals();
			int value = locals.loadInt(0);
			int radix = locals.loadInt(1);
			ctx.setResult(vm.getHelper().newUtf8(Integer.toString(value, radix)));
			return Result.ABORT;
		});
	}

	private void longIntrinsics(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass jc = vm.getSymbols().java_lang_Long();
		vmi.setInvoker(jc, "hashCode", "(J)I", ctx -> {
			ctx.setResult(IntValue.of(Long.hashCode(ctx.getLocals().loadLong(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "highestOneBit", "(J)J", ctx -> {
			ctx.setResult(LongValue.of(Long.highestOneBit(ctx.getLocals().loadLong(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "numberOfLeadingZeros", "(J)I", ctx -> {
			ctx.setResult(IntValue.of(Long.numberOfLeadingZeros(ctx.getLocals().loadLong(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "numberOfTrailingZeros", "(J)I", ctx -> {
			ctx.setResult(IntValue.of(Long.numberOfTrailingZeros(ctx.getLocals().loadLong(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "bitCount", "(J)I", ctx -> {
			ctx.setResult(IntValue.of(Long.bitCount(ctx.getLocals().loadLong(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "reverse", "(J)J", ctx -> {
			ctx.setResult(LongValue.of(Long.reverse(ctx.getLocals().loadLong(0))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "reverseBytes", "(J)J", ctx -> {
			ctx.setResult(LongValue.of(Long.reverseBytes(ctx.getLocals().loadLong(0))));
			return Result.ABORT;
		});
	}

	private void arrayIntrinsics(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass jc = (InstanceJavaClass) vm.findBootstrapClass("java/util/Arrays");
		vmi.setInvoker(jc, "hashCode", "([J)I", ctx -> {
			ObjectValue arr = ctx.getLocals().loadReference(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				ArrayValue array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Long.hashCode(array.getLong(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([D)I", ctx -> {
			ObjectValue arr = ctx.getLocals().loadReference(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				ArrayValue array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Double.hashCode(array.getDouble(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([I)I", ctx -> {
			ObjectValue arr = ctx.getLocals().loadReference(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				ArrayValue array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Integer.hashCode(array.getInt(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([F)I", ctx -> {
			ObjectValue arr = ctx.getLocals().loadReference(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				ArrayValue array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Float.hashCode(array.getFloat(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([C)I", ctx -> {
			ObjectValue arr = ctx.getLocals().loadReference(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				ArrayValue array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Character.hashCode(array.getChar(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([S)I", ctx -> {
			ObjectValue arr = ctx.getLocals().loadReference(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				ArrayValue array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Short.hashCode(array.getShort(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([B)I", ctx -> {
			ObjectValue arr = ctx.getLocals().loadReference(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				ArrayValue array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Byte.hashCode(array.getByte(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([Z)I", ctx -> {
			ObjectValue arr = ctx.getLocals().loadReference(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				ArrayValue array = (ArrayValue) arr;
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					result = 31 * result + Boolean.hashCode(array.getBoolean(i));
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hashCode", "([Ljava/lang/Object;)I", ctx -> {
			ObjectValue arr = ctx.getLocals().loadReference(0);
			if (arr.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				LinkResolver linkResolver = vm.getPublicLinkResolver();
				ArrayValue array = (ArrayValue) arr;
				VMHelper helper = ctx.getHelper();
				int result = 1;
				for (int i = 0, j = array.getLength(); i < j; i++) {
					ObjectValue value = array.getValue(i);
					result *= 31;
					if (!value.isNull()) {
						JavaMethod method = linkResolver.resolveVirtualMethod(value, "hashCode", "()I");
						Locals locals = vm.getThreadStorage().newLocals(method);
						locals.set(0, value);
						result += helper.invoke(method, locals).getResult().asInt();
					}
				}
				ctx.setResult(IntValue.of(result));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([JJ)V", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			ArrayValue arr = helper.checkNotNull(locals.loadReference(0));
			long v = locals.loadLong(1);
			for (int j = arr.getLength(); j != 0; ) {
				arr.setLong(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([DD)V", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			ArrayValue arr = helper.checkNotNull(locals.loadReference(0));
			double v = locals.loadDouble(1);
			for (int j = arr.getLength(); j != 0; ) {
				arr.setDouble(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([II)V", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			ArrayValue arr = helper.checkNotNull(locals.loadReference(0));
			int v = locals.loadInt(1);
			for (int j = arr.getLength(); j != 0; ) {
				arr.setInt(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([FF)V", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			ArrayValue arr = helper.checkNotNull(locals.loadReference(0));
			float v = locals.loadFloat(1);
			for (int j = arr.getLength(); j != 0; ) {
				arr.setFloat(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([CC)V", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			ArrayValue arr = helper.checkNotNull(locals.loadReference(0));
			char v = (char) locals.loadInt(1);
			for (int j = arr.getLength(); j != 0; ) {
				arr.setChar(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([SS)V", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			ArrayValue arr = helper.checkNotNull(locals.loadReference(0));
			short v = (short) locals.loadInt(1);
			for (int j = arr.getLength(); j != 0; ) {
				arr.setShort(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([BB)V", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			ArrayValue arr = helper.checkNotNull(locals.loadReference(0));
			byte v = (byte) locals.loadInt(1);
			for (int j = arr.getLength(); j != 0; ) {
				arr.setByte(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "fill", "([ZZ)V", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			ArrayValue arr = helper.checkNotNull(locals.loadReference(0));
			boolean v = locals.loadInt(1) != 0;
			for (int j = arr.getLength(); j != 0; ) {
				arr.setBoolean(--j, v);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([J[J)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value $a = locals.loadReference(0);
			Value $a2 = locals.loadReference(1);
			ctx.setResult(primitiveArraysEqual(vm.getMemoryManager(), $a, $a2));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([D[D)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value $a = locals.loadReference(0);
			Value $a2 = locals.loadReference(1);
			ctx.setResult(primitiveArraysEqual(vm.getMemoryManager(), $a, $a2));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([I[I)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value $a = locals.loadReference(0);
			Value $a2 = locals.loadReference(1);
			ctx.setResult(primitiveArraysEqual(vm.getMemoryManager(), $a, $a2));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([F[F)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value $a = locals.loadReference(0);
			Value $a2 = locals.loadReference(1);
			ctx.setResult(primitiveArraysEqual(vm.getMemoryManager(), $a, $a2));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([C[C)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value $a = locals.loadReference(0);
			Value $a2 = locals.loadReference(1);
			ctx.setResult(primitiveArraysEqual(vm.getMemoryManager(), $a, $a2));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([S[S)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value $a = locals.loadReference(0);
			Value $a2 = locals.loadReference(1);
			ctx.setResult(primitiveArraysEqual(vm.getMemoryManager(), $a, $a2));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([B[B)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value $a = locals.loadReference(0);
			Value $a2 = locals.loadReference(1);
			ctx.setResult(primitiveArraysEqual(vm.getMemoryManager(), $a, $a2));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([Z[Z)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value $a = locals.loadReference(0);
			Value $a2 = locals.loadReference(1);
			ctx.setResult(primitiveArraysEqual(vm.getMemoryManager(), $a, $a2));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "equals", "([Ljava/lang/Object;[Ljava/lang/Object;)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value $a = locals.loadReference(0);
			Value $a2 = locals.loadReference(1);
			ctx.setResult(instanceArraysEqual(vm, $a, $a2));
			return Result.ABORT;
		});
	}

	private IntValue primitiveArraysEqual(MemoryManager memoryManager, Value $a, Value $b) {
		if ($a == $b) {
			return IntValue.ONE;
		} else if ($a.isNull() || $b.isNull()) {
			return IntValue.ZERO;
		}
		ArrayValue a = (ArrayValue) $a;
		ArrayValue b = (ArrayValue) $b;
		MemoryData v1 = a.getMemory().getData();
		MemoryData v2 = b.getMemory().getData();
		if (v1.length() != v2.length()) {
			return IntValue.ZERO;
		}
		int offset = memoryManager.arrayBaseOffset(a);
		long total = v1.length();
		while (total != offset) {
			long diff = total - offset;
			if (diff >= 8) {
				if (v1.readLong(offset) != v2.readLong(offset)) {
					return IntValue.ZERO;
				}
				offset += 8;
			} else if (diff >= 4) {
				if (v1.readInt(offset) != v2.readInt(offset)) {
					return IntValue.ZERO;
				}
				offset += 4;
			} else if (diff >= 2) {
				if (v1.readShort(offset) != v2.readShort(offset)) {
					return IntValue.ZERO;
				}
				offset += 2;
			} else {
				if (v1.readByte(offset) != v2.readByte(offset)) {
					return IntValue.ZERO;
				}
				offset++;
			}
		}
		return IntValue.ONE;
	}

	private IntValue instanceArraysEqual(VirtualMachine vm, Value $a, Value $b) {
		if ($a == $b) {
			return IntValue.ONE;
		} else if ($a.isNull() || $b.isNull()) {
			return IntValue.ZERO;
		}
		ArrayValue a = (ArrayValue) $a;
		ArrayValue b = (ArrayValue) $b;
		int length = a.getLength();
		if (length != b.getLength()) {
			return IntValue.ZERO;
		}
		VMHelper helper = vm.getHelper();
		LinkResolver linkResolver = vm.getPublicLinkResolver();
		ThreadStorage ts = vm.getThreadStorage();
		while (length-- != 0) {
			ObjectValue v1 = a.getValue(length);
			ObjectValue v2 = b.getValue(length);
			if (v1 != v2) {
				if (!v1.isNull()) {
					JavaMethod method = linkResolver.resolveVirtualMethod(v1, "equals", "(Ljava/lang/Object;)Z");
					Locals locals = ts.newLocals(method);
					locals.set(0, v1);
					locals.set(1, v2);
					boolean eq = helper.invoke(method, locals).getResult().asBoolean();
					if (!eq) {
						return IntValue.ZERO;
					}
				}
			}
		}
		return IntValue.ONE;
	}
}
