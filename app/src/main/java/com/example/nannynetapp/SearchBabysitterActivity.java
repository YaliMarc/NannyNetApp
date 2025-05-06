package com.example.nannynetapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SearchBabysitterActivity extends AppCompatActivity {

    private EditText locationInput, dateInput, startTimeInput, endTimeInput, salaryRangeInput;
    private Button searchBabysitterButton;
    private RecyclerView babysitterRecyclerView;
    private BabysitterAdapter babysitterAdapter;
    private List<Babysitter> babysitterList;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_babysitter);

        // אתחול רכיבים מה-XML
        locationInput = findViewById(R.id.locationInput);
        dateInput = findViewById(R.id.dateInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        salaryRangeInput = findViewById(R.id.salaryRangeInput);
        searchBabysitterButton = findViewById(R.id.searchBabysitterButton);
        babysitterRecyclerView = findViewById(R.id.babysitterRecyclerView);

        babysitterRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        babysitterList = new ArrayList<>();
        babysitterAdapter = new BabysitterAdapter(this, babysitterList);
        babysitterRecyclerView.setAdapter(babysitterAdapter);
        databaseRef = FirebaseDatabase.getInstance().getReference("Babysitters");

        // בחירת תאריך
        dateInput.setOnClickListener(v -> showDatePicker(dateInput));

        // בחירת שעת התחלה וסיום
        startTimeInput.setOnClickListener(v -> showTimePicker(startTimeInput));
        endTimeInput.setOnClickListener(v -> showTimePicker(endTimeInput));

        // חיפוש
        searchBabysitterButton.setOnClickListener(view -> searchBabysitters());
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> editText.setText(year + "-" + (month + 1) + "-" + dayOfMonth),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> editText.setText(String.format("%02d:%02d", hourOfDay, minute)),
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void searchBabysitters() {
        String location = locationInput.getText().toString().trim();
        String salaryRange = salaryRangeInput.getText().toString().trim();

        if (location.isEmpty() || salaryRange.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות!", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseRef.orderByChild("location").equalTo(location).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                babysitterList.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Babysitter babysitter = snapshot.getValue(Babysitter.class);
                    babysitterList.add(babysitter);
                }
                babysitterAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "לא נמצאו בייביסיטרים במיקום זה.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
