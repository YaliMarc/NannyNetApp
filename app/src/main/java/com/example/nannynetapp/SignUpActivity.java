package com.example.nannynetapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText fullNameInput, emailInput, passwordInput;
    private MaterialButton signUpButton;
    private RadioGroup roleGroup;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // אתחול Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        // חיבור רכיבים
        fullNameInput = findViewById(R.id.inputFullName);
        emailInput = findViewById(R.id.inputEmail);
        passwordInput = findViewById(R.id.inputPassword);
        signUpButton = findViewById(R.id.signupButton);
        roleGroup = findViewById(R.id.radioGroupRole);

        // האזנה ללחיצת כפתור הרשמה
        signUpButton.setOnClickListener(view -> checkIfUserExists());
    }

    private void checkIfUserExists() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("יש להזין כתובת אימייל");
            return;
        }

        // בדיקה אם המשתמש כבר קיים ב- Realtime Database לפי אימייל
        databaseRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(SignUpActivity.this, "משתמש עם אימייל זה כבר קיים!", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignUpActivity.this, "שגיאה בבדיקת המשתמש: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser() {
        String fullName = fullNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // בדיקות תקינות קלט
        if (TextUtils.isEmpty(fullName)) {
            fullNameInput.setError("יש להזין שם מלא");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordInput.setError("יש להזין סיסמה עם לפחות 6 תווים");
            return;
        }

        // קבלת סוג המשתמש (Parent / Babysitter)
        int selectedRoleId = roleGroup.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "יש לבחור תפקיד (הורה או בייביסיטר)", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedRoleButton = findViewById(selectedRoleId);
        String userType = selectedRoleButton.getText().toString();

        // יצירת משתמש ב-Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserToDatabase(user.getUid(), fullName, email, userType);
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "שגיאה בהרשמה: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(String userId, String fullName, String email, String userType) {
        // יצירת אובייקט HashMap עם פרטי המשתמש
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("fullName", fullName);
        userMap.put("email", email);
        userMap.put("userType", userType);

        // שמירת המשתמש ב-Firebase Realtime Database
        databaseRef.child(userId).setValue(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SignUpActivity.this, "ההרשמה הצליחה!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(SignUpActivity.this, "שגיאה בשמירת המשתמש: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
// User Model Class
class User {
    private String fullName, email, role;

    public User() {}

    public User(String fullName, String email, String role) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    // Getters (needed for Firestore)
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}






