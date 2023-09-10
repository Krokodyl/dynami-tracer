package services.lz;

import compression.REPEAT_ALGORITHM;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertArrayEquals;
import static services.Utils.x;

public class DynamiTracerLzTest extends TestCase {

    byte[] data;
    private final static String ROM_INPUT = "D:\\git\\dynami-tracer\\roms\\original\\BS Dynami Tracer (Japan).sfc";

    @Override
    public void setUp() throws Exception {
        data = Files.readAllBytes(new File(ROM_INPUT).toPath());
    }

    public void testDecompressData() {
        int pointer = x("9A800");
        //DynamiTracerLz.decompressData(data, pointer, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);
        pointer = x("7AF1B");
        DynamiTracerLz.decompressData(data, pointer, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);
    }

    @Test
    public void testDecompressData2() {
        int pointer = x("9A800");
        //DynamiTracerLz.decompressData(data, pointer, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);
        pointer = 0x9A800;
        byte[] expectedData = DynamiTracerLz.decompressData(data, pointer, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);
        byte[] compressedData = DynamiTracerLz.compressData(expectedData, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);
        byte[] decompressData = DynamiTracerLz.decompressData(compressedData, 0, REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_4BITS);
        assertArrayEquals(expectedData, decompressData);
    }
}