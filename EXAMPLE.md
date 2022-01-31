## Basic example

Shows how to boot VM & run an application.

```Java
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.fs.HostFileDescriptorManager;
import dev.xdark.ssvm.jit.JitClass;
import dev.xdark.ssvm.jit.JitCompiler;
import dev.xdark.ssvm.jit.JitInstaller;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.objectweb.asm.MethodTooLargeException;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MyApplication {

  public static void main(String[] args) {
    val vm =
        new VirtualMachine() {
          @Override
          public FileDescriptorManager createFileDescriptorManager() {
            // Allow any files from host OS
            return new HostFileDescriptorManager() {
              @Override
              public long open(String path, int mode) throws IOException {
                // Only enable read mode
                if (mode != READ) throw new FileNotFoundException(path);
                return super.open(path, mode);
              }
            };
          }
        };
    val helper = vm.getHelper();
    try {
      val vmi = vm.getInterface();
      // Enable JIT, if needed
      val definer = new JitClassLoader();
      vmi.registerMethodEnter(
          ctx -> {
            val jm = ctx.getMethod();
            int count = jm.getInvocationCount();
            if (count == 256 && !Modifier.isCompiledMethod(jm.getAccess())) {
              if (JitCompiler.isCompilable(jm)) {
                try {
                  val jit = JitCompiler.compile(jm, 3);
                  JitInstaller.install(jm, definer, jit);
                } catch (MethodTooLargeException ex) {
                  val node = jm.getNode();
                  node.access |= Modifier.ACC_JIT;
                } catch (Throwable ex) {
                  throw new IllegalStateException("Could not install JIT class for " + jm, ex);
                }
              }
            }
          });
      // Bootstrap VM
      vm.bootstrap();
      val symbols = vm.getSymbols();

      // Add jar to system class loader
      val cl =
          helper
              .invokeStatic(
                  symbols.java_lang_ClassLoader,
                  "getSystemClassLoader",
                  "()Ljava/lang/ClassLoader;",
                  new Value[0],
                  new Value[0])
              .getResult();
      addURL(vm, cl, "c:\\Users\\User\\obf.jar");

      // Invoke main, setup hooks to do stuff, etc
      val klass = (InstanceJavaClass) helper.findClass(cl, "sample/string/StringsLong", true);
      val method = klass.getStaticMethod("main", "([Ljava/lang/String;)V");

      helper.invokeStatic(
          klass, method, new Value[0], new Value[] {helper.emptyArray(symbols.java_lang_String)});
    } catch (VMException ex) {
      helper.invokeVirtual("printStackTrace", "()V", new Value[0], new Value[] {ex.getOop()});
    }
  }

  private static final class JitClassLoader extends ClassLoader
      implements JitInstaller.ClassDefiner {

    @Override
    public Class<?> define(JitClass jitClass) {
      val code = jitClass.getCode();
      return defineClass(jitClass.getClassName().replace('/', '.'), code, 0, code.length);
    }
  }

  private static void addURL(VirtualMachine vm, Value loader, String path) {
    // ((URLClassLoader)loader).addURL(new File(path).toURI().toURL());
    val helper = vm.getHelper();
    val fileClass = (InstanceJavaClass) vm.findBootstrapClass("java/io/File", true);
    val file = vm.getMemoryManager().newInstance(fileClass);
    helper.invokeExact(
        fileClass,
        "<init>",
        "(Ljava/lang/String;)V",
        new Value[0],
        new Value[] {file, helper.newUtf8(path)});
    val uri =
        helper
            .invokeVirtual("toURI", "()Ljava/net/URI;", new Value[0], new Value[] {file})
            .getResult();
    val url =
        helper
            .invokeVirtual("toURL", "()Ljava/net/URL;", new Value[0], new Value[] {uri})
            .getResult();
    helper.invokeVirtual("addURL", "(Ljava/net/URL;)V", new Value[0], new Value[] {loader, url});
  }
}
```