package com.github.groundbreakingmc.fission.benchmarks;

import com.github.groundbreakingmc.fission.Fission;
import com.github.groundbreakingmc.fission.source.CharSource;
import com.github.groundbreakingmc.fission.source.impl.FileCharSource;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark class for comparing file reading performance between Fission library
 * and standard Java I/O approaches.
 * <p>
 * <h2>Benchmark Comparison Matrix:</h2>
 *
 * <table border="1">
 *   <tr>
 *     <th>Benchmark Method</th>
 *     <th>Competing Method</th>
 *     <th>Use Case</th>
 *     <th>Performance Summary</th>
 *   </tr>
 *   <tr>
 *     <td>{@link #fissionFileCharSource(Blackhole)}</td>
 *     <td>{@link #standardBufferedReader(Blackhole)}</td>
 *     <td>Low-level character-by-character reading</td>
 *     <td><b>Fission wins significantly</b> - optimized char-by-char reading</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #fissionHighLevelRead(Blackhole)}</td>
 *     <td>{@link #standardFilesReadString(Blackhole)}</td>
 *     <td>Reading entire file into String</td>
 *     <td><b>Standard Java wins</b> - Files.readString() is fastest for bulk reading</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #fissionReadLines(Blackhole)}</td>
 *     <td>{@link #standardBufferedReaderLines(Blackhole)}</td>
 *     <td>Line-by-line reading with processing</td>
 *     <td><b>Standard Java wins for large files</b> - BufferedReader.readLine() more efficient</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #fissionStreamingParser(Blackhole)}</td>
 *     <td>N/A (Unique Fission feature)</td>
 *     <td>Complex streaming parsing with word/line counting</td>
 *     <td><b>Fission unique capability</b> - Advanced parsing not available in standard API</td>
 *   </tr>
 * </table>
 * <p>
 * <h2>Key Findings:</h2>
 * <ul>
 *   <li>Use {@code Files.readString()} for simple full-file reading</li>
 *   <li>Use Fission {@code FileCharSource} for low-level character processing</li>
 *   <li>Use Fission for complex parsing scenarios requiring fine-grained control</li>
 *   <li>Use standard {@code BufferedReader.readLine()} for simple line processing</li>
 * </ul>
 * <p>
 * <h2>Test Parameters:</h2>
 * <ul>
 *   <li>File sizes: 1KB, 10KB, 100KB</li>
 *   <li>Encoding: UTF-8</li>
 *   <li>Content: Repeated lines of text</li>
 * </ul>
 *
 * @see com.github.groundbreakingmc.fission.Fission
 * @see com.github.groundbreakingmc.fission.source.CharSource
 * @see com.github.groundbreakingmc.fission.source.impl.FileCharSource
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(2)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
public class FileReadingBenchmark {

    @Param({"1024", "10240", "102400"}) // 1KB, 10KB, 100KB
    private int fileSize;

    private Path testFile;

    @Setup
    public void setup() throws IOException {
        testFile = Files.createTempFile("benchmark", ".txt");

        StringBuilder sb = new StringBuilder(fileSize);
        String line = "This is a test line with some content that makes it reasonably long\n";

        while (sb.length() < fileSize) {
            sb.append(line);
        }

        String testContent = sb.toString();
        Files.writeString(testFile, testContent, StandardCharsets.UTF_8);
    }

    @TearDown
    public void tearDown() throws IOException {
        Files.deleteIfExists(testFile);
    }

    @Benchmark
    public void fissionFileCharSource(Blackhole bh) throws IOException {
        CharSource source = new FileCharSource(testFile, StandardCharsets.UTF_8);

        while (source.hasNext()) {
            bh.consume(source.read());
        }
    }

    @Benchmark
    public void fissionHighLevelRead(Blackhole bh) {
        String content = Fission.readString(testFile);
        bh.consume(content);
    }

    @Benchmark
    public void standardBufferedReader(Blackhole bh) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(testFile.toFile(), StandardCharsets.UTF_8))) {
            int ch;
            while ((ch = reader.read()) != -1) {
                bh.consume(ch);
            }
        }
    }

    @Benchmark
    public void standardFilesReadString(Blackhole bh) throws IOException {
        String content = Files.readString(testFile, StandardCharsets.UTF_8);
        bh.consume(content);
    }

    @Benchmark
    public void standardBufferedReaderLines(Blackhole bh) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(testFile.toFile(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                bh.consume(line);
            }
        }
    }

    @Benchmark
    public void fissionReadLines(Blackhole bh) throws IOException {
        CharSource source = new FileCharSource(testFile, StandardCharsets.UTF_8);

        while (source.hasNext()) {
            String line = source.readLine();
            if (!line.isEmpty()) {
                bh.consume(line);
            }
        }
    }

    // Memory usage test
    @Benchmark
    public void fissionStreamingParser(Blackhole bh) throws IOException {
        CharSource source = new FileCharSource(testFile, StandardCharsets.UTF_8);

        int lineCount = 0;
        int wordCount = 0;

        while (source.hasNext()) {
            String word = source.readWhile(Character::isLetter);
            if (!word.isEmpty()) {
                wordCount++;
                bh.consume(word);
            }

            int ch = source.read();
            if (ch == '\n') {
                lineCount++;
            }
        }

        bh.consume(lineCount);
        bh.consume(wordCount);
    }
}
