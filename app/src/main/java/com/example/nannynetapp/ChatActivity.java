package com.example.nannynetapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private ImageButton sendButton, imageButton, videoCallButton;

    private static final int IMAGE_PICK_CODE = 1001;
    private static final int CAMERA_PERMISSION_CODE = 1002;

    private FirebaseUser currentUser;
    private String recipientId; // יש לעדכן לפי מי שנבחר בצ'אט

    private DatabaseReference chatRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        imageButton = findViewById(R.id.imageButton);
        videoCallButton = findViewById(R.id.videoCallButton);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recipientId = getIntent().getStringExtra("recipientId"); // מעביר את מזהה הנמען

        chatRef = FirebaseDatabase.getInstance().getReference("Chats");
        storageRef = FirebaseStorage.getInstance().getReference("ChatImages");

        sendButton.setOnClickListener(view -> sendMessage());

        imageButton.setOnClickListener(view -> pickImageFromGallery());

        videoCallButton.setOnClickListener(view -> checkCameraPermissionAndStartVideoCall());
    }

    private void sendMessage() {
        String msg = messageInput.getText().toString().trim();
        if (!msg.isEmpty()) {
            HashMap<String, Object> messageData = new HashMap<>();
            messageData.put("sender", currentUser.getUid());
            messageData.put("receiver", recipientId);
            messageData.put("message", msg);
            messageData.put("type", "text");
            messageData.put("timestamp", System.currentTimeMillis());

            chatRef.push().setValue(messageData);
            messageInput.setText("");
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            String fileName = System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child(fileName);

            imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        HashMap<String, Object> imageMessage = new HashMap<>();
                        imageMessage.put("sender", currentUser.getUid());
                        imageMessage.put("receiver", recipientId);
                        imageMessage.put("message", uri.toString());
                        imageMessage.put("type", "image");
                        imageMessage.put("timestamp", System.currentTimeMillis());

                        chatRef.push().setValue(imageMessage);
                    })
            ).addOnFailureListener(e ->
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void checkCameraPermissionAndStartVideoCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            startVideoCall();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVideoCall();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startVideoCall() {
        // מעבר למסך שיחת וידאו, לדוגמה:
        Intent intent = new Intent(this, VideoCallActivity.class);
        intent.putExtra("recipientId", recipientId);
        startActivity(intent);
    }
}
