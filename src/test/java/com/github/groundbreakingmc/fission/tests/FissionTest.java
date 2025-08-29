
package com.github.groundbreakingmc.fission.tests;

import com.github.groundbreakingmc.fission.Fission;
import com.github.groundbreakingmc.fission.exceptions.FileReadException;
import com.github.groundbreakingmc.fission.source.CharSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Fission Integration Tests")
class FissionTest {

    @TempDir
    Path tempDir;

    private Path testFile;

    @BeforeEach
    void setUp() {
        testFile = tempDir.resolve("test.txt");
    }

    @Nested
    @DisplayName("High-level API")
    class HighLevelAPI {

        @Test
        @DisplayName("Should read string from file")
        void shouldReadStringFromFile() throws IOException {
            String content = "Hello\nWorld\nTest";
            Files.writeString(testFile, content, StandardCharsets.UTF_8);

            String result = Fission.readString(testFile);
            assertEquals(content, result);
        }

        @Test
        @DisplayName("Should read lines from file")
        void shouldReadLinesFromFile() throws IOException {
            Files.writeString(testFile, "Line 1\nLine 2\nLine 3", StandardCharsets.UTF_8);

            List<String> lines = Fission.readLines(testFile);
            assertEquals(List.of("Line 1", "Line 2", "Line 3"), lines);
        }

        @Test
        @DisplayName("Should create CharSource from string")
        void shouldCreateCharSourceFromString() {
            CharSource source = Fission.chars("test content");

            assertInstanceOf(com.github.groundbreakingmc.fission.source.impl.StringCharSource.class, source);
            assertEquals("test content", source.readWhile(ch -> ch != -1));
        }

        @Test
        @DisplayName("Should create CharSource from file")
        void shouldCreateCharSourceFromFile() throws IOException {
            Files.writeString(testFile, "file content", StandardCharsets.UTF_8);

            CharSource source = Fission.chars(testFile);

            assertInstanceOf(com.github.groundbreakingmc.fission.source.impl.FileCharSource.class, source);
            assertEquals("file content", source.readWhile(ch -> ch != -1));
        }

        @Test
        @DisplayName("Should throw FileReadException for non-existent file")
        void shouldThrowFileReadExceptionForNonExistentFile() {
            Path nonExistent = tempDir.resolve("does-not-exist.txt");

            assertThrows(FileReadException.class, () -> Fission.readString(nonExistent));
            assertThrows(FileReadException.class, () -> Fission.readLines(nonExistent));
            assertThrows(FileReadException.class, () -> Fission.chars(nonExistent));
        }
    }

    @Nested
    @DisplayName("Charset Support")
    class CharsetSupport {

        @Test
        @DisplayName("Should read with custom charset")
        void shouldReadWithCustomCharset() throws IOException {
            String content = "Héllo Wörld";
            Files.writeString(testFile, content, StandardCharsets.ISO_8859_1);

            String result = Fission.readString(testFile, StandardCharsets.ISO_8859_1);
            assertEquals(content, result);
        }

        @Test
        @DisplayName("Should create CharSource with custom charset")
        void shouldCreateCharSourceWithCustomCharset() throws IOException {
            String content = "Custom encoding test";
            Files.writeString(testFile, content, StandardCharsets.ISO_8859_1);

            CharSource source = Fission.chars(testFile, StandardCharsets.ISO_8859_1);
            assertEquals(content, source.readWhile(ch -> ch != -1));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should wrap IOException in FileReadException")
        void shouldWrapIOExceptionInFileReadException() {
            Path nonExistent = tempDir.resolve("non-existent.txt");

            FileReadException exception = assertThrows(FileReadException.class,
                    () -> Fission.readString(nonExistent));

            assertInstanceOf(FileReadException.class, exception);
            assertTrue(exception.getMessage().startsWith("File not found:"));
        }

        @Test
        @DisplayName("Should handle empty file gracefully")
        void shouldHandleEmptyFileGracefully() throws IOException {
            Files.writeString(testFile, "", StandardCharsets.UTF_8);

            assertEquals("", Fission.readString(testFile));
            assertEquals(List.of(), Fission.readLines(testFile));

            CharSource source = Fission.chars(testFile);
            assertFalse(source.hasNext());
        }
    }

    @Nested
    @DisplayName("TOML Parser Integration")
    class TomlParserIntegration {

        @Test
        @DisplayName("Should work with TOML-like content")
        void shouldWorkWithTomlLikeContent() throws IOException {
            String tomlContent = """
                    # This is a TOML comment
                    title = "My Application"
                                    
                    [database]
                    server = "192.168.1.1"
                    ports = [ 8001, 8001, 8002 ]
                    """;

            Files.writeString(testFile, tomlContent, StandardCharsets.UTF_8);

            CharSource source = Fission.chars(testFile);

            // Skip comment
            source.consume("#");
            String comment = source.readUntil('\n');
            assertEquals(" This is a TOML comment", comment);

            source.readLine(); // consume newline

            // Read key-value pair
            String key = source.readWhile(ch -> ch != ' ');
            assertEquals("title", key);

            source.skipWhitespace();
            source.consume("=");
            source.skipWhitespace();

            assertTrue(source.startsWith("\""));
        }
    }
}