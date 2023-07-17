package dev.xdark.ssvm.mirror.member;

import dev.xdark.jlinker.MemberInfo;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.mirror.type.InstanceClass;
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
	private final MemberInfo<JavaField> linkerInfo;
	private final InstanceClass owner;
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
	public SimpleJavaField(InstanceClass owner, FieldNode node, int slot, long offset) {
		this.owner = owner;
		this.node = node;
		this.slot = slot;
		this.offset = offset;
		linkerInfo = makeLinkerInfo(this);
	}

	@Override
	public InstanceClass getOwner() {
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
	public MemberInfo<? extends JavaMember> linkerInfo() {
		return linkerInfo;
	}

	@Override
	public int hashCode() {
		return node.hashCode();
	}

	@Override
	public String toString() {
		FieldNode node = this.node;
		return getOwner().getInternalName() + '.' + node.name + ' ' + node.desc;
	}

	private void resolveFieldType() {
		InstanceClass owner = this.owner;
		VirtualMachine vm = owner.getVM();
		type = vm.getOperations().findClass(owner, Type.getType(node.desc), false);
	}

	private static MemberInfo<JavaField> makeLinkerInfo(JavaField field) {
		return new MemberInfo<JavaField>() {
			@Override
			public JavaField innerValue() {
				return field;
			}

			@Override
			public int accessFlags() {
				return Modifier.eraseField(field.getModifiers());
			}

			@Override
			public boolean isPolymorphic() {
				return false;
			}
		};
	}
}
