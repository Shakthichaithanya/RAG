package com.ai.rag.controller;

import com.ai.rag.service.RagService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/add")
    public String addDoc(@RequestBody String content) {
        ragService.addDocument(content);
        return "Document added!";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        File convFile = File.createTempFile("upload-", file.getOriginalFilename());
        file.transferTo(convFile);

        ragService.addFile(convFile);

        return "File uploaded and processed!";
    }

    @PostMapping("/ask")
    public String ask(@RequestBody String question) {
        return ragService.ask(question);
    }

    @PostMapping(value = "/ask/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askStream(@RequestBody String question) {
        return ragService.askStream(question).delayElements(Duration.ofMillis(10));
    }
}

