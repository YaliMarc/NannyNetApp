package com.example.nannynetapp;

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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SearchParentActivity extends AppCompatActivity {

    private EditText locationInput, dateInput, startTimeInput, endTimeInput;
    private Button searchButton;
    private RecyclerView parentRecyclerView;
    private List<Parent> parentList;
    private ParentAdapter parentAdapter;

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
        parentRecyclerView = findViewById(R.id.parentRecyclerView);

        parentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        parentList = new ArrayList<>();
        parentAdapter = new ParentAdapter(this, parentList);
        parentRecyclerView.setAdapter(parentAdapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("Users");
         // בחירת תאריך
        dateInput.setOnClickListener(v -> showDatePicker(dateInput));

        // בחירת שעת התחלה וסיום
        startTimeInput.setOnClickListener(v -> showTimePicker(startTimeInput));
        endTimeInput.setOnClickListener(v -> showTimePicker(endTimeInput));

        searchButton.setOnClickListener(view -> searchParents());
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

    private void searchParents() {
        String location = locationInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String startTime = startTimeInput.getText().toString().trim();
        String endTime = endTimeInput.getText().toString().trim();

        if (location.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseRef.orderByChild("userType").equalTo("Parent").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parentList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Parent parent = data.getValue(Parent.class);
                    if (parent != null &&
                            location.equalsIgnoreCase(parent.getLocation()) &&
                            date.equals(parent.getDate()) &&
                            startTime.equals(parent.getStartTime()) &&
                            endTime.equals(parent.getEndTime())) {
                        parentList.add(parent);
                    }
                }
                parentAdapter.notifyDataSetChanged();
                if (parentList.isEmpty()) {
                    Toast.makeText(SearchParentActivity.this, "לא נמצאו הורים מתאימים", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchParentActivity.this, "שגיאה בשליפת נתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
