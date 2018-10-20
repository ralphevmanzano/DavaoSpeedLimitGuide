package com.dslg.app.utils;

import android.content.Context;
import android.widget.TextView;

import com.dslg.app.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class ChartMarkerView extends MarkerView
{
  private TextView txtMarker;
  
  public ChartMarkerView(Context context, int layoutResource)
  {
    super(context, layoutResource);
    txtMarker = findViewById(R.id.txt_chart_marker);
  }
  
  
  // callbacks everytime the MarkerView is redrawn, can be used to update the
  // content (user-interface)
  @Override
  public void refreshContent(Entry e, Highlight highlight) {
  
    String tooltip = String.valueOf((int)e.getY()) + " " +getContext().getResources().getString(R.string.km_h);
    txtMarker.setText(tooltip);
    
    // this will perform necessary layouting
    super.refreshContent(e, highlight);
  }
  
  private MPPointF mOffset;
  
  @Override
  public MPPointF getOffset() {
    
    if(mOffset == null) {
      // center the marker horizontally and vertically
      mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
    }
    
    return mOffset;
  }
}
