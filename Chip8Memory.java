import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/* A class for reading and writing to the chip8 RAM
 */
public class Chip8Memory {
    private final short[] memory;

    public Chip8Memory(int memSize) {
        memory = new short[memSize];
        initiateSpriteMemory();
    }

    private static void throwInvalidAccessException(int idx) {
        throw new IndexOutOfBoundsException(String.format("%d is an invalid memory location", idx));
    }

    private boolean validAccess(int idx) {
        if (idx < 0 || idx > memory.length) {
            return false;
        }
        return true;
    }

    public short readMemory(int idx) {
        if (validAccess(idx)) {
            return memory[idx];
        } else {
            throwInvalidAccessException(idx);
            return 0;
        }
    }

    public void writeMemory(int idx, short value) {
        if (validAccess(idx)) {
            memory[idx] = (short) (value & 0xff);
        } else {
            throwInvalidAccessException(idx);
        }
    }

    /*
    Writes 1-F character sprites to interpreter area of memory
     */
    private void initiateSpriteMemory() {
        byte[] characterSprites =
                {       // sprite 0
                        (byte) 0xf0, (byte) 0x90, (byte) 0x90,
                        (byte) 0x90, (byte) 0xf0,
                        // sprite 1
                        (byte) 0x20, (byte) 0x60, (byte) 0x20,
                        (byte) 0x20, (byte) 0x70,
                        // sprite 2
                        (byte) 0xf0, (byte) 0x10, (byte) 0xf0,
                        (byte) 0x80, (byte) 0xf0,
                        // sprite 3
                        (byte) 0xf0, (byte) 0x10, (byte) 0xf0,
                        (byte) 0x10, (byte) 0xf0,
                        // sprite 4
                        (byte) 0x90, (byte) 0x90, (byte) 0xf0,
                        (byte) 0x10, (byte) 0x10,
                        // sprite 5
                        (byte) 0xf0, (byte) 0x80, (byte) 0xf0,
                        (byte) 0x10, (byte) 0xf0,
                        // sprite 6
                        (byte) 0xf0, (byte) 0x80, (byte) 0xf0,
                        (byte) 0x90, (byte) 0xf0,
                        // sprite 7
                        (byte) 0xf0, (byte) 0x10, (byte) 0x20,
                        (byte) 0x40, (byte) 0x40,
                        // sprite 8
                        (byte) 0xf0, (byte) 0x90, (byte) 0xf0,
                        (byte) 0x90, (byte) 0xf0,
                        // sprite 9
                        (byte) 0xf0, (byte) 0x90, (byte) 0xf0,
                        (byte) 0x10, (byte) 0xf0,
                        // sprite A
                        (byte) 0xf0, (byte) 0x90, (byte) 0xf0,
                        (byte) 0x90, (byte) 0x90,
                        // sprite B
                        (byte) 0xe0, (byte) 0x90, (byte) 0xe0,
                        (byte) 0x90, (byte) 0xe0,
                        // sprite C
                        (byte) 0xf0, (byte) 0x80, (byte) 0xf0,
                        (byte) 0x80, (byte) 0xf0,
                        // sprite D
                        (byte) 0xe0, (byte) 0x90, (byte) 0x90,
                        (byte) 0x90, (byte) 0xe0,
                        // sprite E
                        (byte) 0xf0, (byte) 0x80, (byte) 0xf0,
                        (byte) 0x80, (byte) 0xf0,
                        // sprite F
                        (byte) 0xf0, (byte) 0x80, (byte) 0xf0,
                        (byte) 0x80, (byte) 0x80};
        for (int i = 0; i < characterSprites.length; i++) {
            writeMemory(i, characterSprites[i]);
        }
    }

    public void loadRomFile(File romFile) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(romFile.toPath());
        } catch (IOException e) {
            System.out.println(String.format("Issue with file: %s", e.getCause()));
            System.exit(0);
        }
        for (int i = 0; i < bytes.length; i++) {
            byte w = bytes[i];
            writeMemory(Chip8Machine.DEFAULT_CHIP8_ROM_LOCATION + i, w);
        }
    }

    public short getInstructionAt(int idx) {
        return (short) ((memory[idx] << 8) | ((memory[idx + 1] & 0xff)));
    }
}