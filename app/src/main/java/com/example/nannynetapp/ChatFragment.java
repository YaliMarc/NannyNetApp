package com.example.nannynetapp;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nannynetapp.R;
import com.example.nannynetapp.ChatPreviewAdapter;
import com.example.nannynetapp.ChatPreview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

/**
 * The type Chat fragment.
 */
public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";
    private RecyclerView recyclerView;
    private Button startChatButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ChatPreviewAdapter adapter;
    private List<ChatPreview> chatList = new ArrayList<>();
    private String userType;

    /**
     * Instantiates a new Chat fragment.
     */
    public ChatFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Check user type
        String currentUserId = auth.getCurrentUser().getUid();
        db.collection("Users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    userType = documentSnapshot.getString("userType");
                    Log.d(TAG, "User type: " + userType);
                    // After determining user type, refresh chat list
                    if (recyclerView != null && adapter != null) {
                        loadChatList();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking user type", e);
                    if (recyclerView != null && adapter != null) {
                        loadChatList();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewChats);
        startChatButton = view.findViewById(R.id.startChatButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new ChatPreviewAdapter(chatList, chat -> openChatFragment(chat));
        recyclerView.setAdapter(adapter);

        startChatButton.setOnClickListener(v -> openNewChatDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChatList();
    }

    private void openChatFragment(ChatPreview chat) {
        ChatMessageFragment chatFragment = ChatMessageFragment.newInstance(
                chat.chatId,
                chat.displayName,
                chat.imageUrl,
                "Parent".equals(userType)
        );

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadChatList() {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (currentUserId == null) return;

        db.collection("Chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    chatList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String chatId = doc.getId();
                        String otherUserId = getOtherUserId(doc.get("participants", List.class), currentUserId);
                        
                        // Skip if other user type matches current user type (parent-parent or babysitter-babysitter)
                        if (!isValidChatPartner(otherUserId)) {
                            continue;
                        }

                        String imageUrl = doc.getString("otherUserImage");
                        String displayName = doc.getString("otherUserName");
                        String lastMessage = doc.getString("lastMessage");
                        Date lastMessageTime = doc.getTimestamp("lastMessageTime") != null ?
                                doc.getTimestamp("lastMessageTime").toDate() : new Date();

                        ChatPreview chatPreview = new ChatPreview(chatId, displayName, imageUrl, lastMessage, lastMessageTime);
                        chatList.add(chatPreview);
                    }
                    adapter.notifyDataSetChanged();
                    updateStartChatButtonVisibility();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading chats", e);
                    updateStartChatButtonVisibility();
                });
    }

    private boolean isValidChatPartner(String otherUserId) {
        if (otherUserId == null) return false;
        
        final boolean[] isValid = {false};
        final boolean[] checkComplete = {false};
        
        db.collection("Users").document(otherUserId).get()
                .addOnSuccessListener(doc -> {
                    String otherUserType = doc.getString("userType");
                    // Parent can only chat with Babysitter and vice versa
                    isValid[0] = ("Parent".equals(userType) && "Babysitter".equals(otherUserType)) ||
                                ("Babysitter".equals(userType) && "Parent".equals(otherUserType));
                    checkComplete[0] = true;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking other user type", e);
                    checkComplete[0] = true;
                });
        
        // Wait for the check to complete
        while (!checkComplete[0]) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        return isValid[0];
    }

    private String getOtherUserId(List<String> participants, String currentUserId) {
        if (participants == null || participants.size() != 2) return null;
        return participants.get(0).equals(currentUserId) ? participants.get(1) : participants.get(0);
    }

    private void updateStartChatButtonVisibility() {
        startChatButton.setVisibility(chatList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openNewChatDialog() {
        String currentUserId = auth.getCurrentUser().getUid();
        
        // Query for opposite user type
        String targetUserType = "Parent".equals(userType) ? "Babysitter" : "Parent";
        
        db.collection("Users")
                .whereEqualTo("userType", targetUserType)
                .get()
                .addOnSuccessListener(query -> {
                    List<String> userNames = new ArrayList<>();
                    Map<String, String> userIdMap = new HashMap<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String name = doc.getString("fullName");
                        if (name != null) {
                            userNames.add(name);
                            userIdMap.put(name, doc.getId());
                        }
                    }

                    if (!userNames.isEmpty()) {
                        showSelectionDialog(userNames, userIdMap);
                    } else {
                        showNoUsersFoundMessage(targetUserType);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading users", e);
                    Context context = getContext();
                    if (context != null) {
                        Toast.makeText(context, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showNoUsersFoundMessage(String targetUserType) {
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, "No " + targetUserType.toLowerCase() + "s found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSelectionDialog(List<String> userNames, Map<String, String> userIdMap) {
        if (userNames.isEmpty()) return;

        String[] options = userNames.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("בחר משתמש לצ'אט");
        builder.setItems(options, (dialog, which) -> {
            String selectedName = options[which];
            String selectedId = userIdMap.get(selectedName);
            createNewChat(selectedName, selectedId);
        });
        builder.show();
    }

    private void createNewChat(String selectedName, String selectedId) {
        String currentUserId = auth.getCurrentUser().getUid();
        if (currentUserId == null || selectedId == null || selectedId.isEmpty()) {
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, "Could not create chat", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Check if chat already exists
        db.collection("Chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean chatExists = false;
                    String existingChatId = null;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        List<String> participants = (List<String>) doc.get("participants");
                        if (participants != null && participants.contains(selectedId)) {
                            chatExists = true;
                            existingChatId = doc.getId();
                            break;
                        }
                    }

                    if (chatExists && existingChatId != null) {
                        // Open existing chat
                        ChatPreview existingChat = new ChatPreview(
                                existingChatId,
                                selectedName,
                                "",
                                "",
                                new Date()
                        );
                        openChatFragment(existingChat);
                    } else {
                        // Create new chat
                        String chatId = db.collection("Chats").document().getId();
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("participants", Arrays.asList(currentUserId, selectedId));
                        chatData.put("createdAt", new Date());
                        chatData.put("lastMessageTime", new Date());
                        chatData.put("otherUserName", selectedName);

                        db.collection("Chats").document(chatId)
                                .set(chatData)
                                .addOnSuccessListener(aVoid -> {
                                    ChatPreview newChat = new ChatPreview(
                                            chatId,
                                            selectedName,
                                            "",
                                            "",
                                            new Date()
                                    );
                                    openChatFragment(newChat);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating chat", e);
                                    Context context = getContext();
                                    if (context != null) {
                                        Toast.makeText(context, "Error creating chat", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking existing chats", e);
                    Context context = getContext();
                    if (context != null) {
                        Toast.makeText(context, "Error creating chat", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}