package com.ecom.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VietnameseTextEncodingTest {
    @Test void detectsBrokenVietnameseFromLegacySqlImport() {
        assertTrue(VietnameseTextEncoding.isMojibake("MÃ¡y giáº·t gia Ä‘Ã¬nh"));
        assertTrue(VietnameseTextEncoding.isMojibake("Thiáº¿t bá»‹ gia dá»¥ng"));
    }
    @Test void keepsCorrectVietnameseAndPlainLatinUnchanged() {
        assertFalse(VietnameseTextEncoding.isMojibake("Máy giặt gia đình"));
        assertFalse(VietnameseTextEncoding.isMojibake("Điện thoại, Phụ kiện, Tủ lạnh"));
        assertFalse(VietnameseTextEncoding.isMojibake("Laptop HP"));
        assertFalse(VietnameseTextEncoding.isMojibake(null));
    }
}
