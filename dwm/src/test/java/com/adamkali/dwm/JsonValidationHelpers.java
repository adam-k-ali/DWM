package com.adamkali.dwm;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class JsonValidationHelpers {
    public static boolean validateJsonFiles(String schemaPath, String jsonPath) {
        try {
            String schemaContent = new String(Files.readAllBytes(Paths.get(schemaPath)));
            JSONObject rawSchema = new JSONObject(new JSONTokener(schemaContent));
            Schema schema = SchemaLoader.load(rawSchema);

            try (Stream<Path> paths = Files.walk(Paths.get(jsonPath)).filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".json"))) {
                for (Path path : (Iterable<Path>) paths::iterator) {
                    if (!validateJsonFile(schema, path)) {
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private static void unpackValidationException(ValidationException e) {
        System.out.println(e.getMessage());

        e.getCausingExceptions().forEach(ex -> {
            if (ex.getCausingExceptions().isEmpty()) {
                System.out.println(ex.getMessage());
            } else {
                unpackValidationException(ex);
            }
        });
    }

    private static boolean validateJsonFile(Schema schema, Path path) {
        try {
            System.out.println("Validating: " + path);
            String content = new String(Files.readAllBytes(path));
            JSONObject json = new JSONObject(new JSONTokener(content));
            schema.validate(json);
            System.out.println("Validation successful for: " + path);
        } catch (ValidationException e) {
            unpackValidationException(e);
            return false;
        } catch (IOException e) {
            System.err.println("Validation failed for: " + path);
            return false;
        }
        return true;
    }
}
