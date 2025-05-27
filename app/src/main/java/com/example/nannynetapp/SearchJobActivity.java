package com.example.nannynetapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The type Search job activity.
 */
public class SearchJobActivity extends AppCompatActivity {
    private TextInputEditText locationInput, dateInput, startTimeInput, endTimeInput, minSalaryInput, maxSalaryInput;
    private ChipGroup requirementsChipGroup;
    private Button searchButton;
    private RecyclerView resultsRecyclerView;
    private TextView titleText;
    private Calendar selectedDate = Calendar.getInstance();
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isBabysitterMode;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_job);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // קבלת המצב (חיפוש בייביסיטר או עבודה) מה-Intent
        isBabysitterMode = getIntent().getBooleanExtra("isBabysitterMode", false);

        initializeViews();
        setupDateTimePickers();
        setupRequirementsChips();
        setupSearchButton();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        locationInput = findViewById(R.id.locationInput);
        dateInput = findViewById(R.id.dateInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        minSalaryInput = findViewById(R.id.minSalaryInput);
        maxSalaryInput = findViewById(R.id.maxSalaryInput);
        requirementsChipGroup = findViewById(R.id.requirementsChipGroup);
        searchButton = findViewById(R.id.searchButton);
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);

        titleText.setText(isBabysitterMode ? "חיפוש בייביסיטר" : "חיפוש עבודה");
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupDateTimePickers() {
        dateInput.setOnClickListener(v -> showDatePicker());
        startTimeInput.setOnClickListener(v -> showTimePicker(startTimeInput));
        endTimeInput.setOnClickListener(v -> showTimePicker(endTimeInput));
    }

    private void setupRequirementsChips() {
        String[] requirements = {"ניסיון קודם", "המלצות", "עזרה בשיעורי בית", "הכנת אוכל", "ניקיון"};
        for (String requirement : requirements) {
            Chip chip = new Chip(this);
            chip.setText(requirement);
            chip.setCheckable(true);
            requirementsChipGroup.addView(chip);
        }
    }

    private void setupSearchButton() {
        searchButton.setOnClickListener(v -> performSearch());
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateInput();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showTimePicker(TextInputEditText timeInput) {
        Calendar currentTime = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                timeInput.setText(time);
            },
            currentTime.get(Calendar.HOUR_OF_DAY),
            currentTime.get(Calendar.MINUTE),
            true
        );
        dialog.show();
    }

    private void updateDateInput() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateInput.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void performSearch() {
        if (!validateInputs()) {
            return;
        }

        // יצירת אובייקט Job חדש עם הפרטים שהוזנו
        Job searchJob = createJobFromInputs();

        // ביצוע החיפוש ב-Firestore
        Query query = db.collection("jobs")
            .whereEqualTo("isBabysitterSearch", !isBabysitterMode) // חיפוש הפוך למצב הנוכחי
            .whereEqualTo("location", searchJob.getLocation());

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Job> results = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Job job = document.toObject(Job.class);
                if (isJobMatching(job, searchJob)) {
                    results.add(job);
                }
            }
            displayResults(results);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "שגיאה בחיפוש: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private boolean validateInputs() {
        if (locationInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "נא להזין מיקום", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dateInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "נא להזין תאריך", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (startTimeInput.getText().toString().trim().isEmpty() || 
            endTimeInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "נא להזין שעות עבודה", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (minSalaryInput.getText().toString().trim().isEmpty() || 
            maxSalaryInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "נא להזין טווח שכר", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Job createJobFromInputs() {
        String location = locationInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String startTime = startTimeInput.getText().toString().trim();
        String endTime = endTimeInput.getText().toString().trim();
        double minSalary = Double.parseDouble(minSalaryInput.getText().toString().trim());
        double maxSalary = Double.parseDouble(maxSalaryInput.getText().toString().trim());

        // איסוף הדרישות שנבחרו
        List<String> selectedRequirements = new ArrayList<>();
        for (int i = 0; i < requirementsChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) requirementsChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedRequirements.add(chip.getText().toString());
            }
        }

        // יצירת אובייקט Job חדש
        Job job = new Job();
        job.setLocation(location);
        job.setDate(date);
        job.setStartTime(startTime);
        job.setEndTime(endTime);
        job.setMinHourlyRate(minSalary);
        job.setMaxHourlyRate(maxSalary);
        job.setRequirements(gson.toJson(selectedRequirements));
        job.setBabysitterSearch(isBabysitterMode);
        job.setParentId(auth.getCurrentUser().getUid());
        
        return job;
    }

    private boolean isJobMatching(Job job1, Job job2) {
        // בדיקת התאמה בין שני ג'ובים
        if (!job1.getLocation().equals(job2.getLocation())) {
            return false;
        }

        if (!job1.getDate().equals(job2.getDate())) {
            return false;
        }

        // בדיקת חפיפה בשעות
        if (!isTimeOverlapping(job1, job2)) {
            return false;
        }

        // בדיקת חפיפה בטווח שכר
        if (!isSalaryOverlapping(job1, job2)) {
            return false;
        }

        // בדיקת דרישות
        return areRequirementsMatching(job1, job2);
    }

    private boolean isTimeOverlapping(Job job1, Job job2) {
        String[] start1 = job1.getStartTime().split(":");
        String[] end1 = job1.getEndTime().split(":");
        String[] start2 = job2.getStartTime().split(":");
        String[] end2 = job2.getEndTime().split(":");

        int start1Minutes = Integer.parseInt(start1[0]) * 60 + Integer.parseInt(start1[1]);
        int end1Minutes = Integer.parseInt(end1[0]) * 60 + Integer.parseInt(end1[1]);
        int start2Minutes = Integer.parseInt(start2[0]) * 60 + Integer.parseInt(start2[1]);
        int end2Minutes = Integer.parseInt(end2[0]) * 60 + Integer.parseInt(end2[1]);

        return !(end1Minutes <= start2Minutes || end2Minutes <= start1Minutes);
    }

    private boolean isSalaryOverlapping(Job job1, Job job2) {
        return !(job1.getMaxHourlyRate() < job2.getMinHourlyRate() || 
                job2.getMaxHourlyRate() < job1.getMinHourlyRate());
    }

    private boolean areRequirementsMatching(Job job1, Job job2) {
        List<String> reqs1 = gson.fromJson(job1.getRequirements(), 
            new TypeToken<List<String>>(){}.getType());
        List<String> reqs2 = gson.fromJson(job2.getRequirements(), 
            new TypeToken<List<String>>(){}.getType());

        // בדיקה שכל הדרישות של המחפש נמצאות אצל המציע
        for (String req : reqs2) {
            if (!reqs1.contains(req)) {
                return false;
            }
        }
        return true;
    }

    private void displayResults(List<Job> results) {
        if (results.isEmpty()) {
            Toast.makeText(this, "לא נמצאו תוצאות מתאימות", Toast.LENGTH_SHORT).show();
            return;
        }

        JobAdapter adapter = new JobAdapter(results, job -> {
            // שמירת ההצעה/בקשה ב-Firestore
            saveJobMatch(job);
        });
        resultsRecyclerView.setAdapter(adapter);
    }

    private void saveJobMatch(Job matchedJob) {
        Job newJob = createJobFromInputs();
        newJob.setJobId(db.collection("jobs").document().getId());
        
        // שמירת שני הג'ובים (ההצעה והבקשה) ב-Firestore
        db.collection("jobs").document(newJob.getJobId())
            .set(newJob)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "ההצעה נשמרה בהצלחה", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "שגיאה בשמירת ההצעה: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }
} 