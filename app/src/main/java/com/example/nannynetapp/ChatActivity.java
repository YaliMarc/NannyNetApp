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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendMessageButton, sendImageButton, videoCallButton;

    private FirebaseUser currentUser;
    private DatabaseReference chatRef;
    private StorageReference storageRef;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int VIDEO_CALL_PERMISSION_CODE = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // אתחול רכיבים
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        sendImageButton = findViewById(R.id.sendImageButton);
        videoCallButton = findViewById(R.id.videoCallButton);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        chatRef = FirebaseDatabase.getInstance().getReference("Chats");
        storageRef = FirebaseStorage.getInstance().getReference("ChatImages");

        // כפתור שליחת הודעה
        sendMessageButton.setOnClickListener(view -> sendMessage());

        // כפתור שליחת תמונה
        sendImageButton.setOnClickListener(view -> openGallery());

        // כפתור שיחת וידאו
        videoCallButton.setOnClickListener(view -> checkVideoCallPermission());
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) return;

        HashMap<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("sender", currentUser.getUid());
        chatMessage.put("message", message);
        chatMessage.put("type", "text");

        chatRef.push().setValue(chatMessage);
        messageInput.setText("");
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void checkVideoCallPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, VIDEO_CALL_PERMISSION_CODE);
        } else {
            startVideoCall();
        }
    }

    private void startVideoCall() {
        Toast.makeText(this, "Starting Video Call...", Toast.LENGTH_SHORT).show();
        // כאן ניתן לשלב WebRTC עבור שיחות וידאו
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            storageRef.child(System.currentTimeMillis() + ".jpg").putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> Toast.makeText(this, "Image Sent!", Toast.LENGTH_SHORT).show());
        }
    }
}
