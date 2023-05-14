# SSVM - Stupidly Simple VM

## Capabilities:
  - InvokeDynamic
  - Reflection
  - Class loading
  - File system (part of it)
  - Mapped I/O streams between VM & host OS
  - sun/misc/Unsafe implementation
  - JDK 8/9 bootstrapping
  - Multi-threading (see ThreadManager)
  - Interface to modify VM behaviour, similar to JVM TI
  - Instruction rewriting: rewrite parts of methods as the VM executes
  - ~~Somewhat *JIT* compiler~~
  - ~~Garbage Collector~~

# TODO:
  - Return back *JIT*
  - Return back GC

For some basic example, see: [EXAMPLE.md](EXAMPLE.md)

For more complex examples, see [Recaf repository](https://github.com/Col-E/Recaf/tree/dev3/recaf-core/src/main/java/me/coley/recaf/ssvm)