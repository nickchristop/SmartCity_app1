package com.smartcity.app.ui;

import android.content.res.ColorStateList;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.smartcity.app.R;
import com.smartcity.app.data.model.Report;

import java.util.List;

/**
 * RecyclerView adapter for the Hazards list.
 * Maps "Resolved" → "Completed" display text in the Past tab.
 * Fires the clickListener so HazardsFragment can open the detail screen.
 */
public class HazardAdapter extends RecyclerView.Adapter<HazardAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Report report);
    }

    private List<Report> reports;
    private final OnItemClickListener clickListener;

    public HazardAdapter(List<Report> reports, OnItemClickListener clickListener) {
        this.reports       = reports;
        this.clickListener = clickListener;
    }

    /** Swap the dataset and redraw */
    public void setReports(List<Report> reports) {
        this.reports = reports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hazard, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Report r = reports.get(position);

        holder.tvTitle.setText(r.getTitle());

        // Hide description if not provided
        String desc = r.getDescription();
        if (desc != null && !desc.isEmpty()) {
            holder.tvDesc.setText(desc);
            holder.tvDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        // Human-readable timestamp
        holder.tvTime.setText(DateFormat.format("MMM dd, HH:mm", r.getTimestamp()));

        // Map "Resolved" → "Completed" for display; keep original for colour logic
        String status = r.getStatus() != null ? r.getStatus() : "Pending";
        String displayStatus = "Resolved".equalsIgnoreCase(status) ? "Completed" : status;
        holder.tvStatus.setText(displayStatus);

        // Colour the status pill
        int colour;
        switch (status) {
            case "Resolved":
                colour = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_resolved);
                break;
            case "In Progress":
                colour = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_in_progress);
                break;
            default:
                colour = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_pending);
        }
        holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(colour));

        // Open detail on click
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(r);
        });
    }

    @Override
    public int getItemCount() { return reports != null ? reports.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus, tvDesc, tvTime;

        ViewHolder(@NonNull View view) {
            super(view);
            tvTitle  = view.findViewById(R.id.tv_hazard_title);
            tvStatus = view.findViewById(R.id.tv_hazard_status);
            tvDesc   = view.findViewById(R.id.tv_hazard_desc);
            tvTime   = view.findViewById(R.id.tv_hazard_time);
        }
    }
}
