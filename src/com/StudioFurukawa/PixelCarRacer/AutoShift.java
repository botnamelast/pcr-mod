package com.StudioFurukawa.PixelCarRacer;

public class AutoShift {

    private static boolean raceStarted = false;
    private static int prevGear = 0;
    private static long lastShiftTime = 0L;
    private static final long SHIFT_COOLDOWN = 300L; // ms

    // Dipanggil tiap frame dari onDrawFrame
    public static void tick() {
        if (!ModMenu.autoShiftEnabled) {
            raceStarted = false;
            prevGear = 0;
            return;
        }

        float rpm  = MemoryUtils.getRPM();
        int gear   = MemoryUtils.getGear();
        float speed = MemoryUtils.getCarSpeed();

        // Deteksi race start:
        // gear berubah dari 0/N ke 1 = launch!
        if (gear == 1 && prevGear == 0) {
            raceStarted = true;
        }

        // Reset kalau balik ke netral
        if (gear == 0) {
            raceStarted = false;
        }

        prevGear = gear;

        if (!raceStarted) return;
        if (gear < 1 || gear >= 6) return;

        // Cooldown antar shift
        long now = System.currentTimeMillis();
        if (now - lastShiftTime < SHIFT_COOLDOWN) return;

        float targetRPM = getTargetRPM(gear);

        if (rpm >= targetRPM) {
            MemoryUtils.setShiftUp(true);
            lastShiftTime = now;

            // Reset shift_up setelah 50ms
            new android.os.Handler().postDelayed(() -> {
                MemoryUtils.setShiftUp(false);
            }, 50);
        }
    }

    // Ambil target RPM berdasarkan mode dan gear
    private static float getTargetRPM(int gear) {
        if (ModMenu.shiftMode == 0) {
            // AUTO = semua gear pakai RPM yang sama
            return ModMenu.shiftRPM;
        } else {
            // MANUAL = per gear
            switch (gear) {
                case 1: return ModMenu.shift1to2;
                case 2: return ModMenu.shift2to3;
                case 3: return ModMenu.shift3to4;
                case 4: return ModMenu.shift4to5;
                case 5: return ModMenu.shift5to6;
                default: return ModMenu.shiftRPM;
            }
        }
    }

    // Reset state waktu keluar race
    public static void reset() {
        raceStarted = false;
        prevGear = 0;
        lastShiftTime 
          = 0L;
    }
}
