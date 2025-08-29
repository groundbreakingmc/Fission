package com.github.groundbreakingmc.fission.source.impl;

import com.github.groundbreakingmc.fission.source.CharSource;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FileCharSource implements CharSource {

    private final CharBuffer buffer;
    private final int length;
    private int pos = 0;
    private int markedPos = -1;

    public FileCharSource(@NotNull Path path, @NotNull Charset charset) throws IOException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found: " + path);
        }

        long size = Files.size(path);
        if (size > Integer.MAX_VALUE) {
            throw new IOException("File too large: " + size);
        }

        if (size == 0) {
            this.buffer = CharBuffer.allocate(0);
            this.length = 0;
        } else {
            try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
                ByteBuffer byteBuffer = ByteBuffer.allocate((int) size);
                channel.read(byteBuffer);
                byteBuffer.flip();
                this.buffer = charset.decode(byteBuffer);
                this.length = this.buffer.limit();
            }
        }
    }

    @Override
    public int read() {
        return this.pos < this.length ? this.buffer.get(this.pos++) : -1;
    }

    @Override
    public int peek() {
        return this.pos < this.length ? this.buffer.get(this.pos) : -1;
    }

    @Override
    public int[] peekAhead(int count) {
        if (count <= 0) return new int[0];
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            int idx = this.pos + i;
            result[i] = idx < this.length ? this.buffer.get(idx) : -1;
        }
        return result;
    }

    @Override
    public boolean hasNext() {
        return this.pos < this.length;
    }

    @Override
    public long position() {
        return this.pos;
    }

    @Override
    public void mark() {
        this.markedPos = this.pos;
    }

    @Override
    public void reset() {
        if (this.markedPos < 0) throw new IllegalStateException("No mark set");
        this.pos = this.markedPos;
    }

    @Override
    public void commit() {
        if (this.markedPos < 0) throw new IllegalStateException("No mark set");
        this.markedPos = -1; // Clear the mark
    }
}
