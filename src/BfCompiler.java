import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.TypeKind;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.classfile.ClassFile.ACC_STATIC;
import static java.lang.classfile.ClassFile.JAVA_6_VERSION;
import static java.lang.constant.ConstantDescs.CD_byte;
import static java.lang.constant.ConstantDescs.CD_char;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.util.jar.Attributes.Name.MAIN_CLASS;
import static java.util.jar.Attributes.Name.MANIFEST_VERSION;

private static final int DATA_POINTER = 0;
private static final int MEMORY = 1;
private static final Stack<LoopInfo> loops = new Stack<>();

public static void main(String[] args) throws IOException {
    if (args.length != 2) throw new RuntimeException("Expected input and output arguments");

    var input = Files.readString(Path.of(args[0]));
    var output = args[1];

    var bytes = ClassFile.of()
        .build(ClassDesc.of("BF"), classBuilder -> classBuilder
            .withVersion(JAVA_6_VERSION, 0)
            .withMethodBody("main", MethodTypeDesc.of(CD_void, ClassDesc.of("java.lang.String").arrayType()), ACC_PUBLIC | ACC_STATIC, codeBuilder -> {

                // Initialize memory.
                codeBuilder
                    .sipush(30_000)
                    .newarray(TypeKind.ByteType)
                    .astore(MEMORY);

                // Initialize data pointer.
                codeBuilder
                    .iconst_0()
                    .istore(DATA_POINTER);

                input.chars().forEach(c -> {
                    switch (c) {
                        case '>' -> move(codeBuilder, 1);
                        case '<' -> move(codeBuilder, -1);
                        case '+' -> increment(codeBuilder, 1);
                        case '-' -> increment(codeBuilder, -1);
                        case ',' -> readChar(codeBuilder);
                        case '.' -> printChar(codeBuilder);
                        case '[' -> loopBegin(codeBuilder);
                        case ']' -> loopEnd(codeBuilder);
                        default -> {
                            // Ignore other characters.
                        }
                    }
                });

                if (!loops.empty()) throw new RuntimeException("Too many '['");

                codeBuilder.return_();
            }));

    // Write the class file bytes into a jar file.
    var manifest = new Manifest();
    manifest.getMainAttributes().put(MANIFEST_VERSION, "1.0");
    manifest.getMainAttributes().put(MAIN_CLASS, "BF");
    try (var os = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(output)), manifest)) {
        os.putNextEntry(new JarEntry("BF.class"));
        os.write(bytes);
        os.closeEntry();
    }
}

/**
 * Move the data pointer by <code>amount</code>
 *
 * @param amount The amount to move the data pointer which can be positive or negative.
 */
private static void move(CodeBuilder codeBuilder, int amount) {
    codeBuilder.iinc(DATA_POINTER, amount);
}

/**
 * Increment or decrement the value in memory at the data pointer position by <code>amount</code>.
 *
 * @param amount The amount to add to the value in memory at the data pointer which can be
 *               positive or negative.
 */
private static void increment(CodeBuilder codeBuilder, int amount) {
    codeBuilder
        .aload(MEMORY)
        .iload(DATA_POINTER)
        .dup2()
        .baload()
        .constantInstruction(amount)
        .iadd()
        .bastore();
}

/**
 * Print the character in memory at the data pointer position to stdout.
 */
private static void printChar(CodeBuilder codeBuilder) {
    codeBuilder
        .getstatic(ClassDesc.of("java.lang.System"), "out", ClassDesc.of("java.io.PrintStream"))
        .aload(MEMORY)
        .iload(DATA_POINTER)
        .baload()
        .i2c()
        .invokevirtual(ClassDesc.of("java.io.PrintStream"), "print", MethodTypeDesc.of(CD_void, CD_char));
}

/**
 * Read a char (1 byte) from stdin and write it to the memory
 * at the data pointer position.
 */
private static void readChar(CodeBuilder codeBuilder) {
    codeBuilder
        .getstatic(ClassDesc.of("java.lang.System"), "in", ClassDesc.of("java.io.InputStream"))
        .aload(MEMORY)
        .iload(DATA_POINTER)
        .iconst_1()
        .invokevirtual(ClassDesc.of("java.io.InputStream"), "read", MethodTypeDesc.of(CD_int, CD_byte.arrayType(), CD_int, CD_int))
        .pop();
}

/**
 * Compare the value in memory at the data pointer position with zero
 * and jump end of the loop if zero.
 * <p/>
 * The {@link Label}s for loop body and loop exit are pushed onto the {@link #loops} stack.
 */
private static void loopBegin(CodeBuilder codeBuilder) {
    var loopInfo = loops.push(new LoopInfo(codeBuilder.newLabel(), codeBuilder.newLabel()));

    codeBuilder
        .aload(MEMORY)
        .iload(DATA_POINTER)
        .baload()
        .ifeq(loopInfo.exit)
        .labelBinding(loopInfo.body);
}

/**
 * Compare the value in memory at the data pointer position with zero
 * and jump back to the beginning of the loop if not zero.
 * <p/>
 * The {@link Label}s for loop body and loop exit are popped from the {@link #loops} stack.
 */
private static void loopEnd(CodeBuilder codeBuilder) {
    if (loops.empty()) throw new RuntimeException("Unexpected ']'");

    var loopInfo = loops.pop();

    codeBuilder
        .aload(MEMORY)
        .iload(DATA_POINTER)
        .baload()
        .ifne(loopInfo.body)
        .labelBinding(loopInfo.exit);
}

private record LoopInfo(Label body, Label exit) {}
