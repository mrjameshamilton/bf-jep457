# Brainf*ck JVM Compiler

A compiler that compiles Brainf*ck input to Java bytecode that can be executed on a
JVM, using the JEP457 Class-File API.

The compiler is simple and does no optimizations. For an optimizing compiler [see here](https://github.com/mrjameshamilton/bf).

# Building

You'll need JDK 22. The easiest way to install this, on Linux, is with [SDK man](https://sdkman.io/):

```shell
sdk install java 22-open
```

You can then compile the `Main` class with `--release 22` and `--enable-preview` flags:

```shell
$ javac --release 22 --enable-preview src/BfCompiler.java -d build
```

# Run

Compile a Brainf*ck program by passing the input file path and the output jar path
to the compiler.

You can execute the compiled class, or simply run `BfCompiler.java` 
directly from source with the `--source 22` parameter:

```shell
$ java --enable-preview --source 22 src/BfCompiler.java examples/hellojvm.bf build/out.jar
```

The compiled jar can then be executed:

```shell
$ java -jar build/out.jar
```
