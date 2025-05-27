package com.example.nannynetapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * The type Parent adapter.
 */
public class ParentAdapter extends RecyclerView.Adapter<ParentAdapter.ParentViewHolder> {

    private Context context;
    private List<Parent> parentList;

    /**
     * Instantiates a new Parent adapter.
     *
     * @param context    the context
     * @param parentList the parent list
     */
    public ParentAdapter(Context context, List<Parent> parentList) {
        this.context = context;
        this.parentList = parentList;
    }

    @NonNull
    @Override
    public ParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parent, parent, false);
        return new ParentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParentViewHolder holder, int position) {
        Parent parent = parentList.get(position);
        holder.nameText.setText("שם: " + parent.getFullName());
        holder.locationText.setText("מיקום: " + parent.getLocation());
        holder.dateText.setText("תאריך: " + parent.getDate());
        holder.timeText.setText("שעות: " + parent.getStartTime() + " - " + parent.getEndTime());
        holder.phoneText.setText("טלפון: " + parent.getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return parentList.size();
    }

    /**
     * The type Parent view holder.
     */
    public static class ParentViewHolder extends RecyclerView.ViewHolder {

        /**
         * The Name text.
         */
        TextView nameText, /**
         * The Location text.
         */
        locationText, /**
         * The Date text.
         */
        dateText, /**
         * The Time text.
         */
        timeText, /**
         * The Phone text.
         */
        phoneText;

        /**
         * Instantiates a new Parent view holder.
         *
         * @param itemView the item view
         */
        public ParentViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.parentFullName);
            locationText = itemView.findViewById(R.id.parentLocation);
            dateText = itemView.findViewById(R.id.parentDate);
            timeText = itemView.findViewById(R.id.parentTime);
            phoneText = itemView.findViewById(R.id.parentPhone);
        }
    }
}
