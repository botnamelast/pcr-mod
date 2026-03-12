package com.StudioFurukawa.PixelCarRacer;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class ModMenu {

    // === SETTINGS ===
    public static boolean modEnabled       = false;
    public static boolean autoShiftEnabled = false;
    public static boolean nosButtonEnabled = false;
    public static int shiftMode            = 0; // 0=AUTO 1=MANUAL

    // AUTO mode
    public static float shiftRPM   = 9300f;

    // MANUAL mode per gear
    public static float shift1to2  = 9300f;
    public static float shift2to3  = 9400f;
    public static float shift3to4  = 9400f;
    public static float shift4to5  = 9500f;
    public static float shift5to6  = 9500f;

    // Gear ratios
    public static float gear1      = 4.00f;
    public static float gear2      = 2.50f;
    public static float gear3      = 1.80f;
    public static float gear4      = 1.30f;
    public static float gear5      = 1.00f;
    public static float gear6      = 0.68f;
    public static float finalDrive = 5.00f;

    // === UI STATE ===
    private static boolean isMinimized = false;
    private static float posX = 50f;
    private static float posY = 150f;

    // === TICK THREAD ===
    private static Thread tickThread = null;
    private static volatile boolean threadRunning = false;

    // === DEBUG INDICATOR REFS ===
    private static TextView tvRaceStatus = null;
    private static TextView tvRPMDebug   = null;
    private static TextView tvGearDebug  = null;
    private static View     ledIndicator = null;

    // === UI REFS ===
    private static WindowManager windowManager;
    private static View modView;
    private static LinearLayout contentLayout;
    private static Button btnModeAuto;
    private static Button btnModeManual;
    private static LinearLayout panelAuto;
    private static LinearLayout panelManual;
    private static Button nosButton;

    // === UPDATE RACE INDICATOR (dipanggil dari AutoShift) ===
    public static void updateRaceIndicator(boolean racing, float rpm, int gear) {
        if (modView == null) return;
        modView.post(() -> {
            if (ledIndicator != null) {
                ledIndicator.setBackgroundColor(
                    racing ? Color.parseColor("#00FF00")
                           : Color.parseColor("#FF0000"));
            }
            if (tvRaceStatus != null) {
                tvRaceStatus.setText(racing ? "RACING" : "IDLE");
                tvRaceStatus.setTextColor(
                    racing ? Color.parseColor("#00FF00")
                           : Color.parseColor("#AAAAAA"));
            }
            if (tvRPMDebug != null) {
                tvRPMDebug.setText("RPM: " + (int) rpm);
            }
            if (tvGearDebug != null) {
                tvGearDebug.setText("GEAR: " + gear);
            }
        });
    }

    public static void attach(final Context context) {
        new Handler(context.getMainLooper()).post(() -> {
            try {
                windowManager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);

                modView = buildUI(context);

                WindowManager.LayoutParams params =
                    new WindowManager.LayoutParams(
                        620,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    );
                params.gravity = Gravity.TOP | Gravity.LEFT;
                params.x = (int) posX;
                params.y = (int) posY;

                windowManager.addView(modView, params);
                modEnabled = true;

                // START TICK THREAD
                startTickThread();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // === TICK THREAD ===
    private static void startTickThread() {
        if (tickThread != null && tickThread.isAlive()) return;
        threadRunning = true;
        tickThread = new Thread(() -> {
            Log.d("PCRMOD", "Tick thread started!");
            while (threadRunning) {
                try {
                    AutoShift.tick();
                    Thread.sleep(16); // ~60fps
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    Log.e("PCRMOD", "Tick error: " + e.getMessage());
                }
            }
            Log.d("PCRMOD", "Tick thread stopped!");
        });
        tickThread.setDaemon(true);
        tickThread.setName("PCRMod-Tick");
        tickThread.start();
    }

    private static void stopTickThread() {
        threadRunning = false;
        if (tickThread != null) {
            tickThread.interrupt();
            tickThread = null;
        }
    }

    private static View buildUI(final Context ctx) {

        // === ROOT ===
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.argb(230, 10, 10, 20));
        root.setPadding(16, 8, 16, 16);

        // === HEADER ===
        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 4, 0, 8);

        // Icon
        ImageView icon = new ImageView(ctx);
        try {
            java.io.InputStream is = ctx.getAssets().open("mod_logo.png");
            Bitmap bmp = BitmapFactory.decodeStream(is);
            icon.setImageBitmap(bmp);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LinearLayout.LayoutParams iconLP =
            new LinearLayout.LayoutParams(44, 44);
        iconLP.setMargins(0, 0, 8, 0);
        icon.setLayoutParams(iconLP);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Title
        TextView tvTitle = new TextView(ctx);
        tvTitle.setText("PCR MOD");
        tvTitle.setTextColor(Color.parseColor("#FFD700"));
        tvTitle.setTextSize(15f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button btnMin   = makeSmallBtn(ctx, "—", Color.argb(180, 60, 60, 60));
        Button btnClose = makeSmallBtn(ctx, "X", Color.argb(180, 180, 40, 40));

        header.addView(icon);
        header.addView(tvTitle);
        header.addView(btnMin);
        header.addView(btnClose);

        // === DIVIDER EMAS ===
        View divGold = makeDivider(ctx, Color.parseColor("#FFD700"), 2);

        // === CONTENT ===
        contentLayout = new LinearLayout(ctx);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(0, 8, 0, 0);

        // === DEBUG INDICATOR ROW ===
        LinearLayout debugRow = new LinearLayout(ctx);
        debugRow.setOrientation(LinearLayout.HORIZONTAL);
        debugRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams debugLP =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        debugLP.setMargins(0, 0, 0, 8);
        debugRow.setLayoutParams(debugLP);

        // LED bulat
        ledIndicator = new View(ctx);
        LinearLayout.LayoutParams ledLP =
            new LinearLayout.LayoutParams(18, 18);
        ledLP.setMargins(0, 0, 8, 0);
        ledIndicator.setLayoutParams(ledLP);
        ledIndicator.setBackgroundColor(Color.parseColor("#FF0000"));

        // Status text
        tvRaceStatus = new TextView(ctx);
        tvRaceStatus.setText("IDLE");
        tvRaceStatus.setTextColor(Color.parseColor("#AAAAAA"));
        tvRaceStatus.setTextSize(11f);
        tvRaceStatus.setTypeface(null, Typeface.BOLD);
        tvRaceStatus.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // RPM debug
        tvRPMDebug = new TextView(ctx);
        tvRPMDebug.setText("RPM: 0");
        tvRPMDebug.setTextColor(Color.parseColor("#FFD700"));
        tvRPMDebug.setTextSize(10f);
        LinearLayout.LayoutParams rpmLP =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rpmLP.setMargins(8, 0, 0, 0);
        tvRPMDebug.setLayoutParams(rpmLP);

        // Gear debug
        tvGearDebug = new TextView(ctx);
        tvGearDebug.setText("GEAR: 0");
        tvGearDebug.setTextColor(Color.parseColor("#FFD700"));
        tvGearDebug.setTextSize(10f);
        LinearLayout.LayoutParams gearLP =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        gearLP.setMargins(8, 0, 0, 0);
        tvGearDebug.setLayoutParams(gearLP);

        debugRow.addView(ledIndicator);
        debugRow.addView(tvRaceStatus);
        debugRow.addView(tvRPMDebug);
        debugRow.addView(tvGearDebug);
        contentLayout.addView(debugRow);

        contentLayout.addView(makeDivider(ctx,
            Color.argb(60, 255, 255, 255), 1));

        // -- Toggle AUTO SHIFT --
        contentLayout.addView(makeToggleRow(ctx,
            "AUTO SHIFT", autoShiftEnabled,
            (val) -> autoShiftEnabled = val));

        // -- Toggle NOS BUTTON --
        contentLayout.addView(makeToggleRow(ctx,
            "NOS BUTTON", nosButtonEnabled,
            (val) -> {
                nosButtonEnabled = val;
                nosButton.setVisibility(
                    val ? View.VISIBLE : View.GONE);
            }));

        contentLayout.addView(makeDivider(ctx,
            Color.argb(80, 255, 255, 255), 1));

        // -- Mode Selector --
        contentLayout.addView(makeModeSelector(ctx));

        contentLayout.addView(makeDivider(ctx,
            Color.argb(80, 255, 255, 255), 1));

        // -- Panel AUTO --
        panelAuto = new LinearLayout(ctx);
        panelAuto.setOrientation(LinearLayout.VERTICAL);
        panelAuto.addView(makeLabel(ctx, "AUTO MODE", "#FFD700"));
        panelAuto.addView(makeSliderRow(ctx,
            "Shift RPM", 7000f, 10500f, shiftRPM, 1f,
            (val) -> shiftRPM = val));
        panelAuto.addView(makeSliderRow(ctx,
            "Final Drive", 2.0f, 7.0f, finalDrive, 0.01f,
            (val) -> finalDrive = val));
        contentLayout.addView(panelAuto);

        // -- Panel MANUAL --
        panelManual = new LinearLayout(ctx);
        panelManual.setOrientation(LinearLayout.VERTICAL);
        panelManual.setVisibility(View.GONE);
        panelManual.addView(makeLabel(ctx, "MANUAL MODE", "#FFD700"));
        panelManual.addView(makeSliderRow(ctx,
            "1→2 RPM", 7000f, 10500f, shift1to2, 1f,
            (val) -> shift1to2 = val));
        panelManual.addView(makeSliderRow(ctx,
            "2→3 RPM", 7000f, 10500f, shift2to3, 1f,
            (val) -> shift2to3 = val));
        panelManual.addView(makeSliderRow(ctx,
            "3→4 RPM", 7000f, 10500f, shift3to4, 1f,
            (val) -> shift3to4 = val));
        panelManual.addView(makeSliderRow(ctx,
            "4→5 RPM", 7000f, 10500f, shift4to5, 1f,
            (val) -> shift4to5 = val));
        panelManual.addView(makeSliderRow(ctx,
            "5→6 RPM", 7000f, 10500f, shift5to6, 1f,
            (val) -> shift5to6 = val));
        contentLayout.addView(panelManual);

        // == NOS BUTTON ==
        nosButton = new Button(ctx);
        nosButton.setText("ACTIVATE NOS");
        nosButton.setTextColor(Color.WHITE);
        nosButton.setBackgroundColor(Color.parseColor("#CC2200"));
        nosButton.setTextSize(13f);
        nosButton.setTypeface(null, Typeface.BOLD);
        nosButton.setPadding(0, 16, 0, 16);
        nosButton.setVisibility(View.GONE);
        LinearLayout.LayoutParams nosLP =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        nosLP.setMargins(0, 12, 0, 0);
        nosButton.setLayoutParams(nosLP);
        nosButton.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                MemoryUtils.setNOS(true);
                nosButton.setBackgroundColor(Color.parseColor("#FF4400"));
            } else if (e.getAction() == MotionEvent.ACTION_UP) {
                MemoryUtils.setNOS(false);
                nosButton.setBackgroundColor(Color.parseColor("#CC2200"));
            }
            return true;
        });
        contentLayout.addView(nosButton);

        root.addView(header);
        root.addView(divGold);
        root.addView(contentLayout);

        // === DRAG ===
        root.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = e.getRawX() - posX;
                        dY = e.getRawY() - posY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        posX = e.getRawX() - dX;
                        posY = e.getRawY() - dY;
                        WindowManager.LayoutParams lp =
                            (WindowManager.LayoutParams)
                            modView.getLayoutParams();
                        lp.x = (int) posX;
                        lp.y = (int) posY;
                        windowManager.updateViewLayout(modView, lp);
                        break;
                }
                return false;
            }
        });

        // === BUTTON ACTIONS ===
        btnMin.setOnClickListener(v -> {
            isMinimized = !isMinimized;
            contentLayout.setVisibility(
                isMinimized ? View.GONE : View.VISIBLE);
            btnMin.setText(isMinimized ? "+" : "—");
        });

        btnClose.setOnClickListener(v -> {
            try {
                modEnabled = false;
                stopTickThread();
                AutoShift.reset();
                windowManager.removeView(modView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return root;
    }

    // === HELPERS ===
    private static Button makeSmallBtn(Context ctx, String text, int bgColor) {
        Button btn = new Button(ctx);
        btn.setText(text);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(bgColor);
        btn.setTextSize(12f);
        btn.setPadding(8, 0, 8, 0);
        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(56, 44);
        lp.setMargins(4, 0, 0, 0);
        btn.setLayoutParams(lp);
        return btn;
    }

    private static View makeDivider(Context ctx, int color, int height) {
        View v = new View(ctx);
        v.setBackgroundColor(color);
        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height);
        lp.setMargins(0, 6, 0, 6);
        v.setLayoutParams(lp);
        return v;
    }

    private static TextView makeLabel(Context ctx, String text, String hex) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(hex));
        tv.setTextSize(11f);
        tv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 4, 0, 4);
        tv.setLayoutParams(lp);
        return tv;
    }

    private static LinearLayout makeToggleRow(Context ctx,
            String label, boolean initial, ToggleCB cb) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 4, 0, 4);
        row.setLayoutParams(lp);

        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(13f);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Switch sw = new Switch(ctx);
        sw.setChecked(initial);
        sw.setOnCheckedChangeListener((v, checked) -> cb.on(checked));

        row.addView(tv);
        row.addView(sw);
        return row;
    }

    private static LinearLayout makeModeSelector(Context ctx) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 6, 0, 6);
        row.setLayoutParams(lp);

        TextView tv = new TextView(ctx);
        tv.setText("MODE:");
        tv.setTextColor(Color.parseColor("#AAAAAA"));
        tv.setTextSize(13f);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        btnModeAuto   = new Button(ctx);
        btnModeManual = new Button(ctx);
        btnModeAuto.setText("AUTO");
        btnModeAuto.setTextSize(11f);
        btnModeAuto.setPadding(12, 4, 12, 4);
        btnModeManual.setText("MANUAL");
        btnModeManual.setTextSize(11f);
        btnModeManual.setPadding(12, 4, 12, 4);

        updateModeButtons();

        btnModeAuto.setOnClickListener(v -> {
            shiftMode = 0;
            updateModeButtons();
            panelAuto.setVisibility(View.VISIBLE);
            panelManual.setVisibility(View.GONE);
        });
        btnModeManual.setOnClickListener(v -> {
            shiftMode = 1;
            updateModeButtons();
            panelAuto.setVisibility(View.GONE);
            panelManual.setVisibility(View.VISIBLE);
        });

        LinearLayout.LayoutParams btnLP =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnLP.setMargins(4, 0, 0, 0);
        btnModeAuto.setLayoutParams(btnLP);
        btnModeManual.setLayoutParams(btnLP);

        row.addView(tv);
        row.addView(btnModeAuto);
        row.addView(btnModeManual);
        return row;
    }

    private static void updateModeButtons() {
        if (btnModeAuto == null || btnModeManual == null) return;
        if (shiftMode == 0) {
            btnModeAuto.setBackgroundColor(Color.parseColor("#FFD700"));
            btnModeAuto.setTextColor(Color.BLACK);
            btnModeManual.setBackgroundColor(Color.argb(150, 50, 50, 50));
            btnModeManual.setTextColor(Color.WHITE);
        } else {
            btnModeManual.setBackgroundColor(Color.parseColor("#FFD700"));
            btnModeManual.setTextColor(Color.BLACK);
            btnModeAuto.setBackgroundColor(Color.argb(150, 50, 50, 50));
            btnModeAuto.setTextColor(Color.WHITE);
        }
    }

    private static LinearLayout makeSliderRow(Context ctx,
            String label, float min, float max,
            float initial, float step, SliderCB cb) {
        LinearLayout col = new LinearLayout(ctx);
   
