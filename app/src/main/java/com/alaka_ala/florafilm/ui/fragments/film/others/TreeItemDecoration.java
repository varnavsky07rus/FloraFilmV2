package com.alaka_ala.florafilm.ui.fragments.film.others;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.alaka_ala.florafilm.R;
import java.util.List;

public class TreeItemDecoration extends RecyclerView.ItemDecoration {

    // Ширина отступа для каждого уровня вложенности (в dp)
    private static final int INDENT_DP = 28;
    // Смещение центра иконки от начала элемента (в dp)
    private static final int ICON_CENTER_OFFSET_DP = 42;

    private final Paint linePaint;
    private final float indentPx;
    private final float iconCenterOffsetPx;

    public TreeItemDecoration(Context context) {
        linePaint = new Paint();
        // Берем цвет из атрибута темы, чтобы он соответствовал вашему дизайну
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.IconsTintThemeInverse, typedValue, true);
        linePaint.setColor(typedValue.data);
        linePaint.setStrokeWidth(2 * context.getResources().getDisplayMetrics().density); // Ширина линии 2dp

        indentPx = INDENT_DP * context.getResources().getDisplayMetrics().density;
        iconCenterOffsetPx = ICON_CENTER_OFFSET_DP * context.getResources().getDisplayMetrics().density;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);

        TreeAdapter adapter = (TreeAdapter) parent.getAdapter();
        if (adapter == null || adapter.getItemCount() == 0) {
            return;
        }

        List<TreeItem> items = adapter.getItems();

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);
            if (position == RecyclerView.NO_POSITION) {
                continue;
            }

            TreeItem item = items.get(position);
            int level = item.getLevel();
            float yCenter = child.getTop() + (child.getHeight() / 2.0f);

            // Рисуем вертикальные линии для всех родительских уровней
            for (int l = 0; l < level; l++) {
                // Проверяем, есть ли у предка 'l' еще дочерние элементы ниже текущего
                boolean hasMoreChildrenForAncestor = false;
                for (int j = position + 1; j < items.size(); j++) {
                    if (items.get(j).getLevel() <= l) {
                        break; // Дошли до следующего "брата" предка
                    }
                    if (items.get(j).getLevel() > l) {
                        hasMoreChildrenForAncestor = true;
                        break;
                    }
                }

                if (hasMoreChildrenForAncestor) {
                    float x = (l * indentPx) + iconCenterOffsetPx;
                    c.drawLine(x, child.getTop(), x, child.getBottom(), linePaint);
                }
            }

            if (level > 0) {
                // Координата X для вертикальной линии родителя
                float parentLineX = ((level - 1) * indentPx) + iconCenterOffsetPx;
                // Координата X для центра иконки текущего элемента
                float currentIconX = (level * indentPx) + iconCenterOffsetPx;

                // Горизонтальная линия от родительской вертикальной до текущей
                c.drawLine(parentLineX, yCenter, currentIconX, yCenter, linePaint);

                // Проверяем, является ли текущий элемент последним в своей группе
                boolean isLastChild = true;
                if (position + 1 < items.size()) {
                    if (items.get(position + 1).getLevel() >= level) {
                        isLastChild = false;
                    }
                }

                // Вертикальный сегмент родительской линии
                if (isLastChild) {
                    // Если последний, рисуем до центра
                    c.drawLine(parentLineX, child.getTop(), parentLineX, yCenter, linePaint);
                } else {
                    // Если не последний, рисуем через весь элемент
                    c.drawLine(parentLineX, child.getTop(), parentLineX, child.getBottom(), linePaint);
                }
            }
        }
    }
}