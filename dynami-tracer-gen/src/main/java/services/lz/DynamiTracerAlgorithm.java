package services.lz;

import compression.*;

public class DynamiTracerAlgorithm extends LzAlgorithm {

    public DynamiTracerAlgorithm() {
        this(REPEAT_ALGORITHM.REPEAT_ALGORITHM_SIZE_5BITS);
    }
    
    public DynamiTracerAlgorithm(REPEAT_ALGORITHM ra) {
        super(0x01, 0x00, ra);
        headerSize = 2;
        footerSize = 3;
    }

    @Override
    public RepeatCommand buildRepeatCommand(byte[] data, int offset) {
        byte a = data[offset++];
        byte b = data[offset];
        int length =  (b & 0xFF) >> repeatAlgorithm.getShift();
        //int length = ((b & 0xFF) >>> algorithm.getShift()) + 3;
        //a = (byte) (a + ((b & 0xF0) * 0x100));
        int shift = ((b & repeatAlgorithm.getMask())*0x100) + (a & 0xFF);
        length = length + 3;
        RepeatCommand repeatCommand = new RepeatCommand(shift, length, this);
        return repeatCommand;
    }

    @Override
    public byte[] getBytes(RepeatCommand repeatCommand) {
        //return super.getBytes(repeatCommand);
        byte a = (byte) (repeatCommand.getShift() & 0xFF);
        byte b = (byte) ((repeatCommand.getLength()-3 << repeatAlgorithm.getShift()) + ((repeatCommand.getShift() >> 8) & repeatAlgorithm.getMask()));
        byte[] bytes = new byte[2];
        bytes[0] = a;
        bytes[1] = b;
        return bytes;
    }

    public HeaderCommand buildHeaderCommand(byte[] data, int start) {
        int valueHeader = (data[start+1] & 0xFF)*0x100+(data[start] & 0xFF);
        //int offsetFooter = start + 2 + valueHeader;
        //int compressedLength = (data[1] & 0xFF)*x("100")+(bytes[0] & 0xFF)+headerSize+2;
        HeaderCommand headerCommand = new HeaderCommand(this);
        headerCommand.setOffsetFooter(valueHeader);
        return headerCommand;
    }

    @Override
    public FooterCommand buildFooterCommand(HeaderCommand headerCommand, byte[] data, int start, int offset) {
        int count = (data[offset] & 0xFF) & 0x3F;
        int valueHeader = (data[offset+2] & 0xFF)*0x100+(data[offset+1] & 0xFF);
        FooterCommand footerCommand = new FooterCommand(count, valueHeader, this);
        return footerCommand;
    }

    @Override
    public byte[] getBytes(FooterCommand footerCommand) {
        byte[] bytes = new byte[3];
        bytes[0] = (byte) (footerCommand.getCommandCount() + 0x40);
        bytes[1] = (byte) ((footerCommand.getOffset() % 0x100) & 0xFF);
        bytes[2] = (byte) ((footerCommand.getOffset() / 0x100) & 0xFF);
        return bytes;
    }

    @Override
    public byte[] getBytes(HeaderCommand headerCommand) {
        byte[] bytes = new byte[2];
        int a = headerCommand.getOffsetFooter() % 0x100;
        int b = headerCommand.getOffsetFooter() / 0x100;
        bytes[0] = (byte) (a & 0xFF);
        bytes[1] = (byte) (b & 0xFF);
        return bytes;
    }

    @Override
    public boolean endDecompression(HeaderCommand headerCommand, int decompressedLength, int compressedLength, int flagCount) {
        if (headerCommand.getDecompressedLength()>0) return (decompressedLength<headerCommand.getDecompressedLength());
        else if (headerCommand.getCompressedLength()>0) return (compressedLength>=headerCommand.getCompressedLength());
        return false;
    }

    @Override
    public boolean footerReached(HeaderCommand headerCommand, int start, int offset, int flagCount) {
        return headerCommand.hasFooter() && start + headerCommand.getOffsetFooter() + 2 <= offset;
    }

    public boolean hasTerminalByte() {
        return true;
    }

    public byte getTerminalByte() {
        return 0x40;
    }
}
