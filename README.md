# SSVM - Stupidly Simple VM

## Capabilities

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

## Usage

For a basic example, see: [EXAMPLE.md](EXAMPLE.md)

More simple cases can be found in the test cases: [ssvm-invoke tests](ssvm-invoke/src/test/java/dev/xdark/ssvm)

For complex examples, see: [Recaf v3](https://github.com/Col-E/Recaf/tree/dev3/recaf-core/src/main/java/me/coley/recaf/ssvm) _(Old, using SSVM 1.0)_

To add SSVM to your project, add it as a dependency via [JitPack](https://jitpack.io/#xxDark/SSVM). 
Be sure to use the _"commits"_ tab to pull the latest version, as 2.0.0 has not officially been tagged for release yet.

SSVM depends on [JLinker](https://github.com/xxDark/jlinker/), which is also available via [JitPack](https://jitpack.io/#xxDark/jlinker/)