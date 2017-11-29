package lecho.lib.hellocharts.listener;


import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;

public interface ComboLineColumnChartOnValueSelectListener extends OnValueDeselectListener {

    void onColumnValueSelected(int columnIndex, int subcolumnIndex, SubcolumnValue value);

    void onPointValueSelected(int lineIndex, int pointIndex, PointValue value);

}
