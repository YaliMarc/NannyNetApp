package com.example.nannynetapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

/**
 * The type Settings activity.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;

    private ImageView profileImage;
    private MaterialButton changeProfilePicBtn, deleteAccountBtn;
    private Switch notificationsSwitch;

    private FirebaseAuth auth;
    private DatabaseReference userDatabaseRef;
    private StorageReference storageRef;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // קישור רכיבים
        profileImage = findViewById(R.id.profileImage);
        changeProfilePicBtn = findViewById(R.id.changeProfilePicBtn);
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);

        // אתחול Firebase
        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures").child(userId + ".jpg");

        // שינוי תמונת פרופיל
        changeProfilePicBtn.setOnClickListener(view -> showImagePickerDialog());

        // מחיקת חשבון
        deleteAccountBtn.setOnClickListener(view -> confirmDeleteAccount());

        // הפעלת/כיבוי התראות
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userDatabaseRef.child("notificationsEnabled").setValue(isChecked);
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications off", Toast.LENGTH_SHORT).show();
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"camera", "gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(" Select an image source");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // מצלמה
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, CAMERA_REQUEST);
            } else { // גלריה
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST);
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                imageUri = data.getData();
                profileImage.setImageURI(imageUri);
                uploadProfilePicture();
            } else if (requestCode == CAMERA_REQUEST && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                profileImage.setImageBitmap(photo);
                uploadProfilePicture();
            }
        }
    }

    private void uploadProfilePicture() {
        if (imageUri != null) {
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        userDatabaseRef.child("profileImageUrl").setValue(uri.toString());
                        Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Error uploading image!", Toast.LENGTH_SHORT).show());
        }
    }

    private void confirmDeleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Account deletion");
        builder.setMessage("Are you sure you want to delete your account?");
        builder.setPositiveButton("Yes", (dialog, which) -> deleteAccount());
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void deleteAccount() {
        String userId = auth.getCurrentUser().getUid();

        userDatabaseRef.removeValue();
        storageRef.delete();

        auth.getCurrentUser().delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SettingsActivity.this, "The account was successfully deleted!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Error deleting account!", Toast.LENGTH_SHORT).show());
    }
}
