package com.StudioFurukawa.PixelCarRacer;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.view.*;
import android.widget.*;

public class ModMenu {

    // === SETTINGS ===
    public static boolean modEnabled       = false;
    public static boolean autoShiftEnabled = false;
    public static boolean nosEnabled       = false;
    public static int shiftMode            = 0; // 0=AUTO 1=MANUAL

    // AUTO mode
    public static float shiftRPM   = 9300f;
    public static float finalDrive = 5.00f;

    // MANUAL mode per gear
    public static float shift1to2 = 9300f;
    public static float shift2to3 = 9400f;
    public static float shift3to4 = 9400f;
    public static float shift4to5 = 9500f;
    public static float shift5to6 = 9500f;

    // === UI STATE ===
    private static boolean isMinimized = false;
    private static float posX = 50f;
    private static float posY = 150f;

    // === UI REFS ===
    private static WindowManager windowManager;
    private static View modView;
    private static LinearLayout contentLayout;
    private static Button btnModeAuto;
    private static Button btnModeManual;
    private static LinearLayout panelAuto;
    private static LinearLayout panelManual;
    private static Button nosButton;

    // === INDICATOR REFS ===
    private static View ledRace;        // LED merah/hijau race status
    private static TextView tvRaceStatus; // teks status
    private static TextView tvDebugInfo;  // RPM/Gear debug realtime
    private static Handler uiHandler;

    // === ATTACH ===
    public static void attach(final Context context) {
        uiHandler = new Handler(context.getMainLooper());
        uiHandler.post(() -> {
            try {
                windowManager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);

                modView = buildUI(context);

                WindowManager.LayoutParams params =
                    new WindowManager.LayoutParams(
                        640,
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

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // === UPDATE INDICATOR (dipanggil dari AutoShift.tick()) ===
    public static void updateRaceIndicator(final boolean started,
            final float rpm, final int gear) {
        if (uiHandler == null) return;
        uiHandler.post(() -> {
            if (ledRace != null) {
                ledRace.setBackgroundColor(started
                    ? Color.parseColor("#00FF44")   // hijau = race on
                    : Color.parseColor("#FF2222"));  // merah = belum
            }
            if (tvRaceStatus != null) {
                tvRaceStatus.setText(started ? "RACING" : "IDLE");
                tvRaceStatus.setTextColor(started
                    ? Color.parseColor("#00FF44")
                    : Color.parseColor("#FF2222"));
            }
            if (tvDebugInfo != null) {
                tvDebugInfo.setText(
                    "RPM: " + (int)rpm + "  |  GEAR: " + gear);
            }
        });
    }

    // === BUILD UI ===
    private static View buildUI(final Context ctx) {

        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.argb(230, 10, 10, 20));
        root.setPadding(16, 8, 16, 16);

        // === HEADER ===
        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 4, 0, 8);

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

        TextView tvTitle = new TextView(ctx);
        tvTitle.setText("PCR MOD");
        tvTitle.setTextColor(Color.parseColor("#FFD700"));
        tvTitle.setTextSize(15f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Button btnMin = makeSmallBtn(ctx, "—",
            Color.argb(180, 60, 60, 60));
        Button btnClose = makeSmallBtn(ctx, "X",
            Color.argb(180, 180, 40, 40));

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

        // === RACE STATUS INDICATOR ===
        LinearLayout statusRow = new LinearLayout(ctx);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams statusLP =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        statusLP.setMargins(0, 4, 0, 8);
        statusRow.setLayoutParams(statusLP);

        // LED bulat
        ledRace = new View(ctx);
        ledRace.setBackgroundColor(Color.parseColor("#FF2222"));
        LinearLayout.LayoutParams ledLP =
            new LinearLayout.LayoutParams(20, 20);
        ledLP.setMargins(0, 0, 8, 0);
        ledRace.setLayoutParams(ledLP);

        tvRaceStatus = new TextView(ctx);
        tvRaceStatus.setText("IDLE");
        tvRaceStatus.setTextColor(Color.parseColor("#FF2222"));
        tvRaceStatus.setTextSize(12f);
        tvRaceStatus.setTypeface(null, Typeface.BOLD);
        tvRaceStatus.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // Debug RPM/Gear
        tvDebugInfo = new TextView(ctx);
        tvDebugInfo.setText("RPM: 0  |  GEAR: 0");
        tvDebugInfo.setTextColor(Color.parseColor("#888888"));
        tvDebugInfo.setTextSize(11f);

        statusRow.addView(ledRace);
        statusRow.addView(tvRaceStatus);
        statusRow.addView(tvDebugInfo);
        contentLayout.addView(statusRow);

        contentLayout.addView(makeDivider(ctx,
            Color.argb(60, 255, 255, 255), 1));

        // === TOGGLE AUTO SHIFT ===
        contentLayout.addView(makeToggleRow(ctx,
            "AUTO SHIFT", autoShiftEnabled,
            (val) -> autoShiftEnabled = val));

        // === TOGGLE NOS ===
        contentLayout.addView(makeToggleRow(ctx,
            "NOS BUTTON", nosEnabled,
            (val) -> {
                nosEnabled = val;
                if (nosButton != null) {
                    nosButton.setVisibility(
                        val ? View.VISIBLE : View.GONE);
                }
            }));

        contentLayout.addView(makeDivider(ctx,
            Color.argb(80, 255, 255, 255), 1));

        // === MODE SELECTOR ===
        contentLayout.addView(makeModeSelector(ctx));

        contentLayout.addView(makeDivider(ctx,
            Color.argb(80, 255, 255, 255), 1));

        // === PANEL AUTO ===
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

        // === PANEL MANUAL ===
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

        // === NOS BUTTON (hold) ===
        nosButton = new Button(ctx);
        nosButton.setText("💨  HOLD = ACTIVATE NOS");
        nosButton.setTextColor(Color.WHITE);
        nosButton.setBackgroundColor(Color.parseColor("#CC2200"));
        nosButton.setTextSize(13f);
        nosButton.setTypeface(null, Typeface.BOLD);
        nosButton.setPadding(0, 20, 0, 20);
        nosButton.setVisibility(View.GONE); // default hidden
        LinearLayout.LayoutParams nosLP =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        nosLP.setMargins(0, 12, 0, 0);
        nosButton.setLayoutParams(nosLP);
        nosButton.setOnTouchListener((v, e) -> {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                MemoryUtils.setNOS(true);
                nosButton.setBackgroundColor(
                    Color.parseColor("#FF6600"));
                nosButton.setText("💨  NOS ACTIVE!");
            } else if (e.getAction() == MotionEvent.ACTION_UP
                    || e.getAction() == MotionEvent.ACTION_CANCEL) {
                MemoryUtils.setNOS(false);
                nosButton.setBackgroundColor(
                    Color.parseColor("#CC2200"));
                nosButton.setText("💨  HOLD = ACTIVATE NOS");
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

        // === HEADER BUTTONS ===
        btnMin.setOnClickListener(v -> {
            isMinimized = !isMinimized;
            contentLayout.setVisibility(
                isMinimized ? View.GONE : View.VISIBLE);
            btnMin.setText(isMinimized ? "+" : "—");
        });

        btnClose.setOnClickListener(v -> {
            try {
                modEnabled = false;
                AutoShift.reset();
                windowManager.removeView(modView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return root;
    }

    // === HELPERS ===

    private static Button makeSmallBtn(Context ctx,
            String text, int bgColor) {
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

    private static View makeDivider(Context ctx,
            int color, int height) {
        View v = new View(ctx);
        v.setBackgroundColor(color);
        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height);
        lp.setMargins(0, 6, 0, 6);
        v.setLayoutParams(lp);
        return v;
    }

    private static TextView makeLabel(Context ctx,
            String text, String hexColor) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(hexColor));
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
        row.setBackgroundColor(Color.argb(40, 255, 255, 255));
        row.setPadding(8, 10, 8, 10);
        LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 3, 0, 3);
        row.setLayoutParams(lp);

        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(13f);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // Pakai Button sebagai toggle (lebih visible dari Switch)
        final boolean[] state = {initial};
        Button btnToggle = new Button(ctx);
        btnToggle.setText(initial ? "ON" : "OFF");
        btnToggle.setTextSize(11f);
        btnToggle.setTypeface(null, Typeface.BOLD);
        btnToggle.setPadding(16, 4, 16, 4);
        btnToggle.setBackgroundColor(initial
            ? Color.parseColor("#00AA44")
            : Color.parseColor("#555555"));
        btnToggle.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams btnLP =
            new LinearLayout.LayoutParams(100,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnToggle.setLayoutParams(btnLP);
        btnToggle.setOnClickListener(v -> {
            state[0] = !state[0];
            btnToggle.setText(state[0] ? "ON" : "OFF");
            btnToggle.setBackgroundColor(state[0]
                ? Color.parseColor("#00AA44")
                : Color.parseColor("#555555"));
            cb.on(state[0]);
        });

        row.addView(tv);
        row.addView(btnToggle);
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

        btnModeAuto = new Button(ctx);
        btnModeAuto.setText("AUTO");
        btnModeAuto.setTextSize(11f);
        btnModeAuto.setPadding(12, 4, 12, 4);

        btnModeManual = new Button(ctx);
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
        col.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colLP =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        colLP.setMargins(0, 4, 0, 4);
        col.setLayoutParams(colLP);

        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvLabel = new TextView(ctx);
        tvLabel.setText(label + ":");
        tvLabel.setTextColor(Color.parseColor("#AAAAAA"));
        tvLabel.setTextSize(12f);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvVal = new TextView(ctx);
        if (step >= 1f) {
            tvVal.setText(String.valueOf((int) initial));
        } else {
            tvVal.setText(String.format("%.2f", initial));
        }
        tvVal.setTextColor(Color.parseColor("#FFD700"));
        tvVal.setTextSize(12f);
        tvVal.setTypeface(null, Typeface.BOLD);
        tvVal.setMinWidth(80);
        tvVal.setGravity(Gravity.END);

        row.addView(tvLabel);
        row.addView(tvVal);

        SeekBar seekBar = new SeekBar(ctx);
        int steps = (int)((max - min) / step);
        seekBar.setMax(steps);
        seekBar.setProgress((int)((initial - min) / step));
        seekBar.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar sb,
                    int progress, boolean fromUser) {
                float val = min + (progress * step);
                if (step >= 1f) {
                    tvVal.setText(String.valueOf((int) val));
                } else {
                    tvVal.setText(String.format("%.2f", val));
                }
                cb.on(val);
            }
            public void onStartTrackingTouch(SeekBar sb) {}
            public void onStopTrackingTouch(SeekBar sb) {}
        });

        col.addView(row);
        col.addView(seekBar);
        return col;
    }

    // === CALLBACKS ===
    interface ToggleCB { void on(boolean val); }
    interface SliderCB { void on(float val); }
}
