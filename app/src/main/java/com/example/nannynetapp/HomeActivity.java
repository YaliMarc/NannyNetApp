package com.example.nannynetapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat; //
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.ValueEventListener;

/**
 * The type Home activity.
 */
public class HomeActivity extends AppCompatActivity {

    private Button searchBabysitterBtn, lookingForJobBtn, settingsBtn, chatBtn;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private String userType; // משתנה לאחסון סוג המשתמש

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // אתחול Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        // קישור הכפתורים
        searchBabysitterBtn = findViewById(R.id.searchBabysitterBtn);
        lookingForJobBtn = findViewById(R.id.lookingForJobBtn);
        settingsBtn = findViewById(R.id.settingsBtn);
        chatBtn = findViewById(R.id.chatBtn);

        // בדיקה מאיזה סוג המשתמש
        String userId = auth.getCurrentUser().getUid();
        databaseRef.child(userId).child("userType").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userType = snapshot.getValue(String.class);

                    if ("Parent".equals(userType)) {
                        lookingForJobBtn.setVisibility(View.GONE); // מסתיר את הכפתור להורים
                    } else if ("Babysitter".equals(userType)) {
                        searchBabysitterBtn.setVisibility(View.GONE); // מסתיר את כפתור החיפוש לבייביסיטרים
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Error retrieving user type!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Database error!", Toast.LENGTH_SHORT).show();
            }
        });

        // מאזינים לכפתורים
        searchBabysitterBtn.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, SearchBabysitterActivity.class);
            startActivity(intent);
        });

        lookingForJobBtn.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, SearchParentActivity.class);
            startActivity(intent);
        });

        settingsBtn.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        chatBtn.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
            startActivity(intent);
        });
    }
}
