package com.example.nannynetapp;

import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nannynetapp.ChatViewHolder;
import com.example.nannynetapp.R;
import com.example.nannynetapp.ChatPreview;

import java.util.*;

/**
 * The type Chat preview adapter.
 */
public class ChatPreviewAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    /**
     * The interface On chat click listener.
     */
    public interface OnChatClickListener {
        /**
         * On chat click.
         *
         * @param chat the chat
         */
        void onChatClick(ChatPreview chat);
    }

    private List<ChatPreview> chatList;
    private OnChatClickListener listener;

    /**
     * Instantiates a new Chat preview adapter.
     *
     * @param chatList the chat list
     * @param listener the listener
     */
    public ChatPreviewAdapter(List<ChatPreview> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_preview, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatPreview chat = chatList.get(position);

        // Set name and image
        holder.name.setText(chat.displayName);
        Glide.with(holder.itemView.getContext())
                .load(chat.imageUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(holder.image);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}