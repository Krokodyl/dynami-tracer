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
    
    int indexDataByteArray = 0;
    byte[] dataByteArray = null;
    
    public ResIO(ResType type) {
        this.type = type;
    }

    public static ResIO getTextResource(String filename) {
        ResIO res = new ResIO(ResType.TEXT_FILE);
        res.loadResource(filename);
        return res;
    }

    public static ResIO getBinaryResource(String filename) {
        ResIO res = new ResIO(ResType.BINARY_FILE);
        res.loadResource(filename);
        return res;
    }
    
    public void loadResource(String filename) {
        if (type==ResType.TEXT_FILE) loadTextResource(filename);
        if (type==ResType.BINARY_FILE) loadBinaryResource(filename);
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

    private void loadBinaryResource(String filename) {
        try {
            dataByteArray = Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(filename)).readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean hasNext() {
        return iterator.hasNext();
    } 
    
    public Object next() {
        if (!iterator.hasNext()) return null;
        return iterator.next();
    }

    public boolean hasNextByte() {
        return indexDataByteArray < dataByteArray.length;
    }

    public byte nextByte() {
        return dataByteArray[indexDataByteArray++];
    }
    
    public byte[] getBytes() {
        return dataByteArray;
    }

}
