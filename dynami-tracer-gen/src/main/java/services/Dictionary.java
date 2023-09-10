package services;

import org.apache.commons.lang.ArrayUtils;
import resources.ResIO;

import java.util.*;

import static services.Utils.h2;
import static services.Utils.x;

public class Dictionary {

    Map<Integer, String> map = new TreeMap<>();
    Map<String, Integer> reverseMap = new TreeMap<>();

    /**
     * English String / Hexadecimal code 
     */
    Map<String, String> presets = new HashMap<>();
    
    public Dictionary(){
        
    }

    public void loadDictionary(String filename) {
        ResIO io = ResIO.getTextResource(filename);
        while (io.hasNext()) {
            String line = io.next().toString();
            if (line!=null && line.contains("=")) {
                String[] split = line.split("=");
                if (split.length>1) {
                    String code = split[0];
                    String value = split[1];
                    map.put(x(code), value);
                    reverseMap.put(value, x(code));
                }
            }
        }
    }

    public void loadPresets(String filename) {
        ResIO io = ResIO.getTextResource(filename);
        while (io.hasNext()) {
            String line = io.next().toString();
            if (line!=null && line.contains("=")) {
                String[] split = line.split("=");
                if (split.length>1) {
                    String code = split[0];
                    String value = split[1];
                    presets.put(value, code);
                    //map.put(x(code), value);
                    //reverseMap.put(value, x(code));
                }
            }
        }
    }

    /**
     * 
     * @param code Hexadecimal code
     * @param value Printed String
     */
    public void addEntry(int code, String value) {
        map.put(code, value);
        reverseMap.put(value, code);
    }
    
    public void print() {
        for (Map.Entry<Integer, String> e : map.entrySet()) {
            System.out.println(h2(e.getKey())+"\t"+e.getValue());
        }
    }
    
    public String getValue(byte b) {
        return map.get(b & 0xFF);
    }

    public byte getSingleCode(String value) {
        return reverseMap.get(value).byteValue();
    }

    public String getValue(byte[] bytes) {
        String res = "";
        for (byte b : bytes) {
            if (!map.containsKey(b & 0xFF)) res += "{"+h2(b)+"}";
            else res += map.get(b & 0xFF);
        }
        return res;
    }
    
    public byte[] getCode(String s) {
        byte[] res = new byte[0];
        String[] characters = parseCharacters(s);
        for (String value : characters) {
            byte b = 0;
            if (!reverseMap.containsKey(value)) {
                value = value.replaceAll("\\{","").replaceAll("}","");
                b = (byte) Integer.parseInt(value,16);
            } else b = reverseMap.get(value).byteValue();
            res = ArrayUtils.addAll(res, new byte[]{b});
        }
        return res;
    }

    /**
     * Turns a Japanese or English into separate characters and special codes
     */
    private String[] parseCharacters(String s) {
        List<String> values = new ArrayList<>();
        boolean special = false;
        String specialCode = "";
        for (char c : s.toCharArray()) {
            if (c=='{') {
                special = true;
            } else if (c=='}') {
                special = false;
                values.add("{"+specialCode+"}");
                specialCode = "";
            } else {
                if (special) specialCode+=c;
                else {
                    values.add(""+c);
                }
            }
            
        }
        return values.toArray(new String[0]);
    }

    public boolean containsPresets(String translation) {
        for (Map.Entry<String, String> e : presets.entrySet()) {
            String value = e.getValue();
            String key = e.getKey();
            if (translation.contains(key)) return true;
        }
        return false;
    }
    
    public String applyPresets(String translation) {
        for (Map.Entry<String, String> e : presets.entrySet()) {
            String value = e.getValue();
            String key = e.getKey();
            while (translation.contains(key)) {
                translation = translation.replace(key, value);
            }
            //translation = translation.replaceAll(key, value);
        }
        return translation;
    }
}
