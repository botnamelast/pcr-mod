package com.StudioFurukawa.PixelCarRacer;

import android.util.Log;

public class AutoShift {

    private static final String TAG = "PCRMOD";

    public static boolean raceStarted = false; // public biar ModMenu bisa baca
    private static int prevGear = 0;
    private static long lastShiftTime = 0L;
    private static final long SHIFT_COOLDOWN = 300L;

    public static void tick() {
        if (!ModMenu.autoShiftEnabled) {
            raceStarted = false;
            prevGear = 0;
            return;
        }

        float rpm   = MemoryUtils.getRPM();
        int gear    = MemoryUtils.getGear();
        float speed = MemoryUtils.getCarSpeed();

        // DEBUG LOG — cek di logcat tag "PCRMOD"
        Log.d(TAG, "RPM=" + rpm + " GEAR=" + gear + " SPEED=" + speed
            + " raceStarted=" + raceStarted);

        // Deteksi race start
        if (gear == 1 && prevGear == 0) {
            raceStarted = true;
            Log.d(TAG, ">>> RACE STARTED DETECTED!");
        }

        // Reset kalau balik ke netral
        if (gear == 0) {
            raceStarted = false;
        }

        prevGear = gear;

        // Update LED indicator di ModMenu
        ModMenu.updateRaceIndicator(raceStarted, rpm, gear);

        if (!raceStarted) return;
        if (gear < 1 || gear >= 6) return;

        long now = System.currentTimeMillis();
        if (now - lastShiftTime < SHIFT_COOLDOWN) return;

        float targetRPM = getTargetRPM(gear);

        if (rpm >= targetRPM) {
            Log.d(TAG, ">>> SHIFT UP! rpm=" + rpm + " target=" + targetRPM);
            MemoryUtils.setShiftUp(true);
            lastShiftTime = now;

            new android.os.Handler().postDelayed(() -> {
                MemoryUtils.setShiftUp(false);
            }, 50);
        }
    }

    private static float getTargetRPM(int gear) {
        if (ModMenu.shiftMode == 0) {
            return ModMenu.shiftRPM;
        } else {
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

    public static void reset() {
        raceStarted = false;
        prevGear = 0;
        lastShiftTime = 0L;
        ModMenu.updateRaceIndicator(false, 0, 0);
    }
}
