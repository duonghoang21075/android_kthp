package com.example.android_haui.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.android_haui.R;

/**
 * Custom View vẽ biểu đồ vòng (Donut Chart) hiển thị tỷ lệ các phòng học theo trạng thái:
 * Trống (Xanh lá), Đang sử dụng (Xanh dương), Bảo trì (Cam/Vàng đậm).
 */
public class StatusDonutChartView extends View {

    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();

    private int emptyCount = 12;
    private int inUseCount = 10;
    private int maintenanceCount = 3;

    private int colorEmpty;
    private int colorInUse;
    private int colorMaintenance;
    private int colorTrack;

    private float strokeWidthPx;

    public StatusDonutChartView(Context context) {
        super(context);
        init(context);
    }

    public StatusDonutChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StatusDonutChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        strokeWidthPx = 18f * density;

        colorEmpty = ContextCompat.getColor(context, R.color.status_empty_text);
        colorInUse = ContextCompat.getColor(context, R.color.status_inuse_text);
        colorMaintenance = ContextCompat.getColor(context, R.color.status_maint_text);
        colorTrack = ContextCompat.getColor(context, R.color.border_gray);

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(strokeWidthPx);
        ringPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    /**
     * Cập nhật số lượng phòng theo từng trạng thái và vẽ lại biểu đồ.
     */
    public void setStatusCounts(int empty, int inUse, int maintenance) {
        this.emptyCount = Math.max(0, empty);
        this.inUseCount = Math.max(0, inUse);
        this.maintenanceCount = Math.max(0, maintenance);
        invalidate();
    }

    @Override
    protected void onDraw(@androidx.annotation.NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        int size = Math.min(width, height);
        float halfStroke = strokeWidthPx / 2f;
        float padding = halfStroke + (2f * getResources().getDisplayMetrics().density);

        float left = (width - size) / 2f + padding;
        float top = (height - size) / 2f + padding;
        float right = (width + size) / 2f - padding;
        float bottom = (height + size) / 2f - padding;
        arcBounds.set(left, top, right, bottom);

        // Vẽ vòng nền mặc định
        ringPaint.setColor(colorTrack);
        canvas.drawOval(arcBounds, ringPaint);

        int total = emptyCount + inUseCount + maintenanceCount;
        if (total <= 0) {
            return;
        }

        int activeSegments = 0;
        if (emptyCount > 0) activeSegments++;
        if (inUseCount > 0) activeSegments++;
        if (maintenanceCount > 0) activeSegments++;

        // Khoảng cách nhỏ giữa các phân đoạn nếu có từ 2 trạng thái trở lên
        float gapAngle = activeSegments > 1 ? 2.5f : 0f;
        float totalGap = gapAngle * activeSegments;
        float availableAngle = 360f - totalGap;

        int[] counts = {emptyCount, inUseCount, maintenanceCount};
        int[] colors = {colorEmpty, colorInUse, colorMaintenance};

        float startAngle = -90f; // Bắt đầu từ đỉnh 12 giờ

        for (int i = 0; i < counts.length; i++) {
            if (counts[i] <= 0) continue;

            float sweepAngle = (counts[i] / (float) total) * availableAngle;
            ringPaint.setColor(colors[i]);
            canvas.drawArc(arcBounds, startAngle, sweepAngle, false, ringPaint);

            startAngle += sweepAngle + gapAngle;
        }
    }
}
