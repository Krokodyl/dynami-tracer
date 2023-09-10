import com.google.common.primitives.Bytes;
import compression.REPEAT_ALGORITHM;
import compression.algorithms.DynamiTracerCompressor;
import entities.Constants;
import entities.Patch;
import enums.Language;
import images.Sprite;
import old.ImageParser;
import old.Palette4bpp;
import org.apache.commons.lang.ArrayUtils;
import palette.ColorGraphics;
import resources.Hex;
import resources.Memory;
import resources.ResIO;
import resources.ResourceLoader;
import services.*;
import services.Dictionary;
import services.lz.DynamiTracerAlgorithm;
import services.lz.DynamiTracerLz;
import services.pointers.PointerEntry;
import services.pointers.PointerRange;
import services.pointers.PointerTable;
import services.pointers.PointerTableType;
import services.vwf.Font;
import tile.ColorDepth;
import tile.TileParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static services.Utils.*;

public class DynamiTracer {

    public static byte[] data;
    public static byte[] dataInput;

    private final static String ROM_INPUT = "D:\\git\\dynami-tracer\\roms\\input\\BS Dynami Tracer (Japan Extended).sfc";
    private final static String ROM_OUTPUT = "D:\\git\\dynami-tracer\\roms\\output\\BS Dynami Tracer (English Extended).sfc";

    static Dictionary japaneseDictionary = new Dictionary();
    static Dictionary japaneseSmallDictionary = new Dictionary();
    static Dictionary latinSmall = new Dictionary();
    static Dictionary latin = new Dictionary();

    static List<Patch> patches = new ArrayList<>();

    public static boolean VERBOSE = false;
    public static boolean SKIP_FILES_GENERATION = true;
    
    public static Language TARGET_LANGUAGE = Language.ENGLISH;

    public static void main(String[]args) {
        loadRom();
        
        // Images fonts
        Font.writeSmallFontData(data);
        Font.generateVWFFontData();
        patches.addAll(Font.getFontPatches());

        // Texts
        loadDictionaries();

        int bank1PointerOffset = 0x1A92D;
        int bank1Shift = 0x10000;

        List<PointerTable> tables = new ArrayList<>();
        
        PointerTable tableIntro = new PointerTable(0x2C9E0, 0x10C9E0, 0x10C9E0);
        tableIntro.addRange(new PointerRange(x("2C980"), x("2C983")));
        tableIntro.addRange(new PointerRange(x("2C990"), x("2C9DB")));
        tableIntro.setName("01-INTRO");
        tables.add(tableIntro);
        /*tableIntro.loadPointers(data);
        generateEmptyTranslationFiles(data, tableIntro, japaneseSmallDictionary);
        tableIntro.loadTranslations(latinSmall);
        tableIntro.writeEnglish(data);*/

        /**
         * C19AB4  A9 2D A9       LDA #$A92D
         * C19AB7  8D 0D 04       STA $040D
         * C19ABA  A9 C1 00       LDA #$00C1
         * C19ABD  8D 0F 04       STA $040F
         * 
         */
        //patches.add(new Patch(0x19AB4, Hex.parseHex("A9 00 80")));
        //patches.add(new Patch(0x19ABA, Hex.parseHex("A9 C4 00")));
        
        PointerTable table1 = new PointerTable(0x10000, 0x1A965, 0x10000);
        table1.addRange(new PointerRange(0x1A92D, 0x1A964));
        //PointerTable table1 = new PointerTable(0x10000, 0x48100, 0x40000);
        //table1.addRange(new PointerRange(0x1A92D, 0x1A964, 0x48000));
        table1.setName("02-ANNOUNCER");
        table1.setMaxLineLength(23);
        tables.add(table1);
        /*table1.loadPointers(data);
        generateEmptyTranslationFiles(data, table1, japaneseDictionary);
        table1.loadTranslations(latin);
        table1.writeEnglish(data);*/
        //table1.writeBlanks(data);
        
        bank1PointerOffset = 0x1AA00;

        PointerTable table4 = new PointerTable(0x30000, 0x30E3E, 0x30000);
        table4.addRange(new PointerRange(0x30E00, 0x30E3D));
        table4.setName("05-PRESETS");
        table4.setType(PointerTableType.SIZE_PREFIX);
        //tables.add(table4);
        table4.loadPointers(data);
        generateEmptyTranslationFiles(data, table4, japaneseDictionary);
        table4.loadTranslations(latin, true);
        table4.writeEnglish(data);
        
        PointerTable table7 = new PointerTable(0x50000, 0x100268, 0x100000);
        table7.addRange(new PointerRange(0x50000, 0x50267, 0x100000));
        //PointerTable table7 = new PointerTable(0x50000, bank1PointerOffset+0x268, 0x10000);
        //table7.addRange(new PointerRange(0x50000, 0x50267, bank1PointerOffset));
        table7.setName("07-SKULL");
        table7.setMaxLineLength(30);
        tables.add(table7);
        /*table7.loadPointers(data);
        generateEmptyTranslationFiles(data, table7, japaneseDictionary);
        table7.loadTranslations(latin, true);
        table7.writeEnglish(data);*/
        //table7.writeBlanks(data);

        //patches.add(new Patch(0x3962C, Hex.parseHex("00 80 50"))); // 00 00 C5

        
        


        PointerTable table2 = new PointerTable(0x50000, 0x110272, 0x110000);
        table2.addRange(new PointerRange(0x54000, 0x54271, 0x110000));
        /*PointerTable table2 = new PointerTable(0x50000, 0x53200+0x272, 0x50000);
        table2.addRange(new PointerRange(0x54000, 0x54271, 0x53200));*/
        table2.setName("03-TWIN-STAR");
        table2.setMaxLineLength(30);
        tables.add(table2);
        /*table2.loadPointers(data);
        generateEmptyTranslationFiles(data, table2, japaneseDictionary);
        table2.loadTranslations(latin);
        table2.writeEnglish(data);*/
        //table2.writeBlanks(data);

        //patches.add(new Patch(0x3F7D5, Hex.parseHex("00 00 00 51"))); // 00 40 C5
        //patches.add(new Patch(0x3F55F, Hex.parseHex("00 00 51"))); // 00 40 C5

        PointerTable table3 = new PointerTable(0x50000, 0x58004, 0x50000);
        table3.addRange(new PointerRange(0x58000, 0x58003));
        table3.setName("04-TWIN-STAR-CHEST");
        //tables.add(table3);
        table3.loadPointers(data);
        //generateEmptyTranslationFiles(data, table3, japaneseDictionary);
        table3.loadTranslations(latin);
        table3.writeEnglish(data);



        PointerTable table8 = new PointerTable(0x50000, 0x12009E, 0x120000);
        table8.addRange(new PointerRange(0x5CF00, 0x5CF9D, 0x120000));
        /*PointerTable table8 = new PointerTable(0x50000, 0x5E800+0x9E, 0x50000);
        table8.addRange(new PointerRange(0x5CF00, 0x5CF9D, 0x5E800));*/
        table8.setName("08-STATION");
        table8.setMaxLineLength(30);
        tables.add(table8);
        /*table8.loadPointers(data);
        //generateEmptyTranslationFiles(data, table8, japaneseDictionary);
        table8.loadTranslations(latin, true);
        table8.writeEnglish(data);*/

        PointerTable table9 = new PointerTable(0x50000, 0x12801A, 0x120000);
        table9.addRange(new PointerRange(0x5E000, 0x5E019, 0x128000));
        /*PointerTable table9 = new PointerTable(0x50000, 0x5011A, 0x50000);
        table9.addRange(new PointerRange(0x5E000, 0x5E019, 0x50100));*/
        table9.setName("09-SAND");
        table9.setMaxLineLength(30);
        tables.add(table9);
        /*table9.loadPointers(data);
        //generateEmptyTranslationFiles(data, table9, japaneseDictionary);
        table9.loadTranslations(latin, true);
        table9.writeEnglish(data);*/
        //table9.writeBlanks(data);

        PointerTable table6 = new PointerTable(0x50000, 0x58456, 0x50000);
        table6.addRange(new PointerRange(0x58100, 0x58455));
        table6.setName("06-MUSIC-FACTORY");
        table6.setMaxLineLength(30);
        tables.add(table6);
        /*table6.loadPointers(data);
        //generateEmptyTranslationFiles(data, table6, japaneseDictionary);
        table6.loadTranslations(latin, true);
        table6.writeEnglish(data);*/
        
        table1.checkTranslationsLength(TARGET_LANGUAGE);
        table2.checkTranslationsLength(TARGET_LANGUAGE);
        table6.checkTranslationsLength(TARGET_LANGUAGE);
        table7.checkTranslationsLength(TARGET_LANGUAGE);
        table8.checkTranslationsLength(TARGET_LANGUAGE);
        table9.checkTranslationsLength(TARGET_LANGUAGE);
        //tableIntro.setMaxLineLength(26);
        //tableIntro.checkTranslationsLength(TARGET_LANGUAGE);

        for (PointerTable table : tables) {
            table.loadPointers(data);
            //generateEmptyTranslationFiles(data, table6, japaneseDictionary);
            table.loadTranslations(latin, true);
            table.writeEnglish(data);
        }


        // Switch bank to load texts
        /**
         LDA $7F2001,X
         STA $17
         PHP
         REP #$20
         LDA $16

         CMP #$C501
         BNE 7 ; jump to next cmp
         LDA #$5000
         STA $16
         BRA 58 ; jump to end
         
         CMP #$C541
         BNE 7 ; jump to next cmp
         LDA #$C501
         STA $16
         BRA 46 ; jump to end

         CMP #$C500
         BNE 7 ; jump to next cmp
         LDA #$5000
         STA $16
         BRA 34 ; jump to end

         CMP #$C540
         BNE 7 ; jump to next cmp
         LDA #$C500
         STA $16
         BRA 22 ; jump to end

         CMP #$C5CF
         BNE 7 ; jump to next cmp
         LDA #$5200
         STA $16
         BRA 10 ; jump to end
         CMP #$C5E0
         BNE 5 ; jump to next cmp
         LDA #$5280
         STA $16
         PLP
         LDA $17
         RTL
         */
        patches.add(new Patch(0x243E, Hex.parseHex("22 D0 B6 C2")));
        patches.add(new Patch(0x2B6D0, Hex.parseHex("" +
                "BF 01 20 7F 85 17 08 C2 20 A5 16 C9 01 C5 D0 07 " +
                "A9 01 50 " +
                "85 16 80 3A C9 41 C5 D0 07 " +
                "A9 01 51 " +
                "85 16 80 2E C9 00 C5 D0 07 " +
                "A9 00 50 " +
                "85 16 80 22 C9 40 C5 D0 07 " +
                "A9 00 51 " +
                "85 16 80 16 C9 CF C5 D0 07 " +
                "A9 00 52 " +
                "85 16 80 0A C9 E0 C5 D0 05 " +
                "A9 80 52 " +
                "85 16 28 A5 17 6B")));

        /**
         * Reading text pointers
         C30015  B7 0D          LDA [$0D],Y
         C30017  85 1A          STA $1A

         PHP
         ;shift_reset:
         LDA #$10
         STA $70
         STZ $71
         STZ $73; set shift to 0
         PLP
         LDA [$0D],Y
         STA $1A
         RTL
         
         */
        patches.add(new Patch(0x30015, Hex.parseHex("22 A0 B6 C2")));
        patches.add(new Patch(0x2B6A0, Hex.parseHex("08 E2 20 A9 10 85 70 64 71 64 73 28 B7 0D 85 1A 6B")));
        
        insertQuizAnswers();

        fixItemList();

        endingImages();
        postcardImage();

        patches.add(new Patch(0x107FE0, Hex.parseHex("50 72 65 76 69 65 77 20 76 65 72 73 69 6F 6E 20 66 6F 72 20 63 61 62 62 75 73 73 65 73 00 00 00")));
        
        loadPatches();
        applyPatches(data);

        //printQuestions(table2);
        
        
        /*tables.add(tableIntro);
        tables.add(table1);
        tables.add(table2);
        tables.add(table6);
        tables.add(table7);
        tables.add(table8);
        tables.add(table9);*/

        
        
        //analyzeFrequencies(tables);

        //analyzeCommonFrequencies(tables);

        for (PointerTable pt : tables) {
            String name = pt.getName();
            int oldLength = pt.getOldLength(dataInput);
            int newLength = pt.getNewLength();
            int diff = newLength - oldLength;
            System.out.println(String.format("%20s"+"\t\t%05X\t\t%05X\t\t%05X", name, oldLength, newLength, diff));
            //pt.updateFrequencies(wordFrequencies, TARGET_LANGUAGE);
        }


        saveRom();
    }

    private static void analyzeFrequencies(List<PointerTable> tables) {
        Map<String, Integer> wordFrequencies = new HashMap<>();
        for (PointerTable pt : tables) {
            System.out.println(String.format("%20s"+"\t\t%05X\t\t%05X\t\t%05X", pt.getName(), pt.getOldLength(dataInput), pt.getNewLength(), pt.getNewLength()-pt.getOldLength(dataInput)));
            pt.updateFrequencies(wordFrequencies, TARGET_LANGUAGE);
        }

        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(wordFrequencies.entrySet());

        // Sort the list
        Collections.sort(list, Map.Entry.comparingByValue());
        Collections.reverse(list);

        for (Map.Entry<String, Integer> e : list) {
            int gain = (e.getKey().length()-1)*e.getValue();
            //if (gain>200)
            //System.out.println(String.format("%s\t\t%s\t\t%s", e.getValue(), e.getKey(), gain));
        }
    }

    private static void analyzeCommonFrequencies(List<PointerTable> tables) {
        String bigrams = "th he in er an re nd on en at ou ed ha to or it is hi es ng";
        String trigrams = "the and ing her hat his tha ere for ent ion ter was you ith ver all wit thi tio";
        String quadrigrams = "that ther with tion here ould ight have hich whic this thin they atio ever from ough were hing ment";
        Map<String, Integer> commonFrequencies = new HashMap<>();
        for (String s : bigrams.split(" ")) {
            commonFrequencies.put(s, 0);
        }
        for (String s : trigrams.split(" ")) {
            commonFrequencies.put(s, 0);
        }
        for (String s : quadrigrams.split(" ")) {
            commonFrequencies.put(s, 0);
        }
        for (PointerTable pt : tables) {
            pt.updateCommonFrequencies(commonFrequencies, TARGET_LANGUAGE);
        }
        /*for (Map.Entry<String, Integer> e : commonFrequencies.entrySet()) {
            int gain = (e.getKey().length()-1)*e.getValue();
            System.out.println(String.format("%s\t\t%s\t\t%s", e.getKey(), e.getValue(), gain));
        }*/

        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(commonFrequencies.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                int gain1 = (o1.getKey().length()-1)*o1.getValue();
                int gain2 = (o2.getKey().length()-1)*o2.getValue();
                return gain2-gain1;
            }
        });
        Collections.reverse(list);

        for (Map.Entry<String, Integer> e : list) {
            int gain = (e.getKey().length()-1)*e.getValue();
            //if (gain>200)
            System.out.println(String.format("%5s\t\t%5s\t\t%5s", e.getValue(), e.getKey(), gain));
        }
        
    }

    private static void toBeContinued() {
        
    }

    private static void postcardImage() {
        System.out.println("postcardImage");

        /**
         PHP
         LDA $482
         CMP #$00C7
         BNE 32 ; to plp
         LDA $480
         CMP #$AF1B
         BNE 14 ; to cmp
         LDA #$0056
         STA $482
         LDA #$AAAA
         STA $480
         BRA 17; to plp
         CMP #$B1B3
         BNE 12; to plp
         LDA #$0056
         STA $482
         LDA #$BBBB
         STA $480
         PLP
         LDX #$2400
         STX $0484
         RTL
         */

        patches.add(new Patch(0x20A55, Hex.parseHex("22 00 40 C1 EA EA")));
        patches.add(new Patch(0x14000, Hex.parseHex("08 E2 20 AD 82 04 C9 C7 D0 2B C2 20 AD 80 04 C9 1B AF D0 0F A9 AA AA 8D 80 04 E2 20 A9 56 8D 82 04 80 12 C9 B3 B1 D0 0D A9 BB BB 8D 80 04 E2 20 A9 56 8D 82 04 28 A2 00 24 8E 84 04 6B")));
        
        
        TileParser parser = new TileParser();
        ColorGraphics colorGraphics = new ColorGraphics();
        colorGraphics.loadFromDataFile("images/postcard/cgram.data");
        BufferedImage bufferedImage = ResourceLoader.loadImage("images/postcard/tilemap.png");
        parser.parseImage(bufferedImage, colorGraphics, ColorDepth._4BPP, 8, 0);
        byte[] characterBytes = parser.getCharacterBytes();
        byte[] tileMapBytes = parser.getTileMapBytes();
        //System.out.println(Hex.getHexString(characterBytes));
        //System.out.println(Hex.getHexString(tileMapBytes));
        
        int offset = 0x160000;
        int bank = 0x56;
        //patches.add(new Patch(0x20A50, Hex.parseHex("A9 56")));
        
        byte[] pointer = Memory.getPointerLow(offset-0x160000);
        //System.out.println(Arrays.toString(pointer));

        patches.add(new Patch(0x14029, pointer));

        System.out.println("tiles");
        byte[] compressedData = DynamiTracerLz.compressData(characterBytes, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);
        patches.add(new Patch(offset, compressedData));
        offset += compressedData.length;


        System.out.println("tilemap");
        pointer = Memory.getPointerLow(offset-0x160000);
        patches.add(new Patch(0x14015, pointer));
        compressedData = DynamiTracerLz.compressData(tileMapBytes, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);
        patches.add(new Patch(offset, compressedData));

        // TO BE CONTINUED TILES
        /*offset += compressedData.length;
        pointer = Memory.getPointerLow(offset-0x168000);
        patches.add(new Patch(0x2FF80, pointer));
        byte[] bytes = DynamiTracerLz.decompressData(data, 0x78000, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);
        compressedData = DynamiTracerLz.compressData(bytes, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);
        patches.add(new Patch(offset, compressedData));*/

    }

    private static void endingImages() {
        System.out.println("ending final fantasy");
        byte[] bytes = ImageParser.loadImage4bpp(
                "images/ending/tiles-ff.png", new Palette4bpp("images/ending/palette-ff.png"));

        int offset = 0x140000;
        patches.add(new Patch(0x6343C, Hex.parseHex("00 00 54")));
/*
        byte[] decompressData = DynamiTracerLz.decompressData(data, 0x9A800, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);
        
        byte[] compressedData = DynamiTracerLz.compressData(decompressData, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);
        patches.add(new Patch(offset, compressedData));
*/
        System.out.println("tiles");
        int pointer = 0x9A800;
        //byte[] expectedData = DynamiTracerLz.decompressData(data, pointer, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);
        byte[] compressedData = DynamiTracerLz.compressData(bytes, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);
        //byte[] decompressData = DynamiTracerLz.decompressData(compressedData, 0, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);

        patches.add(new Patch(offset, compressedData));
        //byte[] decompressData = DynamiTracerLz.decompressData(data, 0x9A800, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);

        // TILEMAP

        System.out.println("tilemap");
        offset = 0x148000;
        patches.add(new Patch(0x6323C, Hex.parseHex("00 80 54")));

        bytes = ResIO.getBinaryResource("images/ending/tilemap-ff.data").getBytes();
        compressedData = DynamiTracerLz.compressData(bytes, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);

        patches.add(new Patch(offset, compressedData));

        System.out.println("ending chrono trigger");
        System.out.println("tiles");
        // CHRONO TILES
        bytes = ImageParser.loadImage4bpp(
                "images/ending/tiles-chrono.png", new Palette4bpp("images/ending/palette-chrono.png"));

        offset = 0x150000;
        patches.add(new Patch(0x63448, Hex.parseHex("00 00 55")));

        //pointer = 0x9A800;
        //byte[] expectedData = DynamiTracerLz.decompressData(data, pointer, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);
        compressedData = DynamiTracerLz.compressData(bytes, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);
        //byte[] decompressData = DynamiTracerLz.decompressData(compressedData, 0, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);

        patches.add(new Patch(offset, compressedData));
        patches.add(new Patch(0x6B2ED, Hex.parseHex("58")));
        patches.add(new Patch(0x6B2EE, Hex.parseHex("08")));
    }

    private static void printQuestions(PointerTable table) {
        table.printTranslations(0x55F3E,0x57883, TARGET_LANGUAGE, 0x56496);
    }

    private static void insertQuizAnswers() {
        // Decompress quiz answer
        byte[] answersData = DynamiTracerLz.decompressData(data, 0x40BFA, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);
        
        byte[] newAnswersData = new byte[0x1C60+0x800];
        System.arraycopy(answersData, 0, newAnswersData, 0, answersData.length);

        List<String> quizAnswers = loadQuizAnswers(TARGET_LANGUAGE);
        int i = 0x1C60;
        for (String quizAnswer : quizAnswers) {
            byte[] code = latinSmall.getCode(quizAnswer);
            System.arraycopy(code, 0, newAnswersData, i, code.length);
            i += 0x10;
        }


        byte[] compressedData = DynamiTracerLz.compressData(newAnswersData, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);
        
        int offset = 0x130000;

        //patches.add(new Patch(offset, answersData));
        patches.add(new Patch(offset, compressedData));
        //System.arraycopy(data, 0x40BFA, data, offset, 0x800);

        patches.add(new Patch(0x3E06C, Hex.parseHex("00 00 53")));


        /**
         * Check answers main
         JSL $C3E100
         NOP
         NOP
         CMP #$0001
         BNE 40 ;$C049F6
         */
        String thirtyNOP = "EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA ";
        patches.add(new Patch(0x49C3, Hex.parseHex("22 00 E1 C3 EA EA C9 01 00 D0 28 "+thirtyNOP)));
        

        /**
         * Check answers sub
         PHP
         PHX
         SEP #$20
         LDA $114
         SEC
         SBC #$19
         REP #$20
         ASL
         ASL
         ASL
         ASL
         TAX
         LDA $7F3C60,X
         CMP $7F0021
         BNE 74 
         LDA $7F3C62,X
         CMP $7F0023
         BNE 64
         LDA $7F3C64,X
         CMP $7F0025
         BNE 54
         LDA $7F3C66,X
         CMP $7F0027
         BNE 44
         LDA $7F3C68,X
         CMP $7F0029
         BNE 34
         LDA $7F3C6A,X
         CMP $7F002B
         BNE 24
         LDA $7F3C6C,X
         CMP $7F002D
         BNE 14
         LDA $7F3C6E,X
         CMP $7F002F
         BNE 4
         LDA #$0001
         RTL
         LDA #$0002
         RTL
         */
        patches.add(new Patch(0x3E100, Hex.parseHex("08 DA E2 20 AD 14 01 38 E9 19 C2 20 0A 0A 0A 0A AA BF 60 3C 7F CF 21 00 7F D0 4C BF 62 3C 7F CF 23 00 7F D0 42 BF 64 3C 7F CF 25 00 7F D0 38 BF 66 3C 7F CF 27 00 7F D0 2E BF 68 3C 7F CF 29 00 7F D0 24 BF 6A 3C 7F CF 2B 00 7F D0 1A BF 6C 3C 7F CF 2D 00 7F D0 10 BF 6E 3C 7F CF 2F 00 7F D0 06 A9 01 00 FA 28 6B A9 02 00 FA 28 6B")));

        /**
         Answer input length
         23954  A9 08 00       LDA #$0008
         */
        patches.add(new Patch(0x23954, Hex.parseHex("A9 10 00")));
        patches.add(new Patch(0x23932, Hex.parseHex("A2 10 18")));

        /**
         Answer panel width 23725  A2 14 00
         */
        patches.add(new Patch(0x23725, Hex.parseHex("A2 19 00")));

        /**

         Answer input position 
         23750  A9 08          LDA #$08
         23844  A9 09          LDA #$09
         */
        patches.add(new Patch(0x23750, Hex.parseHex("A9 05")));
        patches.add(new Patch(0x23844, Hex.parseHex("A9 06")));

        /**

         Answer cursor position 
         2369F  69 80          ADC #$80
         */
        patches.add(new Patch(0x2369F, Hex.parseHex("69 68")));

    }

    public static List<String> loadQuizAnswers(Language language) {
        List<String> lines = new ArrayList<>();
        ResIO textResource = ResIO.getTextResource(String.format("translations/00-QUIZ-%s.txt", language.getCode()));
        while (textResource.hasNext()){
            String line = textResource.next().toString().trim();
            lines.add(line);
        }
        return lines;
    }



    private static void loadDictionaries() {
        japaneseDictionary.loadDictionary("dictionaries/japanese.txt");
        //japanese.print();
        japaneseSmallDictionary.loadDictionary("dictionaries/small-japanese.txt");
        //japaneseSmall.print();
        //latinSmall.loadDictionary("dictionaries/small-latin.txt");

        latinSmall = Font.getLatinSmallDictionary();
        latin = Font.getLatinDictionary();
    }


    public static void main2(String[]args) {
        
        loadRom();
        

        //testDuplicate(data, x("2C600"), x("2D600"));
        
        //generateSmallLatin();

        /*ImageReader imageReader = new ImageReader();
        imageReader.generateSmallImages();*/

        //List<BufferedImage> alphabetImages = DataReader.readAlphabetMainImages("uppercase/uppercase", Constants.COUNT_UPPERCASE);
        //SpriteWriter spriteWriter = new SpriteWriter();
        //spriteWriter.writeLatinCharacterSprites(alphabetImages, data);
        //spriteWriter.writeSmallLatinCharacterSprites("small/small", data);

        testVWF();
        Font.writeSmallFontData(data);

        PointerTable tableIntro = new PointerTable(0x2C9E0, 0x100000, 0x2C9E0);
        tableIntro.addRange(new PointerRange(x("2C980"), x("2C983")));
        tableIntro.addRange(new PointerRange(x("2C990"), x("2C9DB")));

        tableIntro.setName("01-INTRO");
        tableIntro.loadPointers(data);
        generateEmptyTranslationFiles(data, tableIntro, japaneseSmallDictionary);
        tableIntro.loadTranslations(latinSmall);
        tableIntro.writeEnglish(data);


        PointerTable table1 = new PointerTable(0x10000, 0x1A965, 0x2C9E0);
        table1.addRange(new PointerRange(0x1A92D, 0x1A964));
        table1.setName("02-ANNOUNCER");
        table1.setMaxLineLength(23);
        table1.loadPointers(data);
        generateEmptyTranslationFiles(data, table1, japaneseDictionary);
        table1.loadTranslations(latin);
        //table1.writeEnglish(data);
        //table1.writeBlanks(data);

        PointerTable table4 = new PointerTable(0x30000, 0x30E3E, 0x2C9E0);
        table4.addRange(new PointerRange(0x30E00, 0x30E3D));
        table4.setName("05-PRESETS");
        table4.setType(PointerTableType.SIZE_PREFIX);
        table4.loadPointers(data);
        generateEmptyTranslationFiles(data, table4, japaneseDictionary);
        table4.loadTranslations(latin, true);
        //table4.writeEnglish(data);

        PointerTable table7 = new PointerTable(0x50000, 0x50268, 0x2C9E0);
        table7.addRange(new PointerRange(0x50000, 0x50267));
        table7.setName("07-SKULL");
        table7.setMaxLineLength(30);
        table7.loadPointers(data);
        generateEmptyTranslationFiles(data, table7, japaneseDictionary);
        table7.loadTranslations(latin, true);
        //table7.writeEnglish(data);
        //table7.writeBlanks(data);

        PointerTable table2 = new PointerTable(0x50000, 0x54272, 0x2C9E0);
        table2.addRange(new PointerRange(0x54000, 0x54271));
        table2.setName("03-TWIN-STAR");
        table2.setMaxLineLength(30);
        table2.loadPointers(data);
        generateEmptyTranslationFiles(data, table2, japaneseDictionary);
        table2.loadTranslations(latin);
        //table2.writeEnglish(data);
        //table2.writeBlanks(data);

        /*PointerTable table3 = new PointerTable(0x50000, 0x58004);
        table3.addRange(new PointerRange(0x58000, 0x58003));
        table3.setName("04-TWIN-STAR-CHEST");
        table3.loadPointers(data);
        generateEmptyTranslationFiles(data, table3, japaneseDictionary);
        table3.loadTranslations(latin);
        table3.writeEnglish(data);*/
        //table3.writeBlanks(data);


        PointerTable table6 = new PointerTable(0x50000, 0x58456, 0x2C9E0);
        table6.addRange(new PointerRange(0x58100, 0x58455));
        table6.setName("06-MUSIC-FACTORY");
        table6.setMaxLineLength(30);
        table6.loadPointers(data);
        generateEmptyTranslationFiles(data, table6, japaneseDictionary);
        table6.loadTranslations(latin, true);
        table6.writeEnglish(data);

        

        PointerTable table8 = new PointerTable(0x50000, 0x5CF9E, 0x2C9E0);
        table8.addRange(new PointerRange(0x5CF00, 0x5CF9D));
        table8.setName("08-STATION");
        table8.loadPointers(data);
        generateEmptyTranslationFiles(data, table8, japaneseDictionary);
        table8.loadTranslations(latin, true);
        //table8.writeEnglish(data);

        PointerTable table9 = new PointerTable(0x50000, 0x5E01A, 0x2C9E0);
        table9.addRange(new PointerRange(0x5E000, 0x5E019));
        table9.setName("09-SAND");
        table9.loadPointers(data);
        generateEmptyTranslationFiles(data, table9, japaneseDictionary);
        table9.loadTranslations(latin, true);
        //table9.writeEnglish(data);
        //table9.writeBlanks(data);
        
        PointerTable tableM = new PointerTable(0x50000, 0x58004, 0x2C9E0);
        tableM.addRange(new PointerRange(0x5E000, 0x5E019));
        tableM.setName("77-TEST");
        tableM.loadPointers(data);
        generateEmptyTranslationFiles(data, tableM, japaneseDictionary);
        

        table2.checkTranslationsLength(TARGET_LANGUAGE);
        table6.checkTranslationsLength(TARGET_LANGUAGE);
        table7.checkTranslationsLength(TARGET_LANGUAGE);
        table1.checkTranslationsLength(TARGET_LANGUAGE);
        
        //fixItemList();
        //testQuiz();
        //checkItemLength();
        
        loadPatches();
        applyPatches(data);
        
        
        /*PointerTable tableIntro2 = new PointerTable(x("2D9E0"), x("2D9E0"));
        tableIntro2.addRange(new PointerRange(x("2D980"), x("2D983")));
        tableIntro2.addRange(new PointerRange(x("2D990"), x("2D9DB")));*/
        
        

        //tableIntro2.setName("02-INTRO");

        //tableIntro2.loadPointers(data);

        //generateEmptyTranslationFiles(data, tableIntro2, japaneseSmallDictionary);
        
        //tableIntro2.loadTranslations(latinSmall);
        
        //tableIntro2.writeEnglish(data);
        //generateJapaneseTable();
        //testDecompression();
        
        //testSprites();
        //mergeSprites();

        //testDecompression();
        
        //testIntro();

                
        saveRom();
    }
    
    public static void testVWF(){
        Font.generateVWFFontData();
        patches.addAll(Font.getFontPatches());
    }
    
    public static void checkItemLength() {
        ResIO textResource = ResIO.getTextResource(String.format("translations/00-ITEMS-%s.txt", TARGET_LANGUAGE.getCode()));
        while (textResource.hasNext()) {
            String s = textResource.next().toString();
            s = '"' + s + '"';
            int length = Font.getStringLength(s);
            if (VERBOSE) System.out.println(String.format("%d px\t%d tiles\t%s", length, length/8, s));
        }
    }

    public static void testQuiz() {
        Patch p = new Patch(0x30E7D, Hex.parseHex("61 61 61 61 61 61"));
        patches.add(p);
    }

    public static void fixItemList() {
        Patch p = new Patch(0x215C2, Hex.parseHex("EA"));
        patches.add(p);
        p = new Patch(0x215DC, Hex.parseHex("EA EA EA EA EA EA EA EA EA EA EA EA EA EA EA"));
        patches.add(p);
        p = new Patch(0x20D99, Hex.parseHex("EA 0A"));
        patches.add(p);


        p = new Patch(0x21613, Hex.parseHex("A9 10"));
        patches.add(p);
        p = new Patch(0x2162F, Hex.parseHex("69 00 68"));
        patches.add(p);
        p = new Patch(0x2162C, Hex.parseHex("0A EA"));
        patches.add(p);

        p = new Patch(0x30396, Hex.parseHex("69 00 68"));
        patches.add(p);
        p = new Patch(0x30504, Hex.parseHex("C0 10 00"));
        patches.add(p);
        p = new Patch(0x3038B, Hex.parseHex("A9 10"));
        patches.add(p);

        if (VERBOSE) printItemList(data);
        insertPlaceholderItemName(data);
        insertItemName(data);
        
    }

    public static void loadPatches() {
        System.out.println("loadPatches");
        // Vehicule names
        /*Patch p = new Patch(0x21730, latinSmall.getCode("Cry Baby"));
        patches.add(p);
        p = new Patch(0x21740, latinSmall.getCode("Mach M."));
        patches.add(p);
        p = new Patch(0x21750, latinSmall.getCode("Lightni."));
        patches.add(p);
        p = new Patch(0x21760, latinSmall.getCode("Hell Ha."));
        patches.add(p);
        p = new Patch(0x21770, latinSmall.getCode("The Cas."));
        patches.add(p);
        p = new Patch(0x21780, latinSmall.getCode("Popup.{00}"));
        patches.add(p);*/
        Patch p = new Patch(0x21730, latinSmall.getCode(
                "Cry Baby{00}{00}{00}{00}" +
                        "Mach Mary{00}{00}{00}" +
                        "Lightning{00}{00}{00}" +
                        "Hell Harley{00}" +
                        "The Castle{00}{00}" +
                        "Popuppy{00}{00}{00}{00}{00}"
        ));
        patches.add(p);
        /**
         * C216AF  0A             ASL
         * C216B0  0A             ASL
         * C216B1  0A             ASL
         * C216B2  0A             ASL
         */
        p = new Patch(0x216AF, Hex.parseHex("22 40 B6 C2"));
        patches.add(p);
        p = new Patch(0x2B640, Hex.parseHex("08 0A 0A 85 32 0A 65 32 28 6B"));
        patches.add(p);
        

        p = new Patch(0x22044, latinSmall.getCode("Character Pick "));
        patches.add(p);

        
        // Character description width
        byte[] bytes = Utils.hexStringToByteArray("17 00");
        p = new Patch(x("220E0"), bytes);
        patches.add(p);
        
        // Intro - leading spaces
        // $C2/1EA5 A2 06 00    LDX #$0006
        bytes = Utils.hexStringToByteArray("A2 02 00");
        p = new Patch(x("21EA5"), bytes);
        patches.add(p);

        // 0B clearing length (from x15 to x1A, 21 to 26 chars)
        // C0 15 18    CPY #$1815
        bytes = Utils.hexStringToByteArray("C0 1A 18");
        p = new Patch(x("2145D"), bytes);
        patches.add(p);

        // Moving up the announcer ship sprite to make room for the text
        bytes = Utils.hexStringToByteArray("C0 28 40 1C C0 18 20 1C F0 F0 08 30 F0 F0 00 20 F0 F0 00 20 F0 F0 00 20 B8 30 60 1C C8 30 62 1C D8 30 64 1C B8 40 80 1C C8 40 82 1C D8 40 84 1C C8 20 2C 1C D8 10 24 1C D8 20 44 1C D0 10 0D 3C");
        p = new Patch(x("2F6EB"), bytes);
        patches.add(p);

        // Make the intro longer
        patches.add(new Patch(0x21F06, Hex.parseHex("C9 80 18")));
        
        // Move table info to 0x100000
        patches.add(new Patch(0x21412, Hex.parseHex("20 00 B6"))); // Reads characters to print
        patches.add(new Patch(0x2B600, Hex.parseHex("BF 00 00 50 60")));
        //patches.add(new Patch(0x214AF, Hex.parseHex("69 00 00")));
        patches.add(new Patch(0x214BA, Hex.parseHex("20 00 B6"))); // Reads characters to check end of line

        patches.add(new Patch(0x23819, Hex.parseHex("20 00 B6"))); // Reads characters in name input
        patches.add(new Patch(0x238D5, Hex.parseHex("54 00 50"))); // Reads default character names

        // Modify the default character names
        bytes = ResIO.getBinaryResource("decompressed/3E200.data").getBytes();
        byte[] zeros = new byte[0x3C0-bytes.length];
        bytes = Bytes.concat(bytes, zeros);
        //String names = "Daniel {EL}Rose   {EL}Early  {EL}Wilde  {EL}Harper {EL}Jim    {EL}";
        String names = "Daniel{00}{EL}Rose{00}{00}{00}{EL}Early{00}{00}{EL}Wilde{00}{00}{EL}Harper{00}{EL}Jim{00}{00}{00}{00}{EL}";
        int i = 0x390;
        for (byte b : latinSmall.getCode(names)) {
            bytes[i++] = b;
        }
        for (int k = 0x281; k<0x2A0; k++) {
            bytes[k] = 0x62;
        }
        
        //bytes[0x27F] = 50; // ((6*8)-1)+3
        //bytes[0x10A] = 0x70; // 64
        //bytes[0x10D] = 0x2C; // 38
        
        DynamiTracerCompressor compressor = new DynamiTracerCompressor(new DynamiTracerAlgorithm());
        byte[] compressData = new byte[0];
        try {
            compressData = compressor.compressData(bytes, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        p = new Patch(x("3E1D0"), compressData);
        patches.add(p);

        // Changes the pointer to the compressed data with the default character names
        bytes = Utils.hexStringToByteArray("D0 E1");
        p = new Patch(x("3E03F"), bytes);
        patches.add(p);

        /**
         CMP #$2281
         BNE 21
         LDX #$2390
         LDY #$9000
         LDA #$002F
         PHB
         MVN $7F,$7F
         PLB
         LDX #$22A5
         LDY #$0064
         RTL
         LDA $8A
         PHB
         MVN $7F,$7F
         PLB
         RTL
         */
        //patches.add(new Patch(0x04D7E, Hex.parseHex("22 10 B6 C2")));
        //patches.add(new Patch(0x2B610, Hex.parseHex("C9 80 02 D0 08 08 18 69 10 21 28 AA 6B 69 01 20 AA 6B")));

        //patches.add(new Patch(0x04D8B, Hex.parseHex("22 30 B6 C2")));
        //patches.add(new Patch(0x2B630, Hex.parseHex("C9 C0 23 D0 08 08 38 E9 10 21 28 AA 6B 38 E9 01 20 AA 6B")));

        patches.add(new Patch(0x04D82, Hex.parseHex("22 10 B6 C2 EA EA EA")));
        patches.add(new Patch(0x2B610, Hex.parseHex("C9 81 22 D0 15 A2 90 23 A0 00 F0 A9 2F 00 8B 54 7F 7F AB A2 A5 22 A0 64 00 6B A5 8A 8B 54 7F 7F AB 6B")));
        
        // Name change to 8 letters
        // Before : C2021A  65 10          ADC $10
        patches.add(new Patch(0x2021A, Hex.parseHex("0A EA")));
        //Before : C20222  69 40 00       ADC #$0040
        patches.add(new Patch(0x20222, Hex.parseHex("69 00 F0")));
        // Before : C238F9  A2 05 18       LDX #$1805
        patches.add(new Patch(0x238F9, Hex.parseHex("A2 07 18")));
        // 40 00 46 00 4C 00 52 00 58 00 5E 00
        patches.add(new Patch(0x30793, Hex.parseHex("00 F0 08 F0 10 F0 18 F0 20 F0 28 F0")));

        //237AB  A9 09          LDA #$09
        patches.add(new Patch(0x237AB, Hex.parseHex("A9 06")));

        
        // C30515  C0 05 00       CPY #$0005
        patches.add(new Patch(0x30515, Hex.parseHex("C0 07 00")));
        // C238D2  A9 05 00       LDA #$0005
        patches.add(new Patch(0x238D2, Hex.parseHex("A9 07 00")));
        // C238EF  E0 05 18       CPX #$1805
        patches.add(new Patch(0x238EF, Hex.parseHex("E0 07 18")));
        // C238F6  8D 05 18       STA $1805
        patches.add(new Patch(0x238F6, Hex.parseHex("8D 07 18")));


        // C20C38  A9 05 00       LDA #$0005            Early
        patches.add(new Patch(0x20C38, Hex.parseHex("A9 07 00")));



        /**
         * SNES HEADER
         */
        patches.add(new Patch(0xFFC0, Hex.parseHex("44 59 4E 41 4D 49 20 54 52 41 43 45 52 20 20 20 20 20 20 20 20 31 02 0A 05 00")));

        sramPatches();

        /**
         * PASSWORD SCREEN
         */

        patches.add(new Patch(0x23B35, Hex.parseHex("EA EA EA")));

        /**
         * HIGH SCORE
         * CHECKSUM HIGH SCORE
         * C23579  A9 00 00       LDA #$0000
         * C2357D  7D 00 00       ADC $0000,X
         */
        patches.add(new Patch(0x23579, Hex.parseHex("A9 FF FF")));
        patches.add(new Patch(0x2357D, Hex.parseHex("EA EA EA")));

        patches.add(new Patch(0x2D91E, latinSmall.getCode("Daniel K.{0A}{00}")));
        patches.add(new Patch(0x2D929, latinSmall.getCode("Rose T.{0A}{00}")));
        patches.add(new Patch(0x2D932, latinSmall.getCode("Capt. Early{0A}{00}")));
        patches.add(new Patch(0x2D93F, latinSmall.getCode("Wilde H.{0A}{00}")));
        patches.add(new Patch(0x2D949, latinSmall.getCode("Harper M.{0A}{00}")));
        patches.add(new Patch(0x2D954, latinSmall.getCode("Jim T.{0A}{00}")));
        
        String defaultHighScore =   
                        "F8 0C 01 02 00 F8 D8 F8 00 " +
                        "CD 08 02 01 00 F8 5A 00 00 " +
                        "F6 02 03 00 00 B4 AE DA 00 " +
                        "FD 01 04 03 00 AC DA 66 00 " +
                        "64 00 05 04 00 F8 69 F8 00 " +
                        "00 00 06 05 00 D0 00 00 00 " +
                        "08 E2 20 A9 00 48 20 46 2F 20 1F 30 C9 00 F0 0A 68 1A C9 08 90 EF A9 FF 80 01 68 85 7C 28 60 08 E2 20 A5 7C 20 E7";

        patches.add(new Patch(0x230AE, Hex.parseHex(defaultHighScore)));
    }
    
    public static void sramPatches() {

        // Treasure PTS
        patches.add(new Patch(0x215A8, latin.getCode("TREAS{00}{B5}{B2}{A5}")));
        // ABCD EFGHI   JKLMNO  PQRST   UVWXYZ            
        // A1   A5      AA      B0      B5
        

        /**
         * SAVE/LOAD TO SRAM
         */
        String sram = "30";
        // C22EEF  09 16          ORA #$16
        patches.add(new Patch(0x22EEF, Hex.parseHex(String.format("09 %s", sram))));
        // C22EF8  09 50          ORA #$50
        patches.add(new Patch(0x22EF8, Hex.parseHex("09 60")));

        // C22F09  C9 16          CMP #$16
        patches.add(new Patch(0x22F09, Hex.parseHex(String.format("C9 %s", sram))));
        // C22F10  54 00 16       MVN $16,$00
        patches.add(new Patch(0x22F10, Hex.parseHex(String.format("54 00 %s", sram))));
        // C22F2C  C9 16          CMP #$16
        patches.add(new Patch(0x22F2C, Hex.parseHex(String.format("C9 %s", sram))));
        // C22F33  54 16 00       MVN $00,$16
        patches.add(new Patch(0x22F33, Hex.parseHex(String.format("54 %s 00", sram))));


        // Write names to sram
        /**
         CPY #$6400
         BEQ 14
         TYA
         ADC #$0220
         TAY
         LDX #$F000
         LDA #$002F
         MVN $30,$7F
         RTL
         */
        // Jump to subroutine 2B740
        patches.add(new Patch(0x22F36, Hex.parseHex("22 40 B7 C2 80 08")));
        patches.add(new Patch(0x2B740, Hex.parseHex("C0 00 64 F0 0E 98 69 20 02 A8 A2 00 F0 A9 2F 00 54 30 7F 6B")));

        // Reads names from sram
        /**
         CPY #$11C0
         BNE 35
         TXA
         CLC
         ADC #$0220
         TAX
         LDA #$002F
         MVN $00,$30
         SEP #$20
         LDA $7FF030
         CMP #$BB
         BNE 13
         LDX #$11C0
         LDY #$F000
         LDA #$2F
         PHB
         MVN $7F,$7E
         PLB
         RTL
         */
        patches.add(new Patch(0x22F13, Hex.parseHex("22 70 B7 C2 80 08")));
        //patches.add(new Patch(0x2B770, Hex.parseHex("C0 C0 11 D0 0C 8A 18 69 20 02 AA A9 2F 00 54 00 30 6B")));
        patches.add(new Patch(0x2B770, Hex.parseHex("C0 C0 11 D0 23 8A 18 69 20 02 AA A9 2F 00 54 00 30 E2 20 AF 30 F0 7F C9 BB D0 0D A2 C0 11 A0 00 F0 A9 2F 8B 54 7F 7E AB 6B")));

        // Before : C233C1  65 10          ADC $10
        patches.add(new Patch(0x233C1, Hex.parseHex("0A EA")));

        // Before : C233C9  69 40 10       ADC #$1040
        patches.add(new Patch(0x233C9, Hex.parseHex("69 C0 11")));
        // Before : C233D0  A9 05 00       LDA #$0005
        patches.add(new Patch(0x233D0, Hex.parseHex("A9 07 00")));

        /**
         SEP #$20
         PHA
         LDA #$AA
         STA $7FF030
         PLA
         
         JSR $30E4
         RTL
         */
        // Set save menu flag
        patches.add(new Patch(0x231AF, Hex.parseHex("22 60 B6 C2 EA")));
        patches.add(new Patch(0x2B660, Hex.parseHex("E2 20 48 A9 AA 8F 30 F0 7F 68 20 E4 30 6B")));

        /**
         PHA
         LDA #$BB
         STA $7FF030
         PLA
         SEP #$20
         LDA #$01
         RTL
         */
        // Set load menu flag
        patches.add(new Patch(0x22708, Hex.parseHex("22 80 B6 C2")));
        patches.add(new Patch(0x2B680, Hex.parseHex("48 A9 BB 8F 30 F0 7F 68 E2 20 A9 01 6B")));

        /**
         PHP
         REP #$30
         LDX #$B7B0
         LDY #$6010
         LDA #$000F
         PHB
         MVN $30,$C2
         LDX #$30AE
         LDY #$63A0
         LDA #$005C
         MVN $30,$C2
         PLB
         PLP
         JML $C11C43
         */
        // Init sram
        patches.add(new Patch(0x2B7B0, Hex.parseHex("44 59 4E 41 4D 49 54 52 41 43 45 52 20 20 20 20")));
        patches.add(new Patch(0xFF03, Hex.parseHex("5C C0 B7 C2")));
        patches.add(new Patch(0x2B7C0, Hex.parseHex("08 C2 30 A2 B0 B7 A0 10 60 A9 0F 00 8B 54 30 C2 A2 AE 30 A0 A0 63 A9 5C 00 54 30 C2 AB 28 5C 43 1C C1")));
    }

    public static void loadPatches2() {

        // Vehicule names
        Patch p = new Patch(0x21730, latinSmall.getCode("Cry Baby"));
        patches.add(p);
        p = new Patch(0x21740, latinSmall.getCode("Mach M."));
        patches.add(p);
        p = new Patch(0x21750, latinSmall.getCode("Lightni."));
        patches.add(p);
        p = new Patch(0x21760, latinSmall.getCode("Hell Ha."));
        patches.add(p);
        p = new Patch(0x21770, latinSmall.getCode("The Cas."));
        patches.add(p);
        p = new Patch(0x21780, latinSmall.getCode("Popuppy"));
        patches.add(p);


        p = new Patch(0x22044, latinSmall.getCode("Character Pick "));
        patches.add(p);
        
        // Character description width
        byte[] bytes = Utils.hexStringToByteArray("17 00");
        p = new Patch(x("220E0"), bytes);
        patches.add(p);

        // Intro - leading spaces
        // $C2/1EA5 A2 06 00    LDX #$0006
        bytes = Utils.hexStringToByteArray("A2 02 00");
        p = new Patch(x("21EA5"), bytes);
        patches.add(p);
    
        // 0B clearing length (from x15 to x1A, 21 to 26 chars)
        // C0 15 18    CPY #$1815
        bytes = Utils.hexStringToByteArray("C0 1A 18");
        p = new Patch(x("2145D"), bytes);
        patches.add(p);
    
        // Moving up the announcer ship sprite to make room for the text
        bytes = Utils.hexStringToByteArray("C0 28 40 1C C0 18 20 1C F0 F0 08 30 F0 F0 00 20 F0 F0 00 20 F0 F0 00 20 B8 30 60 1C C8 30 62 1C D8 30 64 1C B8 40 80 1C C8 40 82 1C D8 40 84 1C C8 20 2C 1C D8 10 24 1C D8 20 44 1C D0 10 0D 3C");
        p = new Patch(x("2F6EB"), bytes);
        patches.add(p);

        bytes = Utils.hexStringToByteArray("D0 E1");
        p = new Patch(x("3E03F"), bytes);
        patches.add(p);

        bytes = ResIO.getBinaryResource("decompressed/3E200.data").getBytes();
        String names = "Dan{00}{00}{EL}Rose{00}{EL}Early{EL}Wilde{EL}Harp{00}{EL}Jim{00}{00}{EL}";
        int i = x("281");
        for (byte b : latinSmall.getCode(names)) {
            bytes[i++] = b;
        }
        /*LzCompressor compressor = new LzCompressor();
        try {
            byte[] zeros = new byte[x("3E32A")-x("3E1D0")];
            bytes = compressor.compressData(bytes, false);
            p = new Patch(x("3E1D0"), zeros);
            patches.add(p);
            p = new Patch(x("3E1D0"), bytes);
            patches.add(p);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }


    /*// Character description width
    byte[] bytes = Utils.hexStringToByteArray("17 00");
    Patch p = new Patch(x("220E0"), bytes);
        patches.add(p);

    // 0B clearing length (from x15 to x1A, 21 to 26 chars)
    // C0 15 18    CPY #$1815
    bytes = Utils.hexStringToByteArray("1A 18");
    p = new Patch(x("2145D"), bytes);
        patches.add(p);

    // Moving up the announcer ship sprite to make room for the text
    bytes = Utils.hexStringToByteArray("C0 28 40 1C C0 18 20 1C F0 F0 08 30 F0 F0 00 20 F0 F0 00 20 F0 F0 00 20 B8 30 60 1C C8 30 62 1C D8 30 64 1C B8 40 80 1C C8 40 82 1C D8 40 84 1C C8 20 2C 1C D8 10 24 1C D8 20 44 1C D0 10 0D 3C");
    p = new Patch(x("2F6EB"), bytes);
    //patches.add(p);*/

    public static void applyPatches(byte[] data) {
        System.out.println("applyPatches");
        for (Patch patch : patches) {
            patch.applyPatch(data);
        }

    }

    public static void generateEmptyTranslationFiles(byte[] bytes, PointerTable table, Dictionary dictionary) {
        if (SKIP_FILES_GENERATION) return;
        int dataLength = 0;
        Map<Integer, PointerEntry> pointers = table.getPointers();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(String.format("src/main/resources/gen/translations/%s.txt", table.getName()));
        } catch (IOException ex) {
            Logger.getLogger(DynamiTracer.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Map.Entry<Integer, PointerEntry> e : pointers.entrySet()) {
            PointerEntry value = e.getValue();
            int offsetData = value.getValue();
            byte[] dataValue = null;
            if (table.getType() == PointerTableType.SIZE_PREFIX) {
                int length = data[offsetData];
                dataValue = DataReader.readByteCount(bytes, offsetData, length);
            } else dataValue = DataReader.readUntilEndOfLine(bytes, offsetData);
            dataLength += dataValue.length;
            String text = dictionary.getValue(dataValue);
            if (VERBOSE) System.out.println(text);
            
            String out = Constants.TRANSLATION_FILE_POINTER+"="+h(value.getOffset())+";"+h(value.getValue());
            if (pw!=null) pw.write(out+"\n");
            if (VERBOSE) System.out.println(out);

            out = Constants.TRANSLATION_FILE_DATA+"="+h(offsetData)+";"+bytesToHex(dataValue);
            if (pw!=null) pw.write(out+"\n");
            if (VERBOSE) System.out.println(out);

            out = Constants.TRANSLATION_FILE_JPN+"="+text;
            if (pw!=null) pw.write(out+"\n");
            if (VERBOSE) System.out.println(out);

            out = TARGET_LANGUAGE.getCode()+"=";
            if (pw!=null) pw.write(out+"\n");
            if (VERBOSE) System.out.println(out);

            if (pw!=null) pw.write("\n");
            if (VERBOSE) System.out.println();

        }
        System.out.println(table.getName()+" length = "+dataLength);
        if (pw!=null) {
            pw.flush();
            pw.close();
        }
    }

    private static void generateSmallLatin() {
        ResIO resIO = ResIO.getTextResource("dictionaries/small-latin-gen.txt");
        while (resIO.hasNext()) {
            String line = resIO.next().toString();
            String[] split = line.split(";");
            if (split.length>2) {
                String jpn = split[1];
                String eng = split[2];
                byte code = japaneseSmallDictionary.getSingleCode(jpn);
                System.out.println(h2(code & 0xFF) + "=" + eng);
            }
        }
    }

    public static void testDuplicate(byte[] data, int offsetA, int offsetB) {
        System.out.println("Duplicate test : "+h(offsetA)+" and "+h(offsetB));
        int count = 0;
        while (data[offsetA++]==data[offsetB++]) count++;
        System.out.println("Duplicate bytes (end "+h(offsetA)+"): "+count+" x"+h(count));
    }
    
    public static void testIntro() {
        String s = "0A FF FF FF FF FF A7 8D 81 FF BF D7 AD FB FB 0B 0A 0A FF FF 57 C9 DB AD 4B FF 89 8B BB FF 89 97 49 97 A7 0A FF FF FF FF 9F AD A7 A9 B5 FF 93 A9 D9 E1 A9 CB 0A FF FF D1 99 45 BD 65 B5 FF B3 CD 9F FF 57 5F AD FB FB 0B 0A A9 A1 E1 A9 FF 49 A9 AF 8D 65 FF AD A1 5F AD B5 97 A5 0A A7 BF CB FF 8D 59 AD 9F FF A9 A1 E1 A9 FF 97 E3 A9 97 CF 0A FF BE A2 A6 D4 7D 4C CE D6 8E 96 F8 7D 8A C8 69 B4 F8 0A FF 8B AD A1 E3 A9 FF 92 A8 BA 98 90 7D BE A2 A6 D4 81 0A 5B C9 5F AD BF D5 C9 BB FF DC F8 98 CB FF D5 C5 B1 99 F7 0B 0A 9D BB DC F8 98 81 FF 98 6B F8 66 CB FF 8D 9D A9 BB BF B5 0A FF A7 67 BD D7 59 FF A9 A1 E1 A9 BB FF 89 97 49 97 B5 0A FF B9 D1 DB FF B1 9F 8B D7 EE A6 DC 56 CE F8 EF CB 0A FF FF FF FF FF FF BF A3 91 5F 97 FB FB 0B 0A 52 F8 DA BD 65 FF D5 A1 93 C9 5F EE A6 DC 56 CE F8 EF BB 0A FF 51 AD 57 E1 A3 A5 8D FF 8B A1 CB FF 8B C9 A5 AD 97 0A FF FF FF FF FF FF 71 AC C8 A6 CB FF AF DB F7 0B 0A 52 F8 DA 97 9F FF 57 E1 C9 AD A7 FF 9D BB 71 AC C8 A6 65 0A FF FF FF 97 E3 A9 81 AD 4B FF 8D BD DB A7 AD A9 F7 0A A9 A1 E1 A9 B5 FF 45 C9 D3 AD 4B FF A9 BD DD A5 FF AD D7 AD 0A D5 CD A7 D5 FF 8B 93 8F 65 AB DF A9 FF 93 BB DC F8 98 81 0B 0A FF FF FF 5E AC B2 BC AC A6 B3 BF B5 FF 8D 91 C9 65 0A FF FF A6 DC 56 CE F8 CB FF AB A3 D3 DB FF DC F8 98 A7 0A FF AD A9 AD BF 65 FF 5E AC B2 BC AC 7D A6 DC F8 98 A7 0A FF FF FF FF FF FF D3 AD D3 AD FF 95 DD 9F F7 0B 0A 00 FF FF FF 5F DD D5 4B FF A1 E1 A9 D5 8F 99 DB B3 8B 0A FF AE C8 A6 D8 F8 99 DB D5 BB 4B FF AD B3 AD BB 65 81 A7 0A AB C1 45 BD DD 9F 4B FF 9D 93 B5 FF 8B 8F C7 8F 9B AD BB 0A FF FF D4 94 9F A1 4B FF B3 BB D9 CB FF AB 51 9F 7E 0C 0A 00 FF 57 95 A3 93 A9 AD B5 D5 FF 83 A7 97 AD FF 95 C9 8B CB 0A FF 83 E3 A9 D3 AD 97 9F FF 8B DD D7 CB FF 83 A7 43 A7 81 0A FF FF 9D C9 91 AD A7 FF AD 85 BB B9 C9 CB FF 93 D3 A5 0A FF FF FF FF FF FF 93 A9 FF C5 C9 5F FB FB 0B 0A 00 0A FF FF EE 5E AC B2 BC AC 7D A6 DC F8 94 F8 EF A7 7E 7E 0C 0A 00 93 A9 9D 8F BB FF 5E B4 AE DA 0A FF FF B3 8D A1 A1 BB FF AD 97 CB A3 4D FF D3 55 99 81 0A FF FF A9 A1 E1 A9 AD A1 BB FF 69 AC DE CC A6 7E 0A FF FF 8C CE 6D A4 C8 7D AA F8 D8 F8 4B FF AB 93 4B DD F7 0B 0A 8E D6 AC 7D 46 AC 42 F8 0A FF FF A1 A1 BB FF 8B 9F BF BB FF BC 96 C8 F7 0A FF FF 8D E1 A9 4B 9F 5F 4B FF BD 59 BD 59 BB FF 81 C1 95 F7 0A FF FF AB A7 81 FF 69 AC DE CC A6 BB FF A8 64 97 5F AD 7E 0C 0A 00 A5 A3 C7 C9 FF DE F8 5A 0A FF FF 95 C9 8B D5 8F A5 8D 81 FF 56 CE C8 8E C1 BB 0A FF FF 6B F8 AA F8 DA A7 FF A9 A1 E1 A9 AD A1 BB 0A FF FF AC AC B1 A7 93 CB FF 95 4B 99 9F D3 7E 0B 0A BC CC 80 7D D2 AA D8 0A FF FF DE F8 5A 57 BD C9 BB FF 95 AD 97 C9 95 8F F7 0A FF FF AB A3 8B AD B5 FF C5 CD A5 81 FF AA F8 D8 F8 BB 0A FF FF D6 AC A6 B4 C8 4E B5 D5 FF 83 91 CB FF A7 D7 B3 AD F7 0C 0A 00 8C CE 6D A4 C8 7D AA F8 D8 F8 0A FF FF A9 A1 E1 A9 FF 95 AD 9D 8F BB FF B1 A7 93 F7 0A FF FF 51 41 83 E3 A9 65 D5 FF 8B DD 4B FF C3 A9 97 E3 A9 0A FF FF 99 DB A7 BB FF D5 CD 6A D7 BB FF A9 C7 95 F7 0B 0A D6 AC A6 B4 C8 4E 0A FF FF A9 A1 E1 A9 FF 95 AD 9D 8F CB FF 89 93 DB BC 96 C8 F7 0A FF FF BC 96 C8 7D 98 6F CC 8E 65 81 B3 8F FF BB D9 A5 BB 0A FF FF A9 65 5F A7 81 FF AA F8 D8 F8 BB FF";
        byte[] bytes = hexStringToByteArray(s.split(" "));
        String japanese = japaneseSmallDictionary.getValue(bytes);
        System.out.println(japanese);
        String english = "Rose";
        System.out.println(english+"="+bytesToHex(latinSmall.getCode(english)));

        s = "00 00 43 08 BD 77 54 7E ED 68 7E 13 99 0D 0B 09 29 25 A6 18 C3 52 21 1D 86 40 44 28 51 4E FF 7F 00 00 43 08 BD 77 74 46 4B 29 C6 10 65 0C 9A 1C 71 10 29 08 7E 13 F9 0D F2 00 F0 39 09 1D 18 58 00 00 23 00 84 1C 46 1D E9 2D 51 4E FD 31 DE 6B 6C 08 B1 0C 18 1D 0F 01 F9 01 FE 02 AA 04 0E 01 00 00 00 08 00 14 00 1C 00 2C 01 3C 05 48 0B 54 1F 7C 1F 7C 1F 7C 08 01 52 02 5A 27 FF 43 FF 7F 00 00 00 00 DE 7B AB 34 67 28 04 1C 02 10 BC 02 54 01 EC 00 F7 00 6E 00 2D 46 87 31 02 21 15 71 00 00 43 08 BD 77 7F 1E 75 15 30 11 A9 0C 66 08 E3 67 40 36 C5 3C ED 0C 18 58 18 58 18 58 18 58 00 00 00 00 D6 5E 31 56 CE 39 09 4D 84 18 9E 17 FE 04 5E 0E D0 14 DE 0D 05 62 05 2D 02 23 FF 7F 00 00 43 08 DE 7B 10 66 08 2D A5 1C 63 14 7C 14 B2 14 B8 02 8F 01 CA 00 EB 2D 26 19 83 0C 18 58 00 00 43 08 BD 77 54 7E ED 68 7E 13 99 0D 0B 09 29 25 A6 18 C3 52 21 1D 86 40 44 28 51 4E FF 7F 00 00 43 08 BD 77 74 46 4B 29 C6 10 65 0C 9A 1C 71 10 29 08 7E 13 F9 0D F2 00 F0 39 09 1D 18 58 00 00 23 00 84 1C 46 1D E9 2D 51 4E FD 31 DE 6B 6C 08 B1 0C 18 1D 0F 01 F9 01 FE 02 AA 04 0E 01 00 00 00 08 00 14 00 1C 00 2C 01 3C 05 48 0B 54 1F 7C 1F 7C 1F 7C 08 01 52 02 5A 27 FF 43 FF 7F 00 00 00 00 DE 7B AB 34 67 28 04 1C 02 10 BC 02 54 01 EC 00 F7 00 6E 00 2D 46 87 31 02 21 15 71 00 00 43 08 BD 77 7F 1E 75 15 30 11 A9 0C 66 08 E3 67 40 36 C5 3C ED 0C 18 58 18 58 18 58 18 58 00 00 00 00 D6 5E 31 56 CE 39 09 4D 84 18 9E 17 FE 04 5E 0E D0 14 DE 0D 05 62 05 2D 02 23 FF 7F 00 00 43 08 DE 7B 10 66 08 2D A5 1C 63 14 7C 14 B2 14 B8 02 8F 01 CA 00 EB 2D 26 19 83 0C 18 58 F0 35 45 10 FF 7F 3F 4F 1F 36 D5 11 2D 09 70 6F E9 51 07 35 C7 24 AE 18 8B 49 31 42 4B 2D 8B 6A 00 38 65 0C FF 7F 7F 6B BF 52 FF 33 FF 1E 1F 0A F4 08 90 7F 47 6A 27 41 CC 00 39 01 F7 5A FF 7F F0 35 45 10 FF 7F 3F 4F 1F 36 D5 11 2D 09 70 6F E9 51 07 35 C7 24 AE 18 8B 49 31 42 4B 2D 8B 6A 00 38 65 10 FF 7F 7F 53 BF 2E DB 1D 31 1D AB 10 FF 27 BF 09 87 39 E4 1C 0D 45 AB 34 86 20 F0 62 00 38 65 10 FF 7F 7F 53 BF 2E DB 1D 31 1D AB 10 FF 27 BF 09 87 39 E4 1C 0D 45 AB 34 86 20 F0 62 00 38 00 00 FF 7F 72 13 2C 0A 67 09 7F 0B B3 09 2C 0D C9 0C 85 08 00 00 00 00 00 00 00 00 00 00 F0 35 64 10 31 46 B1 29 31 1D 0B 09 A7 04 C9 39 05 2D 84 1C 64 14 68 0C C6 28 29 25 A6 18 66 39 00 38 64 10 31 46 D1 39 71 2D 31 1A 91 11 31 05 8B 04 E9 45 44 39 A4 24 66 00 AD 00 8C 31 31 46 F0 35 64 10 31 46 B1 29 31 1D 0B 09 A7 04 C9 39 05 2D 84 1C 64 14 68 0C C6 28 29 25 A6 18 66 39 00 38 64 10 31 46 D1 2D 71 19 0E 11 31 1D A9 10 31 16 F1 04 C4 20 82 10 87 24 66 1C 43 10 89 35 00 38 64 10 31 46 D1 2D 71 19 0E 11 31 1D A9 10 31 16 F1 04 C4 20 82 10 87 24 66 1C 43 10 89 35 00 38 64 10 31 46 CA 09 26 05 06 05 D1 05 EA 04 A6 08 65 08 43 04 00 00 00 00 00 00 00 00 00 00 00 00 C5 01 00 00 00 00 00 00 00 00 00 00 00 00 13 02 5F 02 75 02 FD 02 7E 03 06 04 8B 04 17 05 9D 05 5A 07 CD 07 54 08 A2 08 1F 09 82 09 C4 09 D9 09 EC 09 10 0A 21 0A 2E 0A 79 0B 86 0B 8D 0B 93 0B 9B 0B A3 0B AE 0B B5 0B BB 0B C0 0B C6 0B CC 0B D2 0B D6 0B DF 0B EF 0B F9 0B 00 00 00 00 0A FF FF FF FF FF A7 8D 81 FF BF D7 AD FB FB 0B 0A 0A FF FF 57 C9 DB AD 4B FF 89 8B BB FF 89 97 49 97 A7 0A FF FF FF FF 9F AD A7 A9 B5 FF 93 A9 D9 E1 A9 CB 0A FF FF D1 99 45 BD 65 B5 FF B3 CD 9F FF 57 5F AD FB FB 0B 0A A9 A1 E1 A9 FF 49 A9 AF 8D 65 FF AD A1 5F AD B5 97 A5 0A A7 BF CB FF 8D 59 AD 9F FF A9 A1 E1 A9 FF 97 E3 A9 97 CF 0A FF BE A2 A6 D4 7D 4C CE D6 8E 96 F8 7D 8A C8 69 B4 F8 0A FF 8B AD A1 E3 A9 FF 92 A8 BA 98 90 7D BE A2 A6 D4 81 0A 5B C9 5F AD BF D5 C9 BB FF DC F8 98 CB FF D5 C5 B1 99 F7 0B 0A 9D BB DC F8 98 81 FF 98 6B F8 66 CB FF 8D 9D A9 BB BF B5 0A FF A7 67 BD D7 59 FF A9 A1 E1 A9 BB FF 89 97 49 97 B5 0A FF B9 D1 DB FF B1 9F 8B D7 EE A6 DC 56 CE F8 EF CB 0A FF FF FF FF FF FF BF A3 91 5F 97 FB FB 0B 0A 52 F8 DA BD 65 FF D5 A1 93 C9 5F EE A6 DC 56 CE F8 EF BB 0A FF 51 AD 57 E1 A3 A5 8D FF 8B A1 CB FF 8B C9 A5 AD 97 0A FF FF FF FF FF FF 71 AC C8 A6 CB FF AF DB F7 0B 0A 52 F8 DA 97 9F FF 57 E1 C9 AD A7 FF 9D BB 71 AC C8 A6 65 0A FF FF FF 97 E3 A9 81 AD 4B FF 8D BD DB A7 AD A9 F7 0A A9 A1 E1 A9 B5 FF 45 C9 D3 AD 4B FF A9 BD DD A5 FF AD D7 AD 0A D5 CD A7 D5 FF 8B 93 8F 65 AB DF A9 FF 93 BB DC F8 98 81 0B 0A FF FF FF 5E AC B2 BC AC A6 B3 BF B5 FF 8D 91 C9 65 0A FF FF A6 DC 56 CE F8 CB FF AB A3 D3 DB FF DC F8 98 A7 0A FF AD A9 AD BF 65 FF 5E AC B2 BC AC 7D A6 DC F8 98 A7 0A FF FF FF FF FF FF D3 AD D3 AD FF 95 DD 9F F7 0B 0A 00 FF FF FF 5F DD D5 4B FF A1 E1 A9 D5 8F 99 DB B3 8B 0A FF AE C8 A6 D8 F8 99 DB D5 BB 4B FF AD B3 AD BB 65 81 A7 0A AB C1 45 BD DD 9F 4B FF 9D 93 B5 FF 8B 8F C7 8F 9B AD BB 0A FF FF D4 94 9F A1 4B FF B3 BB D9 CB FF AB 51 9F 7E 0C 0A 00 FF 57 95 A3 93 A9 AD B5 D5 FF 83 A7 97 AD FF 95 C9 8B CB 0A FF 83 E3 A9 D3 AD 97 9F FF 8B DD D7 CB FF 83 A7 43 A7 81 0A FF FF 9D C9 91 AD A7 FF AD 85 BB B9 C9 CB FF 93 D3 A5 0A FF FF FF FF FF FF 93 A9 FF C5 C9 5F FB FB 0B 0A 00 0A FF FF EE 5E AC B2 BC AC 7D A6 DC F8 94 F8 EF A7 7E 7E 0C 0A 00 93 A9 9D 8F BB FF 5E B4 AE DA 0A FF FF B3 8D A1 A1 BB FF AD 97 CB A3 4D FF D3 55 99 81 0A FF FF A9 A1 E1 A9 AD A1 BB FF 69 AC DE CC A6 7E 0A FF FF 8C CE 6D A4 C8 7D AA F8 D8 F8 4B FF AB 93 4B DD F7 0B 0A 8E D6 AC 7D 46 AC 42 F8 0A FF FF A1 A1 BB FF 8B 9F BF BB FF BC 96 C8 F7 0A FF FF 8D E1 A9 4B 9F 5F 4B FF BD 59 BD 59 BB FF 81 C1 95 F7 0A FF FF AB A7 81 FF 69 AC DE CC A6 BB FF A8 64 97 5F AD 7E 0C 0A 00 A5 A3 C7 C9 FF DE F8 5A 0A FF FF 95 C9 8B D5 8F A5 8D 81 FF 56 CE C8 8E C1 BB 0A FF FF 6B F8 AA F8 DA A7 FF A9 A1 E1 A9 AD A1 BB 0A FF FF AC AC B1 A7 93 CB FF 95 4B 99 9F D3 7E 0B 0A BC CC 80 7D D2 AA D8 0A FF FF DE F8 5A 57 BD C9 BB FF 95 AD 97 C9 95 8F F7 0A FF FF AB A3 8B AD B5 FF C5 CD A5 81 FF AA F8 D8 F8 BB 0A FF FF D6 AC A6 B4 C8 4E B5 D5 FF 83 91 CB FF A7 D7 B3 AD F7 0C 0A 00 8C CE 6D A4 C8 7D AA F8 D8 F8 0A FF FF A9 A1 E1 A9 FF 95 AD 9D 8F BB FF B1 A7 93 F7 0A FF FF 51 41 83 E3 A9 65 D5 FF 8B DD 4B FF C3 A9 97 E3 A9 0A FF FF 99 DB A7 BB FF D5 CD 6A D7 BB FF A9 C7 95 F7 0B 0A D6 AC A6 B4 C8 4E 0A FF FF A9 A1 E1 A9 FF 95 AD 9D 8F CB FF 89 93 DB BC 96 C8 F7 0A FF FF BC 96 C8 7D 98 6F CC 8E 65 81 B3 8F FF BB D9 A5 BB 0A FF FF A9 65 5F A7 81 FF AA F8 D8 F8 BB FF 6B 2D 45 10 FF 7F 3F 4F 1F 36 D5 11 2D 09 50 6B C7 51 E5 34 C7 24 E8 20 C9 0C B5 4E 6B 2D 66 5E 00 00 45 14 FF 7F 7F 6B BF 52 FF 27 BF 1E BF 09 70 00 90 7F 47 6A 27 41 6B 2D F5 00 1F 36 C9 0C 6B 2D 45 10 FF 7F 3F 4F 1F 36 D5 11 2D 09 50 6B C7 51 E5 34 C7 24 E8 20 C9 0C B5 4E 6B 2D 66 5E 00 00 23 10 FF 7F 7F 53 BF 2E DB 1D 31 1D CD 14 FF 27 BF 09 6B 2D E4 1C B3 48 67 0C 6B 30 F0 62 6B 2D 45 10 FF 7F 3F 4F 1F 36 D5 11 2D 09 50 6B C7 51 E5 34 C7 24 E8 20 C9 0C B5 4E 6B 2D 66 5E 00 00 23 10 FF 7F 72 13 2C 0A 67 09 7F 0B B3 09 2C 0D C9 0C 85 08 E4 08 A2 04 99 0A CA 09 6B 2D 00 00 2A 2F 00 00 34 38 00 00 3D 44 00 00 4C 4D 57 56 00 00 01 02 0E 0D 00 00 00 1F 24 00 00 00 00 00 2B 30 00 00 36 3A 00 00 3E 45 00 00 50 51 5B 5A 00 0C 03 04 10 0F 18 00 00 20 25 00 00 00 2C 28 29 2E 2D 31 35 39 3C 3F 40 47 46 43 4E 4F 59 58 00 0B 05 06 12 11 17 00 1C 1D 22 21 00 00 00 2A 2B 30 2F 00 37 3B 00 41 42 49 48 00 52 53 5D 5C 00 00 07 08 14 13 00 00 00 1E 23 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 54 55 5F 5E 00 00 09 0A 16 15 00 00 00 00 00 00 00 00 E4 1C 43 08 BD 77 54 7E ED 68 7E 13 99 0D 0B 09 29 25 A6 18 C3 52 21 1D 86 40 44 28 51 4E 18 58 40 02 23 00 84 1C 46 1D E9 2D 51 4E FD 31 DE 6B 6C 08 B1 0C 18 1D 0F 01 F9 01 FE 02 AA 04 0E 01 04 00 43 08 BD 77 74 46 4B 29 C6 10 65 0C 9A 1C 71 10 29 08 7E 13 F9 0D F2 00 F0 39 09 1D 18 58 44 02 43 08 DE 7B 10 66 08 2D A5 1C 63 14 BC 00 B4 00 B8 02 8F 01 CA 00 EB 2D 26 19 83 0C E7 24 09 00 23 00 DE 7B 8F 3C 27 28 03 1C 02 10 BC 02 6B 38 EC 00 F7 00 6E 00 2D 46 87 31 02 21 15 71 49 02 43 08 BD 77 7F 1E 75 15 30 11 A9 0C 66 08 E3 67 40 36 C5 3C ED 0C 18 58 18 58 18 58 18 58";
        bytes = hexStringToByteArray(s.split(" "));
        japanese = japaneseSmallDictionary.getValue(bytes);
        System.out.println(japanese);
    }

    public static void mergeSprites() {
        mergeImages("src/main/resources/gen/small-chars", 16, "src/main/resources/merged.png");
    }

    private static void testSprites() {
        int offset = x("33A70");
        int count = 0;
        while ((count++)<80) {
            byte[] bytes = Arrays.copyOfRange(data, offset, offset + 16);
            Sprite s = new Sprite(bytes);
            String file = String.format("src/main/resources/gen/small-chars/%s.png", h(offset));
            try {
                ImageIO.write(s.getGameImage(), "png", new File(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            offset += 16;
        }
    }

    public static void loadRom() {
        try {
            data = Files.readAllBytes(new File(ROM_INPUT).toPath());
            dataInput = Files.readAllBytes(new File(ROM_INPUT).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveRom() {
        DataWriter.saveData(ROM_OUTPUT, data);
    }
    
    public static void testDecompression() {

        int pointer = x("40BFC");
        DynamiTracerLz.decompressData(data, pointer, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);
        
        /*int[] pointers = new int[]{
                x("70000"),
                x("3E200"),
                x("2741C"),
                x("29151")
        };*/

        //for (int pointer : pointers) {
            /*LzDecompressor decompressor = new LzDecompressor();
            decompressor.decompressData(data, pointer);
            byte[] decompressedData = decompressor.getDecompressedData();
            String file = String.format("src/main/resources/tmp/%s.data", h(pointer));
            DataWriter.saveData(file, decompressedData);*/
        //}


        /*List<Integer> pointers = DataReader.loadPointers(data, x("A0000"), x("A0014"));
        int shift = x("A0000");*/
        
        /*for (Integer pointer : pointers) {
            pointer += shift;
            LzDecompressor decompressor = new LzDecompressor();
            decompressor.decompressData(data, pointer);
            byte[] decompressedData = decompressor.getDecompressedData();
            String file = String.format("src/main/resources/tmp/%s.data", h(pointer));
            DataWriter.saveData(file, decompressedData);
        }*/
        
        
        
        /*ResIO resource = ResIO.getBinaryResource("decompressed/A0020.data");
        byte[] bytes = resource.getBytes();*/
    }
    
    public static void generateJapaneseTable() {
        String inputCodes = "tmp/name-input.txt";
        String inputValues = "tmp/name-input-values.txt";
        ResIO resInputCodes = ResIO.getTextResource(inputCodes);
        ResIO resInputValues = ResIO.getTextResource(inputValues);
        while (resInputCodes.hasNext() && resInputValues.hasNext()) {
            String[] codes = resInputCodes.next().toString().split(" ");
            char[] values = resInputValues.next().toString().toCharArray();
            for (int i = 0; i < codes.length; i++) {
                String code = codes[i];
                String value = "" + values[i];
                System.out.println(code+"="+value);
            }
        }
    }
    
    public static void printItemList(byte[] data) {
        int offset = 0x37000;
        int id = 0;
        while (offset<0x37600) {
            byte[] bytes = Arrays.copyOfRange(data, offset, offset + 12);
            String japanese = japaneseDictionary.getValue(bytes);
            System.out.println(String.format("Item %d\t%s", id++, japanese.trim()));
            offset += 12;
        }
    }

    public static void insertPlaceholderItemName(byte[] data) {
        int offset = 0x37000;
        int id = 0;
        byte[] bytes = new byte[12];
        Arrays.fill(bytes, (byte) 0xFF);
        while (offset<0x37600) {
            byte[] code = latin.getCode(String.format("ITEM %05d", id++));
            int x = 0;
            for (byte b : code) {
                bytes[x++] = b;
            }
            int i = offset;
            for (byte b : bytes) {
                data[i++] = b;
            }
            offset += 12;
        }
    }

    public static void insertItemName(byte[] data) {
        int offset = 0x36800;
        ResIO textResource = ResIO.getTextResource(String.format("translations/00-ITEMS-%s.txt", TARGET_LANGUAGE.getCode()));
        while (textResource.hasNext()) {
            String s = textResource.next().toString();

            byte[] bytes = new byte[16];
            Arrays.fill(bytes, (byte) 0xFF);
            
            byte[] code = latin.getCode(s.trim());
            int x = 0;
            for (byte b : code) {
                bytes[x++] = b;
            }
            int i = offset;
            for (byte b : bytes) {
                data[i++] = b;
            }
            offset += 16;
            
        }
        
    }
}
