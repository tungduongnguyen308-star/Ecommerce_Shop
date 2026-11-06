package com.ecom.config;

/** Detects the common UTF-8 -> Windows-1252/Latin-1 mojibake seen in old SQL imports. */
public final class VietnameseTextEncoding {
    private VietnameseTextEncoding() { }

    public static boolean isMojibake(String value) {
        if (value == null || value.isBlank()) return false;
        // A valid Vietnamese seed product never contains these mojibake markers.
        return value.contains("Ã") || value.contains("Â") || value.contains("Ä")
                || value.contains("áº") || value.contains("á»")
                || value.contains("â€") || value.contains("�");
    }
}
