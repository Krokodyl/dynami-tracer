package services.lz;

import compression.LzCompressor;
import compression.LzDecompressor;
import compression.REPEAT_ALGORITHM;
import compression.algorithms.DynamiTracerCompressor;

import java.io.IOException;

import static services.Utils.h;

public class DynamiTracerLz {
    
    public static byte[] decompressData(byte[] data, int offset, REPEAT_ALGORITHM ra){
        LzDecompressor decompressor = new LzDecompressor(new DynamiTracerAlgorithm(ra));
        decompressor.setVerbose(false);
        decompressor.decompressData(data, offset);
        byte[] decompressedData = decompressor.getDecompressedData();
        //String file = String.format("src/main/resources/tmp/%s.data", h(offset));
        //DataWriter.saveData(file, decompressedData);
        return decompressedData;
    }

    public static byte[] compressData(byte[] data, REPEAT_ALGORITHM ra) {
        DynamiTracerCompressor compressor = new DynamiTracerCompressor(new DynamiTracerAlgorithm(ra));
        byte[] compressedData = new byte[0];
        try {
            compressedData = compressor.compressData(data, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressedData;
    }
}
