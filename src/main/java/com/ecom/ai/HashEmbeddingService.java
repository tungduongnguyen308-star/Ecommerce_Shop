package com.ecom.ai;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.util.Locale;
@Service
@ConditionalOnProperty(name="qdrant.embedding-provider",havingValue="hash",matchIfMissing=true)
public class HashEmbeddingService implements EmbeddingService {
    public static final int SIZE=256;
    @Override public int dimensions(){return SIZE;}
    @Override public String provider(){return "hash-demo";}
    @Override public float[] embed(String input){
        String normalized=Normalizer.normalize(input==null?"":input.toLowerCase(Locale.ROOT),Normalizer.Form.NFD)
                .replaceAll("\\p{M}+","").replace('đ','d');
        String[] tokens=normalized.split("[^a-z0-9]+");
        float[] v=new float[SIZE];
        for(String token:tokens){
            if(token.length()<2)continue;
            add(v,token,1.0f);
            // Trigrams improve fuzzy matching for product names; not a true semantic model.
            if(token.length()>=5)for(int i=0;i<=token.length()-3;i++)add(v,"#"+token.substring(i,i+3),0.25f);
        }
        double norm=0;for(float f:v)norm+=f*f;
        if(norm>0){float d=(float)Math.sqrt(norm);for(int i=0;i<v.length;i++)v[i]/=d;}
        return v;
    }
    private static void add(float[] v,String s,float w){int h=s.hashCode();int index=(h&0x7fffffff)%SIZE;v[index]+=((h>>>16)&1)==0?w:-w;}
}
