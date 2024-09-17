package io.teamchallenge.project.bazario;

import java.nio.file.Files;
import java.nio.file.Path;

public interface Util {

    static void loadPropertiesIntoEnv(String filename) {
        final var envPath = Path.of(filename);

        if (!Files.exists(envPath)) {
            return;
        }

        try (final var reader = Files.newBufferedReader(envPath)) {
            while (reader.ready()) {
                final var line = reader.readLine();
                if (line.startsWith("#")) {
                    continue;
                }

                final var delimiter = line.indexOf('=');
                if (delimiter == -1) {
                    continue;
                }

                final var key = line.substring(0, delimiter).trim();
                final var value = line.substring(delimiter + 1).trim();

                if (key.isEmpty()) {
                    continue;
                }

                System.setProperty(key, value);
            }

        } catch (Exception e) {
            //todo: add logger here
        }
    }
}
