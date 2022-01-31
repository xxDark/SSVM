# SSVM - Stupidly Simple VM

## Capabilities:
  - InvokeDynamic
  - Reflection
  - Class loading
  - File system (part of it)
  - Mapped I/O streams between VM & host OS
  - sun/misc/Unsafe implementation (part of it)
  - JDK 8/9 bootstrapping
  - Multi-threading (see ThreadManager)
  - Interface to modify VM behaviour, similar to JVM TI
  - Instruction rewriting: rewrite parts of methods as the VM executes
  - Somewhat *JIT* compiler

For some basic example, see: [EXAMPLE.md](EXAMPLE.md)