package com.example.nannynetapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * The type Login activity.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private TextView forgotPassword, signUpText;

    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // אתחול Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        // אתחול רכיבי UI
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUpText = findViewById(R.id.signUpText);

        // מאזין ללחיצה על כפתור התחברות
        loginButton.setOnClickListener(view -> loginUser());

        // מעבר למסך הרשמה
        signUpText.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });

        // מעבר למסך שחזור סיסמה
        forgotPassword.setOnClickListener(view -> {
            Toast.makeText(LoginActivity.this, "Forgot password clicked!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        // התחברות ל-Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            // קבלת סוג המשתמש מ- Firebase Database
                            databaseRef.child(userId).get().addOnSuccessListener(snapshot -> {
                                if (snapshot.exists()) {
                                    String userType = snapshot.child("userType").getValue(String.class);
                                    Toast.makeText(LoginActivity.this, "Successful login!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Toast.makeText(LoginActivity.this, "User details not found!", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(e ->
                                    Toast.makeText(LoginActivity.this, "Error getting user data", Toast.LENGTH_SHORT).show()
                            );

                        }
                    } else {
                        Toast.makeText(LoginActivity.this, " Login error:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
