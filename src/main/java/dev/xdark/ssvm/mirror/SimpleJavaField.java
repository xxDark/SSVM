package dev.xdark.ssvm.mirror;

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
	private Type type;

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
	public Type getType() {
		Type type = this.type;
		if (type == null) {
			return this.type = Type.getType(node.desc);
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
}
