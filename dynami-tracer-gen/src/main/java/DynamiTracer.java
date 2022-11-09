import entities.Constants;
import images.Sprite;
import resources.ResIO;
import services.*;
import services.lz.LzDecompressor;
import services.pointers.PointerEntry;
import services.pointers.PointerTable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static services.Utils.*;

public class DynamiTracer {
    
    public static byte[] data;

    private final static String ROM_INPUT = "D:\\git\\dynami-tracer\\roms\\work\\BS Dynami Tracer (Japan).sfc";
    private final static String ROM_OUTPUT = "D:\\git\\dynami-tracer\\roms\\work\\BS Dynami Tracer (English).sfc";

    static Dictionary japaneseDictionary = new Dictionary();
    static Dictionary japaneseSmallDictionary = new Dictionary();
    static Dictionary latinSmall = new Dictionary();

    
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


        PointerTable tableIntro = new PointerTable(x("2C980"), x("2C983"), x("2C9E0"), x("2C9E0"));
        PointerTable tableIntro2 = new PointerTable(x("2C990"), x("2C9DB"), x("2C9E0"), x("2C9E0"));

        tableIntro.setName("01-INTRO");
        tableIntro2.setName("02-INTRO");

        tableIntro.loadPointers(data);
        tableIntro2.loadPointers(data);

        generateEmptyTranslationFiles(data, tableIntro, japaneseSmallDictionary);
        generateEmptyTranslationFiles(data, tableIntro2, japaneseSmallDictionary);
        
        tableIntro.loadTranslations(latinSmall);
        
        tableIntro.writeEnglish(data);
        //generateJapaneseTable();
        //testDecompression();
        
        //testSprites();
        //mergeSprites();
        
        //testIntro();
        
        saveRom();
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
        String s = "0A FF FF FF FF FF A7 8D 81 FF BF D7 AD FB FB 0B 0A 0A FF FF 57 C9 DB AD 4B FF 89 8B BB FF 89 97 49 97 A7 0A FF FF FF FF 9F AD A7 A9 B5 FF 93 A9 D9 E1 A9 CB 0A FF FF D1 99 45 BD 65 B5 FF B3 CD 9F FF 57 5F AD FB FB 0B 0A A9 A1 E1 A9 FF 49 A9 AF 8D 65 FF AD A1 5F AD B5 97 A5 0A A7 BF CB FF 8D 59 AD 9F FF A9 A1 E1 A9 FF 97 E3 A9 97 CF 0A FF BE A2 A6 D4 7D 4C CE D6 8E 96 F8 7D 8A C8 69 B4 F8 0A FF 8B AD A1 E3 A9 FF 92 A8 BA 98 90 7D BE A2 A6 D4 81 0A 5B C9 5F AD BF D5 C9 BB FF DC F8 98 CB FF D5 C5 B1 99 F7 0B 0A 9D BB DC F8 98 81 FF 98 6B F8 66 CB FF 8D 9D A9 BB BF B5 0A FF A7 67 BD D7 59 FF A9 A1 E1 A9 BB FF 89 97 49 97 B5 0A FF B9 D1 DB FF B1 9F 8B D7 EE A6 DC 56 CE F8 EF CB 0A FF FF FF FF FF FF BF A3 91 5F 97 FB FB 0B 0A 52 F8 DA BD 65 FF D5 A1 93 C9 5F EE A6 DC 56 CE F8 EF BB 0A FF 51 AD 57 E1 A3 A5 8D FF 8B A1 CB FF 8B C9 A5 AD 97 0A FF FF FF FF FF FF 71 AC C8 A6 CB FF AF DB F7 0B 0A 52 F8 DA 97 9F FF 57 E1 C9 AD A7 FF 9D BB 71 AC C8 A6 65 0A FF FF FF 97 E3 A9 81 AD 4B FF 8D BD DB A7 AD A9 F7 0A A9 A1 E1 A9 B5 FF 45 C9 D3 AD 4B FF A9 BD DD A5 FF AD D7 AD 0A D5 CD A7 D5 FF 8B 93 8F 65 AB DF A9 FF 93 BB DC F8 98 81 0B 0A FF FF FF 5E AC B2 BC AC A6 B3 BF B5 FF 8D 91 C9 65 0A FF FF A6 DC 56 CE F8 CB FF AB A3 D3 DB FF DC F8 98 A7 0A FF AD A9 AD BF 65 FF 5E AC B2 BC AC 7D A6 DC F8 98 A7 0A FF FF FF FF FF FF D3 AD D3 AD FF 95 DD 9F F7 0B 0A 00 FF FF FF 5F DD D5 4B FF A1 E1 A9 D5 8F 99 DB B3 8B 0A FF AE C8 A6 D8 F8 99 DB D5 BB 4B FF AD B3 AD BB 65 81 A7 0A AB C1 45 BD DD 9F 4B FF 9D 93 B5 FF 8B 8F C7 8F 9B AD BB 0A FF FF D4 94 9F A1 4B FF B3 BB D9 CB FF AB 51 9F 7E 0C 0A 00 FF 57 95 A3 93 A9 AD B5 D5 FF 83 A7 97 AD FF 95 C9 8B CB 0A FF 83 E3 A9 D3 AD 97 9F FF 8B DD D7 CB FF 83 A7 43 A7 81 0A FF FF 9D C9 91 AD A7 FF AD 85 BB B9 C9 CB FF 93 D3 A5 0A FF FF FF FF FF FF 93 A9 FF C5 C9 5F FB FB 0B 0A 00 0A FF FF EE 5E AC B2 BC AC 7D A6 DC F8 94 F8 EF A7 7E 7E 0C 0A 00 93 A9 9D 8F BB FF 5E B4 AE DA 0A FF FF B3 8D A1 A1 BB FF AD 97 CB A3 4D FF D3 55 99 81 0A FF FF A9 A1 E1 A9 AD A1 BB FF 69 AC DE CC A6 7E 0A FF FF 8C CE 6D A4 C8 7D AA F8 D8 F8 4B FF AB 93 4B DD F7 0B 0A 8E D6 AC 7D 46 AC 42 F8 0A FF FF A1 A1 BB FF 8B 9F BF BB FF BC 96 C8 F7 0A FF FF 8D E1 A9 4B 9F 5F 4B FF BD 59 BD 59 BB FF 81 C1 95 F7 0A FF FF AB A7 81 FF 69 AC DE CC A6 BB FF A8 64 97 5F AD 7E 0C 0A 00 A5 A3 C7 C9 FF DE F8 5A 0A FF FF 95 C9 8B D5 8F A5 8D 81 FF 56 CE C8 8E C1 BB 0A FF FF 6B F8 AA F8 DA A7 FF A9 A1 E1 A9 AD A1 BB 0A FF FF AC AC B1 A7 93 CB FF 95 4B 99 9F D3 7E 0B 0A BC CC 80 7D D2 AA D8 0A FF FF DE F8 5A 57 BD C9 BB FF 95 AD 97 C9 95 8F F7 0A FF FF AB A3 8B AD B5 FF C5 CD A5 81 FF AA F8 D8 F8 BB 0A FF FF D6 AC A6 B4 C8 4E B5 D5 FF 83 91 CB FF A7 D7 B3 AD F7 0C 0A 00 8C CE 6D A4 C8 7D AA F8 D8 F8 0A FF FF A9 A1 E1 A9 FF 95 AD 9D 8F BB FF B1 A7 93 F7 0A FF FF 51 41 83 E3 A9 65 D5 FF 8B DD 4B FF C3 A9 97 E3 A9 0A FF FF 99 DB A7 BB FF D5 CD 6A D7 BB FF A9 C7 95 F7 0B 0A D6 AC A6 B4 C8 4E 0A FF FF A9 A1 E1 A9 FF 95 AD 9D 8F CB FF 89 93 DB BC 96 C8 F7 0A FF FF BC 96 C8 7D 98 6F CC 8E 65 81 B3 8F FF BB D9 A5 BB 0A FF FF A9 65 5F A7 81 FF AA F8 D8 F8 BB FF 47 C9 F7 0C 0A 00 81 8B AD A5 C9 97 FF C6 AC DA 66 0A FF FF A6 D6 CC 8E C1 DF A9 FF 92 58 D4 7D 92 C8 48 AC F7 0A FF FF 92 C6 D4 A4 BB FF 8B B1 A7 FF 9D BB FF 69 C6 F8 65 0A FF FF 97 E1 D7 41 CB FF 8B AD 8F 4F CD A5 8D 9F F7 0B 0A 86 DA 80 F8 DC F8 0A FF FF 9F AD 8D E1 A9 67 FF 69 C6 F8 81 FF 6B 8A AC A0 5F 4B 0A FF FF 98 6B F8 66 65 81 FF 89 8B BB FF BC 96 C8 B5 0A FF FF 8B B3 D9 FF B1 A7 DB 4B FB FB F7 0C 0A 00 BE D8 B0 C8 44 DC AC C8 7D 80 F8 69 F8 0A FF FF EE A1 D9 E3 8F EF 65 FF 97 E3 A9 45 A7 AD A9 0A FF FF 57 97 E3 A9 FF A9 A1 E1 A9 AD A1 BB FF 59 BB A9 CB 0A FF FF D5 A3 FF BC CC 66 7D 94 AC AE C8 A4 E6 98 A6 F7 0B 0A 54 7D 8C CE CC 98 DA 0A FF FF 8B DD BB FF AF AD A1 CB FF 9D 9D AD 5F FF BC 96 C8 F7 0A FF FF 57 E3 A9 8D E3 A9 B5 FF B1 A9 57 A5 FF 9B AD BB A9 4B 0A FF FF 87 C9 8B FF 99 DB D7 97 AD 4B FB FB F7 0C 0A 00 9F 9D 4B DD BB FF 56 D0 0A FF FF 97 E3 A9 9F AD FF 85 D3 AD BB FF 9B AD 45 A3 F7 0A FF FF 9D BB 83 A7 BF 81 FF 9F 9D 4B DD A5 AD DB BB 8B 0A FF FF B3 B5 D5 FF 8B C9 4B AF A5 AD B3 AD BB 8B FB FB 0B 0A 71 6B E0 CC 6B F8 0A FF FF AE C8 A6 D8 F8 A1 E1 A9 FF D5 CD A7 D5 B1 9D AD F7 0A FF FF C5 8F FF C7 8B D7 B3 AD FF 8B DD BB FF BC 96 C8 81 0A FF FF C1 CD 6A D9 FF C5 8F FF C7 8B D7 B3 AD FB FB 0C 0A 00 8B 8F 97 CF A7 D5 FF AA AC 66 D8 C8 4E FF 97 A3 A3 0A 98 9E F8 A6 BB FF 96 4E B2 DA CB FF AD BD C1 FF B1 9D 97 A7 0A BD CD A5 AD BD 99 7E 0B 0A 0A 0A 65 81 FF AE C8 A6 D8 F8 95 DD 9F FF 8B 8F 97 CF CB 0A 53 97 E3 A9 8B AD FF 97 A5 AD 8D BD 97 E3 A9 7E 0B 0A 0A 5A CC 90 C8 74 41 C9 7E 0A 8E D6 AC 7D 46 AC 42 F8 CB 8B DB FF 97 C9 97 C9 8D AF AD BB 0A 69 AC DE CC A6 FF EE 93 A9 9D 8F BB FF 13 EF 7E 0B 0A 0A 5A CC 90 C8 75 41 C9 7E 0A A9 A1 E1 A9 FF 95 AD 8D E3 A9 BB FF 56 CE C8 8E C1 7E 0A BC CC 80 7D D2 AA D8 B5 FF BD 9F 4B DB 81 0A EE A5 A3 C7 C9 FF 14 EF 7E 0B 0A 0A 5A CC 90 C8 76 41 C9 7E 0A AD C7 59 A7 FF 97 DD 9F FF A9 A1 E1 A9 FF 95 AD 9D 8F BB 0A 69 AC DE CC A6 7E FF AB AD 8D FF D6 AC A6 B4 C8 4E A7 0A EE 8C CE 6D A4 C8 7D 15 EF 7E 0B 0A 0A 5A CC 90 C8 77 41 C9 7E 0A AB D7 8F DD FF AB AD A5 B5 FF 89 97 8B D7 FF 89 97 87 0A AB AD 49 A9 BB FF 86 DA 80 F8 DC F8 65 FF AE C8 A6 D8 F8 0A EE 81 8B AD A5 C9 97 FF 16 EF 7E 0B 0A 0A 5A CC 90 C8 78 41 C9 7E 0A 57 97 E3 A9 FF A9 A1 E1 A9 FF 95 AD 93 A9 BB FF 59 BB A9 0A 54 7D 8C CE CC 98 DA FF AB C1 A3 DB 81 0A EE BE D8 B0 C8 44 DC AC C8 7D 17 EF 7E 0B 0A 0A 9D 97 A5 FF 5A CC 90 C8 79 41 C9 7E 0A BD CD 9F 8F FF D5 CD A5 FF 97 E3 A9 9F AD FF 85 D3 AD 0A B1 BD AF 81 FF AD CD 9F AD FF B2 C8 B3 C9 5F 7E 7F 0A 71 6B E0 CC 6B F8 BB FF EE 9F 9D 4B DD BB FF 18 EF 7E 0C 0A 0A 00 5A CC 90 C8 74 41 C9 EE 93 A9 9D 8F BB FF 13 EF 81 0A B3 8D FF A1 A1 BB FF AB AD 8D 65 FF AB 93 4B DD BB 0A 8C CE 6D A4 C8 7D AA F8 D8 F8 A7 FF 97 E3 A9 45 97 9F AD A7 0A AD CD A5 B1 D9 BD 97 9F F7 0B 0A 0A 95 AB FF 8B 91 5F 97 BB 69 AC DE CC A6 4B 0A A9 A1 E1 A9 FF 95 AD 9D 8F BB B1 A7 93 B5 FF 67 93 BD 65 0A B1 AD A3 91 DB 8B 7E 7F 0C 0A 00 5A CC 90 C8 75 41 C9 FF EE A5 A3 C7 C9 FF 14 EF 81 0A 56 CE C8 8E C1 BB FF 6B F8 AA F8 DA A7 FF 9D 97 A5 0A A9 A1 E1 A9 AD A1 BB FF AC AC B1 A7 93 CB FF 95 4B 99 9F D3 0A AE C8 A6 D8 F8 97 9F A7 FF 8D 8D BD 99 F7 0B 0A 0A 95 AB FF 81 9F 97 A5 FF 93 C9 B3 FF B1 C9 B3 B5 FF AC C0 0A A9 A1 E1 A9 CB FF BD 9F B5 8B 91 9F FF AD A1 5F AD 0A 81 B3 D0 92 95 4B 97 81 FF 9B AD 93 A9 FF 99 DB BB 8B 7E 7F 0C 0A 00 5A CC 90 C8 76 41 C9 81 FF AD C7 59 A7 97 DD 9F FF AB BB 0A EE 8C CE 6D A4 C8 7D 15 EF 65 AB D9 BD 99 4B 0A D3 AD 57 A3 A7 D5 B5 FF A9 A1 E1 A9 FF 95 AD 9D 8F BB 0A B1 A7 93 BB FF 55 CB FF D9 CD 97 E3 A9 FF 65 8D DB 8B 7E 7F 0C 0A 00 5A CC 90 C8 77 41 C9 FF A6 D6 CC 8E C1 DF A9 BB 0A EE 81 8B AD A5 C9 97 FF 16 EF 7E 0A 8B DD BB FF 86 DA 80 F8 DC F8 81 FF 98 6B F8 66 65 81 0A 8B B3 D9 BB FF 8F 9B C9 4B FF C5 9D A9 95 DD BD 99 F7 0B 0A 0A B3 5B FF 93 BB C5 A9 B3 FF D1 49 A9 B3 FF A1 E3 A9 9B C9 CB 0A 99 DB BB 8B A7 AD A9 FF 97 A3 D5 C9 B5 D5 FF D1 8F A1 B3 0A 16 81 FF BA F8 92 D2 C8 A6 65 99 F7 0C 0A 00 5A CC 90 C8 78 41 C9 0A EE BE D8 B0 C8 44 DC AC C8 7D 17 EF 81 0A A7 A3 57 E3 A7 97 A5 FF AB D7 C7 DD 9F FF B2 5C BB 0A 8B 4B 8F 97 CF F7 0B 0A 0A AB AD 97 CF BB FF 54 7D 8C CE CC 98 DA B5 81 0A A9 A1 E1 A9 BB FF AF AD A1 4B FF A3 D3 93 BD DD A5 AD DB A7 0A A8 9C 45 AD A5 B1 D9 BD 99 7E 0C 0A 00 9D DD B5 97 A5 D5 FF 5A CC 90 C8 79 41 C9 0A EE 9F 9D 4B DD BB FF 18 EF 7E 0A BC 96 C8 BB FF B3 BD AF D5 FF EE 71 6B E0 CC 6B F8 EF A7 0A BD CD 9F 8F FF C5 A9 D9 E3 A9 CB FF AF BD 9B C9 7E 0C 0A 00 0A 0A 0A 95 AB FF AD C5 AD C5 98 9E F8 A6 65 99 7E 7E 0C 0A 00 9A F8 44 99 DB A7 93 DF CB AF D7 C9 65 8F 5F 95 AD 0A 00 9A F8 44 AE D8 AA 4B AD CD 6A AD 65 99 F7 0A 91 97 A5 D5 AD AD A7 93 DF CB AF D7 C9 65 8F 5F 95 AD F7 0A 00 67 A1 D7 BB 64 F8 9E 65 81 57 D3 BD 99 8B 7F 0A 00 5E AC B2 BC AC 7D A6 DC F8 94 F8 0A 00 AB FF 8B FF 95 FF 9F FF B3 FF 81 FF BD FF C1 FF D7 FF 4B FF 55 FF 5F FF 41 FF 6A FF E5 0A AD FF 8D FF 97 FF A1 FF B5 FF 83 FF BF FF C3 FF D9 FF 4D FF 57 FF 61 FF 43 FF 6C FF E7 0A A9 FF 8F FF 99 FF A3 FF B7 FF 85 FF D1 FF C5 FF DB FF 4F FF 59 FF 63 FF 45 FF 6E FF E9 0A AF FF 91 FF 9B FF A5 FF B9 FF 87 FF D3 FF C7 FF DD FF 51 FF 5B FF 65 FF 47 FF 70 FF EB 0A B1 FF 93 FF 9D FF A7 FF BB FF 89 FF D5 FF C9 FF DF FF 53 FF 5D FF 67 FF 49 FF 72 FF ED 0A AA FF 8A FF 94 FF 9E FF B2 FF 80 FF BC FF C0 FF D6 FF 4A FF 54 FF 5E FF 40 FF 69 FF E4 0A AC FF 8C FF 96 FF A0 FF B4 FF 82 FF BE FF C2 FF D8 FF 4C FF 56 FF 60 FF 42 FF 6B FF E6 0A A8 FF 8E FF 98 FF A2 FF B6 FF 84 FF D0 FF C4 FF DA FF 4E FF 58 FF 62 FF 44 FF 6D FF E8 0A AE FF 90 FF 9A FF A4 FF B8 FF 86 FF D2 FF C6 FF DC FF 50 FF 5A FF 64 FF 46 FF 6F FF EA 0A B0 FF 92 FF 9C FF A6 FF BA FF 88 FF D4 FF C8 FF DE FF 52 FF 5C FF 66 FF 48 FF 71 FF EC 0A FF FF 68 FF CD FF CF FF E1 FF E3 FF CC FF CE FF E0 FF E2 FF 7D FF F8 FF 0D FF B1 C7 D9 0A 00 B3 BD AF CB 8D D3 A5 8F 5F 95 AD 0A 00 93 A9 9D 8F BB 0A 00 A5 A3 C7 C9 0A 00 8C CE 6D A4 C8 7D 0A 00 81 8B AD A5 C9 97 0A 00 BE D8 B0 C8 44 DC AC C8 7D 0A 00 9F 9D 4B DD BB 0A 00 5E B4 AE DA 0A 00 DE F8 5A 0A 00 AA F8 D8 F8 0A 00 C6 AC DA 66 0A 00 80 F8 69 F8 0A 00 56 D0 0A 00 93 9F AF CB 67 A9 5D 0A 00 94 F8 42 98 92 F8 66 FF FB FB FB FF 74 77 0A 00 D2 D4 97 B1 C7 CD 9F D7 0A 00 48 9E C8 CB B1 97 A5 8F 5F 95 AD F7 0A 00";
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
