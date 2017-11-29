package lecho.lib.hellocharts.formatter;


import lecho.lib.hellocharts.model.PointValue;

public interface LineChartValueFormatter {

    int formatChartValue(char[] formattedValue, PointValue value);
}
