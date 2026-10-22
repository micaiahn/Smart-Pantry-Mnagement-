package com.smartpantry.app.ui.widget;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.smartpantry.app.R;

/** A thin, rounded progress indicator (red or green fill on a soft peach track). */
public class ThinProgressView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private float fraction = 0f;

    public ThinProgressView(Context context) {
        this(context, null);
    }

    public ThinProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        trackPaint.setColor(ContextCompat.getColor(context, R.color.pantry_peach_track));
        fillPaint.setColor(ContextCompat.getColor(context, R.color.pantry_green));
    }

    /** @param fraction 0..1, how much of the track is filled */
    public void setFraction(float fraction) {
        this.fraction = Math.max(0f, Math.min(1f, fraction));
        invalidate();
    }

    public void setFillColor(int color) {
        fillPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        float radius = h / 2f;
        rect.set(0, 0, w, h);
        canvas.drawRoundRect(rect, radius, radius, trackPaint);
        if (fraction > 0f) {
            rect.set(0, 0, Math.max(h, w * fraction), h);
            canvas.drawRoundRect(rect, radius, radius, fillPaint);
        }
    }
}
