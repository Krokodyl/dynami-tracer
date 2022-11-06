import resources.ResIO;
import services.DataReader;
import services.DataWriter;
import services.lz.LzDecompressor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static services.Utils.h;
import static services.Utils.x;

public class DynamiTracer {
    
    public static byte[] data;
    
    private final static String ROM_INPUT = "D:\\git\\dynami-tracer\\roms\\work\\BS Dynami Tracer (Japan).sfc"; 
    
    public static void main(String[]args) {
        
        loadRom();
        
        //generateJapaneseTable();
        testDecompression();
    }
    
    public static void loadRom() {
        try {
            data = Files.readAllBytes(new File(ROM_INPUT).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void testDecompression() {

        /*int pointer = x("A3B28");
        LzDecompressor decompressor = new LzDecompressor();
        decompressor.decompressData(data, pointer);
        byte[] decompressedData = decompressor.getDecompressedData();
        String file = String.format("src/main/resources/tmp/%s.data", h(pointer));
        DataWriter.saveData(file, decompressedData);*/

        List<Integer> pointers = DataReader.loadPointers(data, x("A0000"), x("A0014"));
        int shift = x("A0000");
        for (Integer pointer : pointers) {
            pointer += shift;
            LzDecompressor decompressor = new LzDecompressor();
            decompressor.decompressData(data, pointer);
            byte[] decompressedData = decompressor.getDecompressedData();
            String file = String.format("src/main/resources/tmp/%s.data", h(pointer));
            DataWriter.saveData(file, decompressedData);
        }
        
        
        
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
