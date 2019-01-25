public class BitUtils {
    private static short mask[] = {128, 64, 32, 16, 8, 4, 2, 1};
    // takes in short but idx as if it were a byte
    public static boolean readNthBitFromLeft(short val, int idx) {
        return (val & mask[idx]) > 0;
    }

    public static short getNNN(short instruction) {
        return (short)(instruction & 0xFFF);
    }

    public static byte getN(short instruction) {
        return (byte)(instruction & 0b1111);
    }

    public static byte getX(short instruction) {
        return (byte)((instruction & 0x0f00) >> 8);
    }

    public static byte getY(short instruction) {
        return (byte)((instruction & 0x00f0) >> 4);
    }

    public static short getKK(short instruction) {
        return (short)(instruction & 0x00ff);
    }

    public static int getDigitOfPlaceN(int value, int N) {
        return (int)Math.floor(value / Math.pow(10, N-1)) % 10;
    }

}
