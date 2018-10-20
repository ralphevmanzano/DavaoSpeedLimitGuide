package com.dslg.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dslg.app.R;
import com.dslg.app.databinding.RecentSpeedsRowBinding;
import com.dslg.app.models.Speed;
import com.dslg.app.viewholders.LineChartViewHolder;

import java.util.LinkedList;

public class RecentSpeedsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	
	private LinkedList<Speed> speedList;
	private Context           context;
	
	public RecentSpeedsAdapter(LinkedList<Speed> speedList, Context context) {
		this.speedList = speedList;
		this.context = context;
	}
	
	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder holder ;
		View view;
		switch (viewType) {
			case Speed.CHART:
				view = LayoutInflater.from(context).inflate(R.layout.recent_speeds_chart, parent, false);
				holder = new LineChartViewHolder(view, context);
				break;
				
			case Speed.TABLE_LABEL:
				view = LayoutInflater.from(context).inflate(R.layout.recent_speeds_labels, parent, false);
				holder = new RecentSpeedsLabelsViewHolder(view);
				break;
				
			default:
				RecentSpeedsRowBinding binding =
						DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
								R.layout.recent_speeds_row, parent, false);
				holder = new TableRowViewHolder(binding);
				break;
		}
		
		return holder;
	}
	
	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		final int pos = holder.getAdapterPosition();
		switch (getItemViewType(pos)) {
			case Speed.CHART:
				LineChartViewHolder lineChartViewHolder = (LineChartViewHolder) holder;
				lineChartViewHolder.setSpeedList(speedList.get(pos).getListSpeed());
				break;
				
			case Speed.TABLE_LABEL:
				break;
				
			default:
				TableRowViewHolder tableRowViewHolder = (TableRowViewHolder) holder;
				tableRowViewHolder.recentSpeedsRowBinding.setSpeed(speedList.get(pos));
				break;
		}
		
	}
	
	@Override
	public int getItemCount() {
		return speedList.size();
	}
	
	@Override
	public int getItemViewType(int position) {
		return speedList.get(position).getType();
	}
	
	private class TableRowViewHolder extends RecyclerView.ViewHolder {
		RecentSpeedsRowBinding recentSpeedsRowBinding;
		
		TableRowViewHolder(RecentSpeedsRowBinding binding) {
			super(binding.getRoot());
			recentSpeedsRowBinding = binding;
		}
	}
	
	private class RecentSpeedsLabelsViewHolder extends RecyclerView.ViewHolder {
		RecentSpeedsLabelsViewHolder(View view) {
			super(view);
		}
	}
}
