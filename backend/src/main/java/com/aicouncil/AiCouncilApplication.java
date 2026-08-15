package com.aicouncil;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;

@SpringBootApplication
public class AiCouncilApplication {

    private static final Logger log = LoggerFactory.getLogger(AiCouncilApplication.class);

    public static void main(String[] args) {
        loadDotenvBeforeSpring();
        ConfigurableApplicationContext context = SpringApplication.run(AiCouncilApplication.class, args);

        String resolvedAiMode = context.getEnvironment().getProperty("ai.mode", "MOCK");
        log.info("==================================================");
        log.info("AI Council Engine Mode: {}", resolvedAiMode.toUpperCase());
        log.info("==================================================");
    }

    private static void loadDotenvBeforeSpring() {
        try {
            File[] candidates = { new File(".env"), new File("../.env"), new File("../../.env") };
            File found = null;
            for (File f : candidates) {
                if (f.exists()) {
                    found = f;
                    break;
                }
            }

            if (found != null) {
                File parent = found.getParentFile();
                String dir = parent != null ? parent.getCanonicalPath() : ".";
                Dotenv dotenv = Dotenv.configure().directory(dir).ignoreIfMissing().load();

                dotenv.entries().forEach(entry -> {
                    String key = entry.getKey();
                    String val = entry.getValue();

                    // System environment variables take higher precedence than .env file
                    if (System.getenv(key) == null) {
                        System.setProperty(key, val);
                        if ("AI_MODE".equalsIgnoreCase(key)) System.setProperty("ai.mode", val);
                        if ("GEMINI_API_KEY".equalsIgnoreCase(key)) System.setProperty("gemini.api.key", val);
                        if ("OPENROUTER_API_KEY".equalsIgnoreCase(key)) System.setProperty("openrouter.api.key", val);
                        if ("OPENROUTER_MODEL".equalsIgnoreCase(key)) System.setProperty("openrouter.model", val);
                        if ("ANTHROPIC_API_KEY".equalsIgnoreCase(key)) System.setProperty("anthropic.api.key", val);
                        if ("GROQ_API_KEY".equalsIgnoreCase(key)) System.setProperty("groq.api.key", val);
                        if ("GROQ_MODEL".equalsIgnoreCase(key)) System.setProperty("groq.model", val);
                        if ("MONGODB_URI".equalsIgnoreCase(key)) System.setProperty("spring.data.mongodb.uri", val);
                    }
                });
            }
        } catch (Exception e) {
            log.warn("Notice: Pre-loading .env into System properties skipped: {}", e.getMessage());
        }
    }
}
