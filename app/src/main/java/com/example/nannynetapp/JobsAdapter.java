package com.example.nannynetapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

/**
 * The type Jobs adapter.
 */
public class JobsAdapter extends RecyclerView.Adapter<JobsAdapter.JobViewHolder> {

    private Context context;
    private List<HashMap<String, String>> jobs;
    private DatabaseReference databaseRef;
    private boolean isMyOffers;

    /**
     * Instantiates a new Jobs adapter.
     *
     * @param context    the context
     * @param jobs       the jobs
     * @param isMyOffers the is my offers
     */
    public JobsAdapter(Context context, List<HashMap<String, String>> jobs, boolean isMyOffers) {
        this.context = context;
        this.jobs = jobs;
        this.isMyOffers = isMyOffers;
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job_offer, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        HashMap<String, String> job = jobs.get(position);

        holder.locationText.setText("מיקום: " + job.get("location"));
        holder.dateText.setText("תאריך: " + job.get("date"));
        holder.timeText.setText("שעות: " + job.get("startTime") + " - " + job.get("endTime"));

        String salary = job.get("salary");
        if (salary != null) {
            holder.salaryText.setText("שכר: " + salary + " ₪ לשעה");
            holder.salaryText.setVisibility(View.VISIBLE);
        } else {
            holder.salaryText.setVisibility(View.GONE);
        }

        if (isMyOffers) {
            holder.actionButton.setText("מחק הצעה");
            holder.actionButton.setBackgroundTintList(context.getColorStateList(android.R.color.holo_red_light));
            holder.actionButton.setOnClickListener(v -> deleteJob(job));
        } else {
            holder.actionButton.setText("אשר עבודה");
            holder.actionButton.setBackgroundTintList(context.getColorStateList(android.R.color.holo_green_light));
            holder.actionButton.setOnClickListener(v -> acceptJob(job));
        }
    }

    private void acceptJob(HashMap<String, String> job) {
        String jobId = job.get("jobId");
        String babysitterId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (jobId != null) {
            DatabaseReference jobRef = databaseRef.child("ParentJobs").child(jobId);

            // עדכון סטטוס העבודה
            HashMap<String, Object> updates = new HashMap<>();
            updates.put("status", "accepted");
            updates.put("babysitterId", babysitterId);

            jobRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "העבודה אושרה בהצלחה", Toast.LENGTH_SHORT).show();

                        // שליחת התראה להורה
                        String parentId = job.get("parentId");
                        if (parentId != null) {
                            sendNotificationToParent(parentId, jobId);
                        }

                        // הגדרת תזכורת
                        setupJobReminder(job);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "שגיאה באישור העבודה", Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteJob(HashMap<String, String> job) {
        String jobId = job.get("jobId");
        if (jobId != null) {
            databaseRef.child("ParentJobs").child(jobId)
                    .removeValue()
                    .addOnSuccessListener(unused ->
                            Toast.makeText(context, "העבודה נמחקה בהצלחה", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "שגיאה במחיקת העבודה", Toast.LENGTH_SHORT).show());
        }
    }

    private void sendNotificationToParent(String parentId, String jobId) {
        DatabaseReference notificationsRef = databaseRef.child("Notifications").push();

        HashMap<String, Object> notification = new HashMap<>();
        notification.put("userId", parentId);
        notification.put("title", "הצעת העבודה שלך אושרה!");
        notification.put("message", "בייביסיטר אישר/ה את הצעת העבודה שלך");
        notification.put("jobId", jobId);
        notification.put("type", "job_accepted");
        notification.put("timestamp", System.currentTimeMillis());

        notificationsRef.setValue(notification);
    }

    private void setupJobReminder(HashMap<String, String> job) {
        // יצירת תזכורת שעה לפני תחילת העבודה
        if (context instanceof SearchBabysitterActivity) {
            SearchBabysitterActivity activity = (SearchBabysitterActivity) context;
            String jobId = job.get("jobId");
            String date = job.get("date");
            String startTime = job.get("startTime");

            // הפעלת פונקציית התזכורת מה-Activity
            if (jobId != null && date != null && startTime != null) {
                activity.scheduleJobReminder(jobId, date, startTime);
            }
        }
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
         * The Salary text.
         */
        salaryText;
        /**
         * The Action button.
         */
        Button actionButton;

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
            salaryText = itemView.findViewById(R.id.salaryText);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }
}