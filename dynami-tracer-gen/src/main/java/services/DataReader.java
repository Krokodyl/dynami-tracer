package services;

import java.util.ArrayList;
import java.util.List;

public class DataReader {
    
    public static List<Integer> loadPointers(byte[] data, int start, int end) {
        List<Integer> result = new ArrayList<>();
        int offset = start;
        while (offset<end) {
            int pointer = readPointer(data, offset);
            result.add(pointer);
            offset += 2;
        }
        return result;
    }

    private static int readPointer(byte[] bytes, int pointerOffset) {
        byte a = bytes[pointerOffset];
        byte b = bytes[pointerOffset+1];
        return ((b & 0xFF) * 0x100) + (a & 0xFF);
    }
}
