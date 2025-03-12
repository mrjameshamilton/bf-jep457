# Brainf*ck JVM Compiler

A compiler that compiles Brainf*ck input to Java bytecode that can be executed on a
JVM, using the Java [Class-File API](https://openjdk.org/jeps/484).

The compiler is simple and does no optimizations. For an optimizing compiler [see here](https://github.com/mrjameshamilton/bf).

# Running

You'll need JDK 24, the easiest way to install this, on Linux, is with [SDK man](https://sdkman.io/):

```shell
sdk install java 24.ea.36-open
```

Compile a Brainf*ck program by passing the input file path and the output jar path
to the compiler. You can compile & execute the compiled class, or simply run `BfCompiler.java` 
directly from the source file:

```shell
$ java BfCompiler.java examples/hellojvm.bf out.jar
```

The compiled jar can then be executed:

```shell
$ java -jar out.jar
```
