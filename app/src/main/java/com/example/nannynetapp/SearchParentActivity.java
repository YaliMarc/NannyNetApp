package com.example.nannynetapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;

public class SearchParentActivity extends AppCompatActivity {

    private EditText locationInput, dateInput, startTimeInput, endTimeInput;
    private Button searchButton;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_parent);

        locationInput = findViewById(R.id.locationInput);
        dateInput = findViewById(R.id.dateInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        searchButton = findViewById(R.id.searchButton);

        databaseRef = FirebaseDatabase.getInstance().getReference("SearchRequests");

        // בחירת תאריך
        dateInput.setOnClickListener(v -> showDatePicker(dateInput));

        // בחירת שעות
        startTimeInput.setOnClickListener(v -> showTimePicker(startTimeInput));
        endTimeInput.setOnClickListener(v -> showTimePicker(endTimeInput));

        // כפתור חיפוש ושמירה
        searchButton.setOnClickListener(view -> saveSearchRequest());
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) ->
                        editText.setText(year + "-" + (month + 1) + "-" + dayOfMonth),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) ->
                        editText.setText(String.format("%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void saveSearchRequest() {
        String location = locationInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String startTime = startTimeInput.getText().toString().trim();
        String endTime = endTimeInput.getText().toString().trim();

        if (location.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        String requestId = databaseRef.push().getKey();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "";

        if (requestId != null && !userId.isEmpty()) {
            HashMap<String, String> requestData = new HashMap<>();
            requestData.put("userId", userId);
            requestData.put("location", location);
            requestData.put("date", date);
            requestData.put("startTime", startTime);
            requestData.put("endTime", endTime);

            databaseRef.child(requestId).setValue(requestData)
                    .addOnSuccessListener(unused -> {
                        // מעבר למסך MatchingJobsActivity
                        Intent intent = new Intent(SearchParentActivity.this, MatchingJobsActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(SearchParentActivity.this, "שגיאה בשמירת החיפוש", Toast.LENGTH_SHORT).show());
        }
    }
}
