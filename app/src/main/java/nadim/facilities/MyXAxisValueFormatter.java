package nadim.facilities;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Just formats the dates to be shown on bottom of graph, using mpandroidchart
 */
class MyXAxisValueFormatter implements IAxisValueFormatter {

    private String[] vals;

    public MyXAxisValueFormatter(String[] values) {
        vals = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
            return vals[(int) value];
    }

}
