package com.pharmacy.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class TemplateUtil {

    private TemplateUtil() {
    }

    public static String loadTemplate(String classpathLocation) {
        try (InputStream in = TemplateUtil.class.getClassLoader().getResourceAsStream(classpathLocation)) {
            if (in == null) {
                throw new IllegalStateException("Template not found: " + classpathLocation);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Cannot load template: " + classpathLocation, e);
        }
    }
}

