package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.util.TypeSafeMap;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

/**
 * Field info.
 *
 * @author xDark
 */
public final class SimpleJavaField implements JavaField {

	private final TypeSafeMap metadata = new TypeSafeMap();
	private final InstanceJavaClass owner;
	private final FieldNode node;
	private final int slot;
	private final long offset;
	private JavaClass type;

	/**
	 * @param owner  Field owner.
	 * @param node   ASM field info.
	 * @param slot   Field slot.
	 * @param offset Field offset.
	 */
	public SimpleJavaField(InstanceJavaClass owner, FieldNode node, int slot, long offset) {
		this.owner = owner;
		this.node = node;
		this.slot = slot;
		this.offset = offset;
	}

	@Override
	public InstanceJavaClass getOwner() {
		return owner;
	}

	@Override
	public FieldNode getNode() {
		return node;
	}

	@Override
	public int getSlot() {
		return slot;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public JavaClass getType() {
		JavaClass type = this.type;
		if (type == null) {
			resolveFieldType();
			return this.type;
		}
		return type;
	}

	@Override
	public TypeSafeMap getMetadata() {
		return metadata;
	}

	@Override
	public int hashCode() {
		return node.hashCode();
	}

	@Override
	public String toString() {
		FieldNode node = this.node;
		return getOwner().getInternalName() + '.' + node.name + node.desc;
	}

	private void resolveFieldType() {
		JavaClass type;
		VirtualMachine vm = owner.getVM();
		VMPrimitives primitives = vm.getPrimitives();
		Type asmType = Type.getType(node.desc);
		switch (asmType.getSort()) {
			case Type.LONG:
				type = primitives.longPrimitive();
				break;
			case Type.DOUBLE:
				type = primitives.doublePrimitive();
				break;
			case Type.INT:
				type = primitives.intPrimitive();
				break;
			case Type.FLOAT:
				type = primitives.floatPrimitive();
				break;
			case Type.CHAR:
				type = primitives.charPrimitive();
				break;
			case Type.SHORT:
				type = primitives.shortPrimitive();
				break;
			case Type.BYTE:
				type = primitives.bytePrimitive();
				break;
			case Type.BOOLEAN:
				type = primitives.booleanPrimitive();
				break;
			default:
				type = vm.getHelper().findClass(owner.getClassLoader(), asmType.getInternalName(), false);
		}
		this.type = type;
	}
}
