# Brainf*ck JVM Compiler

A compiler that compiles Brainf*ck input to Java bytecode that can be executed on a
JVM, using the preview Class-File API.

The compiler is simple and does no optimizations. For an optimizing compiler [see here](https://github.com/mrjameshamilton/bf).

# Building

You'll need JDK 23, the easiest way to install this, on Linux, is with [SDK man](https://sdkman.io/):

```shell
sdk install java 23-open
```

You can then compile the `Main` class with `--release 23` and `--enable-preview` flags:

```shell
$ javac --release 23 --enable-preview src/BfCompiler.java -d build
```

# Run

Compile a Brainf*ck program by passing the input file path and the output jar path
to the compiler.

You can execute the compiled class, or simply run `BfCompiler.java` 
directly from source with the `--source 23` parameter:

```shell
$ java --enable-preview --source 23 src/BfCompiler.java examples/hellojvm.bf build/out.jar
```

The compiled jar can then be executed:

```shell
$ java -jar build/out.jar
```
