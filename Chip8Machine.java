import java.awt.*;
import java.io.File;
import java.util.Random;

public class Chip8Machine {
    public static short DEFAULT_CHIP8_ROM_LOCATION = 0x200;
    public static int DEFAULT_MEMORY_SIZE = 4096;
    public static int NUM_REGISTERS = 16;
    public static int STACK_SIZE = 16;
    public static int INSTRUCTION_DELAY_IN_MILLIS = 1;
    private static Color DEFAULT_FOREGROUND_COLOR = Color.GREEN;
    private static Color DEFAULT_BACKGROUND_COLOR = Color.BLACK;

    private Chip8Memory memory;
    private Chip8KeyPad keyPad;
    private Chip8ScreenData screenData;
    private Chip8Timers timers;

    private short[] V = new short[NUM_REGISTERS];
    private short[] stack = new short[STACK_SIZE];
    private int sp = -1;
    private short PC = DEFAULT_CHIP8_ROM_LOCATION;
    private short I;
    private short DT;
    private short ST;

    private boolean incrementPC = false;
    private boolean debugStepThrough = false;

    public Chip8Machine() {
        memory = new Chip8Memory(DEFAULT_MEMORY_SIZE);
        keyPad = new Chip8KeyPad();
        screenData = new Chip8ScreenData();
        timers = new Chip8Timers(this);
    }

    public Chip8ScreenData getScreenData() {
        return screenData;
    }
    public Chip8KeyPad getKeyPad() { return keyPad; }

    public void step() {
        try {
            Thread.sleep(INSTRUCTION_DELAY_IN_MILLIS);
        } catch(InterruptedException e) {
            System.out.println(String.format("error with Thread.sleep: ", e.getMessage()));
            System.exit(0);
        }
        if(debugStepThrough) {
            try {
                System.in.read();
            } catch (Exception e) {

            }
        }
        timers.update();
        if(incrementPC) {
            PC += 2;
        }
        incrementPC = true;
        short instruction = memory.getInstructionAt(PC);
        short category = (short)((instruction & 0xf000) >> 12);
        short nnn = BitUtils.getNNN(instruction);
        short n = BitUtils.getN(instruction);
        short x = BitUtils.getX(instruction);
        short y = BitUtils.getY(instruction);
        short kk = BitUtils.getKK(instruction);
        //displayDebugInfo();
        switch (category) {
            case 0:
                if (nnn == 0xE0) { // CLS
                    screenData.clearScreen();
                    break;
                } else if (nnn == 0xEE) { // RET
                    PC = stack[sp];
                    sp -= 1;
                    break;
                }
            case 1:
                // JP Address
                incrementPC = false;
                PC = nnn;
                break;
            case 2:
                // CALL address
                incrementPC = false;
                sp += 1;
                stack[sp] = PC;
                PC = nnn;
                break;
            case 3:
                // SE Vx, byte
                if (V[x] == kk) {
                    PC += 2;
                }
                break;
            case 4:
                // SNE Vx, byte
                if (V[x] != kk) {
                    PC += 2;
                }
                break;
            case 5:
                // SE Vx, Vy
                if (V[x] == V[y]) {
                    PC += 2;
                }
                break;
            case 6:
                // LD Vx, byte
                V[x] = kk;
                break;
            case 7:
                // ADD Vx, byte
                V[x] += kk;
                V[x] &= 0xff;
                break;
            case 8:
                switch (n) {
                    case 0:
                        // LD Vx, Vy
                        V[x] = V[y];
                        break;
                    case 1:
                        // OR Vx, Vy
                        V[x] = (short)(V[x] | V[y]);
                        break;
                    case 2:
                        // AND Vx, Vy
                        V[x] = (short)(V[x] & V[y]);
                        break;
                    case 3:
                        // XOR Vx, Vy
                        V[x] = (short)(V[x] ^ V[y]);
                        break;
                    case 4:
                        // ADD Vx, Vy
                        int addition = V[x] + V[y];
                        if (addition > 255) {
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        V[x] = (short)(addition & 255);
                        break;
                    case 5:
                        // SUB Vx, Vy
                        if (V[x] > V[y]) {
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        V[x] -= V[y];
                        V[x] = (short)(V[x]&0xff);
                        break;
                    case 6:
                        // SHR Vx
                        if((V[x] & 0b1) == 1) {
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        V[x] /= 2;
                        break;
                    case 7:
                        // SUBN Vx, Vy
                        if(V[y] > V[x]) {
                            V[0xF] = 1;
                        } else {
                            V[0xF] = 0;
                        }
                        V[x] = (short)((V[y] - V[x]) & 0xff);
                        break;
                    case 0xE:
                        // SHL Vx
                        if((V[x] & 1) > 0) {
                            V[0xF] ^= 1;
                        } else {
                            V[0xF] ^= 0;
                        }
                        V[x] = (short)((V[x] << 1) & 0xff);
                        break;
                }
                break;
            case 9:
                // SNE Vx, Vy
                if(V[x] != V[y]) {
                    PC += 2;
                }
                break;
            case 0xA:
                // LD I nnn
                I = nnn;
                break;
            case 0xB:
                // JP V0, addr
                incrementPC = false;
                PC = (short)(nnn + V[0]);
                break;
            case 0xC:
                //  RND Vx, byte
                Random random = new Random();
                V[x] = (short)((random.nextInt() & 0b11111111) & kk);
                break;
            case 0xD:
                // DRW Vx, Vy, n
                int[] sprite = new int[n];
                for(int i=0; i < n; i++) {
                    sprite[i] = memory.readMemory(I + i);
                }
                if(screenData.fillOutSprite(sprite, V[x], V[y])) {
                    V[0xF] = 1;
                } else {
                    V[0xF] = 0;
                }
                break;
            case 0xE:
                if(kk == 0x9E) {
                    // SKP Vx
                    //System.out.println("SKP Vx called");
                    if (keyPad.getCurrentKeyDown() == V[x]) {
                        PC += 2;
                    }
                    break;
                } else if(kk == 0xA1) {
                    // SKNP Vx
                    System.out.println("SKNP called");
                    if(keyPad.getCurrentKeyDown() != V[x]) {
                        PC += 2;
                    }
                    break;
                }
                break;
            case 0xF:
                switch(kk) {
                    case 0x07:
                        // LD Vx, DT
                        V[x] = DT;
                        break;
                    case 0x0A:
                        // LD Vx, Key
                        System.out.println("LD Vx, Key called");
                        while(true) {
                            int key = keyPad.getCurrentKeyDown();
                            System.out.println("waiting");
                            if(key != keyPad.EMPTY_KEY) {
                                System.out.println(key);
                                V[x] = (short)key;
                                break;
                            }
                        }
                    case 0x15:
                        // LD DT, Vx
                        DT = V[x];
                        break;
                    case 0x18:
                        // LD ST, Vx
                        ST = V[x];
                        break;
                    case 0x1E:
                        // ADD I, Vx
                        I = (short)(I + V[x]);
                        break;
                    case 0x29:
                        // LD F, Vx
                        I = (short)(5*V[x]);
                        break;
                    case 0x33:
                        // LD B, Vx
                        memory.writeMemory(I, (short)BitUtils.getDigitOfPlaceN(V[x], 3));
                        memory.writeMemory(I+1, (short)BitUtils.getDigitOfPlaceN(V[x], 2));
                        memory.writeMemory(I+2, (short)BitUtils.getDigitOfPlaceN(V[x], 1));
                        break;
                    case 0x55:
                        // LD [I], Vx
                        for(byte i=0; i <= x; i++) {
                            memory.writeMemory(I + i, V[i]);
                        }
                        break;
                    case 0x65:
                        // LD Vx, [I]
                        for(int i=0; i <= x; i++) {
                            V[i] = memory.readMemory(I + i);
                        }
                }
                break;
            default:
                System.out.println(String.format("Instruction not recognized: %x", instruction));
                System.exit(0);
        }
    }

    public static String getInstructionString(short instruction) {
        short category = (short) ((instruction & 0b1111000000000000) >> 12);
        short nnn = BitUtils.getNNN(instruction);
        short n = BitUtils.getN(instruction);
        short x = BitUtils.getX(instruction);
        short y = BitUtils.getY(instruction);
        short kk = BitUtils.getKK(instruction);
        if(instruction == 0) {
            return "EXIT";
        }
        switch (category) {
            case 0:
                if (nnn == 0xE0) { // CLS
                    return "CLS";
                } else if (nnn == 0xEE) { // RET
                    return "RET";
                } else { // SYS Address
                    return String.format("SYS %x", nnn);
                }
            case 1:
                // JP Address
                return String.format("JP %x", nnn);
            case 2:
                // CALL address
                return String.format("CALL %x", nnn);
            case 3:
                // SE Vx, byte
                return String.format("SE V%x %x", x, kk);
            case 4:
                // SNE Vx, byte
                return String.format("SNE V%x %x", x, kk);
            case 5:
                // SE Vx, Vy
                return String.format("SE V%x V%x", x, y);
            case 6:
                // LD Vx, byte
                return String.format("LD V%x %x", x, kk);
            case 7:
                // ADD Vx, byte
                return String.format("ADD V%x %x", x, kk);
            case 8:
                switch (n) {
                    case 0:
                        // LD Vx, Vy
                        return String.format("LD V%x V%x", x, y);
                    case 1:
                        // OR Vx, Vy
                        return String.format("OR V%x V%x", x, y);
                    case 2:
                        // AND Vx, Vy
                        return String.format("AND V%x V%x", x, y);
                    case 3:
                        // XOR Vx, Vy
                        return String.format("XOR V%x V%x", x, y);
                    case 4:
                        // ADD Vx, Vy
                        return String.format("ADD V%x V%x", x, y);
                    case 5:
                        // SUB Vx, Vy
                        return String.format("SUB V%x V%x", x, y);
                    case 6:
                        // SHR Vx
                        return String.format("SHR V%x", x);
                    case 7:
                        // SUBN Vx, Vy
                        return String.format("SUBN V%x V%x", x, y);
                    case 0xE:
                        // SHL Vx
                        return String.format("SHL V%x", x);
                }
            case 9:
                // SNE Vx, Vy
                return String.format("SNE V%x V%x", x, y);
            case 0xA:
                // LD I nnn
                return String.format("LD I %x", nnn);
            case 0xB:
                // JP V0, addr
                return String.format("JP V0, %x", nnn);
            case 0xC:
                //  RND Vx, byte
                return String.format("RND V%x %x", x, kk);
            case 0xD:
                // DRW Vx, Vy, n
                return String.format("DRW V%x V%x %x", x, y, n);
            case 0xE:
                if(kk == 0x9E) {
                    // SKP Vx
                    return String.format("SKP V%x", x);
                } else if(kk == 0xA1) {
                    // SKNP Vx
                    return String.format("SKNP V%x", x);
                }
                break;
            case 0xF:
                switch(kk) {
                    case 0x07:
                        // LD Vx, DT
                        return String.format("LD V%x DT", x);
                    case 0x0A:
                        // LD Vx, Key
                        return String.format("LD V%x KEY", x);
                    case 0x15:
                        // LD DT, Vx
                        return String.format("LD DT V%x", x);
                    case 0x18:
                        // LD ST, Vx
                        return String.format("LD ST V%x", x);
                    case 0x1E:
                        // ADD I, Vx
                        return String.format("ADD I V%x", x);
                    case 0x29:
                        // LD F, Vx
                        return String.format("LD F V%x", x);
                    case 0x33:
                        // LD B, Vx
                        return String.format("LD B, V%x", x);
                    case 0x55:
                        // LD [I], Vx
                        return String.format("LD [I] V%x", x);
                    case 0x65:
                        // LD Vx, [I]
                        return String.format("LD V%x [I]", x);
                }
        }
        return "INVALID INSTRUCTION";
    }

    public void displayDebugInfo() {
        short currentInstruction = memory.getInstructionAt(PC);
        System.out.println(String.format("current opcode: %x", currentInstruction));
        System.out.println(String.format("current instruction: %s", getInstructionString(currentInstruction)));
        System.out.println(String.format("incrementPC: %b", incrementPC));
        System.out.println("registers  stack");
        for(int i=0; i < 16; i++) {
            System.out.print(String.format("V[%d]: %x    stack index: %d: %x ", i, V[i], i, stack[i]));
            if(sp == i) {
                System.out.print("<- sp");
            }
            System.out.println();
        }
        System.out.println(String.format("PC: %x", PC));
        System.out.println(String.format("I: %x", I));
        System.out.println("\n\n");
    }

    public int getDT() {
        return DT;
    }
    public int getST() {
        return ST;
    }
    public void decDT() {
        DT -= 1;
    }
    public void decST() {
        ST -= 1;
    }
    public void loadRomFile(File romFile) {
        memory.loadRomFile(romFile);
    }

}
