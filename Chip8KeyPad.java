import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

import static java.awt.event.KeyEvent.*;

public class Chip8KeyPad implements KeyListener {
    public int EMPTY_KEY = -1;
    private HashMap<Integer, Integer> keys;
    private int currentKeyDown = EMPTY_KEY;

    public Chip8KeyPad() {
        keys = new HashMap<>();
        keys.put(VK_1, 0x1);
        keys.put(VK_2, 0x2);
        keys.put(VK_3, 0x3);
        keys.put(VK_4, 0xC);
        keys.put(VK_Q, 0x4);
        keys.put(VK_W, 0x5);
        keys.put(VK_E, 0x6);
        keys.put(VK_R, 0xD);
        keys.put(VK_A, 0x7);
        keys.put(VK_S, 0x8);
        keys.put(VK_D, 0x9);
        keys.put(VK_F, 0xE);
        keys.put(VK_Z, 0xA);
        keys.put(VK_X, 0);
        keys.put(VK_C, 0xB);
        keys.put(VK_V, 0xF);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if(keys.containsKey(key)) {
            currentKeyDown = keys.get(key);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        currentKeyDown = EMPTY_KEY;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public int getCurrentKeyDown() {
        return currentKeyDown;
    }
}
