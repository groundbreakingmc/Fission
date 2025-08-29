
package com.github.groundbreakingmc.fission.tests.impl;

import com.github.groundbreakingmc.fission.source.CharSource;
import com.github.groundbreakingmc.fission.source.impl.FileCharSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileCharSource Tests")
class FileCharSourceTest {

    @TempDir
    Path tempDir;

    private Path testFile;

    @BeforeEach
    void setUp() throws IOException {
        testFile = tempDir.resolve("test.txt");
    }

    @Nested
    @DisplayName("File Reading")
    class FileReading {

        @Test
        @DisplayName("Should read simple file")
        void shouldReadSimpleFile() throws IOException {
            Files.writeString(testFile, "Hello World", StandardCharsets.UTF_8);

            CharSource source = new FileCharSource(testFile, StandardCharsets.UTF_8);
            StringBuilder content = new StringBuilder();
            while (source.hasNext()) {
                content.append((char) source.read());
            }

            assertEquals("Hello World", content.toString());
        }

        @Test
        @DisplayName("Should handle empty file")
        void shouldHandleEmptyFile() throws IOException {
            Files.writeString(testFile, "", StandardCharsets.UTF_8);

            CharSource source = new FileCharSource(testFile, StandardCharsets.UTF_8);
            assertFalse(source.hasNext());
            assertEquals(-1, source.read());
        }

        @Test
        @DisplayName("Should handle file with different encoding")
        void shouldHandleFileWithDifferentEncoding() throws IOException {
            String content = "Müller";
            Files.writeString(testFile, content, StandardCharsets.ISO_8859_1);

            CharSource source = new FileCharSource(testFile, StandardCharsets.ISO_8859_1);
            String result = source.readWhile(ch -> ch != -1);
            assertEquals(content, result);
        }

        @Test
        @DisplayName("Should throw for non-existent file")
        void shouldThrowForNonExistentFile() {
            Path nonExistent = tempDir.resolve("does-not-exist.txt");

            assertThrows(IOException.class, () ->
                    new FileCharSource(nonExistent, StandardCharsets.UTF_8));
        }
    }

    @Nested
    @DisplayName("Large File Handling")
    class LargeFileHandling {

        @Test
        @DisplayName("Should handle reasonably large file")
        void shouldHandleReasonablyLargeFile() throws IOException {
            // Create a file with repeated content
            StringBuilder largeContent = new StringBuilder();
            String pattern = "Line ";
            for (int i = 0; i < 10000; i++) {
                largeContent.append(pattern).append(i).append("\n");
            }
            Files.writeString(testFile, largeContent.toString(), StandardCharsets.UTF_8);

            CharSource source = new FileCharSource(testFile, StandardCharsets.UTF_8);

            // Read first few lines
            String firstLine = source.readUntil('\n');
            assertEquals("Line 0", firstLine);
            source.read(); // consume \n

            String secondLine = source.readUntil('\n');
            assertEquals("Line 1", secondLine);
        }
    }

    @Nested
    @DisplayName("Mark and Reset with Files")
    class MarkAndResetWithFiles {

        @Test
        @DisplayName("Should support mark and reset with files")
        void shouldSupportMarkAndResetWithFiles() throws IOException {
            Files.writeString(testFile, "abcdefgh", StandardCharsets.UTF_8);

            CharSource source = new FileCharSource(testFile, StandardCharsets.UTF_8);

            assertEquals('a', source.read());
            source.mark();
            assertEquals('b', source.read());
            assertEquals('c', source.read());

            source.reset();
            assertEquals('b', source.read());
        }
    }
}
