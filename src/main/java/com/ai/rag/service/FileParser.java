package com.ai.rag.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class FileParser {

    private final Tika tika = new Tika();

    public String parseFile(File file) throws IOException, TikaException {
        return tika.parseToString(file);
    }
}
