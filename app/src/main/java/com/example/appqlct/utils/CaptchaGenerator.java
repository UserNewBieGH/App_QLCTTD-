package com.example.appqlct.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.Random;

public class CaptchaGenerator {

    private static final String CHARSET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 5;
    private static final Random RANDOM = new Random();

    public static class CaptchaResult {
        private final String code;
        private final Bitmap bitmap;

        public CaptchaResult(String code, Bitmap bitmap) {
            this.code = code;
            this.bitmap = bitmap;
        }

        public String getCode() {
            return code;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }

    public static CaptchaResult createCaptcha(int width, int height) {
        // 1. Generate 5-character uppercase code
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(index));
        }
        String code = sb.toString();

        // 2. Create Bitmap and Canvas
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 3. Background color - gentle tinted light gradient
        canvas.drawColor(Color.parseColor("#F1F5F9"));

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 4. Draw random noise lines
        int[] lineColors = {
                Color.parseColor("#94A3B8"),
                Color.parseColor("#CBD5E1"),
                Color.parseColor("#64748B"),
                Color.parseColor("#A855F7"),
                Color.parseColor("#3B82F6")
        };
        for (int i = 0; i < 5; i++) {
            paint.setColor(lineColors[RANDOM.nextInt(lineColors.length)]);
            paint.setStrokeWidth(RANDOM.nextInt(3) + 2);
            paint.setAlpha(120);
            int startX = RANDOM.nextInt(width);
            int startY = RANDOM.nextInt(height);
            int stopX = RANDOM.nextInt(width);
            int stopY = RANDOM.nextInt(height);
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }

        // 5. Draw random noise dots
        for (int i = 0; i < 35; i++) {
            paint.setColor(lineColors[RANDOM.nextInt(lineColors.length)]);
            paint.setAlpha(140);
            float cx = RANDOM.nextInt(width);
            float cy = RANDOM.nextInt(height);
            float radius = RANDOM.nextFloat() * 3.0f + 1.5f;
            canvas.drawCircle(cx, cy, radius, paint);
        }

        // 6. Draw characters with random colors, rotation angles, and fonts
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        int[] textColors = {
                Color.parseColor("#1E3A8A"), // Blue
                Color.parseColor("#065F46"), // Emerald
                Color.parseColor("#7C2D12"), // Amber / Rust
                Color.parseColor("#581C87"), // Purple
                Color.parseColor("#9D174D"), // Pink
                Color.parseColor("#111827")  // Slate
        };

        float charWidth = (float) (width - 30) / CODE_LENGTH;
        float baseTextSize = height * 0.58f;

        for (int i = 0; i < CODE_LENGTH; i++) {
            paint.setColor(textColors[RANDOM.nextInt(textColors.length)]);
            paint.setAlpha(255);
            paint.setTextSize(baseTextSize + (RANDOM.nextInt(8) - 4));

            char c = code.charAt(i);
            float x = 18 + (i * charWidth);
            float y = height * 0.70f + (RANDOM.nextInt(10) - 5);

            // Random rotation between -16 and +16 degrees
            float angle = (RANDOM.nextFloat() * 32.0f) - 16.0f;

            canvas.save();
            canvas.rotate(angle, x + (charWidth / 2f), y - (baseTextSize / 2f));
            canvas.drawText(String.valueOf(c), x, y, paint);
            canvas.restore();
        }

        return new CaptchaResult(code, bitmap);
    }
}
