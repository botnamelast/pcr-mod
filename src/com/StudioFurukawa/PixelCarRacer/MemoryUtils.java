package com.StudioFurukawa.PixelCarRacer;

import java.io.*;
import java.util.*;

public class MemoryUtils {

    private static long baseAddress = 0L;
    private static final String LIB_NAME = "libyoyo.so";

    // Offsets dari string search
    public static final long OFF_RPM          = 0x19107cL;
    public static final long OFF_RPM_GAUGE    = 0x191086L;
    public static final long OFF_CURRENTGEAR  = 0x19744aL;
    public static final long OFF_GEAR_FINAL   = 0x1998d2L;
    public static final long OFF_SHIFT_UP     = 0x19e862L;
    public static final long OFF_SHIFT_DOWN   = 0x19e82cL;
    public static final long OFF_NOS          = 0x19c0feL;
    public static final long OFF_NOS_ENABLED  = 0x19c13dL;
    public static final long OFF_NOS_TANK     = 0x19c1dfL;
    public static final long OFF_NOS_TANK_SET = 0x19c1eeL;
    public static final long OFF_ENGINE_ON    = 0x198eeeL;
    public static final long OFF_PEAK_HP_RPM  = 0x19cd58L;
    public static final long OFF_PEAK_TQ_RPM  = 0x19cd78L;
    public static final long OFF_GAS_RPM      = 0x199878L;
    public static final long OFF_GEAR         = 0x199892L;
    public static final long OFF_PREV_GEAR    = 0x19d445L;
    public static final long OFF_SHIFTER_ON   = 0x19e90bL;
    public static final long OFF_ENGINE_HP    = 0x198eb7L;
    public static final long OFF_CAR_SPEED    = 0x19642cL;
    public static final long OFF_TOPSPEED     = 0x1a062eL;

    // Dapatkan base address libyoyo.so dari /proc/self/maps
    public static long getBaseAddress() {
        if (baseAddress != 0L) return baseAddress;

        try {
            BufferedReader reader = new BufferedReader(
                new FileReader("/proc/self/maps"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(LIB_NAME) && line.contains("r-xp")) {
                    // Format: startaddr-endaddr perms offset dev inode pathname
                    String[] parts = line.split("-");
                    baseAddress = Long.parseLong(parts[0], 16);
                    reader.close();
                    return baseAddress;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    // Baca float dari memory (untuk RPM, gear, dll)
    public static float readFloat(long offset) {
        long base = getBaseAddress();
        if (base == 0L) return 0f;

        long addr = base + offset;
        try {
            RandomAccessFile mem = new RandomAccessFile(
                "/proc/self/mem", "r");
            mem.seek(addr);
            byte[] buf = new byte[8]; // double = 8 bytes di GMS2
            mem.read(buf);
            mem.close();

            // GMS2 YYC pakai double (64-bit float)
            long bits = ((long)(buf[7] & 0xFF) << 56)
                      | ((long)(buf[6] & 0xFF) << 48)
                      | ((long)(buf[5] & 0xFF) << 40)
                      | ((long)(buf[4] & 0xFF) << 32)
                      | ((long)(buf[3] & 0xFF) << 24)
                      | ((long)(buf[2] & 0xFF) << 16)
                      | ((long)(buf[1] & 0xFF) << 8)
                      | ((long)(buf[0] & 0xFF));
            return (float) Double.longBitsToDouble(bits);

        } catch (Exception e) {
            e.printStackTrace();
            return 0f;
        }
    }

    // Tulis float ke memory
    public static void writeFloat(long offset, float value) {
        long base = getBaseAddress();
        if (base == 0L) return;

        long addr = base + offset;
        try {
            RandomAccessFile mem = new RandomAccessFile(
                "/proc/self/mem", "rw");
            mem.seek(addr);

            double dval = (double) value;
            long bits = Double.doubleToLongBits(dval);
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
            e.printStackTrace();
        }
    }

    // Helper baca RPM langsung
    public static float getRPM() {
        return readFloat(OFF_RPM);
    }

    // Helper baca gear langsung
    public static int getGear() {
        return (int) readFloat(OFF_CURRENTGEAR);
    }

    // Helper set shift up
    public static void setShiftUp(boolean val) {
        writeFloat(OFF_SHIFT_UP, val ? 1.0f : 0.0f);
    }

    // Helper set shift down
    public static void setShiftDown(boolean val) {
        writeFloat(OFF_SHIFT_DOWN, val ? 1.0f : 0.0f);
    }

    // Helper set NOS
    public static void setNOS(boolean val) {
        writeFloat(OFF_NOS_ENABLED, val ? 1.0f : 0.0f);
    }

    // Helper baca car speed
    public static float getCarSpeed() {
        return readFloat(OFF_CAR_SPEED);
    }
}
