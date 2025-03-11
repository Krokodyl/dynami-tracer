package services;

import entities.Constants;
import enums.Language;
import org.apache.commons.lang.ArrayUtils;
import resources.ResIO;
import services.pointers.PointerTable;
import services.pointers.PointerTableType;
import services.vwf.Font;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static services.Utils.*;

public class DataReader {

    private static final boolean PRINT_JAPANESE = true;

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
        boolean ignoreByte = false; // used for the control code 03 that eats the next byte for the pause duration
        for (int i = start; i < bytes.length; i++) {
            byte a = bytes[i];
            result = ArrayUtils.addAll(result, new byte[]{a});
            /*if ((a & 0xFF) == (0x03) || ignoreByte) {
                i++;
                byte b = bytes[i];
                result = ArrayUtils.addAll(result, new byte[]{b});
                if (ignoreByte) ignoreByte = false;
                else ignoreByte = true;
            }*/
            if ((a & 0xFF) == (end & 0xFF)) {
                return result;
            } /*else if ((a & 0xFF) >= 0x80 && (a & 0xFF) < 0xA0) {
                i++;
                byte b = bytes[i];
                result = ArrayUtils.addAll(result, new byte[]{b});
            }*/
        }
        return result;
    }

    public static byte[] readByteCount(byte[] bytes, int start, int count) {
        byte[] result = new byte[0];
        for (int i = start; i < bytes.length; i++) {
            byte a = bytes[i];
            result = ArrayUtils.addAll(result, new byte[]{a});
            if (--count == 0) {
                return result;
            } else if ((a & 0xFF) >= 0x80 && (a & 0xFF) < 0xA0) {
                i++;
                byte b = bytes[i];
                result = ArrayUtils.addAll(result, new byte[]{b});
            }
        }
        return result;
    }

    public static Map<Integer, Translation> loadTranslations(PointerTable table, Dictionary dictionary, boolean replaceMissingWithAddress) {
        System.out.println("Loading Translations for "+table.getName());
        Map<Integer, Translation> translationMap = new TreeMap<>();
        String file = String.format("translations/%s.txt", table.getName());
        if (!new File("src/main/resources/"+file).exists()) return null;
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(DataReader.class.getClassLoader().getResourceAsStream(file)), StandardCharsets.UTF_8));
        int songLineId = 1;
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
                    if (Language.codes().contains(split[0])) {
                        Language language = Language.getLanguage(split[0]);
                        if (isSongLine(t) && songLineId<145) {
                            String format = "{NL}{03}{FF}{03}{EL}";
                            //if (songLineId == 145) format = "♪ Octave{NL}{03}{CLTB}{03}{EL}";
                            //String value = String.format(format, songLineId);
                            String value = "♪ " +getSongLine(language, songLineId)+format;//String.format(format, getSongLine(language, songLineId));
                            //System.out.println(songLineId+"\t"+h(t.getOffsetData())+"\t"+ Font.stripStringSpecialCode(t.getJapanese()));
                            songLineId++;
                            t.setTranslation(language, value);
                            byte[] bytes = dictionary.getCode(value);
                            t.setData(bytes);
                            engCount++;
                        }
                        else if (split.length>1 && split[1].length()>0) {
                            String value = split[1];
                            if (value != null && !value.isEmpty()) {
                                t.setTranslation(language, value);
                                byte[] bytes = dictionary.getCode(value);
                                t.setData(bytes);
                            }
                            //translationEngCount++;
                            engCount++;
                        } else if (t.getOffsetData() == 0x5E128 || t.getOffsetData() == 0x5F109) {
                            Map<Integer, String> lines = new HashMap<>();
                            ResIO textResource = ResIO.getTextResource(String.format("translations/00-MOLEMAN-%s-%s.txt", h(t.getOffsetData()), language.getCode()));
                            String value = "";
                            while (textResource.hasNext()){
                                String segment = textResource.next().toString();
                                
                                /*for (String specialCode : Font.SPECIAL_CODES) {
                                    segment = segment.replaceAll(specialCode.replaceAll("\\{", "\\\\{"),"§");
                                }*/
                                
                                String[] splitNL = segment.split("\\{NL}");
                                
                                segment = Arrays.stream(splitNL).map(
                                        s -> {
                                            String[] splitWPNL = s.split("\\{WPNL}");
                                            return Arrays.stream(splitWPNL).map(
                                                    s1 -> Font.autoInsertNewLines(s1, table.getMaxLineLength() - 1, false)
                                                    ).collect(Collectors.joining("{WPNL}"));
                                        }
                                ).collect(Collectors.joining("{NL}"));
                                /*for (String s : splitNL) {
                                    segment = Font.autoInsertNewLines(s, table.getMaxLineLength() - 1, false);
                                }*/
                                if (textResource.hasNext()) {
                                    segment = segment + "{WPCL}";
                                }
                                value += segment;
                            }
                            //value = String.format("Story %s", h(t.getOffsetData()));
                            value += "{EL}";
                            System.err.println("MOLEMAN\t"+value);
                            t.setTranslation(language, value);
                            byte[] bytes = dictionary.getCode(value);
                            t.setData(bytes);
                        } else if (replaceMissingWithAddress) {
                            t.setAddressReplacement(true);
                            String value = h(t.getOffsetData())+"{EL}";
                            if (table.getType() == PointerTableType.SIZE_PREFIX) {
                                value = h(t.getOffsetData());
                                value = "{"+h2(value.length())+"}"+value;
                            }
                            t.setTranslation(language, value);
                            byte[] bytes = dictionary.getCode(value);
                            t.setData(bytes);
                        }
                        /*if (translationMap.containsKey(t.getDataOffset())) {
                            System.err.println(t);
                        }*/
                        if (!table.getName().contains("PRESET") && !table.getName().contains("INTRO")) {
                            String translation = t.getTranslation(language);
                            if (dictionary.containsPresets(translation)) {
                                translation = dictionary.applyPresets(translation);
                                t.setTranslation(language, translation);
                                byte[] bytes = dictionary.getCode(translation);
                                t.setData(bytes);
                            }
                        }
                        
                        if (PRINT_JAPANESE) {
                            if (t.getTranslation(language) == null || (t.getTranslation(language).isEmpty() || t.isAddressReplacement()) )
                            System.out.println(String.format("%s-%s", h(t.getOffsetData()), Font.stripStringSpecialCode(t.getJapanese())));
                        }
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

    private static boolean isSongLine(Translation t) {
        return (t.getOffsetData()>=0x5C368 && t.getOffsetData()<=0x5CD6F);
    }
    
    private static String getSongLine(Language language, int id) {
        Map<Integer, String> map = songLines.get(language);
        if (map == null) {
            map = loadSongLines(language);
        }
        return map == null ? null : map.get(id);
    }
    
    static Map<Language, Map<Integer, String>> songLines = new HashMap<>();
    
    public static Map<Integer, String> loadSongLines(Language language) {
        Map<Integer, String> lines = new HashMap<>();
        ResIO textResource = ResIO.getTextResource(String.format("translations/00-LYRICS-%s.txt", language.getCode()));
        int id = 1;
        while (textResource.hasNext()){
            String line = textResource.next().toString();
            lines.put(id++, line);
        }
        songLines.put(language, lines);
        return songLines.get(language);
    }
}
