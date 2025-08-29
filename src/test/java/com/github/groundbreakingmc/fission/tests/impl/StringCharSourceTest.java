package com.github.groundbreakingmc.fission.tests.impl;

import com.github.groundbreakingmc.fission.source.CharSource;
import com.github.groundbreakingmc.fission.source.impl.StringCharSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StringCharSource Tests")
class StringCharSourceTest {

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Should read characters sequentially")
        void shouldReadCharactersSequentially() {
            CharSource source = new StringCharSource("abc");

            assertEquals('a', source.read());
            assertEquals('b', source.read());
            assertEquals('c', source.read());
            assertEquals(-1, source.read());
        }

        @Test
        @DisplayName("Should peek without advancing")
        void shouldPeekWithoutAdvancing() {
            CharSource source = new StringCharSource("ab");

            assertEquals('a', source.peek());
            assertEquals('a', source.peek()); // Still 'a'
            assertEquals('a', source.read());  // Now advance
            assertEquals('b', source.peek());
        }

        @Test
        @DisplayName("Should handle empty string")
        void shouldHandleEmptyString() {
            CharSource source = new StringCharSource("");

            assertFalse(source.hasNext());
            assertEquals(-1, source.read());
            assertEquals(-1, source.peek());
        }

        @Test
        @DisplayName("Should handle null string")
        void shouldHandleNullString() {
            CharSource source = new StringCharSource(null);

            assertFalse(source.hasNext());
            assertEquals(-1, source.read());
        }
    }

    @Nested
    @DisplayName("Peek Ahead")
    class PeekAhead {

        @Test
        @DisplayName("Should peek ahead multiple characters")
        void shouldPeekAheadMultipleCharacters() {
            CharSource source = new StringCharSource("hello");

            int[] ahead = source.peekAhead(3);
            assertArrayEquals(new int[]{'h', 'e', 'l'}, ahead);

            // Position shouldn't change
            assertEquals('h', source.read());
        }

        @Test
        @DisplayName("Should handle peek ahead beyond end")
        void shouldHandlePeekAheadBeyondEnd() {
            CharSource source = new StringCharSource("hi");

            int[] ahead = source.peekAhead(5);
            assertArrayEquals(new int[]{'h', 'i', -1, -1, -1}, ahead);
        }

        @Test
        @DisplayName("Should handle zero count peek ahead")
        void shouldHandleZeroCountPeekAhead() {
            CharSource source = new StringCharSource("test");

            int[] ahead = source.peekAhead(0);
            assertEquals(0, ahead.length);
        }
    }

    @Nested
    @DisplayName("Mark and Reset")
    class MarkAndReset {

        @Test
        @DisplayName("Should mark and reset position")
        void shouldMarkAndResetPosition() {
            CharSource source = new StringCharSource("abcd");

            assertEquals('a', source.read());
            source.mark();
            assertEquals('b', source.read());
            assertEquals('c', source.read());

            source.reset();
            assertEquals('b', source.read()); // Back to marked position
        }

        @Test
        @DisplayName("Should throw when reset without mark")
        void shouldThrowWhenResetWithoutMark() {
            CharSource source = new StringCharSource("test");

            assertThrows(IllegalStateException.class, source::reset);
        }
    }

    @Nested
    @DisplayName("Utility Methods")
    class UtilityMethods {

        @Test
        @DisplayName("Should read while predicate is true")
        void shouldReadWhilePredicateIsTrue() {
            CharSource source = new StringCharSource("123abc");

            String digits = source.readWhile(Character::isDigit);
            assertEquals("123", digits);
            assertEquals('a', source.peek());
        }

        @Test
        @DisplayName("Should read until delimiter")
        void shouldReadUntilDelimiter() {
            CharSource source = new StringCharSource("hello,world");

            String word = source.readUntil(',');
            assertEquals("hello", word);
            assertEquals(',', source.peek());
        }

        @Test
        @DisplayName("Should read line with different line endings")
        void shouldReadLineWithDifferentLineEndings() {
            // Unix line ending
            CharSource source1 = new StringCharSource("line1\nline2");
            assertEquals("line1", source1.readLine());
            assertEquals("line2", source1.readLine());

            // Windows line ending
            CharSource source2 = new StringCharSource("line1\r\nline2");
            assertEquals("line1", source2.readLine());
            assertEquals("line2", source2.readLine());
        }

        @Test
        @DisplayName("Should skip whitespace")
        void shouldSkipWhitespace() {
            CharSource source = new StringCharSource("  \t\nabc");

            source.skipWhitespace();
            assertEquals('a', source.peek());
        }

        @Test
        @DisplayName("Should check if starts with prefix")
        void shouldCheckIfStartsWithPrefix() {
            CharSource source = new StringCharSource("hello world");

            assertTrue(source.startsWith("hello"));
            assertTrue(source.startsWith(""));
            assertFalse(source.startsWith("world"));
            assertFalse(source.startsWith("hello world!!!"));
        }

        @Test
        @DisplayName("Should consume matching string")
        void shouldConsumeMatchingString() {
            CharSource source = new StringCharSource("version = 1.0");

            assertTrue(source.consume("version"));
            assertEquals(' ', source.peek());

            assertFalse(source.consume("invalid"));
            assertEquals(' ', source.peek()); // Position unchanged
        }
    }

    @Nested
    @DisplayName("Unicode Support")
    class UnicodeSupport {

        @Test
        @DisplayName("Should handle Unicode characters")
        void shouldHandleUnicodeCharacters() {
            CharSource source = new StringCharSource("Müller 你好");

            assertEquals('M', source.read());
            assertEquals('ü', source.read());
            assertEquals('l', source.read());
            assertEquals('l', source.read());
            assertEquals('e', source.read());
            assertEquals('r', source.read());
            assertEquals(' ', source.read());
            assertEquals('你', source.read());
            assertEquals('好', source.read());
        }

        @Test
        @DisplayName("Should handle emojis")
        void shouldHandleEmojis() {
            // Note: emojis might be surrogate pairs in Java strings
            CharSource source = new StringCharSource("Hello 🌍");

            String greeting = source.readUntil(' ');
            assertEquals("Hello", greeting);
            source.read(); // skip space

            // Emoji handling depends on your requirements
            assertTrue(source.hasNext());
        }
    }

    @Nested
    @DisplayName("Position Tracking")
    class PositionTracking {

        @Test
        @DisplayName("Should track position correctly")
        void shouldTrackPositionCorrectly() {
            CharSource source = new StringCharSource("abcd");

            assertEquals(0, source.position());
            source.read();
            assertEquals(1, source.position());
            source.read();
            assertEquals(2, source.position());
        }

        @Test
        @DisplayName("Should track position with mark/reset")
        void shouldTrackPositionWithMarkReset() {
            CharSource source = new StringCharSource("abcd");

            source.read(); // pos = 1
            source.mark();
            source.read(); // pos = 2

            source.reset(); // back to pos = 1
            assertEquals(1, source.position());
        }
    }
}