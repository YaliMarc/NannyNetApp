package com.example.nannynetapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nannynetapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * The type Message adapter.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;

    /**
     * Instantiates a new Message adapter.
     *
     * @param context     the context
     * @param messageList the message list
     */
    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    /**
     * Gets item view type.
     *
     * @param position the position
     * @return the item view type
     */
    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    /**
     * On create view holder message view holder.
     *
     * @param parent   the parent
     * @param viewType the view type
     * @return the message view holder
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    /**
     * On bind view holder.
     *
     * @param holder   the holder
     * @param position the position
     */
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (message.getType().equals("text")) {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
            holder.messageText.setText(message.getMessageText());
        } else if (message.getType().equals("image")) {
            holder.messageText.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getImageUrl()).into(holder.messageImage);
        }
        // לשיחת וידאו תוכל להוסיף תנאי נוסף אם תרצה להציג כפתור קבלה/סירוב
    }

    /**
     * Gets item count.
     *
     * @return the item count
     */
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * The type Message view holder.
     */
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        /**
         * The Message text.
         */
        TextView messageText;
        /**
         * The Message image.
         */
        ImageView messageImage;

        /**
         * Instantiates a new Message view holder.
         *
         * @param itemView the item view
         */
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageImage = itemView.findViewById(R.id.messageImage);
        }
    }
}
