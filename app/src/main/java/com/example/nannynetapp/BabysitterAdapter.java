package com.example.nannynetapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BabysitterAdapter extends RecyclerView.Adapter<BabysitterAdapter.BabySitterViewHolder> {

    private Context context;
    private List<Babysitter> babySitterList;

    public BabysitterAdapter(Context context, List<Babysitter> babySitterList) {
        this.context = context;
        this.babySitterList = babySitterList;
    }

    @NonNull
    @Override
    public BabySitterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_babysitter, parent, false);
        return new BabySitterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BabySitterViewHolder holder, int position) {
        Babysitter babySitter = babySitterList.get(position);

        holder.nameText.setText(babySitter.getName());
        holder.locationText.setText("מיקום: " + babySitter.getLocation());
        holder.dateText.setText("תאריך: " + babySitter.getDate());
        holder.timeRangeText.setText("שעות: " + babySitter.getStartTime() + " - " + babySitter.getEndTime());
    }

    @Override
    public int getItemCount() {
        return babySitterList.size();
    }

    public static class BabySitterViewHolder extends RecyclerView.ViewHolder {

        TextView nameText, locationText, dateText, timeRangeText;

        public BabySitterViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.babysitterName);
            locationText = itemView.findViewById(R.id.babysitterLocation);
            dateText = itemView.findViewById(R.id.babysitterDate);
            timeRangeText = itemView.findViewById(R.id.babysitterTimeRange);
        }
    }
}
