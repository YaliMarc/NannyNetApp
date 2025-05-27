package com.example.nannynetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

/**
 * The type Job details activity.
 */
public class JobDetailsActivity extends AppCompatActivity {

    private TextView locationText, dateText, timeText, requirementsText, statusText, approvedByText;
    private Button approveButton, chatButton, viewProfileButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String jobId;
    private Job currentJob;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        locationText = findViewById(R.id.locationText);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);
        requirementsText = findViewById(R.id.requirementsText);
        statusText = findViewById(R.id.statusText);
        approvedByText = findViewById(R.id.approvedByText);
        approveButton = findViewById(R.id.approveButton);
        chatButton = findViewById(R.id.chatButton);
        viewProfileButton = findViewById(R.id.viewProfileButton);

        // Get job ID from intent
        jobId = getIntent().getStringExtra("jobId");
        if (jobId == null) {
            Toast.makeText(this, "שגיאה: לא נמצא מזהה עבודה", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get user type
        String userId = auth.getCurrentUser().getUid();
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    userType = documentSnapshot.getString("userType");
                    loadJobDetails();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בטעינת פרטי משתמש", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadJobDetails() {
        db.collection("Jobs").document(jobId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentJob = documentSnapshot.toObject(Job.class);
                        if (currentJob != null) {
                            updateUI();
                            setupButtons();
                        }
                    } else {
                        Toast.makeText(this, "העבודה לא נמצאה", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בטעינת פרטי העבודה", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateUI() {
        locationText.setText("מיקום: " + currentJob.getLocation());
        dateText.setText("תאריך: " + currentJob.getDate());
        timeText.setText("שעות: " + currentJob.getStartTime() + " - " + currentJob.getEndTime());

        // Show requirements
        Gson gson = new Gson();
        Map<String, Boolean> requirements = gson.fromJson(currentJob.getRequirements(), 
            new TypeToken<Map<String, Boolean>>(){}.getType());
        StringBuilder reqBuilder = new StringBuilder("דרישות:\n");
        for (Map.Entry<String, Boolean> req : requirements.entrySet()) {
            if (req.getValue()) {
                reqBuilder.append("• ").append(req.getKey()).append("\n");
            }
        }
        requirementsText.setText(reqBuilder.toString());

        // Show status
        String status = currentJob.getStatus();
        statusText.setText("סטטוס: " + status);
        if ("Approved".equals(status)) {
            statusText.setTextColor(getResources().getColor(R.color.colorSuccess));
            approveButton.setEnabled(false);
            approveButton.setText("העבודה אושרה");
        }

        // Show who approved
        if (currentJob.getApprovedBy() != null) {
            approvedByText.setVisibility(View.VISIBLE);
            approvedByText.setText("אושר על ידי: " + currentJob.getApprovedBy());
        }
    }

    private void setupButtons() {
        boolean isParent = "Parent".equals(userType);
        String currentUserId = auth.getCurrentUser().getUid();

        // Setup approve button
        if (isParent) {
            approveButton.setEnabled(!currentJob.isParentApproved());
        } else {
            approveButton.setEnabled(!currentJob.isBabysitterApproved());
        }

        approveButton.setOnClickListener(v -> {
            if (isParent) {
                currentJob.setParentApproved(true);
                currentJob.setApprovedBy("parent:" + currentUserId);
            } else {
                currentJob.setBabysitterApproved(true);
                currentJob.setApprovedBy("babysitter:" + currentUserId);
            }

            if (currentJob.isFullyApproved()) {
                currentJob.setStatus("Approved");
            }

            db.collection("Jobs").document(jobId)
                    .set(currentJob)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "העבודה אושרה בהצלחה", Toast.LENGTH_SHORT).show();
                        updateUI();
                        
                        // Send notifications
                        if (currentJob.isFullyApproved()) {
                            String notificationTitle = "עבודה אושרה!";
                            String notificationMessage = "העבודה ב-" + currentJob.getDate() + " אושרה על ידי שני הצדדים";
                            
                            // Send to both parties
                            NotificationHelper.sendFCMNotification(
                                currentJob.getParentId(),
                                notificationTitle,
                                notificationMessage
                            );
                            NotificationHelper.sendFCMNotification(
                                currentJob.getBabySitterId(),
                                notificationTitle,
                                notificationMessage
                            );
                        }
                    })
                    .addOnFailureListener(e -> 
                        Toast.makeText(this, "שגיאה באישור העבודה", Toast.LENGTH_SHORT).show());
        });

        // Setup chat button
        chatButton.setOnClickListener(v -> {
            String otherUserId = isParent ? currentJob.getBabySitterId() : currentJob.getParentId();
            String otherUserName = isParent ? currentJob.getBabysitterName() : currentJob.getParentName();
            
            Intent chatIntent = new Intent(this, ChatActivity.class);
            chatIntent.putExtra("otherUserId", otherUserId);
            chatIntent.putExtra("otherUserName", otherUserName);
            chatIntent.putExtra("jobId", jobId);
            startActivity(chatIntent);
        });

        // Setup view profile button
        viewProfileButton.setOnClickListener(v -> {
            String otherUserId = isParent ? currentJob.getBabySitterId() : currentJob.getParentId();
            Intent profileIntent = new Intent(this, UserProfileActivity.class);
            profileIntent.putExtra("userId", otherUserId);
            startActivity(profileIntent);
        });
    }
} 