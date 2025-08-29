package com.github.groundbreakingmc.fission;

import com.github.groundbreakingmc.fission.exceptions.FileReadException;
import com.github.groundbreakingmc.fission.source.CharSource;
import com.github.groundbreakingmc.fission.source.impl.FileCharSource;
import com.github.groundbreakingmc.fission.source.impl.StringCharSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * A fast file reader library for reading file contents efficiently without checked exceptions or synchronization.
 * <p>
 * This utility class provides methods to read entire files as strings, read lines, or create character streams
 * for sequential processing. It wraps I/O operations in runtime exceptions ({@link FileReadException}) for
 * simpler error handling and avoids synchronization for better performance.
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *   <li>Reads files as strings or lines with customizable charset</li>
 *   <li>Creates streaming character sources for parser-friendly operations</li>
 *   <li>Handles large files efficiently with minimal memory overhead</li>
 *   <li>Thread-unsafe for performance; use external synchronization if needed</li>
 * </ul>
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * // Read entire file as string
 * String content = Fission.readString("example.txt");
 *
 * // Read file lines
 * List<String> lines = Fission.readLines("example.txt");
 *
 * // Stream characters from file
 * CharSource source = Fission.chars(Path.of("example.txt"));
 * while (source.hasNext()) {
 *     char ch = (char) source.read();
 *     // Process character
 * }
 * }</pre>
 *
 * @see CharSource
 * @see FileReadException
 * @see FileCharSource
 * @see StringCharSource
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public final class Fission {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException if an attempt is made to instantiate this class
     */
    private Fission() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Reads the entire content of a file as a string using UTF-8 encoding.
     *
     * @param path the path to the file as a string
     * @return the file content as a string
     * @throws FileReadException    if the file does not exist, is too large, or an I/O error occurs
     * @throws NullPointerException if the path is null
     */
    @NotNull
    public static String readString(@NotNull String path) {
        return readString(Path.of(path), StandardCharsets.UTF_8);
    }

    /**
     * Reads the entire content of a file as a string using UTF-8 encoding.
     *
     * @param path the {@link Path} to the file
     * @return the file content as a string
     * @throws FileReadException    if the file does not exist, is too large, or an I/O error occurs
     * @throws NullPointerException if the path is null
     */
    @NotNull
    public static String readString(@NotNull Path path) {
        return readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Reads the entire content of a file as a string using the specified charset.
     * <p>
     * The file is read into memory using a {@link ByteBuffer} and decoded with the provided charset.
     * If the file size exceeds {@link Integer#MAX_VALUE}, a {@link FileReadException} is thrown.
     *
     * @param path    the {@link Path} to the file
     * @param charset the charset to use for decoding the file content
     * @return the file content as a string
     * @throws FileReadException    if the file does not exist, is too large, or an I/O error occurs
     * @throws NullPointerException if the path or charset is null
     */
    @NotNull
    public static String readString(@NotNull Path path, @NotNull Charset charset) {
        try {
            if (!Files.exists(path)) {
                throw new FileReadException("File not found: " + path);
            }

            long size = Files.size(path);
            if (size == 0) return "";
            if (size > Integer.MAX_VALUE) {
                throw new FileReadException("File too large: " + size + " bytes");
            }

            try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
                ByteBuffer buffer = ByteBuffer.allocate((int) size);
                channel.read(buffer);
                buffer.flip();
                return charset.decode(buffer).toString();
            }
        } catch (IOException ex) {
            throw new FileReadException("Failed to read file: " + path, ex);
        }
    }

    /**
     * Reads all lines from a file using UTF-8 encoding.
     *
     * @param path the path to the file as a string
     * @return a list of strings, each representing a line in the file
     * @throws FileReadException    if the file cannot be read or an I/O error occurs
     * @throws NullPointerException if the path is null
     */
    @NotNull
    public static List<String> readLines(@NotNull String path) {
        return readLines(Path.of(path), StandardCharsets.UTF_8);
    }

    /**
     * Reads all lines from a file using UTF-8 encoding.
     *
     * @param path the {@link Path} to the file
     * @return a list of strings, each representing a line in the file
     * @throws FileReadException    if the file cannot be read or an I/O error occurs
     * @throws NullPointerException if the path is null
     */
    @NotNull
    public static List<String> readLines(@NotNull Path path) {
        return readLines(path, StandardCharsets.UTF_8);
    }

    /**
     * Reads all lines from a file using the specified charset.
     * <p>
     * Lines are split based on line endings (\n, \r, or \r\n), and the line endings are not included
     * in the returned strings.
     *
     * @param path    the {@link Path} to the file
     * @param charset the charset to use for decoding the file content
     * @return a list of strings, each representing a line in the file
     * @throws FileReadException    if the file cannot be read or an I/O error occurs
     * @throws NullPointerException if the path or charset is null
     */
    @NotNull
    public static List<String> readLines(@NotNull Path path, Charset charset) {
        try {
            return Files.readAllLines(path, charset);
        } catch (IOException ex) {
            throw new FileReadException("Failed to read lines: " + path, ex);
        }
    }

    /**
     * Creates a streaming character source for a file using UTF-8 encoding.
     * <p>
     * The returned {@link CharSource} allows sequential character-by-character reading
     * with support for look-ahead and backtracking operations.
     *
     * @param path the {@link Path} to the file
     * @return a {@link CharSource} for streaming the file's characters
     * @throws FileReadException    if the file cannot be accessed or an I/O error occurs
     * @throws NullPointerException if the path is null
     * @see FileCharSource
     */
    @NotNull
    public static CharSource chars(@NotNull Path path) {
        return chars(path, StandardCharsets.UTF_8);
    }

    /**
     * Creates a streaming character source for a file using the specified charset.
     * <p>
     * The returned {@link CharSource} allows sequential character-by-character reading
     * with support for look-ahead and backtracking operations. The source must be closed
     * explicitly when used in a try-with-resources block, as it implements {@link com.github.groundbreakingmc.fission.source.ClosableCharSource}.
     *
     * @param path    the {@link Path} to the file
     * @param charset the charset to use for decoding the file content
     * @return a {@link CharSource} for streaming the file's characters
     * @throws FileReadException    if the file cannot be accessed or an I/O error occurs
     * @throws NullPointerException if the path or charset is null
     * @see FileCharSource
     */
    @NotNull
    public static CharSource chars(@NotNull Path path, @NotNull Charset charset) {
        try {
            return new FileCharSource(path, charset);
        } catch (IOException ex) {
            throw new FileReadException("Failed to create char source: " + path, ex);
        }
    }

    /**
     * Creates a streaming character source from a string.
     * <p>
     * The returned {@link CharSource} allows sequential character-by-character reading
     * of the input string with support for look-ahead and backtracking operations.
     * If the input string is null, an empty source is returned.
     *
     * @param content the string to stream, or null for an empty source
     * @return a {@link CharSource} for streaming the string's characters
     * @see StringCharSource
     */
    @NotNull
    public static CharSource chars(@Nullable String content) {
        return new StringCharSource(content);
    }
}