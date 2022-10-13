package dev.xdark.ssvm.inject;

import lombok.experimental.UtilityClass;

import static org.objectweb.asm.Opcodes.*;

/**
 * All injected fields.
 *
 * @author xDark
 */
@UtilityClass
public class InjectedClassLayout {

	public final InjectedField java_lang_Class_protectionDomain = field(ACC_PRIVATE | ACC_FINAL, "protectionDomain", "Ljava/lang/Object;");
	public final InjectedField java_lang_Class_id = field(ACC_PRIVATE | ACC_FINAL, "id", "I");
	public final InjectedField java_io_FileDescriptor_handle = field(ACC_PRIVATE, "handle", "J");

	private InjectedField field(int accessFlags, String name, String descriptor) {
		return new InjectedField(accessFlags, name, descriptor);
	}
}
