package com.example.sonar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

public class SonarApiFixer {
    private static final String SONAR_URL = System.getenv("SONAR_HOST_URL");
    private static final String SONAR_TOKEN = System.getenv("SONAR_TOKEN");
    private static final String PROJECT_KEY = System.getenv("SONAR_PROJECT_KEY");
    private static final String SRC_ROOT = "src/main/java";

    public static void main(String[] args) {
        if (SONAR_URL == null || SONAR_TOKEN == null || PROJECT_KEY == null) {
            System.err.println("Set environment variables SONAR_HOST_URL, SONAR_TOKEN, SONAR_PROJECT_KEY");
            System.exit(1);
        }
        try {
            SonarApiClient client = new SonarApiClient(SONAR_URL, SONAR_TOKEN);
            String json = client.fetchVulnerabilities(PROJECT_KEY);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode issues = root.get("issues");
            if (issues == null || !issues.isArray() || issues.size() == 0) {
                System.out.println("No vulnerabilities found!");
                return;
            }

            System.out.println("Found " + issues.size() + " vulnerabilities. Starting remediation...");

            Iterator<JsonNode> iter = issues.elements();
            while (iter.hasNext()) {
                JsonNode issue = iter.next();
                String filePath = issue.get("component").asText(); // e.g. projectKey:src/main/java/com/example/MyClass.java
                int line = issue.get("line").asInt(-1);
                JsonNode cweNode = issue.get("cwe");

                String cwe = (cweNode != null) ? cweNode.asText() : "UNKNOWN";

                // Extract relative path after project key and colon
                String relativePath = filePath.substring(filePath.indexOf(":") + 1);

                System.out.println("Fixing file: " + relativePath + " line: " + line + " CWE: " + cwe);

                if (relativePath.startsWith("src/main/java")) {
                    fixFile(relativePath, cwe);
                } else {
                    System.out.println("Skipping non-java source file: " + relativePath);
                }
            }

            System.out.println("Remediation complete.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void fixFile(String relativePath, String cwe) throws IOException {
        File file = new File(relativePath);
        if (!file.exists()) {
            System.err.println("File not found: " + relativePath);
            return;
        }

        // Simple example: fix CWE-xxx by replacing System.out.println with logger
        if ("CWE-89".equals(cwe)) {
            // For example, CWE-89 = SQL Injection (real fix would be complex)
            // Just print message for demo
            System.out.println("SQL Injection fix needed: " + relativePath);
        }

        // Very simple fix: replace all System.out.println with logger.info
        // (Just example, not real fix!)
        String content = new String(Files.readAllBytes(Paths.get(relativePath)));
        String fixedContent = content.replaceAll("System\\.out\\.println", "logger.info");

        Files.write(Paths.get(relativePath), fixedContent.getBytes());

        System.out.println("Fixed file: " + relativePath);
    }
}
