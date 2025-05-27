package com.example.nannynetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Chat list activity.
 */
public class ChatListActivity extends AppCompatActivity {
    private RecyclerView userList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userList = findViewById(R.id.userList);
        userList.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();
    }

    private void loadUsers() {
        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("users")
            .whereNotEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<User> users = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    User user = document.toObject(User.class);
                    users.add(user);
                }
                UserAdapter adapter = new UserAdapter(users);
                userList.setAdapter(adapter);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "שגיאה בטעינת משתמשים: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<User> users;

        /**
         * Instantiates a new User adapter.
         *
         * @param users the users
         */
        public UserAdapter(List<User> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.bind(user);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        /**
         * The type User view holder.
         */
        class UserViewHolder extends RecyclerView.ViewHolder {
            private TextView nameText;
            private TextView typeText;

            /**
             * Instantiates a new User view holder.
             *
             * @param itemView the item view
             */
            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.userNameTextView);
                typeText = itemView.findViewById(R.id.userTypeTextView);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        User user = users.get(position);
                        openChat(user);
                    }
                });
            }

            /**
             * Bind.
             *
             * @param user the user
             */
            public void bind(User user) {
                nameText.setText(user.getName());
                typeText.setText(user.getType().equals("parent") ? "הורה" : "בייביסיטר");
            }
        }
    }

    private void openChat(User otherUser) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("otherUserId", otherUser.getUserId());
        intent.putExtra("otherUserName", otherUser.getName());
        startActivity(intent);
    }
} 