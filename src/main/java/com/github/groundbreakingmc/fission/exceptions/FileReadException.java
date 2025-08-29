package com.github.groundbreakingmc.fission.exceptions;

public final class FileReadException extends RuntimeException {

    public FileReadException(String message) {
        super(message);
    }

    public FileReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
