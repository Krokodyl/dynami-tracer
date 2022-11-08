package services;

import org.apache.commons.lang.ArrayUtils;

import java.awt.image.BufferedImage;
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

    public static List<BufferedImage> readAlphabetMainImages(String collectionName, int collectionCount) {
        List<BufferedImage> images = new ArrayList<>();
        for (int i=1;i<=collectionCount;i++) {
            String file = "images/"+collectionName+i+".png";
            images.add(Utils.loadSubImage(file,0,0, 8,4));
            images.add(Utils.loadSubImage(file,0,4, 8,4));
            images.add(Utils.loadSubImage(file,0,8, 8,4));
        }
        return images;
    }

    public static List<BufferedImage> readAlphabetSideImages(String collectionName, int collectionCount) {
        List<BufferedImage> images = new ArrayList<>();
        for (int i=1;i<=collectionCount;i=i+2) {
            String file = "images/"+collectionName+i+".png";
            String file2 = "images/"+collectionName+(i+1)+".png";

            images.add(Utils.concatImagesSide(Utils.loadSubImage(file,8,0, 4,4), Utils.loadSubImage(file2,8,0, 4,4)));
            images.add(Utils.concatImagesSide(Utils.loadSubImage(file,8,4, 4,4), Utils.loadSubImage(file2,8,4, 4,4)));
            images.add(Utils.concatImagesSide(Utils.loadSubImage(file,8,8, 4,4), Utils.loadSubImage(file2,8,8, 4,4)));


        }
        return images;
    }

    public static byte[] readUntilEndOfLine(byte[] bytes, int start) {
        return readUntil(bytes, start, (byte) 0x00);
    }

    public static byte[] readUntil(byte[] bytes, int start, byte end) {
        byte[] result = new byte[0];
        for (int i = start; i < bytes.length; i++) {
            byte a = bytes[i];
            result = ArrayUtils.addAll(result, new byte[]{a});
            if ((a & 0xFF) == end) {
                return result;
            } else if ((a & 0xFF) >= 0x80 && (a & 0xFF) < 0xA0) {
                i++;
                byte b = bytes[i];
                result = ArrayUtils.addAll(result, new byte[]{b});
            }
        }
        return result;
    }
}
