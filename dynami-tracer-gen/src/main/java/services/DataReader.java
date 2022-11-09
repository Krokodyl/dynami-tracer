package services;

import entities.Constants;
import org.apache.commons.lang.ArrayUtils;
import services.pointers.PointerTable;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static services.Utils.*;

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

    public static Map<Integer, Translation> loadTranslations(PointerTable table, Dictionary dictionary) {
        System.out.println("Loading Translations for "+table.getName());
        Map<Integer, Translation> translationMap = new TreeMap<>();
        String file = String.format("translations/%s.txt", table.getName());
        if (!new File("src/main/resources/"+file).exists()) return null;
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(DataReader.class.getClassLoader().getResourceAsStream(file)), StandardCharsets.UTF_8));
        String line = null;
        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int jpnCount = 0;
        int engCount = 0;
        Translation t = new Translation();
        while (line != null) {
            if (line.contains("=")) {
                String[] split = line.split("=");
                if (split.length > 0) {
                    /*if (split[0].equals(Constants.TRANSLATION_FILE_POINTER)) {
                        String[] pointer = split[1].split(";");
                        t.setPointerFile(pointer[0]);
                        if (pointer[1].length()>0) {
                            //t.setPointerOffset(x(pointer[1]));
                            //t.setPointerValue(x(pointer[2]));
                            t.addPointer(new Pointer(x(pointer[1]), x(pointer[2])));
                        }
                        if (pointer.length>=4) {
                            t.setGlobalPointer(pointer[3].equals("GLOBAL"));
                        }
                    }*/
                    if (split[0].equals(Constants.TRANSLATION_FILE_DATA)) {
                        String[] data = split[1].split(";");
                        t.setOffsetData(x(data[0]));
                    }
                    if (split[0].equals(Constants.TRANSLATION_FILE_JPN)) {
                        t.setJapanese(split[1]);
                        //translationCount++;
                        jpnCount++;
                    }
                    if (split[0].equals(Constants.TRANSLATION_FILE_ENG)) {
                        if (split.length>1 && split[1].length()>0) {
                            String english = split[1];
                            if (english!=null && !english.isEmpty()) {
                                t.setEnglish(english);
                                byte[] bytes = dictionary.getCode(english);
                                t.setData(bytes);
                            }
                            //translationEngCount++;
                            engCount++;
                        }
                        /*if (translationMap.containsKey(t.getDataOffset())) {
                            System.err.println(t);
                        }*/
                        translationMap.put(t.getOffsetData(), t);
                        t = new Translation();
                    }
                }
            }
            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Trans Count "+file+" "+engCount+"/"+jpnCount);
        return translationMap;
    }
}
