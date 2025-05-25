package com.example.nannynetapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private Spinner recipientSpinner;
    private TextInputEditText messageInput;
    private ImageButton sendButton, cameraButton, videoCallButton;
    private RecyclerView messagesRecyclerView;

    private FirebaseAuth auth;
    private DatabaseReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // התחברות ל־Firebase
        auth = FirebaseAuth.getInstance();
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        // איתחול רכיבים
        recipientSpinner = findViewById(R.id.recipientSpinner);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        cameraButton = findViewById(R.id.cameraButton);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);

        sendButton.setOnClickListener(view -> sendMessage());

        cameraButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                openCamera();
            }
        });

    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        String recipient = recipientSpinner.getSelectedItem().toString();
        String senderId = auth.getCurrentUser().getUid();

        if (TextUtils.isEmpty(messageText)) {
            messageInput.setError("Write something...");
            return;
        }

        String messageId = messagesRef.push().getKey();

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("recipient", recipient);
        messageData.put("message", messageText);
        messageData.put("timestamp", System.currentTimeMillis());

        messagesRef.child(messageId).setValue(messageData);
        messageInput.setText("");
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

            // הפיכת התמונה ל־Base64 או העלאה ל־Firebase Storage (לא חובה כעת)
            // הדוגמה כאן שומרת רק טקסט בתור תיאור
            sendMessageWithImage("Image sent.");
        }
    }

    private void sendMessageWithImage(String description) {
        String recipient = recipientSpinner.getSelectedItem().toString();
        String senderId = auth.getCurrentUser().getUid();
        String messageId = messagesRef.push().getKey();

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderId);
        messageData.put("recipient", recipient);
        messageData.put("imageMessage", description);
        messageData.put("timestamp", System.currentTimeMillis());

        messagesRef.child(messageId).setValue(messageData);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }
}
