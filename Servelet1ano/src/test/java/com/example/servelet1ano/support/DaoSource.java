package com.example.servelet1ano.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DaoSource {
    private DaoSource() {}

    public static String read(String relativeToSrcMainJava) throws IOException {
        Path path = Path.of("src/main/java", relativeToSrcMainJava);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
