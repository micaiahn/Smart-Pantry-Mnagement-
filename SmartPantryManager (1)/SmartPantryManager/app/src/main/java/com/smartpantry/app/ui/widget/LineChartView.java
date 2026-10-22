package com.smartpantry.app.ui.widget;

/*
 * Smart Pantry Manager - Mobile App Development 700
 * Author: Micaiah Nhamburo
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.smartpantry.app.R;

/**
 * A small hand-drawn line chart (brown line, soft peach fill, dashed beige grid)
 * used for the dashboard stock history and the per-item history.
 * Drawn with Canvas so no chart library is needed and it scales to any screen width.
 */
public class LineChartView extends View {

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    private final float density;
    private float[] values = new float[0];
    private String startLabel = "";
    private String endLabel = "";

    public LineChartView(Context context) {
        this(context, null);
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;

        gridPaint.setColor(ContextCompat.getColor(context, R.color.pantry_border));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(density);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{4 * density, 4 * density}, 0));

        linePaint.setColor(ContextCompat.getColor(context, R.color.pantry_brown));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2.5f * density);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setColor(ContextCompat.getColor(context, R.color.pantry_peach));
        fillPaint.setAlpha(140);
        fillPaint.setStyle(Paint.Style.FILL);

        dotPaint.setColor(ContextCompat.getColor(context, R.color.pantry_brown));
        dotRingPaint.setColor(ContextCompat.getColor(context, R.color.white));

        textPaint.setColor(ContextCompat.getColor(context, R.color.pantry_text_secondary));
        textPaint.setTextSize(11 * context.getResources().getDisplayMetrics().scaledDensity);
    }

    /** Sets the points to draw (oldest first) and the labels under the first and last point. */
    public void setData(float[] newValues, String start, String end) {
        values = newValues == null ? new float[0] : newValues;
        startLabel = start == null ? "" : start;
        endLabel = end == null ? "" : end;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float left = 10 * density;
        float right = getWidth() - 10 * density;
        float top = 14 * density;
        float bottom = getHeight() - 26 * density;
        float chartW = right - left;
        float chartH = bottom - top;
        if (chartW <= 0 || chartH <= 0) {
            return;
        }

        // dashed horizontal grid lines
        for (int i = 0; i <= 3; i++) {
            float y = top + chartH * i / 3f;
            canvas.drawLine(left, y, right, y, gridPaint);
        }

        // x-axis labels
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(startLabel, left, getHeight() - 6 * density, textPaint);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(endLabel, right, getHeight() - 6 * density, textPaint);

        int n = values.length;
        if (n == 0) {
            return;
        }
        float max = 1f;
        for (float v : values) {
            max = Math.max(max, v);
        }
        float yMax = max * 1.15f;

        float[] xs = new float[n];
        float[] ys = new float[n];
        for (int i = 0; i < n; i++) {
            xs[i] = n == 1 ? (left + right) / 2f : left + chartW * i / (n - 1);
            ys[i] = bottom - (values[i] / yMax) * chartH;
        }

        linePath.reset();
        fillPath.reset();
        linePath.moveTo(xs[0], ys[0]);
        fillPath.moveTo(xs[0], bottom);
        fillPath.lineTo(xs[0], ys[0]);
        for (int i = 1; i < n; i++) {
            linePath.lineTo(xs[i], ys[i]);
            fillPath.lineTo(xs[i], ys[i]);
        }
        fillPath.lineTo(xs[n - 1], bottom);
        fillPath.close();

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // dots: every point on short series, only the latest on long ones
        int first = n <= 15 ? 0 : n - 1;
        for (int i = first; i < n; i++) {
            float r = (i == n - 1) ? 5 * density : 3.5f * density;
            canvas.drawCircle(xs[i], ys[i], r + density, dotRingPaint);
            canvas.drawCircle(xs[i], ys[i], r, dotPaint);
        }
    }
}
