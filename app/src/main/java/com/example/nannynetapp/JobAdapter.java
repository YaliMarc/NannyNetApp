package com.example.nannynetapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * The type Job adapter.
 */
public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {
    private List<Job> jobs;
    private OnJobClickListener listener;

    /**
     * The interface On job click listener.
     */
    public interface OnJobClickListener {
        /**
         * On job click.
         *
         * @param job the job
         */
        void onJobClick(Job job);
    }

    /**
     * Instantiates a new Job adapter.
     *
     * @param jobs     the jobs
     * @param listener the listener
     */
    public JobAdapter(List<Job> jobs, OnJobClickListener listener) {
        this.jobs = jobs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobs.get(position);
        holder.bind(job);
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    /**
     * The type Job view holder.
     */
    class JobViewHolder extends RecyclerView.ViewHolder {
        private TextView locationText;
        private TextView dateTimeText;
        private TextView salaryText;
        private TextView requirementsText;
        private Button actionButton;

        /**
         * Instantiates a new Job view holder.
         *
         * @param itemView the item view
         */
        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            locationText = itemView.findViewById(R.id.locationText);
            dateTimeText = itemView.findViewById(R.id.dateTimeText);
            salaryText = itemView.findViewById(R.id.salaryText);
            requirementsText = itemView.findViewById(R.id.requirementsText);
            actionButton = itemView.findViewById(R.id.actionButton);
        }

        /**
         * Bind.
         *
         * @param job the job
         */
        public void bind(Job job) {
            locationText.setText(job.getLocation());
            dateTimeText.setText(String.format("%s, %s-%s", 
                job.getDate(), job.getStartTime(), job.getEndTime()));
            salaryText.setText(job.getSalaryRangeText());
            requirementsText.setText(job.getRequirements());

            actionButton.setText(job.isBabysitterSearch() ? "הצע שירות" : "הגש מועמדות");
            actionButton.setOnClickListener(v -> listener.onJobClick(job));
        }
    }
} 