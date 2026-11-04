package com.ecom.ai;
import com.ecom.model.Product;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Component
public class QdrantGateway {
    private final String baseUrl,collection;
    private final EmbeddingService embed;
    private final ObjectMapper mapper;
    private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    public QdrantGateway(@Value("${qdrant.url}") String baseUrl,@Value("${qdrant.collection}") String collection,
                         EmbeddingService embed,ObjectMapper mapper){
        this.baseUrl=baseUrl.replaceAll("/$","");this.collection=URLEncoder.encode(collection,StandardCharsets.UTF_8);
        this.embed=embed;this.mapper=mapper;
    }
    private JsonNode call(String method,String path,Object body){
        try {
            String json=body==null?"":mapper.writeValueAsString(body);
            HttpRequest.BodyPublisher data=body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(json);
            HttpRequest request=HttpRequest.newBuilder(URI.create(baseUrl+path))
                    .timeout(Duration.ofSeconds(10)).header("Content-Type","application/json")
                    .method(method,data).build();
            HttpResponse<String> response=http.send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()/100!=2)
                throw new IllegalStateException("Qdrant HTTP "+response.statusCode()+": "+response.body());
            return mapper.readTree(response.body());
        }catch(Exception e){throw new IllegalStateException("Không truy cập được Qdrant (cổng 6333)",e);}
    }
    private void resetCollection() {
        try {
            HttpRequest request=HttpRequest.newBuilder(URI.create(baseUrl+"/collections/"+collection))
                    .timeout(Duration.ofSeconds(10)).DELETE().build();
            HttpResponse<String> response=http.send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()!=404 && response.statusCode()/100!=2)
                throw new IllegalStateException("Qdrant xóa collection: HTTP "+response.statusCode());
        } catch (Exception e) {throw new IllegalStateException("Không làm mới được dữ liệu Qdrant",e);}
    }
    public int sync(List<Product> products){
        resetCollection();
        call("PUT","/collections/"+collection,Map.of("vectors",Map.of("size",embed.dimensions(),"distance","Cosine")));
        // Full reindex removes inactive/deleted products and avoids stale Qdrant vectors.
        if(products.isEmpty())return 0;
        List<Map<String,Object>> points=new ArrayList<>();
        for(Product p:products){
            String text=p.name()+". "+p.categoryName()+". "+p.description();
            points.add(Map.of("id",p.id(),"vector",embed.embed(text),"payload",Map.of("product_id",p.id())));
        }
        for(int i=0;i<points.size();i+=50){
            call("PUT","/collections/"+collection+"/points?wait=true",
                    Map.of("points",points.subList(i,Math.min(i+50,points.size()))));
        }
        return points.size();
    }
    public List<Long> search(String question,int limit){
        JsonNode root=call("POST","/collections/"+collection+"/points/query",
                Map.of("query",embed.embed(question),"limit",limit,"with_payload",true));
        List<Long> ids=new ArrayList<>();
        JsonNode matches=root.path("result").path("points");
        if(matches.isArray())for(JsonNode item:matches){
            long id=item.path("payload").path("product_id").asLong(0);
            if(id>0)ids.add(id);
        }
        return ids;
    }
    public String provider(){return embed.provider();}
}
