package com.example.nannynetapp;

import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nannynetapp.R;

/**
 * The type Message view holder.
 */
public class MessageViewHolder extends RecyclerView.ViewHolder {
    /**
     * The Text message.
     */
    TextView textMessage;
    /**
     * The Image message.
     */
    ImageView imageMessage;
    /**
     * The Video message.
     */
    VideoView videoMessage;
    /**
     * The Message time.
     */
    TextView messageTime;
    /**
     * The Date header.
     */
    TextView dateHeader;
    /**
     * The Message bubble.
     */
    LinearLayout messageBubble;
    /**
     * The Message container.
     */
    LinearLayout messageContainer;

    /**
     * Instantiates a new Message view holder.
     *
     * @param itemView the item view
     */
    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        textMessage = itemView.findViewById(R.id.textMessage);
        imageMessage = itemView.findViewById(R.id.imageMessage);
        videoMessage = itemView.findViewById(R.id.videoMessage);
        messageTime = itemView.findViewById(R.id.messageTime);
        dateHeader = itemView.findViewById(R.id.dateHeader);
        messageBubble = itemView.findViewById(R.id.messageBubble);
        messageContainer = itemView.findViewById(R.id.messageContainer);
    }
}