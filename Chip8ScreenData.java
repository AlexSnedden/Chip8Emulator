/* A singleton class used to keep track of and update the screen pixel values.
   The Screen class will use this to render the chip8 screen.
 */
public class Chip8ScreenData {
    // original chip8 screen dimensions
    private static int width = 64;
    private static int height = 32;

    private int[][] data;

    public Chip8ScreenData() {
        data = new int[height][width];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private boolean legalPixelCoordinate(int x, int y) {
        if(y >= data.length || y < 0 || x >= data[0].length || x < 0) {
            return false;
        }
        return true;
    }

    private void throwOutOfBoundsException(int x, int y) {
        throw new IndexOutOfBoundsException(String.format("invalid coordinates (x,y) = (%d,%d)", x, y));
    }

    public void setPixel(int x, int y, boolean on) {
        if(legalPixelCoordinate(x,y)) {
            if(on) {
                data[y][x] = 1;
            } else {
                data[y][x] = 0;
            }
        } else {
            throwOutOfBoundsException(x, y);
        }
    }


    public void clearScreen() {
        for(int x = 0; x < data.length; x++) {
            for(int y = 0; y < data[0].length; y++) {
                data[x][y] = 0;
            }
        }
    }

    public boolean getPixelValue(int x, int y) {
        if(legalPixelCoordinate(x, y)) {
            if(data[y][x] == 1) {
                return true;
            }
            return false;
        }
        System.out.println(String.format("failed at: %d, %d", x, y));
        throw new IndexOutOfBoundsException();
    }

    /* returns true for collision, false otherwise */
    public boolean fillOutSprite(int[] sprite, int start_x, int start_y) {
        boolean collided = false;
        for (int row = 0; row < sprite.length; row++) {
            int b = sprite[row];
            for (int col = 0; col < 8; col++) {
                boolean on = BitUtils.readNthBitFromLeft((short)b, col);

                if (on) {
                    if(getPixelValue((start_x+col) % width, ((start_y+row) % height))) {
                        collided = true;
                        setPixel((start_x+col) % width, (start_y+row) % height, false);
                    } else {
                        setPixel((start_x + col) % width, (start_y + row) % height, true);
                    }
                }
            }
        }
        return collided;
    }
}
