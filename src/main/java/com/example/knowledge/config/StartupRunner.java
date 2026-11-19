package com.example.knowledge.config;

import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.knowledge.document.DocumentRepository;

/** One-time checks and housekeeping when the application starts. */
@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final AppProperties props;
    private final DocumentRepository documents;
    private final String apiKey;

    public StartupRunner(AppProperties props,
                         DocumentRepository documents,
                         @Value("${spring.ai.openai.api-key}") String apiKey) {
        this.props = props;
        this.documents = documents;
        this.apiKey = apiKey;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Files.createDirectories(Path.of(props.storage().dir()));

        if (apiKey == null || apiKey.isBlank() || apiKey.equals("not-set")) {
            log.warn("OPENAI_API_KEY is not set. Login works, but uploads will fail to embed and questions will fail.");
        }

        // Jobs that were running when the app stopped can never finish, so don't leave them "PROCESSING".
        // (Assumes a single app instance.)
        int recovered = documents.failInterruptedJobs();
        if (recovered > 0) {
            log.warn("Marked {} interrupted document(s) as FAILED; users can re-upload them.", recovered);
        }
    }
}
