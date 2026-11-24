package com.ecom.ai;
public interface EmbeddingService {
    float[] embed(String text);
    int dimensions();
    String provider();
}
