package com.StudioFurukawa.PixelCarRacer;

import android.util.Log;

public class AutoShift {

    private static final String TAG = "PCRMOD";

    public static boolean raceStarted = false;
    private static int prevGear       = 0;
    private static long lastShiftTime = 0L;
    private static final long SHIFT_COOLDOWN = 300L;

    public static void tick() {
        float rpm   = MemoryUtils.getRPM();
        int gear    = MemoryUtils.getGear();
        float speed = MemoryUtils.getCarSpeed();

        // Log RPM tiap frame (throttled di dalam ReportLog)
        ReportLog.logRPM(rpm, gear, speed);

        // Update debug indicator di ModMenu
        ModMenu.updateRaceIndicator(raceStarted, rpm, gear);

        // Kalau AutoShift disabled, reset state tapi tetap log
        if (!ModMenu.autoShiftEnabled) {
            if (raceStarted) {
                raceStarted = false;
                prevGear = 0;
                ReportLog.logStart(false, rpm, gear);
            }
            return;
        }

        // Deteksi race start
        if (gear == 1 && prevGear == 0) {
            raceStarted = true;
            ReportLog.logStart(true, rpm, gear);
        }

        // Reset ke netral
        if (gear == 0 && raceStarted) {
            raceStarted = false;
            ReportLog.logStart(false, rpm, gear);
        }

        prevGear = gear;

        if (!raceStarted) return;
        if (gear < 1 || gear >= 6) return;

        long now = System.currentTimeMillis();
        if (now - lastShiftTime < SHIFT_COOLDOWN) return;

        float targetRPM = getTargetRPM(gear);

        if (rpm >= targetRPM) {
            ReportLog.logShift("UP", rpm, gear, targetRPM);
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
        raceStarted   = false;
        prevGear      = 0;
        lastShiftTime = 0L;
        ModMenu.updateRaceIndicator(false, 0, 0);
    }
}
