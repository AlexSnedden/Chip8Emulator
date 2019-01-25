import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Screen extends JPanel {
    private static Chip8ScreenData screenData;
    private static Chip8KeyPad keyPad;
    private static final Color foregroundColor = Color.GREEN;
    private static final Color backgroundColor = Color.BLACK;
    private static int renderWidth = 64*20;
    private static int renderHeight = 32*20;

    private static int blockWidth;
    private static int blockHeight;

    public static void main(String[] args) {
        JFrame frame = new JFrame("chip8");
        frame.add(new Screen());
        frame.setSize(renderWidth, renderHeight);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Chip8Machine c8Machine = new Chip8Machine();
        screenData = c8Machine.getScreenData();
        blockWidth = (int)Math.floor(renderWidth / screenData.getWidth());
        blockHeight = (int)Math.floor(renderHeight / screenData.getHeight());
        frame.addKeyListener(c8Machine.getKeyPad());
        c8Machine.loadRomFile(new File("C:\\Users\\Alex\\Chip8Emulator\\ROMS\\INVADERS.ch8"));
        while(true) {
            c8Machine.step();
            frame.repaint();
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        for(int y=0; y < screenData.getHeight(); y++) {
            for(int x=0; x < screenData.getWidth(); x++) {
                if(screenData.getPixelValue(x, y)) {
                    g2d.setColor(foregroundColor);
                    g2d.fillRect(x*blockWidth, y*blockHeight, blockWidth, blockHeight);
                } else {
                    g2d.setColor(backgroundColor);
                    g2d.fillRect(x*blockWidth, y*blockHeight, blockWidth, blockHeight);
                }
            }
        }
    }
}