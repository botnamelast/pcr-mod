package com.StudioFurukawa.PixelCarRacer;

import android.util.Log;
import java.io.*;
import java.util.*;

public class MemoryUtils {

    private static final String TAG     = "PCRMOD";
    private static final String LIB_NAME = "libyoyo.so";

    // Base address segments
    private static long baseExec = 0L; // r-xp (code)
    private static long baseData = 0L; // rw-p  (data/variables)

    // Offsets dari so_strings.txt reconnaissance
    // CATATAN: ini offset dari DATA segment (rw-p), bukan code segment!
    public static final long OFF_RPM         = 0x19107cL;
    public static final long OFF_RPM_GAUGE   = 0x191086L;
    public static final long OFF_CURRENTGEAR = 0x19744aL;
    public static final long OFF_GEAR_FINAL  = 0x1998d2L;
    public static final long OFF_SHIFT_UP    = 0x19e862L;
    public static final long OFF_SHIFT_DOWN  = 0x19e82cL;
    public static final long OFF_NOS         = 0x19c0feL;
    public static final long OFF_NOS_ENABLED = 0x19c13dL;
    public static final long OFF_NOS_TANK    = 0x19c1dfL;
    public static final long OFF_ENGINE_ON   = 0x198eeeL;
    public static final long OFF_PEAK_HP_RPM = 0x19cd58L;
    public static final long OFF_CAR_SPEED   = 0x19642cL;
    public static final long OFF_GEAR        = 0x199892L;

    // =========================================================
    // Ambil SEMUA mapping libyoyo.so dari /proc/self/maps
    // =========================================================
    public static void initBaseAddresses() {
        baseExec = 0L;
        baseData = 0L;

        try {
            BufferedReader reader = new BufferedReader(
                new FileReader("/proc/self/maps"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains(LIB_NAME)) continue;

                String[] parts = line.split("[-\\s]+");
                long start = Long.parseLong(parts[0], 16);
                String perms = parts[2];

                // Code segment
                if (perms.contains("r-xp") || perms.contains("r-x")) {
                    if (baseExec == 0L) {
                        baseExec = start;
                        Log.d(TAG, "libyoyo EXEC base: 0x"
                            + Long.toHexString(baseExec));
                    }
                }

                // Data segment (variables ada di sini!)
                if (perms.contains("rw-p") || perms.contains("rw-")) {
                    if (baseData == 0L) {
                        baseData = start;
                        Log.d(TAG, "libyoyo DATA base: 0x"
                            + Long.toHexString(baseData));
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "initBaseAddresses error: " + e.getMessage());
        }

        // Log semua mapping untuk debug
        logAllMappings();
    }

    // Log semua mapping libyoyo untuk debug
    private static void logAllMappings() {
        try {
            BufferedReader reader = new BufferedReader(
                new FileReader("/proc/self/maps"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(LIB_NAME)) {
                    Log.d(TAG, "MAP: " + line);
                }
            }
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "logAllMappings error: " + e.getMessage());
        }
    }

    public static long getBaseExec() {
        if (baseExec == 0L) initBaseAddresses();
        return baseExec;
    }

    public static long getBaseData() {
        if (baseData == 0L) initBaseAddresses();
        return baseData;
    }

    // =========================================================
    // Baca/tulis memory via /proc/self/mem
    // Coba DATA segment dulu, fallback ke EXEC segment
    // =========================================================
    public static float readFloat(long offset) {
        // Coba dari data segment dulu
        float val = readFloatAt(getBaseData(), offset);
        if (val != 0f) return val;

        // Fallback ke exec segment
        return readFloatAt(getBaseExec(), offset);
    }

    private static float readFloatAt(long base, long offset) {
        if (base == 0L) return 0f;
        long addr = base + offset;
        try {
            RandomAccessFile mem = new RandomAccessFile(
                "/proc/self/mem", "r");
            mem.seek(addr);
            byte[] buf = new byte[8];
            int read = mem.read(buf);
            mem.close();

            if (read < 8) return 0f;

            // GMS2 YYC = double (little-endian)
            long bits = ((long)(buf[7] & 0xFF) << 56)
                      | ((long)(buf[6] & 0xFF) << 48)
                      | ((long)(buf[5] & 0xFF) << 40)
                      | ((long)(buf[4] & 0xFF) << 32)
                      | ((long)(buf[3] & 0xFF) << 24)
                      | ((long)(buf[2] & 0xFF) << 16)
                      | ((long)(buf[1] & 0xFF) << 8)
                      | ((long)(buf[0] & 0xFF));
            double d = Double.longBitsToDouble(bits);

            // Sanity check — nilai tidak masuk akal = offset salah
            if (Double.isNaN(d) || Double.isInfinite(d)) return 0f;

            return (float) d;

        } catch (Exception e) {
            return 0f;
        }
    }

    public static void writeFloat(long offset, float value) {
        long base = getBaseData();
        if (base == 0L) base = getBaseExec();
        if (base == 0L) return;

        long addr = base + offset;
        try {
            RandomAccessFile mem = new RandomAccessFile(
                "/proc/self/mem", "rw");
            mem.seek(addr);

            long bits = Double.doubleToLongBits((double) value);
            byte[] buf = new byte[8];
            buf[0] = (byte)(bits & 0xFF);
            buf[1] = (byte)((bits >> 8) & 0xFF);
            buf[2] = (byte)((bits >> 16) & 0xFF);
            buf[3] = (byte)((bits >> 24) & 0xFF);
            buf[4] = (byte)((bits >> 32) & 0xFF);
            buf[5] = (byte)((bits >> 40) & 0xFF);
            buf[6] = (byte)((bits >> 48) & 0xFF);
            buf[7] = (byte)((bits >> 56) & 0xFF);
            mem.write(buf);
            mem.close();

        } catch (Exception e) {
            Log.e(TAG, "writeFloat error at offset 0x"
                + Long.toHexString(offset) + ": " + e.getMessage());
        }
    }

    // =========================================================
    // Public helpers
    // =========================================================
    public static float getRPM()      { return readFloat(OFF_RPM); }
    public static int   getGear()     { return (int) readFloat(OFF_CURRENTGEAR); }
    public static float getCarSpeed() { return readFloat(OFF_CAR_SPEED); }

    public static void setShiftUp(boolean val) {
        writeFloat(OFF_SHIFT_UP, val ? 1.0f : 0.0f);
    }
    public static void setShiftDown(boolean val) {
        writeFloat(OFF_SHIFT_DOWN, val ? 1.0f : 0.0f);
    }
    public static void setNOS(boolean val) {
        writeFloat(OFF_NOS_ENABLED, val ? 1.0f : 0.0f);
    }

    // Debug: log RPM, gear, speed sekaligus
    public static void logDebug() {
        Log.d(TAG, "DEBUG → RPM=" + getRPM()
            + " GEAR=" + getGear()
            + " SPEED=" + getCarSpeed()
            + " baseData=0x" + Long.toHexString(getBaseData())
            + " baseExec=0x" + Long.toHexString(getBaseExec()));
    }
}
