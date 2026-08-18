package com.aicouncil.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DotenvEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> osProps = new HashMap<>();
        syncOsEnv(osProps, "AI_MODE", "ai.mode");
        syncOsEnv(osProps, "ai.mode", "ai.mode");
        syncOsEnv(osProps, "GEMINI_API_KEY", "gemini.api.key");
        syncOsEnv(osProps, "OPENROUTER_API_KEY", "openrouter.api.key");
        syncOsEnv(osProps, "OPENROUTER_MODEL", "openrouter.model");
        syncOsEnv(osProps, "ANTHROPIC_API_KEY", "anthropic.api.key");
        syncOsEnv(osProps, "GROQ_API_KEY", "groq.api.key");
        syncOsEnv(osProps, "GROQ_MODEL", "groq.model");
        syncOsEnv(osProps, "MONGODB_URI", "spring.data.mongodb.uri");

        File envFile = findDotenvFile();
        if (envFile != null) {
            try {
                String canonicalPath = envFile.getCanonicalPath();
                File parentDir = envFile.getParentFile();
                String dirPath = parentDir != null ? parentDir.getCanonicalPath() : ".";

                Dotenv dotenv = Dotenv.configure()
                        .directory(dirPath)
                        .ignoreIfMissing()
                        .load();

                dotenv.entries().forEach(entry -> {
                    String key = entry.getKey();
                    String val = entry.getValue();

                    // OS Environment variables take higher priority than .env file
                    if (System.getenv(key) == null) {
                        System.setProperty(key, val);
                        osProps.put(key, val);

                        // Also map property aliases for Spring Boot @Value binding
                        if ("AI_MODE".equalsIgnoreCase(key)) {
                            osProps.put("ai.mode", val);
                            System.setProperty("ai.mode", val);
                        } else if ("GEMINI_API_KEY".equalsIgnoreCase(key)) {
                            osProps.put("gemini.api.key", val);
                            System.setProperty("gemini.api.key", val);
                        } else if ("OPENROUTER_API_KEY".equalsIgnoreCase(key)) {
                            osProps.put("openrouter.api.key", val);
                            System.setProperty("openrouter.api.key", val);
                        } else if ("OPENROUTER_MODEL".equalsIgnoreCase(key)) {
                            osProps.put("openrouter.model", val);
                            System.setProperty("openrouter.model", val);
                        } else if ("ANTHROPIC_API_KEY".equalsIgnoreCase(key)) {
                            osProps.put("anthropic.api.key", val);
                            System.setProperty("anthropic.api.key", val);
                        } else if ("GROQ_API_KEY".equalsIgnoreCase(key)) {
                            osProps.put("groq.api.key", val);
                            System.setProperty("groq.api.key", val);
                        } else if ("GROQ_MODEL".equalsIgnoreCase(key)) {
                            osProps.put("groq.model", val);
                            System.setProperty("groq.model", val);
                        } else if ("MONGODB_URI".equalsIgnoreCase(key)) {
                            osProps.put("spring.data.mongodb.uri", val);
                            System.setProperty("spring.data.mongodb.uri", val);
                        }
                    }
                });

                if (!osProps.isEmpty()) {
                    log.info("DotenvEnvironmentPostProcessor: Successfully injected environment properties from {}", canonicalPath);
                }
            } catch (Exception e) {
                log.warn("DotenvEnvironmentPostProcessor notice: .env file loading skipped: {}", e.getMessage());
            }
        }

        if (!osProps.isEmpty()) {
            if (environment.getPropertySources().contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                environment.getPropertySources().addAfter(
                        StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        new MapPropertySource("customEnvProperties", osProps)
                );
            } else {
                environment.getPropertySources().addFirst(new MapPropertySource("customEnvProperties", osProps));
            }
        }
    }

    private void syncOsEnv(Map<String, Object> map, String envName, String springPropName) {
        String val = System.getenv(envName);
        if (val != null && !val.isBlank()) {
            System.setProperty(springPropName, val.trim());
            map.put(springPropName, val.trim());
        }
    }

    private File findDotenvFile() {
        File[] candidates = {
                new File(".env"),
                new File("../.env"),
                new File("../../.env")
        };
        for (File f : candidates) {
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }
}
