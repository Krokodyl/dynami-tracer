package resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Resource Manager
 */
public class ResIO {
    
    private ResType type;
    private List<Object> data = new ArrayList<>();
    private Iterator<Object> iterator;
    
    public ResIO(ResType type) {
        this.type = type;
    }
    
    public static ResIO getTextResource(String filename) {
        ResIO res = new ResIO(ResType.TEXT_FILE);
        res.loadResource(filename);
        return res;
    }
    
    public void loadResource(String filename) {
        if (type==ResType.TEXT_FILE) loadTextResource(filename);
    }

    private void loadTextResource(String filename) {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            Objects.requireNonNull(ResIO.class.getClassLoader().getResourceAsStream(filename)), StandardCharsets.UTF_8));
            String line = br.readLine();
            while (line!=null) {
                data.add(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        iterator = data.iterator();
    }
    
    public boolean hasNext() {
        return iterator.hasNext();
    } 
    
    public Object next() {
        if (!iterator.hasNext()) return null;
        return iterator.next();
    }

}
