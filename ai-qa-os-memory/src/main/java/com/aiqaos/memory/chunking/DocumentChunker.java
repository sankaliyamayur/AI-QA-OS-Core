package com.aiqaos.memory.chunking;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentChunker {

    public List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        // Simple word-based chunker for robust RAG
        String[] words = text.split("\\s+");
        int step = chunkSize - overlap;
        if (step <= 0) step = chunkSize;

        for (int i = 0; i < words.length; i += step) {
            StringBuilder chunkBuilder = new StringBuilder();
            for (int j = i; j < Math.min(i + chunkSize, words.length); j++) {
                chunkBuilder.append(words[j]).append(" ");
            }
            chunks.add(chunkBuilder.toString().trim());
            if (i + chunkSize >= words.length) break;
        }
        return chunks;
    }
}