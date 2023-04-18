package dev.xdark.ssvm.inject;

import lombok.experimental.UtilityClass;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;

/**
 * All injected fields.
 *
 * @author xDark
 */
@UtilityClass
public class InjectedClassLayout {

	public final InjectedField java_lang_Class_protectionDomain = field(ACC_PRIVATE, "protectionDomain", "Ljava/lang/Object;");
	public final InjectedField java_lang_Class_id = field(ACC_PRIVATE, "id", "I");
	public final InjectedField java_lang_Class_anonymousClassLoader = field(ACC_PRIVATE, "acl", "I");
	public final InjectedField java_lang_ClassLoader_oop = field(ACC_PRIVATE, "oop", "I");
	public final InjectedField java_io_FileDescriptor_handle = field(ACC_PRIVATE, "handle", "J");
	public final InjectedField java_lang_invoke_MemberName_vmindex = field(ACC_PRIVATE, "vmindex", "I");
	public final InjectedField java_lang_invoke_MemberName_method = field(ACC_PRIVATE, "method", "Ljava/lang/invoke/ResolvedMethodName;");
	public final InjectedField java_lang_invoke_ResolvedMethodName_vmtarget = field(ACC_PRIVATE, "vmtarget", "Ljava/lang/Object;");
	public final InjectedField java_lang_invoke_ResolvedMethodName_vmholder = field(ACC_PRIVATE, "vmholder", "Ljava/lang/Object;");

	private InjectedField field(int accessFlags, String name, String descriptor) {
		return new InjectedField(accessFlags, name, descriptor);
	}
}
