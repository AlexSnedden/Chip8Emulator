/* A class to simulate the chip8 60Hz timer
 */
public class Chip8Timers {
    private double FREQ_PER_NANOSECOND = 1.0E9/60; // 1/60 seconds worth of nanoseconds
    // needed to access DT and ST
    private Chip8Machine machine;
    private long DTstart;
    private long DTend;
    private long STstart;
    private long STend;

    int sec = 1;
    int sec1 = 1;
    private boolean DTActive = false;
    private boolean STActive = false;

    public Chip8Timers(Chip8Machine machine) {
        this.machine = machine;
    }

    private void updateDT() {
        if (machine.getDT() > 0) {
            if (DTActive) {
                DTend = System.nanoTime();
                if (DTend - DTstart >= FREQ_PER_NANOSECOND) {
                    sec++;
                    machine.decDT();
                    DTstart = System.nanoTime();
                }
            } else {
                DTActive = true;
                DTstart = System.nanoTime();
            }
        } else {
            DTActive = false;
            sec = 1;
        }
    }

    private void updateST() {
        if (machine.getST() > 0) {
            if (STActive) {
                STend = System.nanoTime();
                if (STend - STstart >= FREQ_PER_NANOSECOND) {
                    sec1++;
                    machine.decST();
                    STstart = System.nanoTime();
                }
            } else {
                STActive = true;
                STstart = System.nanoTime();
            }
        } else {
            STActive = false;
            sec1 = 1;
        }
    }

    public void update() {
        updateDT();
        updateST();
    }
}

