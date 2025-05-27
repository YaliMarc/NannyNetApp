package com.example.nannynetapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Matching jobs adapter.
 */
public class MatchingJobsAdapter extends RecyclerView.Adapter<MatchingJobsAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobs;
    private String userType;
    private DatabaseReference databaseRef;
    private MatchingJobsActivity activity;

    /**
     * Instantiates a new Matching jobs adapter.
     *
     * @param context  the context
     * @param jobs     the jobs
     * @param userType the user type
     */
    public MatchingJobsAdapter(Context context, List<Job> jobs, String userType) {
        this.context = context;
        this.jobs = jobs != null ? jobs : new ArrayList<>();
        this.userType = userType;
        this.activity = (MatchingJobsActivity) context;
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Update jobs.
     *
     * @param newJobs the new jobs
     */
    public void updateJobs(List<Job> newJobs) {
        this.jobs = newJobs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_matching_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobs.get(position);

        holder.locationText.setText("מיקום: " + job.getLocation());
        holder.dateText.setText("תאריך: " + job.getDate());
        holder.timeText.setText("שעות: " + job.getStartTime() + " - " + job.getEndTime());
        
        // Show requirements
        Gson gson = new Gson();
        Map<String, Boolean> requirements = gson.fromJson(job.getRequirements(), 
            new TypeToken<Map<String, Boolean>>(){}.getType());
        StringBuilder reqBuilder = new StringBuilder("דרישות: ");
        for (Map.Entry<String, Boolean> req : requirements.entrySet()) {
            if (req.getValue()) {
                reqBuilder.append(req.getKey()).append(", ");
            }
        }
        holder.requirementsText.setText(reqBuilder.substring(0, reqBuilder.length() - 2));

        // Show approval status
        if ("Parent".equals(userType)) {
            holder.approvalStatusText.setText(job.isBabysitterApproved() ? 
                "הבייביסיטר אישר/ה את העבודה" : "ממתין לאישור הבייביסיטר");
            holder.confirmButton.setEnabled(!job.isParentApproved());
            holder.confirmButton.setText(job.isParentApproved() ? "אושר" : "אשר עבודה");
        } else {
            holder.approvalStatusText.setText(job.isParentApproved() ? 
                "ההורה אישר את העבודה" : "ממתין לאישור ההורה");
            holder.confirmButton.setEnabled(!job.isBabysitterApproved());
            holder.confirmButton.setText(job.isBabysitterApproved() ? "אושר" : "אשר עבודה");
        }

        // Show other party's details
        String otherPartyName = "Parent".equals(userType) ? job.getBabysitterName() : job.getParentName();
        holder.otherPartyText.setText(otherPartyName);

        holder.confirmButton.setOnClickListener(v -> {
            activity.approveJob(job, "Parent".equals(userType));
        });

        // Open chat button
        holder.chatButton.setOnClickListener(v -> {
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("otherUserId", "Parent".equals(userType) ? job.getBabySitterId() : job.getParentId());
            chatIntent.putExtra("otherUserName", otherPartyName);
            context.startActivity(chatIntent);
        });

        // View profile button
        holder.viewProfileButton.setOnClickListener(v -> {
            Intent profileIntent = new Intent(context, UserProfileActivity.class);
            profileIntent.putExtra("userId", "Parent".equals(userType) ? job.getBabySitterId() : job.getParentId());
            context.startActivity(profileIntent);
        });
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    /**
     * The type Job view holder.
     */
    static class JobViewHolder extends RecyclerView.ViewHolder {
        /**
         * The Location text.
         */
        TextView locationText, /**
         * The Date text.
         */
        dateText, /**
         * The Time text.
         */
        timeText, /**
         * The Requirements text.
         */
        requirementsText, /**
         * The Approval status text.
         */
        approvalStatusText, /**
         * The Other party text.
         */
        otherPartyText;
        /**
         * The Confirm button.
         */
        Button confirmButton, /**
         * The Chat button.
         */
        chatButton, /**
         * The View profile button.
         */
        viewProfileButton;

        /**
         * Instantiates a new Job view holder.
         *
         * @param itemView the item view
         */
        JobViewHolder(@NonNull View itemView) {
            super(itemView);
            locationText = itemView.findViewById(R.id.locationText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            requirementsText = itemView.findViewById(R.id.requirementsText);
            approvalStatusText = itemView.findViewById(R.id.approvalStatusText);
            otherPartyText = itemView.findViewById(R.id.otherPartyText);
            confirmButton = itemView.findViewById(R.id.confirmButton);
            chatButton = itemView.findViewById(R.id.chatButton);
            viewProfileButton = itemView.findViewById(R.id.viewProfileButton);
        }
    }
}