package com.example.nannynetapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nannynetapp.R;
import com.example.nannynetapp.Message;
import com.example.nannynetapp.MessageAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * The type Chat message fragment.
 */
public class ChatMessageFragment extends Fragment {

    private static final String TAG = "ChatMessageFragment";
    private static final String ARG_CHAT_ID = "chatId";
    private static final String ARG_RECIPIENT_NAME = "recipientName";
    private static final String ARG_RECIPIENT_IMAGE = "recipientImage";
    private static final String ARG_IS_PARENT = "isParent";
    private static final String ARG_JOB_ID = "jobId";

    private ImageView recipientImage;
    private TextView recipientName, jobDetailsText;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private FloatingActionButton sendButton;
    private ImageButton attachButton, backButton;
    private Button viewJobButton;
    private LinearLayout jobDetailsLayout;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String chatId;
    private String recipientDisplayName;
    private String recipientImageUrl;
    private String currentUserId;
    private String jobId;
    private ArrayList<Message> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private boolean isParent;

    // Media variables
    private FrameLayout mediaPreviewLayout;
    private ImageView imagePreview;
    private VideoView videoPreview;
    private ImageButton closeMediaButton;
    private Uri selectedMediaUri = null;
    private String selectedMediaType = null; // "image" / "video"

    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_VIDEO_PICK = 1002;
    private static final int REQUEST_CAMERA = 1003;

    /**
     * New instance chat message fragment.
     *
     * @param chatId         the chat id
     * @param recipientName  the recipient name
     * @param recipientImage the recipient image
     * @param isParent       the is parent
     * @return the chat message fragment
     */
    public static ChatMessageFragment newInstance(String chatId, String recipientName,
                                                String recipientImage, boolean isParent) {
        return newInstance(chatId, recipientName, recipientImage, isParent, null);
    }

    /**
     * New instance chat message fragment.
     *
     * @param chatId         the chat id
     * @param recipientName  the recipient name
     * @param recipientImage the recipient image
     * @param isParent       the is parent
     * @param jobId          the job id
     * @return the chat message fragment
     */
    public static ChatMessageFragment newInstance(String chatId, String recipientName,
                                                String recipientImage, boolean isParent, String jobId) {
        ChatMessageFragment fragment = new ChatMessageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAT_ID, chatId);
        args.putString(ARG_RECIPIENT_NAME, recipientName);
        args.putString(ARG_RECIPIENT_IMAGE, recipientImage);
        args.putBoolean(ARG_IS_PARENT, isParent);
        if (jobId != null) {
            args.putString(ARG_JOB_ID, jobId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        if (getArguments() != null) {
            chatId = getArguments().getString(ARG_CHAT_ID);
            recipientDisplayName = getArguments().getString(ARG_RECIPIENT_NAME);
            recipientImageUrl = getArguments().getString(ARG_RECIPIENT_IMAGE);
            isParent = getArguments().getBoolean(ARG_IS_PARENT);
            jobId = getArguments().getString(ARG_JOB_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_message, container, false);

        recipientImage = view.findViewById(R.id.recipientImage);
        recipientName = view.findViewById(R.id.recipientName);
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        attachButton = view.findViewById(R.id.attachButton);
        backButton = view.findViewById(R.id.backButton);
        jobDetailsLayout = view.findViewById(R.id.jobDetailsLayout);
        jobDetailsText = view.findViewById(R.id.jobDetailsText);
        viewJobButton = view.findViewById(R.id.viewJobButton);

        mediaPreviewLayout = view.findViewById(R.id.mediaPreviewLayout);
        imagePreview = view.findViewById(R.id.imagePreview);
        videoPreview = view.findViewById(R.id.videoPreview);
        closeMediaButton = view.findViewById(R.id.closeMediaButton);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recipientName.setText(recipientDisplayName);
        if (recipientImageUrl != null && !recipientImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(recipientImageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(recipientImage);
        }

        messageAdapter = new MessageAdapter(requireContext(), messageList, currentUserId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesRecyclerView.setAdapter(messageAdapter);

        sendButton.setOnClickListener(v -> {
            if (selectedMediaUri != null && selectedMediaType != null) {
                sendMediaMessage();
            } else {
                sendMessage();
            }
        });
        
        attachButton.setOnClickListener(v -> showAttachmentOptions());
        
        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });
        
        closeMediaButton.setOnClickListener(v -> {
            selectedMediaUri = null;
            selectedMediaType = null;
            mediaPreviewLayout.setVisibility(View.GONE);
            imagePreview.setVisibility(View.GONE);
            videoPreview.setVisibility(View.GONE);
            closeMediaButton.setVisibility(View.GONE);
        });

        // Show job details if available
        if (jobId != null) {
            loadJobDetails();
        }

        listenForMessages();
        listenForJobUpdates();
    }

    private void loadJobDetails() {
        db.collection("Jobs").document(jobId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Job job = documentSnapshot.toObject(Job.class);
                        if (job != null) {
                            jobDetailsLayout.setVisibility(View.VISIBLE);
                            String details = String.format("עבודה ב-%s\nתאריך: %s\nשעות: %s - %s",
                                    job.getLocation(), job.getDate(), job.getStartTime(), job.getEndTime());
                            jobDetailsText.setText(details);
                            
                            viewJobButton.setOnClickListener(v -> {
                                Intent intent = new Intent(getContext(), JobDetailsActivity.class);
                                intent.putExtra("jobId", jobId);
                                startActivity(intent);
                            });
                        }
                    }
                });
    }

    private void listenForJobUpdates() {
        if (jobId == null) return;

        db.collection("Jobs").document(jobId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for job updates", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Job job = documentSnapshot.toObject(Job.class);
                        if (job != null && job.isFullyApproved()) {
                            // Send system message about job approval
                            Message systemMessage = new Message(
                                    "system",
                                    new Date(),
                                    "text",
                                    "העבודה אושרה על ידי שני הצדדים!"
                            );
                            sendSystemMessage(systemMessage);
                        }
                    }
                });
    }

    private void sendSystemMessage(Message message) {
        db.collection("Chats").document(chatId)
                .collection("Messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error sending system message", e));
    }

    private void listenForMessages() {
        if (chatId == null || chatId.isEmpty()) {
            Log.e(TAG, "Chat ID is null or empty");
            Toast.makeText(getContext(), "Error: Invalid chat", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Chats").document(chatId)
                .collection("Messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for messages", e);
                        return;
                    }

                    if (queryDocumentSnapshots == null) return;

                    messageList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Message message = doc.toObject(Message.class);
                        messageList.add(message);
                    }

                    messageAdapter.notifyDataSetChanged();
                    if (messageList.size() > 0) {
                        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        Message message = new Message(
                currentUserId,
                new Date(),
                "text",
                text
        );

        db.collection("Chats").document(chatId)
                .collection("Messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageInput.setText("");

                    Map<String, Object> lastMessageData = new HashMap<>();
                    lastMessageData.put("lastMessage", text);
                    lastMessageData.put("lastMessageTime", new Date());

                    db.collection("Chats").document(chatId)
                            .update(lastMessageData)
                            .addOnSuccessListener(aVoid -> {
                                // Send notification to recipient
                                NotificationHelper.sendFCMNotification(
                                    getRecipientToken(),
                                    recipientDisplayName,
                                    text
                                );
                            })
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Error updating last message", e));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message", e);
                    Toast.makeText(getContext(), "Error sending message", Toast.LENGTH_SHORT).show();
                });
    }

    private String getRecipientToken() {
        final String[] token = {null};
        final boolean[] complete = {false};

        db.collection("Users")
                .document(isParent ? jobId : currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        token[0] = documentSnapshot.getString("fcmToken");
                    }
                    complete[0] = true;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting recipient token", e);
                    complete[0] = true;
                });

        while (!complete[0]) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return token[0];
    }

    private void showAttachmentOptions() {
        PopupMenu popup = new PopupMenu(requireContext(), attachButton);
        popup.getMenuInflater().inflate(R.menu.chat_attachment_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_camera) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
                return true;
            } else if (id == R.id.menu_gallery) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK);
                return true;
            } else if (id == R.id.menu_video) {
                Intent videoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                videoIntent.setType("video/*");
                startActivityForResult(videoIntent, REQUEST_VIDEO_PICK);
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                selectedMediaUri = data.getData();
                selectedMediaType = "image";
                showMediaPreview();
            } else if (requestCode == REQUEST_VIDEO_PICK) {
                selectedMediaUri = data.getData();
                selectedMediaType = "video";
                showMediaPreview();
            } else if (requestCode == REQUEST_CAMERA) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                selectedMediaUri = getImageUri(requireContext(), imageBitmap);
                selectedMediaType = "image";
                showMediaPreview();
            }
        }
    }

    private void showMediaPreview() {
        mediaPreviewLayout.setVisibility(View.VISIBLE);
        closeMediaButton.setVisibility(View.VISIBLE);
        if ("image".equals(selectedMediaType)) {
            imagePreview.setVisibility(View.VISIBLE);
            videoPreview.setVisibility(View.GONE);
            imagePreview.setImageURI(selectedMediaUri);
        } else if ("video".equals(selectedMediaType)) {
            imagePreview.setVisibility(View.GONE);
            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoURI(selectedMediaUri);
            videoPreview.start();
        }
    }

    private Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "CameraImage", null);
        return Uri.parse(path);
    }

    private void sendMediaMessage() {
        if (selectedMediaUri == null || selectedMediaType == null) return;
        String fileName = UUID.randomUUID().toString();
        String path = "chat_media/" + chatId + "/" + fileName;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);

        storageRef.putFile(selectedMediaUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Create message with media link
                    Message message = new Message(
                            currentUserId,
                            new Date(),
                            selectedMediaType,
                            uri.toString()
                    );
                    db.collection("Chats").document(chatId)
                            .collection("Messages")
                            .add(message)
                            .addOnSuccessListener(documentReference -> {
                                // Clear preview
                                selectedMediaUri = null;
                                selectedMediaType = null;
                                mediaPreviewLayout.setVisibility(View.GONE);
                                imagePreview.setVisibility(View.GONE);
                                videoPreview.setVisibility(View.GONE);
                                closeMediaButton.setVisibility(View.GONE);
                            });
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error sending media", Toast.LENGTH_SHORT).show();
                });
    }
}