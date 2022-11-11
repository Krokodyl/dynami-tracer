import entities.Constants;
import entities.Patch;
import images.Sprite;
import resources.ResIO;
import services.*;
import services.lz.LzDecompressor;
import services.pointers.PointerEntry;
import services.pointers.PointerRange;
import services.pointers.PointerTable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static services.Utils.*;

public class DynamiTracer {
    
    public static byte[] data;

    private final static String ROM_INPUT = "D:\\git\\dynami-tracer\\roms\\original\\BS Dynami Tracer (Japan).sfc";
    private final static String ROM_OUTPUT = "D:\\git\\dynami-tracer\\roms\\work\\BS Dynami Tracer (English).sfc";

    static Dictionary japaneseDictionary = new Dictionary();
    static Dictionary japaneseSmallDictionary = new Dictionary();
    static Dictionary latinSmall = new Dictionary();

    static List<Patch> patches = new ArrayList<>();
    
    public static void main(String[]args) {
        
        loadRom();
        japaneseDictionary.loadDictionary("dictionaries/japanese.txt");
        //japanese.print();
        japaneseSmallDictionary.loadDictionary("dictionaries/small-japanese.txt");
        //japaneseSmall.print();
        latinSmall.loadDictionary("dictionaries/small-latin.txt");

        generateSmallLatin();

        ImageReader imageReader = new ImageReader();
        imageReader.generateSmallImages();

        List<BufferedImage> alphabetImages = DataReader.readAlphabetMainImages("uppercase/uppercase", Constants.COUNT_UPPERCASE);
        SpriteWriter spriteWriter = new SpriteWriter();
        spriteWriter.writeLatinCharacterSprites(alphabetImages, data);
        spriteWriter.writeSmallLatinCharacterSprites("small/small", data);


        PointerTable tableIntro = new PointerTable(x("2C9E0"), x("2C9E0"));
        tableIntro.addRange(new PointerRange(x("2C980"), x("2C983")));
        tableIntro.addRange(new PointerRange(x("2C990"), x("2C9DB")));

        tableIntro.setName("01-INTRO");
        tableIntro.loadPointers(data);
        generateEmptyTranslationFiles(data, tableIntro, japaneseSmallDictionary);
        tableIntro.loadTranslations(latinSmall);
        tableIntro.writeEnglish(data);
        
        
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
        
        testIntro();
        
        saveRom();
    }
    
    public static void loadPatches() {
        
        // Character description width
        byte[] bytes = Utils.hexStringToByteArray("17 00");
        Patch p = new Patch(x("220E0"), bytes);
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
        for (Patch patch : patches) {
            patch.applyPatch(data);
        }

    }

    public static void generateEmptyTranslationFiles(byte[] bytes, PointerTable table, Dictionary dictionary) {
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
            byte[] dataValue = DataReader.readUntilEndOfLine(bytes, offsetData);
            String text = dictionary.getValue(dataValue);
            System.out.println(text);
            
            String out = Constants.TRANSLATION_FILE_POINTER+"="+h(value.getOffset())+";"+h(value.getValue());
            if (pw!=null) pw.write(out+"\n");
            System.out.println(out);

            out = Constants.TRANSLATION_FILE_DATA+"="+h(offsetData)+";"+bytesToHex(dataValue);
            if (pw!=null) pw.write(out+"\n");
            System.out.println(out);

            out = Constants.TRANSLATION_FILE_JPN+"="+text;
            if (pw!=null) pw.write(out+"\n");
            System.out.println(out);

            out = Constants.TRANSLATION_FILE_ENG+"=";
            if (pw!=null) pw.write(out+"\n");
            System.out.println(out);

            if (pw!=null) pw.write("\n");
            System.out.println();

        }
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
    
    public static void testIntro() {
        String s = "0A FF FF FF FF FF A7 8D 81 FF BF D7 AD FB FB 0B 0A 0A FF FF 57 C9 DB AD 4B FF 89 8B BB FF 89 97 49 97 A7 0A FF FF FF FF 9F AD A7 A9 B5 FF 93 A9 D9 E1 A9 CB 0A FF FF D1 99 45 BD 65 B5 FF B3 CD 9F FF 57 5F AD FB FB 0B 0A A9 A1 E1 A9 FF 49 A9 AF 8D 65 FF AD A1 5F AD B5 97 A5 0A A7 BF CB FF 8D 59 AD 9F FF A9 A1 E1 A9 FF 97 E3 A9 97 CF 0A FF BE A2 A6 D4 7D 4C CE D6 8E 96 F8 7D 8A C8 69 B4 F8 0A FF 8B AD A1 E3 A9 FF 92 A8 BA 98 90 7D BE A2 A6 D4 81 0A 5B C9 5F AD BF D5 C9 BB FF DC F8 98 CB FF D5 C5 B1 99 F7 0B 0A 9D BB DC F8 98 81 FF 98 6B F8 66 CB FF 8D 9D A9 BB BF B5 0A FF A7 67 BD D7 59 FF A9 A1 E1 A9 BB FF 89 97 49 97 B5 0A FF B9 D1 DB FF B1 9F 8B D7 EE A6 DC 56 CE F8 EF CB 0A FF FF FF FF FF FF BF A3 91 5F 97 FB FB 0B 0A 52 F8 DA BD 65 FF D5 A1 93 C9 5F EE A6 DC 56 CE F8 EF BB 0A FF 51 AD 57 E1 A3 A5 8D FF 8B A1 CB FF 8B C9 A5 AD 97 0A FF FF FF FF FF FF 71 AC C8 A6 CB FF AF DB F7 0B 0A 52 F8 DA 97 9F FF 57 E1 C9 AD A7 FF 9D BB 71 AC C8 A6 65 0A FF FF FF 97 E3 A9 81 AD 4B FF 8D BD DB A7 AD A9 F7 0A A9 A1 E1 A9 B5 FF 45 C9 D3 AD 4B FF A9 BD DD A5 FF AD D7 AD 0A D5 CD A7 D5 FF 8B 93 8F 65 AB DF A9 FF 93 BB DC F8 98 81 0B 0A FF FF FF 5E AC B2 BC AC A6 B3 BF B5 FF 8D 91 C9 65 0A FF FF A6 DC 56 CE F8 CB FF AB A3 D3 DB FF DC F8 98 A7 0A FF AD A9 AD BF 65 FF 5E AC B2 BC AC 7D A6 DC F8 98 A7 0A FF FF FF FF FF FF D3 AD D3 AD FF 95 DD 9F F7 0B 0A 00 FF FF FF 5F DD D5 4B FF A1 E1 A9 D5 8F 99 DB B3 8B 0A FF AE C8 A6 D8 F8 99 DB D5 BB 4B FF AD B3 AD BB 65 81 A7 0A AB C1 45 BD DD 9F 4B FF 9D 93 B5 FF 8B 8F C7 8F 9B AD BB 0A FF FF D4 94 9F A1 4B FF B3 BB D9 CB FF AB 51 9F 7E 0C 0A 00 FF 57 95 A3 93 A9 AD B5 D5 FF 83 A7 97 AD FF 95 C9 8B CB 0A FF 83 E3 A9 D3 AD 97 9F FF 8B DD D7 CB FF 83 A7 43 A7 81 0A FF FF 9D C9 91 AD A7 FF AD 85 BB B9 C9 CB FF 93 D3 A5 0A FF FF FF FF FF FF 93 A9 FF C5 C9 5F FB FB 0B 0A 00 0A FF FF EE 5E AC B2 BC AC 7D A6 DC F8 94 F8 EF A7 7E 7E 0C 0A 00 93 A9 9D 8F BB FF 5E B4 AE DA 0A FF FF B3 8D A1 A1 BB FF AD 97 CB A3 4D FF D3 55 99 81 0A FF FF A9 A1 E1 A9 AD A1 BB FF 69 AC DE CC A6 7E 0A FF FF 8C CE 6D A4 C8 7D AA F8 D8 F8 4B FF AB 93 4B DD F7 0B 0A 8E D6 AC 7D 46 AC 42 F8 0A FF FF A1 A1 BB FF 8B 9F BF BB FF BC 96 C8 F7 0A FF FF 8D E1 A9 4B 9F 5F 4B FF BD 59 BD 59 BB FF 81 C1 95 F7 0A FF FF AB A7 81 FF 69 AC DE CC A6 BB FF A8 64 97 5F AD 7E 0C 0A 00 A5 A3 C7 C9 FF DE F8 5A 0A FF FF 95 C9 8B D5 8F A5 8D 81 FF 56 CE C8 8E C1 BB 0A FF FF 6B F8 AA F8 DA A7 FF A9 A1 E1 A9 AD A1 BB 0A FF FF AC AC B1 A7 93 CB FF 95 4B 99 9F D3 7E 0B 0A BC CC 80 7D D2 AA D8 0A FF FF DE F8 5A 57 BD C9 BB FF 95 AD 97 C9 95 8F F7 0A FF FF AB A3 8B AD B5 FF C5 CD A5 81 FF AA F8 D8 F8 BB 0A FF FF D6 AC A6 B4 C8 4E B5 D5 FF 83 91 CB FF A7 D7 B3 AD F7 0C 0A 00 8C CE 6D A4 C8 7D AA F8 D8 F8 0A FF FF A9 A1 E1 A9 FF 95 AD 9D 8F BB FF B1 A7 93 F7 0A FF FF 51 41 83 E3 A9 65 D5 FF 8B DD 4B FF C3 A9 97 E3 A9 0A FF FF 99 DB A7 BB FF D5 CD 6A D7 BB FF A9 C7 95 F7 0B 0A D6 AC A6 B4 C8 4E 0A FF FF A9 A1 E1 A9 FF 95 AD 9D 8F CB FF 89 93 DB BC 96 C8 F7 0A FF FF BC 96 C8 7D 98 6F CC 8E 65 81 B3 8F FF BB D9 A5 BB 0A FF FF A9 65 5F A7 81 FF AA F8 D8 F8 BB FF";
        byte[] bytes = hexStringToByteArray(s.split(" "));
        String japanese = japaneseSmallDictionary.getValue(bytes);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveRom() {
        DataWriter.saveData(ROM_OUTPUT, data);
    }
    
    public static void testDecompression() {

        //int pointer = x("242C0");
        int[] pointers = new int[]{
                x("70000"),
                x("3E200"),
                x("2741C"),
                x("29151")
        };

        for (int pointer : pointers) {
            LzDecompressor decompressor = new LzDecompressor();
            decompressor.decompressData(data, pointer);
            byte[] decompressedData = decompressor.getDecompressedData();
            String file = String.format("src/main/resources/tmp/%s.data", h(pointer));
            DataWriter.saveData(file, decompressedData);
        }


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
}
