package com.example.nannynetapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BabysitterAdapter extends RecyclerView.Adapter<BabysitterAdapter.BabysitterViewHolder> {

    private Context context;
    private List<Babysitter> babysitterList;

    public BabysitterAdapter(Context context, List<Babysitter> babysitterList) {
        this.context = context;
        this.babysitterList = babysitterList;
    }

    @NonNull
    @Override
    public BabysitterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_babysitter, parent, false);
        return new BabysitterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BabysitterViewHolder holder, int position) {
        Babysitter babysitter = babysitterList.get(position);
        holder.nameTextView.setText(babysitter.getFullName());
        holder.locationTextView.setText("מיקום: " + babysitter.getLocation());
        holder.salaryTextView.setText("שכר: " + babysitter.getSalary() + "₪");
        holder.datesTextView.setText("תאריכים זמינים: " + babysitter.getAvailableDates());
        holder.hoursTextView.setText("שעות זמינות: " + babysitter.getAvailableHours());
    }

    @Override
    public int getItemCount() {
        return babysitterList.size();
    }

    public static class BabysitterViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, locationTextView, salaryTextView, datesTextView, hoursTextView;

        public BabysitterViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.babysitterName);
            locationTextView = itemView.findViewById(R.id.babysitterLocation);
            salaryTextView = itemView.findViewById(R.id.babysitterSalary);
            datesTextView = itemView.findViewById(R.id.babysitterDates);
            hoursTextView = itemView.findViewById(R.id.babysitterHours);
        }
    }
}
