package com.dslg.app.viewholders;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dslg.app.R;
import com.dslg.app.constants.Values;
import com.dslg.app.models.Speed;
import com.dslg.app.utils.ChartMarkerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LineChartViewHolder extends RecyclerView.ViewHolder
		implements OnChartValueSelectedListener {
	
	@BindView(R.id.line_chart)
	LineChart lineChart;
	
	private Context context;
	private List<Entry> speedEntry    = new ArrayList<>();
	private List<Entry> maxSpeedEntry = new ArrayList<>();
	private LinkedList<Speed> speedList;
	
	public LineChartViewHolder(View itemView, Context context) {
		super(itemView);
		ButterKnife.bind(this, itemView);
		this.context = context;
	}
	
	public void setSpeedList(String jsonSpeedList) {
		Type type = new TypeToken<LinkedList<Speed>>() {
		}.getType();
		Gson gson = new Gson();
		speedList = gson.fromJson(jsonSpeedList, type);
		initChart();
	}
	
	private void initChart() {
		final List<String> dateTimeList = new ArrayList<>();
		lineChart.setOnChartValueSelectedListener(this);
		lineChart.setDragEnabled(true);
		lineChart.setScaleEnabled(true);
		lineChart.getAxisRight()
						 .setEnabled(false);
		lineChart.setDrawGridBackground(false);
		lineChart.setDrawBorders(false);
		lineChart.setPinchZoom(true);
		lineChart.setDescription(null);
		lineChart.setAutoScaleMinMaxEnabled(true);
		lineChart.setTouchEnabled(true);
		lineChart.getLegend()
						 .setEnabled(true);
		
		ChartMarkerView markerView = new ChartMarkerView(context, R.layout.chart_marker);
		markerView.setChartView(lineChart);
		lineChart.setMarker(markerView);
		int i = 0;
		
		for (Speed speed : speedList) {
			String currentDateandTime = new SimpleDateFormat(Values.XAXIS_LABEL_TIME, Locale.ENGLISH).format(
					new Date(speed.getDateTimeMillis()));
			dateTimeList.add(currentDateandTime);
			maxSpeedEntry.add(new Entry(i, speed.getMaxSpeed()));
			speedEntry.add(new Entry(i, speed.getCurrentSpeed()));
			i++;
		}
		
		YAxis yAxis = lineChart.getAxisLeft();
		yAxis.setDrawAxisLine(false);
		yAxis.setAxisMinimum(0);
		yAxis.setDrawAxisLine(false);
		yAxis.setEnabled(true);
		yAxis.setLabelCount(10, false);
		yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
		yAxis.setAxisLineColor(Color.WHITE);
		
		XAxis xAxis = lineChart.getXAxis();
		xAxis.setDrawGridLines(false);
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setDrawAxisLine(true);
		xAxis.setValueFormatter(new IAxisValueFormatter() {
			
			@Override
			public String getFormattedValue(float value, AxisBase axis) {
				
				return dateTimeList.get((int) value);
			}
		});
		
		LineDataSet speedDataSet = new LineDataSet(speedEntry, "Driver's Speed");
		speedDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		speedDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
		speedDataSet.setCubicIntensity(0.08f);
		speedDataSet.setColor(ContextCompat.getColor(context, R.color.speed));
		speedDataSet.setDrawCircles(false);
		speedDataSet.setLineWidth(1f);
		speedDataSet.setFillAlpha(220);
		speedDataSet.setDrawFilled(true);
		speedDataSet.setFillColor(ContextCompat.getColor(context, R.color.speed));
		speedDataSet.setDrawValues(true);
		
		LineDataSet maxSpeedDataSet = new LineDataSet(maxSpeedEntry, "Speed Limit");
		maxSpeedDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
		maxSpeedDataSet.setColor(ContextCompat.getColor(context, R.color.max_speed));
		maxSpeedDataSet.setDrawCircles(false);
		maxSpeedDataSet.setLineWidth(3f);
		maxSpeedDataSet.setDrawValues(true);
		
		ArrayList<ILineDataSet> dataSets = new ArrayList<>();
		dataSets.add(speedDataSet);
		dataSets.add(maxSpeedDataSet);
		
		LineData speedData = new LineData(dataSets);
		speedData.setValueTextSize(9f);
		speedData.setDrawValues(false);
		
		lineChart.setData(speedData);
		lineChart.invalidate();
	}
	
	@Override
	public void onValueSelected(Entry e, Highlight h) {
	
	}
	
	@Override
	public void onNothingSelected() {
	
	}
}
