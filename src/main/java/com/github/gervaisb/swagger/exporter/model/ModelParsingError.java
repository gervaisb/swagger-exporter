package com.github.gervaisb.swagger.exporter.model;

import java.io.File;
import java.util.List;

public class ModelParsingError extends RuntimeException {
    private final List<String> messages;
    private final File input;
    ModelParsingError(File input, List<String> messages) {
        this.messages = messages;
        this.input = input;
    }

    @Override
    public String getMessage() {
        StringBuilder errors = new StringBuilder();
        for (String message : messages) {
            errors.append("  - ").append(message).append("\n");
        }
        return "Parsing of " +
                input + "failed dur to the following errors:\n" +
                errors;
    }
}
