package dev.xdark.ssvm.filesystem.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.filesystem.ZipFile;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Initializes java/util/jar/JarFile.
 *
 * @author xDark
 */
@UtilityClass
public class JarFileNatives {

	/**
	 * @param vm VM instance.
	 * @param fileManager File manager.
	 */
	public void init(VirtualMachine vm, FileManager fileManager) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass zf = symbols.java_util_zip_ZipFile();
		InstanceClass jf = symbols.java_util_jar_JarFile();
		if (vm.getJvmVersion() <= 8) {
			vmi.setInvoker(jf, "getMetaInfEntryNames", "()[Ljava/lang/String;", ctx -> {
				VMOperations ops = vm.getOperations();
				ObjectValue _this = ctx.getLocals().loadReference(0);

				// Directly has file handle as a field in JDK 8
				long handle = ops.getLong(_this, zf, "jzfile");

				ZipFile zip = fileManager.getZipFile(handle);
				if (zip == null) {
					ops.throwException(symbols.java_lang_IllegalStateException(), "zip closed");
				}
				ObjectValue[] paths = zip.stream()
						.map(ZipEntry::getName)
						.filter(name -> name.toUpperCase(Locale.ENGLISH).startsWith("META-INF/"))
						.map(ops::newUtf8)
						.toArray(ObjectValue[]::new);
				ctx.setResult(ops.toVMReferences(paths));

				return Result.ABORT;
			});
		} else {
			VMOperations ops = vm.getOperations();
			MemoryManager mem = vm.getMemoryManager();
			ThreadManager threadManager = vm.getThreadManager();

			// TODO: Noop for this method is a hack and should be fixed later (Affects JDK 9+)
			vmi.setInvoker(jf, "checkForSpecialAttributes", "()V", MethodInvoker.noop());
			vmi.setInvoker(jf, "getManifestFromReference", "()Ljava/util/jar/Manifest;", ctx -> {
				ObjectValue _this = ctx.getLocals().loadReference(0);
				long handle = ZipFileNatives.getJdk9ZipFileHandle(ctx, _this);
				ZipFile zipFile = fileManager.getZipFile(handle);
				if (zipFile == null)
					ops.throwException(symbols.java_lang_IllegalStateException(), "zip closed");

				// Get manifest entry
				ZipEntry entry = zipFile.getEntry(JarFile.MANIFEST_NAME);
				if (entry == null) {
					ctx.setResult(ctx.getMemoryManager().nullValue());
					return Result.ABORT;
				}

				try {
					byte[] manifestBytes = zipFile.readEntry(entry);
					ArrayValue vmManifestBytes = ops.toVMBytes(manifestBytes);

					InstanceClass baisClass = (InstanceClass) vm.findBootstrapClass("java/io/ByteArrayInputStream");
					InstanceClass manifestClass = (InstanceClass) vm.findBootstrapClass("java/util/jar/Manifest");

					// new ByteArrayInputStream(manifestBytes)
					InstanceValue bais = mem.newInstance(baisClass);
					Locals baisLocals = threadManager.currentThreadStorage().newLocals(2);
					baisLocals.setReference(0, bais);
					baisLocals.setReference(1, vmManifestBytes);
					ops.invokeVoid(bais.getJavaClass().getMethod("<init>", "([B)V"), baisLocals);

					// new Manifest(stream);
					InstanceValue manifest = mem.newInstance(manifestClass);
					Locals manifestLocals = threadManager.currentThreadStorage().newLocals(2);
					manifestLocals.setReference(0, manifest);
					manifestLocals.setReference(1, bais);
					ops.invokeVoid(manifest.getJavaClass().getMethod("<init>", "(Ljava/io/InputStream;)V"), manifestLocals);

					ctx.setResult(manifest);
					return Result.ABORT;
				} catch (IOException ex) {
					ops.throwException(symbols.java_io_IOException(), ex.getMessage());
					throw new IllegalStateException(ex);
				}
			});
			vmi.setInvoker(jf, "getMetaInfEntryNames", "()[Ljava/lang/String;", ctx -> {
				ObjectValue _this = ctx.getLocals().loadReference(0);

				// We can skip parsing the zip if the 'metanames' is empty
				ObjectValue res = ops.getReference(_this, zf, "res", "Ljava/util/zip/ZipFile$CleanableResource;");
				ObjectValue zsrc = ops.getReference(res, "zsrc", "Ljava/util/zip/ZipFile$Source;");
				ObjectValue metanames = ops.getReference(zsrc, "metanames", "[I");
				if (metanames.isNull()) {
					ObjectValue[] paths = new ObjectValue[0];
					ctx.setResult(ops.toVMReferences(paths));
					return Result.ABORT;
				}

				// Pull meta-inf names from zip-file
				long handle = ZipFileNatives.getJdk9ZipFileHandle(ctx, _this);
				ZipFile zipFile = fileManager.getZipFile(handle);
				if (zipFile == null)
					ops.throwException(symbols.java_lang_IllegalStateException(), "zip closed");
				ObjectValue[] paths = zipFile.stream()
						.map(ZipEntry::getName)
						.filter(name -> name.startsWith("META-INF/"))
						.map(ops::newUtf8)
						.toArray(ObjectValue[]::new);
				ctx.setResult(ops.toVMReferences(paths));
				return Result.ABORT;
			});
		}
	}
}
