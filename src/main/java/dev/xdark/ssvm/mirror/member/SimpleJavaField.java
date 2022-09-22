package dev.xdark.ssvm.mirror.member;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.util.TypeSafeMap;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.util.BitSet;

/**
 * Field info.
 *
 * @author xDark
 */
public final class SimpleJavaField implements JavaField {

	private final TypeSafeMap metadata = new TypeSafeMap();
	private final BitSet extraModifiers = new BitSet();
	private final InstanceJavaClass owner;
	private final FieldNode node;
	private final int slot;
	private final long offset;
	private JavaClass type;
	private MemberIdentifier identifier;

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
	public BitSet extraModifiers() {
		return extraModifiers;
	}

	@Override
	public MemberIdentifier getIdentifier() {
		MemberIdentifier key = this.identifier;
		if (key == null) {
			return this.identifier = new SimpleMemberIdentifier(getName(), node.desc);
		}
		return key;
	}

	@Override
	public int getSlot() {
		return slot;
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
		InstanceJavaClass owner = this.owner;
		VirtualMachine vm = owner.getVM();
		ObjectValue cl = owner.getClassLoader();
		type = vm.getHelper().findClass(cl, Type.getType(node.desc), false);
	}
}
