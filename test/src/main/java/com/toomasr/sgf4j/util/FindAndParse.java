package com.toomasr.sgf4j.util;

import com.toomasr.sgf4j.Sgf;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FindAndParse {
    public static void main(String[] args) throws IOException {
//        if (args.length == 0) {
//            System.out.println("Please provider folder to start from");
//            System.exit(0);
//        }
        // /Users/like/SGF/app/src/main/res/raw/book123244.sgf
        // /Users/like/SGF/app/src/main/res/raw/alphago_opening_book.sgf
        Path path = Paths.get("/Users/like/SGF/app/src/main/res/raw/sina.sgf");
        System.out.println(path.toAbsolutePath());

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                                             BasicFileAttributes attr) {
                if (attr.isRegularFile() && file.getFileName().toString().toLowerCase().endsWith("sgf")) {
                    long time = System.currentTimeMillis();
                    try {
                        System.out.println("Parsing " + file);
                        Sgf.createFromInputStream(new FileInputStream(file.toFile()));
                        System.out.println("Parsed " + file + " " + (System.currentTimeMillis() - time));
                    } catch (Exception e) {
                        System.out.format("Parsing %s\n", file);
                        e.printStackTrace();
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.format("Visiting: %s\n", dir);
                return super.preVisitDirectory(dir, attrs);
            }
        });
    }
}
