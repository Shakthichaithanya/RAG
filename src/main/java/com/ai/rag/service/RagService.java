package com.ai.rag.service;

import org.apache.tika.exception.TikaException;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.print.Doc;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final OllamaChatModel chatModel;



    private final MariaDBVectorStore vectorStore;
    private final FileParser fileParser;

    public RagService(OllamaChatModel chatModel, MariaDBVectorStore vectorStore, FileParser fileParser) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.fileParser = fileParser;
    }

    // Add raw string
    public void addDocument(String content) {
        Document doc = new Document(content);
        vectorStore.add(List.of(doc));
    }

    // Add file
    public void addFile(File file) throws IOException, TikaException {
        String content = fileParser.parseFile(file);

        // optional: split large documents into chunks
        List<String> chunks = splitText(content, 500); // 500 tokens/words per chunk
        List<Document> docs = chunks.stream()
                .map(Document::new)
                .toList();

        vectorStore.add(docs);
    }

    // Ask question with RAG
    public String ask(String question) {
        List<Document> results = vectorStore.similaritySearch(question);

        String context = results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        Prompt prompt = new Prompt("Context:\n" + context + "\n\nQuestion: " + question);

        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    public Flux<String> askStream(String question) {
        List<Document> results = vectorStore.similaritySearch(question);

        String context = results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        Prompt prompt = new Prompt("Context:\n" + context + "\n\nQuestion: " + question);

        return chatModel.stream(prompt).concatWithValues().map(ch -> ch.getResult().getOutput().getText());

    }

    private List<String> splitText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        String[] words = text.split("\\s+");

        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() + word.length() > chunkSize) {
                chunks.add(current.toString());
                current = new StringBuilder();
            }
            current.append(word).append(" ");
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }
}

