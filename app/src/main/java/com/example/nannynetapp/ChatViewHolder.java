package com.example.nannynetapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nannynetapp.R;

/**
 * The type Chat view holder.
 */
public class ChatViewHolder extends RecyclerView.ViewHolder {
    /**
     * The Image.
     */
    ImageView image;
    /**
     * The Name.
     */
    TextView name;

    /**
     * Instantiates a new Chat view holder.
     *
     * @param itemView the item view
     */
    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.imageProfile);
        name = itemView.findViewById(R.id.textName);
    }
}