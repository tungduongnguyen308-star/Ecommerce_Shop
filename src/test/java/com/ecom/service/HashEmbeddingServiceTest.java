package com.ecom.service;

import com.ecom.ai.HashEmbeddingService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashEmbeddingServiceTest {
    @Test void makesDeterministicNormalizedVector() {
        var service=new HashEmbeddingService();
        float[] a=service.embed("laptop HỌC TẬP");
        float[] b=service.embed("laptop hoc tap");
        assertEquals(256,a.length);
        assertArrayEquals(a,b);
    }
    @Test void emptyQueryReturnsZeroVector(){
        var service=new HashEmbeddingService();
        float[] data=service.embed("");
        for (float value : data) assertEquals(0.0f,value);
    }
}
