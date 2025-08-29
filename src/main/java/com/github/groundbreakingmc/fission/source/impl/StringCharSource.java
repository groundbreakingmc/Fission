package com.github.groundbreakingmc.fission.source.impl;

import com.github.groundbreakingmc.fission.source.CharSource;
import org.jetbrains.annotations.Nullable;

public final class StringCharSource implements CharSource {

    private final String str;
    private final int length;
    private int pos = 0;
    private int markedPos = -1;

    public StringCharSource(@Nullable String str) {
        this.str = str != null ? str : "";
        this.length = this.str.length();
    }

    @Override
    public int read() {
        return this.pos < this.length ? this.str.charAt(this.pos++) : -1;
    }

    @Override
    public int peek() {
        return this.pos < this.length ? this.str.charAt(this.pos) : -1;
    }

    @Override
    public int[] peekAhead(int count) {
        if (count <= 0) return new int[0];
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            int idx = this.pos + i;
            result[i] = idx < this.length ? this.str.charAt(idx) : -1;
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
