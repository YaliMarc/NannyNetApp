package com.example.nannynetapp;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nannynetapp.R;
import com.example.nannynetapp.Message;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The type Message adapter.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private Context context;
    private ArrayList<Message> messageList;
    private String currentUserId;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;

    /**
     * Instantiates a new Message adapter.
     *
     * @param context       the context
     * @param messageList   the message list
     * @param currentUserId the current user id
     */
    public MessageAdapter(Context context, ArrayList<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);

        // Reset views
        holder.textMessage.setVisibility(View.GONE);
        holder.imageMessage.setVisibility(View.GONE);
        holder.videoMessage.setVisibility(View.GONE);
        holder.dateHeader.setVisibility(View.GONE);

        // Check if date header should be shown
        if (position == 0 || !isSameDay(messageList.get(position - 1).getTimestamp(), msg.getTimestamp())) {
            holder.dateHeader.setVisibility(View.VISIBLE);
            holder.dateHeader.setText(getDateHeader(msg.getTimestamp()));
        }

        // Set message bubble direction based on sender
        LinearLayout messageBubble = holder.messageBubble;
        LinearLayout messageContainer = holder.messageContainer;

        if (msg.getSenderId() != null && msg.getSenderId().equals(currentUserId)) {
            messageContainer.setGravity(Gravity.END);
            messageBubble.setBackgroundResource(R.drawable.message_background_self);
        } else {
            messageContainer.setGravity(Gravity.START);
            messageBubble.setBackgroundResource(R.drawable.message_background_other);
        }

        // Set message content based on type
        switch (msg.getType()) {
            case "text":
                holder.textMessage.setText(msg.getContent());
                holder.textMessage.setVisibility(View.VISIBLE);
                break;
            case "image":
                holder.imageMessage.setVisibility(View.VISIBLE);
                holder.videoMessage.setVisibility(View.GONE);
                Glide.with(context)
                        .load(msg.getContent())
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .into(holder.imageMessage);
                // Click to enlarge
                holder.imageMessage.setOnClickListener(v -> showFullScreenMedia(msg.getContent(), "image"));
                break;
            case "video":
                holder.imageMessage.setVisibility(View.GONE);
                holder.videoMessage.setVisibility(View.VISIBLE);
                holder.videoMessage.setVideoURI(Uri.parse(msg.getContent()));
                holder.videoMessage.seekTo(1); // Show preview
                // Click to enlarge
                holder.videoMessage.setOnClickListener(v -> showFullScreenMedia(msg.getContent(), "video"));
                break;
        }

        // Set time
        if (msg.getTimestamp() != null) {
            String timeString = timeFormat.format(msg.getTimestamp());
            holder.messageTime.setText(timeString);
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private String getDateHeader(Date date) {
        if (date == null) return "";

        Calendar messageCal = Calendar.getInstance();
        Calendar todayCal = Calendar.getInstance();
        messageCal.setTime(date);
        todayCal.setTime(new Date());

        // If today
        if (isSameDay(date, new Date())) {
            return "Today";
        }

        // If yesterday
        Calendar yesterdayCal = Calendar.getInstance();
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);
        if (messageCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
                messageCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }

        // Otherwise, show full date
        return dateFormat.format(date);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private void showFullScreenMedia(String url, String type) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        if ("image".equals(type)) {
            ImageView imageView = new ImageView(context);
            imageView.setBackgroundColor(0xFF000000);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(context).load(url).into(imageView);
            dialog.setContentView(imageView);
            imageView.setOnClickListener(v -> dialog.dismiss());
        } else if ("video".equals(type)) {
            FrameLayout layout = new FrameLayout(context);
            VideoView videoView = new VideoView(context);
            videoView.setVideoURI(Uri.parse(url));
            videoView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            videoView.setOnPreparedListener(mp -> videoView.start());
            layout.addView(videoView);
            dialog.setContentView(layout);
            layout.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }
}