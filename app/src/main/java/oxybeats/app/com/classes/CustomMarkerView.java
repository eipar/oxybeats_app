package oxybeats.app.com.classes;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import oxybeats.app.com.R;

public class CustomMarkerView extends MarkerView {
    private TextView txtValue;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        txtValue = findViewById(R.id.txtMarkerValue);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight){
        txtValue.setText(Utils.formatNumber(e.getY(), 0, true));
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset(){
        return new MPPointF(-(getWidth()/2), -getHeight());
    }

}
