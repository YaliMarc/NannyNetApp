package com.example.nannynetapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.AlarmManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.PendingIntent;

/**
 * The type Search babysitter activity.
 */
public class SearchBabysitterActivity extends AppCompatActivity {

    private EditText locationInput, dateInput, startTimeInput, endTimeInput;
    private Button searchButton;
    private RecyclerView jobsRecyclerView;
    private TextView noJobsText;
    private List<HashMap<String, String>> jobsList;
    private JobsAdapter jobsAdapter;

    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_babysitter);

        initializeViews();
        setupRecyclerView();

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        dateInput.setOnClickListener(v -> showDatePicker());
        startTimeInput.setOnClickListener(v -> showTimePicker(startTimeInput));
        endTimeInput.setOnClickListener(v -> showTimePicker(endTimeInput));
        searchButton.setOnClickListener(v -> searchJobs());
    }

    private void initializeViews() {
        locationInput = findViewById(R.id.locationInput);
        dateInput = findViewById(R.id.dateInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        searchButton = findViewById(R.id.searchButton);
        jobsRecyclerView = findViewById(R.id.jobsRecyclerView);
        noJobsText = findViewById(R.id.noJobsText);
    }

    private void setupRecyclerView() {
        jobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        jobsList = new ArrayList<>();
        jobsAdapter = new JobsAdapter(this, jobsList, false); // false כי אלו הצעות של הורים
        jobsRecyclerView.setAdapter(jobsAdapter);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                    dateInput.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker(final EditText timeInput) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    timeInput.setText(formattedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void searchJobs() {
        String location = locationInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String startTime = startTimeInput.getText().toString().trim();
        String endTime = endTimeInput.getText().toString().trim();

        if (location.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        // חיפוש הצעות עבודה מתאימות
        DatabaseReference jobsRef = databaseRef.child("ParentJobs");
        Query query = jobsRef.orderByChild("status").equalTo("pending");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jobsList.clear();
                for (DataSnapshot jobSnapshot : snapshot.getChildren()) {
                    HashMap<String, String> job = (HashMap<String, String>) jobSnapshot.getValue();
                    if (job != null && isJobMatching(job, location, date, startTime, endTime)) {
                        job.put("jobId", jobSnapshot.getKey()); // שמירת מזהה העבודה
                        jobsList.add(job);
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchBabysitterActivity.this,
                        "שגיאה בטעינת הצעות העבודה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isJobMatching(HashMap<String, String> job, String location,
                                  String date, String startTime, String endTime) {
        // בדיקת התאמת מיקום
        boolean locationMatch = job.get("location").equalsIgnoreCase(location);
        if (!locationMatch) return false;

        // בדיקת התאמת תאריך
        boolean dateMatch = job.get("date").equals(date);
        if (!dateMatch) return false;

        // בדיקת חפיפת שעות
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date jobStart = timeFormat.parse(job.get("startTime"));
            Date jobEnd = timeFormat.parse(job.get("endTime"));
            Date searchStart = timeFormat.parse(startTime);
            Date searchEnd = timeFormat.parse(endTime);

            // בדיקה אם טווח השעות חופף
            return !searchEnd.before(jobStart) && !searchStart.after(jobEnd);

        } catch (ParseException e) {
            return false;
        }
    }

    private void updateUI() {
        if (jobsList.isEmpty()) {
            noJobsText.setVisibility(View.VISIBLE);
            jobsRecyclerView.setVisibility(View.GONE);
        } else {
            noJobsText.setVisibility(View.GONE);
            jobsRecyclerView.setVisibility(View.VISIBLE);
        }
        jobsAdapter.notifyDataSetChanged();
    }

    /**
     * Schedule job reminder.
     *
     * @param jobId     the job id
     * @param date      the date
     * @param startTime the start time
     */
    public void scheduleJobReminder(String jobId, String date, String startTime) {
        try {
            // המרת התאריך והשעה למילישקונדות
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date jobDate = sdf.parse(date + " " + startTime);

            if (jobDate != null) {
                // הגדרת התזכורת לשעה לפני תחילת העבודה
                long reminderTime = jobDate.getTime() - 3600000; // שעה אחת פחות במילישקונדות

                Intent intent = new Intent(this, ReminderReceiver.class);
                intent.putExtra("jobId", jobId);
                intent.putExtra("date", date);
                intent.putExtra("startTime", startTime);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        jobId.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    reminderTime,
                                    pendingIntent
                            );
                            Toast.makeText(this, "תזכורת נקבעה לשעה לפני תחילת העבודה", Toast.LENGTH_SHORT).show();
                        } else {
                            // בקשת הרשאה להגדרת התראות מדויקות
                            Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(permissionIntent);
                            Toast.makeText(this, "נדרשת הרשאה להגדרת תזכורות", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                reminderTime,
                                pendingIntent
                        );
                        Toast.makeText(this, "תזכורת נקבעה לשעה לפני תחילת העבודה", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (ParseException e) {
            Toast.makeText(this, "שגיאה בהגדרת התזכורת", Toast.LENGTH_SHORT).show();
        }
    }
}
