package com.github.groundbreakingmc.fission.source;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * A fast, efficient character source interface for sequential character reading.
 * <p>
 * This interface provides high-performance character-by-character access with support for:
 * <ul>
 *   <li>Sequential reading with {@link #read()}</li>
 *   <li>Look-ahead operations with {@link #peek()} and {@link #peekAhead(int)}</li>
 *   <li>Mark/reset functionality for backtracking</li>
 *   <li>Utility methods for common parsing operations</li>
 * </ul>
 *
 * <p><b>Design Goals:</b>
 * <ul>
 *   <li>No checked exceptions - all errors are wrapped in runtime exceptions</li>
 *   <li>No synchronization overhead - thread safety is not guaranteed</li>
 *   <li>Minimal memory allocations for high-performance parsing</li>
 *   <li>Parser-friendly API with look-ahead and backtracking support</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b>
 * <pre>{@code
 * // Basic reading
 * CharSource source = new StringCharSource("Hello World");
 * while (source.hasNext()) {
 *     char ch = (char) source.read();
 *     System.out.print(ch);
 * }
 *
 * // Parser operations
 * CharSource source = new StringCharSource("key = \"value\"");
 * String key = source.readWhile(Character::isLetter);      // "key"
 * source.skipWhitespace();                                 // skip spaces
 * source.consume("=");                                     // consume '='
 * source.skipWhitespace();                                 // skip spaces
 * if (source.consume("\"")) {                             // consume opening quote
 *     String value = source.readUntil('"');              // "value"
 * }
 *
 * // Look-ahead operations
 * if (source.startsWith("version")) {
 *     // Handle version directive
 * }
 *
 * // Backtracking
 * source.mark();
 * String token = source.readWhile(Character::isLetter);
 * if (token.length() < 3) {
 *     source.reset(); // Go back and try different approach
 * }
 * }</pre>
 *
 * <p><b>Thread Safety:</b> Implementations are <b>NOT</b> thread-safe for performance reasons.
 * Use external synchronization if accessing from multiple threads.
 *
 * <p><b>Resource Management:</b> For file-based implementations, use {@link ClosableCharSource}
 * which extends this interface with {@link AutoCloseable}.
 *
 * @see ClosableCharSource
 * @see com.github.groundbreakingmc.fission.source.impl.StringCharSource
 * @see com.github.groundbreakingmc.fission.source.impl.FileCharSource
 * @since 1.0.0
 */
public interface CharSource {

    /**
     * Reads the next character from the source and advances the position.
     *
     * @return the next character as an integer (0-65535), or -1 if end of source is reached
     * @see #peek()
     * @see #readChar()
     */
    int read();

    /**
     * Returns the next character without advancing the position.
     * Multiple calls to {@code peek()} will return the same character
     * until {@link #read()} is called.
     *
     * @return the next character as an integer (0-65535), or -1 if end of source is reached
     * @see #read()
     * @see #peekAhead(int)
     */
    int peek();

    /**
     * Looks ahead multiple characters without advancing the position.
     * This is more efficient than calling {@link #peek()} multiple times
     * after advancing the position.
     *
     * @param count the number of characters to look ahead (must be ≥ 0)
     * @return array of characters as integers, with -1 for positions beyond end of source.
     * Returns empty array if count ≤ 0.
     * @example <pre>{@code
     * CharSource source = new StringCharSource("hello");
     * int[] ahead = source.peekAhead(3); // [h, e, l]
     * // Position is still at 'h'
     * }</pre>
     * @see #peek()
     * @see #startsWith(String)
     */
    int[] peekAhead(int count);

    /**
     * Checks if more characters are available for reading.
     *
     * @return {@code true} if {@link #read()} will return a character (not -1),
     * {@code false} if end of source is reached
     */
    boolean hasNext();

    /**
     * Returns the current position in the character source.
     * Position starts at 0 and increments with each {@link #read()} call.
     *
     * @return current position (0-based), primarily useful for debugging and error reporting
     */
    long position();

    /**
     * Marks the current position for later restoration with {@link #reset()}.
     * Only one mark can be active at a time - calling {@code mark()} again
     * will overwrite the previous mark.
     *
     * @example <pre>{@code
     * source.mark();
     * String word = source.readWhile(Character::isLetter);
     * if (word.isEmpty()) {
     *     source.reset(); // Go back to marked position
     * } else {
     *     source.commit(); // Keep current position, clear mark
     * }
     * }</pre>
     * @see #reset()
     * @see #commit()
     */
    void mark();

    /**
     * Restores the position to the last {@link #mark()} location.
     *
     * @throws IllegalStateException if no mark has been set
     * @see #mark()
     * @see #commit()
     */
    void reset();

    /**
     * Commits the current position and clears the mark set by {@link #mark()}.
     * This is useful when you've successfully parsed something after marking
     * and no longer need the ability to reset.
     *
     * @throws IllegalStateException if no mark has been set
     * @example <pre>{@code
     * source.mark();
     * String token = source.readWhile(Character::isLetter);
     * if (isValidToken(token)) {
     *     source.commit(); // Success - keep current position
     * } else {
     *     source.reset();  // Failure - go back to mark
     * }
     * }</pre>
     * @see #mark()
     * @see #reset()
     */
    void commit();

    // ============== Utility Methods ==============

    /**
     * Reads the next character as a {@code char}, converting -1 (EOF) to null character.
     * This is a convenience method for when you know there are characters available.
     *
     * @return the next character, or '\0' if end of source is reached
     * @see #read()
     */
    default char readChar() {
        int ch = this.read();
        return ch == -1 ? '\0' : (char) ch;
    }

    /**
     * Reads characters while the given predicate returns {@code true}.
     * Stops at the first character that fails the predicate or at end of source.
     * The failing character remains in the source (not consumed).
     *
     * @param predicate condition to test each character (receives integer character code)
     * @return string containing all characters that matched the predicate
     * @throws NullPointerException if predicate is null
     * @example <pre>{@code
     * // Read digits
     * String number = source.readWhile(Character::isDigit); // "12345"
     *
     * // Read alphanumeric
     * String identifier = source.readWhile(ch -> Character.isLetterOrDigit(ch) || ch == '_');
     *
     * // Read until space
     * String word = source.readWhile(ch -> ch != ' ');
     * }</pre>
     */
    default String readWhile(@NotNull Predicate<Integer> predicate) {
        StringBuilder result = new StringBuilder(64);
        while (this.hasNext()) {
            int ch = this.peek();
            if (!predicate.test(ch)) break;
            result.append((char) this.read());
        }
        return result.toString();
    }

    /**
     * Reads characters until the specified delimiter is encountered.
     * The delimiter character remains in the source (not consumed).
     *
     * @param delimiter the character to stop at
     * @return string containing all characters before the delimiter
     * @example <pre>{@code
     * CharSource source = new StringCharSource("key=value");
     * String key = source.readUntil('='); // "key"
     * // '=' is still in the source
     * }</pre>
     */
    default String readUntil(int delimiter) {
        return this.readWhile(ch -> ch != delimiter);
    }

    /**
     * Reads a complete line, handling both Unix (\n) and Windows (\r\n) line endings.
     * The line ending character(s) are consumed but not included in the result.
     *
     * @return the line content without line ending characters
     * @example <pre>{@code
     * CharSource source = new StringCharSource("Line 1\nLine 2\r\nLine 3");
     * String line1 = source.readLine(); // "Line 1"
     * String line2 = source.readLine(); // "Line 2"
     * String line3 = source.readLine(); // "Line 3"
     * }</pre>
     */
    default String readLine() {
        String line = this.readUntil('\n');
        if (this.peek() == '\n') this.read(); // consume \n
        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }

    /**
     * Skips all whitespace characters (as defined by {@link Character#isWhitespace(int)}).
     * Advances the position past all consecutive whitespace characters.
     *
     * @example <pre>{@code
     * CharSource source = new StringCharSource("  \t\nabc");
     * source.skipWhitespace(); // Position now at 'a'
     * }</pre>
     * @see Character#isWhitespace(int)
     */
    default void skipWhitespace() {
        while (this.hasNext() && Character.isWhitespace(this.peek())) {
            this.read();
        }
    }

    /**
     * Checks if the source starts with the given string at the current position.
     * Does not advance the position - this is purely a look-ahead operation.
     *
     * @param prefix the string to check for (must not be null)
     * @return {@code true} if the source has enough characters and they match the prefix,
     * {@code false} otherwise. Empty string always returns {@code true}.
     * @throws NullPointerException if prefix is null
     * @example <pre>{@code
     * CharSource source = new StringCharSource("version = 1.0");
     * if (source.startsWith("version")) {
     *     // Handle version directive
     * }
     * // Position is still at 'v'
     * }</pre>
     */
    default boolean startsWith(@NotNull String prefix) {
        if (prefix.isEmpty()) return true;
        int[] ahead = this.peekAhead(prefix.length());
        for (int i = 0; i < prefix.length(); i++) {
            int actual = ahead[i];
            int expected = prefix.charAt(i);
            if (actual != expected) return false;
        }
        return true;
    }

    /**
     * Consumes the given string if it matches the current position.
     * If the string matches, advances the position past it.
     * If the string doesn't match, the position remains unchanged.
     *
     * @param str the string to consume (must not be null)
     * @return {@code true} if the string was found and consumed,
     * {@code false} if the string doesn't match (position unchanged)
     * @throws NullPointerException if str is null
     * @example <pre>{@code
     * CharSource source = new StringCharSource("key = value");
     * source.readWhile(Character::isLetter); // consume "key"
     * source.skipWhitespace();               // skip spaces
     * if (source.consume("=")) {             // consume "=" if present
     *     source.skipWhitespace();           // skip spaces after "="
     *     String value = source.readWhile(ch -> ch != '\n');
     * }
     * }</pre>
     */
    default boolean consume(String str) {
        if (this.startsWith(str)) {
            for (int i = 0; i < str.length(); i++) this.read();
            return true;
        }
        return false;
    }
}
