package lecho.lib.hellocharts.renderer;

import android.graphics.Canvas;

import lecho.lib.hellocharts.model.SelectedValue;
import lecho.lib.hellocharts.model.Viewport;

/**
 * Interface for all chart renderer.
 */
public interface ChartRenderer {

    void onChartSizeChanged();

    void onChartDataChanged();

    void onChartViewportChanged();

    void resetRenderer();

    /**
     * Draw chart data.
     */
    void draw(Canvas canvas);

    /**
     * Draw chart data that should not be clipped to contentRect area.
     */
    void drawUnclipped(Canvas canvas);

    /**
     * Checks if given pixel coordinates corresponds to any chart value. If yes return true and set selectedValue, if
     * not selectedValue should be *cleared* and method should return false.
     */
    boolean checkTouch(float touchX, float touchY);

    /**
     * Returns true if there is value selected.
     */
    boolean isTouched();

    /**
     * Clear value selection.
     */
    void clearTouch();

    Viewport getMaximumViewport();

    void setMaximumViewport(Viewport maxViewport);

    Viewport getCurrentViewport();

    void setCurrentViewport(Viewport viewport);

    boolean isViewportCalculationEnabled();

    void setViewportCalculationEnabled(boolean isEnabled);

    void selectValue(SelectedValue selectedValue);

    SelectedValue getSelectedValue();

}
