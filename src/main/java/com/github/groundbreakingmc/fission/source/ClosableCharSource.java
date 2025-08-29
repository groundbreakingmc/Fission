package com.github.groundbreakingmc.fission.source;

/**
 * A {@link CharSource} that requires explicit resource management and implements {@link AutoCloseable}.
 * <p>
 * This interface extends {@link CharSource} with resource management capabilities, making it suitable
 * for implementations that hold system resources (like file handles, network connections, etc.).
 *
 * <p><b>Key Differences from {@link CharSource}:</b>
 * <ul>
 *   <li>Implements {@link AutoCloseable} for use with try-with-resources</li>
 *   <li>Must be explicitly closed to release system resources</li>
 *   <li>Typically used for file-based or network-based character sources</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b>
 * <pre>{@code
 * // Proper resource management with try-with-resources
 * try (ClosableCharSource source = new FileCharSource(path, charset)) {
 *     while (source.hasNext()) {
 *         char ch = (char) source.read();
 *         // Process character
 *     }
 * } // Automatically closed here
 *
 * // Parser usage
 * try (ClosableCharSource source = Fission.chars(file)) {
 *     TomlParser parser = new TomlParser(source);
 *     return parser.parse();
 * }
 * }</pre>
 *
 * <p><b>Implementation Guidelines:</b>
 * <ul>
 *   <li>{@link #close()} should be idempotent (safe to call multiple times)</li>
 *   <li>{@link #close()} should not throw checked exceptions (wrap in runtime exceptions)</li>
 *   <li>After closing, all read operations should behave as if EOF is reached</li>
 *   <li>Closing should release all held system resources immediately</li>
 * </ul>
 *
 * <p><b>Memory vs File Sources:</b>
 * <ul>
 *   <li>{@link CharSource} - For in-memory sources (strings, byte arrays)</li>
 *   <li>{@link ClosableCharSource} - For system resource sources (files, streams)</li>
 * </ul>
 *
 * @see CharSource
 * @see com.github.groundbreakingmc.fission.source.impl.FileCharSource
 * @see AutoCloseable
 * @since 1.0.0
 */
public interface ClosableCharSource extends CharSource, AutoCloseable {

    /**
     * Closes this character source and releases any system resources associated with it.
     * <p>
     * After calling this method:
     * <ul>
     *   <li>All read operations should behave as if EOF is reached</li>
     *   <li>System resources (file handles, etc.) are released</li>
     *   <li>Subsequent calls to {@code close()} should be safe (idempotent)</li>
     * </ul>
     *
     * <p><b>Exception Handling:</b> Unlike the standard {@link AutoCloseable#close()},
     * this method should not throw checked exceptions. Any I/O errors should be wrapped
     * in runtime exceptions for consistency with the rest of the Fission API.
     *
     * @implNote Implementations should make this method idempotent and should not
     * throw checked exceptions.
     * @example <pre>{@code
     * ClosableCharSource source = new FileCharSource(path, charset);
     * try {
     *     // Use source
     *     String content = source.readWhile(ch -> ch != -1);
     * } finally {
     *     source.close(); // Safe to call even if already closed
     * }
     * }</pre>
     */
    @Override
    void close();
}
