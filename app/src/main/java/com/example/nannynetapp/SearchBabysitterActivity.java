package com.example.nannynetapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class SearchBabysitterActivity extends AppCompatActivity {

    private EditText locationInput, dateInput;
    private Button searchButton;
    private RecyclerView sitterRecyclerView;
    private List<Babysitter> sitterList;
    private BabysitterAdapter sitterAdapter;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_babysitter);

        locationInput = findViewById(R.id.locationInput);
        dateInput = findViewById(R.id.dateInput);
        searchButton = findViewById(R.id.searchButton);
        sitterRecyclerView = findViewById(R.id.babysitterRecyclerView);

        sitterRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sitterList = new ArrayList<>();
        sitterAdapter = new BabysitterAdapter(this, sitterList);
        sitterRecyclerView.setAdapter(sitterAdapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        dateInput.setOnClickListener(v -> showDatePicker());

        searchButton.setOnClickListener(v -> saveSearchAndFindSitters());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) ->
                        dateInput.setText(year + "-" + (month + 1) + "-" + dayOfMonth),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void saveSearchAndFindSitters() {
        String location = locationInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();

        if (location.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("SearchRequests/BabySitter");
        String requestId = requestRef.push().getKey();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : "";

        if (requestId != null && !userId.isEmpty()) {
            HashMap<String, String> requestData = new HashMap<>();
            requestData.put("userId", userId);
            requestData.put("location", location);
            requestData.put("date", date);

            requestRef.child(requestId).setValue(requestData)
                    .addOnSuccessListener(unused -> runSitterSearch(location, date))
                    .addOnFailureListener(e -> Toast.makeText(SearchBabysitterActivity.this, "שגיאה בשמירת הבקשה", Toast.LENGTH_SHORT).show());
        }
    }

    private void runSitterSearch(String location, String date) {
        databaseRef.orderByChild("userType").equalTo("BabySitter").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sitterList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Babysitter sitter = data.getValue(Babysitter.class);
                    if (sitter != null &&
                            sitter.getLocation() != null &&
                            sitter.getDate() != null &&
                            sitter.getLocation().equalsIgnoreCase(location) &&
                            sitter.getDate().equals(date)) {
                        sitterList.add(sitter);
                    }
                }
                sitterAdapter.notifyDataSetChanged();
                if (sitterList.isEmpty()) {
                    Toast.makeText(SearchBabysitterActivity.this, "לא נמצאו בייביסיטרים מתאימים", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchBabysitterActivity.this, "שגיאה בשליפת נתונים", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
