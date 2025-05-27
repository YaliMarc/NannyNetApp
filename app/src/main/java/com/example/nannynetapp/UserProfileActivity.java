package com.example.nannynetapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The type User profile activity.
 */
public class UserProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView nameText, userTypeText, locationText, experienceText, ratingText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        nameText = findViewById(R.id.nameText);
        userTypeText = findViewById(R.id.userTypeText);
        locationText = findViewById(R.id.locationText);
        experienceText = findViewById(R.id.experienceText);
        ratingText = findViewById(R.id.ratingText);

        // Get user ID from intent
        String userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "שגיאה: לא נמצא מזהה משתמש", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile(userId);
    }

    private void loadUserProfile(String userId) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateUI(documentSnapshot);
                    } else {
                        Toast.makeText(this, "המשתמש לא נמצא", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "שגיאה בטעינת פרופיל המשתמש", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateUI(DocumentSnapshot userDoc) {
        // Set user name
        String name = userDoc.getString("fullName");
        nameText.setText(name);

        // Set user type
        String userType = userDoc.getString("userType");
        userTypeText.setText("Parent".equals(userType) ? "הורה" : "בייביסיטר");

        // Set location
        String location = userDoc.getString("location");
        if (location != null) {
            locationText.setVisibility(View.VISIBLE);
            locationText.setText("מיקום: " + location);
        }

        // Set experience (for babysitters)
        if ("Babysitter".equals(userType)) {
            String experience = userDoc.getString("experience");
            if (experience != null) {
                experienceText.setVisibility(View.VISIBLE);
                experienceText.setText("ניסיון: " + experience);
            }

            // Set rating
            Double rating = userDoc.getDouble("rating");
            if (rating != null) {
                ratingText.setVisibility(View.VISIBLE);
                ratingText.setText(String.format("דירוג: %.1f/5.0", rating));
            }
        }

        // Load profile image
        String imageUrl = userDoc.getString("profileImageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(profileImage);
        }
    }
} 