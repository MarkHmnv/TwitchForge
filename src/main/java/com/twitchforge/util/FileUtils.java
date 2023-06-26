package com.twitchforge.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static List<String> read(String filePath) {
        List<String> contents = new ArrayList<>();
        try {
            contents = Files.readAllLines(Path.of(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }
}