package com.StudioFurukawa.PixelCarRacer;

import android.util.Log;

public class ReportLog {

    // =========================================================
    // TAGS
    // =========================================================
    public static final String TAG_PCRMOD = "PCRMOD";
    public static final String TAG_RPM    = "PCRMOD_RPM";
    public static final String TAG_SHIFT  = "PCRMOD_SHIFT";
    public static final String TAG_START  = "PCRMOD_START";

    // =========================================================
    // INDEPENDENT THREAD STATE
    // =========================================================
    private static Thread reportThread      = null;
    private static volatile boolean running = false;

    // Throttle log RPM — jangan spam tiap 16ms
    private static final long RPM_LOG_INTERVAL = 500L; // log tiap 500ms
    private static long lastRpmLog = 0L;

    // Deteksi state untuk independent thread
    private static int  prevGearIndep  = -1;
    private static boolean prevRaceIndep = false;

    // =========================================================
    // START / STOP INDEPENDENT THREAD
    // Dipanggil dari ModMenu.attach() — jalan terus walaupun
    // AutoShift disabled
    // =========================================================
    public static void startReportThread() {
        if (reportThread != null && reportThread.isAlive()) return;

        running = true;
        reportThread = new Thread(() -> {
            log(TAG_PCRMOD, "ReportLog independent thread started!");

            while (running) {
                try {
                    float rpm   = MemoryUtils.getRPM();
                    int   gear  = MemoryUtils.getGear();
                    float speed = MemoryUtils.getCarSpeed();

                    // === LOG RPM (throttled) ===
                    long now = System.currentTimeMillis();
                    if (now - lastRpmLog >= RPM_LOG_INTERVAL) {
                        log(TAG_RPM, "RPM=" + (int) rpm
                            + " GEAR=" + gear
                            + " SPEED=" + String.format("%.1f", speed));
                        lastRpmLog = now;
                    }

                    // === DETEKSI RACE START (independent) ===
                    boolean raceNow = (gear == 1 && prevGearIndep == 0);
                    if (raceNow && !prevRaceIndep) {
                        log(TAG_START, "RACE START DETECTED!"
                            + " rpm=" + (int) rpm
                            + " gear=" + gear);
                        prevRaceIndep = true;
                    }
                    if (gear == 0) {
                        if (prevRaceIndep) {
                            log(TAG_START, "RACE ENDED / RESET to neutral");
                        }
                        prevRaceIndep = false;
                    }

                    prevGearIndep = gear;

                    Thread.sleep(100); // update 10x/detik
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    log(TAG_PCRMOD, "ReportLog thread error: " + e.getMessage());
                }
            }

            log(TAG_PCRMOD, "ReportLog independent thread stopped!");
        });
        reportThread.setDaemon(true);
        reportThread.setName("PCRMod-ReportLog");
        reportThread.start();
    }

    public static void stopReportThread() {
        running = false;
        if (reportThread != null) {
            reportThread.interrupt();
            reportThread = null;
        }
    }

    // =========================================================
    // DIPANGGIL DARI AutoShift.tick()
    // =========================================================

    // Log RPM + gear + speed dari AutoShift (tiap frame, throttled)
    private static long lastAutoShiftRpmLog = 0L;
    public static void logRPM(float rpm, int gear, float speed) {
        long now = System.currentTimeMillis();
        if (now - lastAutoShiftRpmLog < RPM_LOG_INTERVAL) return;
        lastAutoShiftRpmLog = now;
        log(TAG_RPM, "[AutoShift] RPM=" + (int) rpm
            + " GEAR=" + gear
            + " SPEED=" + String.format("%.1f", speed));
    }

    // Log race start/end dari AutoShift
    public static void logStart(boolean started, float rpm, int gear) {
        if (started) {
            log(TAG_START, "[AutoShift] RACE STARTED!"
                + " rpm=" + (int) rpm + " gear=" + gear);
        } else {
            log(TAG_START, "[AutoShift] RACE ENDED/RESET");
        }
    }

    // Log shift event dari AutoShift
    public static void logShift(String direction, float rpm,
            int gear, float target) {
        log(TAG_SHIFT, "[AutoShift] SHIFT " + direction
            + "! rpm=" + (int) rpm
            + " gear=" + gear
            + " target=" + (int) target);
    }

    // Log general dari mana saja
    public static void log(String tag, String msg) {
        Log.d(tag, msg);
    }
}
