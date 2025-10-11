package com.alaka_ala.florafilm.ui.fragments.changelog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class TimelineItemDecoration extends RecyclerView.ItemDecoration {

    private final Paint linePaint;
    private final Paint dotPaint;
    private final float lineWidth;
    private final float dotRadius;
    private final float lineStartX;
    private final float dotCx;
    private final float dotCyOffset;


    public TimelineItemDecoration(Context context) {
        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(context, android.R.color.darker_gray));

        dotPaint = new Paint();
        dotPaint.setColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        dotPaint.setStyle(Paint.Style.FILL);


        lineWidth = dpToPx(context, 2);
        dotRadius = dpToPx(context, 5);
        lineStartX = dpToPx(context, 36); // Center of the left padding (72dp / 2)
        dotCx = lineStartX;
        dotCyOffset = dpToPx(context, 26); // Roughly aligns with the vertical center of the first line of text
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);

        linePaint.setStrokeWidth(lineWidth);

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int position = params.getViewAdapterPosition();

            float lineTop = child.getTop() + params.topMargin;
            float lineBottom = child.getBottom() + params.bottomMargin;
            float dotCy = child.getTop() + dotCyOffset;

            // Draw top line
            if (position > 0) {
                c.drawLine(lineStartX, lineTop, lineStartX, dotCy - dotRadius, linePaint);
            }

            // Draw bottom line
            if (position < state.getItemCount() - 1) {
                c.drawLine(lineStartX, dotCy + dotRadius, lineStartX, lineBottom, linePaint);
            }

            // Draw dot
            c.drawCircle(dotCx, dotCy, dotRadius, dotPaint);
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        // No extra space needed as we are drawing inside the padding
        super.getItemOffsets(outRect, view, parent, state);
    }

    private float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
