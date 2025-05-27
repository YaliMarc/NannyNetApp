package com.example.nannynetapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Matching jobs activity.
 */
public class MatchingJobsActivity extends AppCompatActivity {

    private RecyclerView matchingJobsRecyclerView;
    private TextView noMatchesText;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;
    private String userType;
    private MatchingJobsAdapter adapter;
    private static final String CHANNEL_ID = "job_notifications";
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching_jobs);

        matchingJobsRecyclerView = findViewById(R.id.matchingJobsRecyclerView);
        noMatchesText = findViewById(R.id.noMatchesText);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        createNotificationChannel();

        String userId = auth.getCurrentUser().getUid();
        databaseRef.child("Users").child(userId).child("userType")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            userType = snapshot.getValue(String.class);
                            setupRecyclerView();
                            loadMatches();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MatchingJobsActivity.this, "Error loading user type", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupRecyclerView() {
        matchingJobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Job> emptyList = new ArrayList<>();
        adapter = new MatchingJobsAdapter(this, emptyList, userType);
        matchingJobsRecyclerView.setAdapter(adapter);
    }

    private void loadMatches() {
        if ("Parent".equals(userType)) {
            loadMatchingBabysitters();
        } else {
            loadMatchingJobs();
        }
    }

    private void loadMatchingBabysitters() {
        String userId = auth.getCurrentUser().getUid();
        
        // First get the parent's job requirements
        databaseRef.child("Jobs").orderByChild("parentId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                
                for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                    Job parentJob = jobSnapshot.getValue(Job.class);
                    if (parentJob != null && "Pending".equals(parentJob.getStatus())) {
                        // Query babysitters that match the job requirements
                        databaseRef.child("Users").orderByChild("userType").equalTo("Babysitter")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                List<Job> matches = new ArrayList<>();
                                Gson gson = new Gson();
                                
                                Map<String, Boolean> jobReqs = gson.fromJson(parentJob.getRequirements(), 
                                    new TypeToken<Map<String, Boolean>>(){}.getType());
                                
                                for (DataSnapshot babysitterSnapshot : snapshot.getChildren()) {
                                    Map<String, Object> babysitterData = (Map<String, Object>) babysitterSnapshot.getValue();
                                    String babysitterRequirements = (String) babysitterData.get("requirements");
                                    
                                    // Compare location
                                    if (!parentJob.getLocation().equals(babysitterData.get("preferredLocation"))) {
                                        continue;
                                    }
                                    
                                    // Compare requirements
                                    Map<String, Boolean> sitterReqs = gson.fromJson(babysitterRequirements, 
                                        new TypeToken<Map<String, Boolean>>(){}.getType());
                                    
                                    boolean matchesRequirements = true;
                                    for (Map.Entry<String, Boolean> req : jobReqs.entrySet()) {
                                        if (req.getValue() && !sitterReqs.containsKey(req.getKey())) {
                                            matchesRequirements = false;
                                            break;
                                        }
                                    }
                                    
                                    if (matchesRequirements) {
                                        Job matchedJob = new Job(parentJob);
                                        matchedJob.setBabysitterName((String) babysitterData.get("name"));
                                        matchedJob.setBabySitterId(babysitterSnapshot.getKey());
                                        matches.add(matchedJob);
                                    }
                                }
                                updateUI(matches);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MatchingJobsActivity.this, "Error loading babysitters", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MatchingJobsActivity.this, "Error loading parent jobs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMatchingJobs() {
        String userId = auth.getCurrentUser().getUid();
        
        // First get the babysitter's criteria
        databaseRef.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                
                Map<String, Object> babysitterData = (Map<String, Object>) snapshot.getValue();
                String babysitterRequirements = (String) babysitterData.get("requirements");
                
                // Now query all pending jobs
                Query query = databaseRef.child("Jobs").orderByChild("status").equalTo("Pending");
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Job> matches = new ArrayList<>();
                        Gson gson = new Gson();
                        
                        for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                            Job job = jobSnapshot.getValue(Job.class);
                            if (job != null && !job.isBabysitterApproved()) {
                                // Compare location
                                if (!job.getLocation().equals(babysitterData.get("preferredLocation"))) {
                                    continue;
                                }
                                
                                // Compare requirements
                                Map<String, Boolean> jobReqs = gson.fromJson(job.getRequirements(), 
                                    new TypeToken<Map<String, Boolean>>(){}.getType());
                                Map<String, Boolean> sitterReqs = gson.fromJson(babysitterRequirements, 
                                    new TypeToken<Map<String, Boolean>>(){}.getType());
                                
                                boolean matchesRequirements = true;
                                for (Map.Entry<String, Boolean> req : jobReqs.entrySet()) {
                                    if (req.getValue() && !sitterReqs.containsKey(req.getKey())) {
                                        matchesRequirements = false;
                                        break;
                                    }
                                }
                                
                                if (matchesRequirements) {
                                    matches.add(job);
                                }
                            }
                        }
                        updateUI(matches);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MatchingJobsActivity.this, "Error loading matches", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MatchingJobsActivity.this, "Error loading babysitter data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(List<Job> matches) {
        if (matches.isEmpty()) {
            noMatchesText.setVisibility(View.VISIBLE);
            matchingJobsRecyclerView.setVisibility(View.GONE);
        } else {
            noMatchesText.setVisibility(View.GONE);
            matchingJobsRecyclerView.setVisibility(View.VISIBLE);
            adapter.updateJobs(matches);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Job Notifications";
            String description = "Channel for job related notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Send job confirmation notification.
     *
     * @param title   the title
     * @param message the message
     */
    public void sendJobConfirmationNotification(String title, String message) {
        Intent intent = new Intent(this, MatchingJobsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Schedule job reminder.
     *
     * @param jobId     the job id
     * @param startTime the start time
     */
    public void scheduleJobReminder(String jobId, long startTime) {
        // Schedule the reminder for one hour before the job starts
        long reminderTime = startTime - 3600000; // One hour before

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("jobId", jobId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                jobId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Check if we have permission to schedule exact alarms
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            android.app.AlarmManager.RTC_WAKEUP,
                            reminderTime,
                            pendingIntent
                    );
                    Toast.makeText(this, "תזכורת תישלח שעה לפני תחילת העבודה", Toast.LENGTH_SHORT).show();
                } else {
                    // Request permission to schedule exact alarms
                    Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(permissionIntent);
                    Toast.makeText(this, "נדרשת הרשאה לתזכורות מדויקות", Toast.LENGTH_LONG).show();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        android.app.AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                );
                Toast.makeText(this, "תזכורת תישלח שעה לפני תחילת העבודה", Toast.LENGTH_SHORT).show();
            } else {
                alarmManager.setExact(
                        android.app.AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                );
                Toast.makeText(this, "תזכורת תישלח שעה לפני תחילת העבודה", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Approve job.
     *
     * @param job      the job
     * @param isParent the is parent
     */
    public void approveJob(Job job, boolean isParent) {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference jobRef = databaseRef.child("Jobs").child(job.getJobId());
        
        if (isParent) {
            job.setParentApproved(true);
            job.setApprovedBy("parent:" + userId);
        } else {
            job.setBabysitterApproved(true);
            job.setApprovedBy("babysitter:" + userId);
        }
        
        if (job.isFullyApproved()) {
            job.setStatus("Approved");
            // Schedule reminder
            scheduleJobReminder(job.getJobId(), job.getStartTimeMillis());
            
            // Send notifications to both parties
            String notificationTitle = "עבודה אושרה!";
            String notificationMessage = "העבודה ב-" + job.getDate() + " אושרה על ידי שני הצדדים";
            
            // Send to parent
            sendNotificationToUser(job.getParentId(), notificationTitle, notificationMessage);
            // Send to babysitter
            sendNotificationToUser(job.getBabySitterId(), notificationTitle, notificationMessage);
        }
        
        jobRef.setValue(job);
    }

    private void sendNotificationToUser(String userId, String title, String message) {
        DatabaseReference userTokenRef = databaseRef.child("Users").child(userId).child("fcmToken");
        userTokenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userToken = snapshot.getValue(String.class);
                    // Send FCM notification using the token
                    NotificationHelper.sendFCMNotification(userToken, title, message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}