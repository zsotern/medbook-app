package com.example.medbook.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medbook.R;
import com.example.medbook.model.Appointment;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> implements Filterable {

    private ArrayList<Appointment> mAppointmentData;
    private List<Appointment> mAppointmentDataAll;
    private Context mContext;
    private int lastPosition = -1;

    public HistoryAdapter(Context context, ArrayList<Appointment> appointmentData) {
        this.mContext = context;
        this.mAppointmentData = appointmentData;
        this.mAppointmentDataAll = new ArrayList<>(appointmentData);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        Appointment appointment = mAppointmentData.get(position);

        String summary = "Foglalás:\n" +
                "Szakterület ID: " + appointment.getSpecialtyId() + "\n" +
                "Orvos ID: " + appointment.getDoctorId() + "\n" +
                "Dátum: " + appointment.getYear() + "-" + appointment.getMonth() + "-" + appointment.getDay() + "\n" +
                "Időpont: " + appointment.getTime();

        holder.historyTV.setText(summary);

        // Animáció
        if (holder.getBindingAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getBindingAdapterPosition();
        }
    }


    @Override
    public int getItemCount() {
        return mAppointmentData.size();
    }

    @Override
    public Filter getFilter() {
        return appointmentFilter;
    }

    private final Filter appointmentFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Appointment> filteredList = new ArrayList<>();
            FilterResults result = new FilterResults();

            if (charSequence == null || charSequence.length() == 0) {
                result.values = new ArrayList<>(mAppointmentDataAll);
                result.count = mAppointmentData.size();
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (Appointment appo : mAppointmentDataAll) {
                    if (appo.getMonth().toLowerCase().contains(filterPattern)) {
                        filteredList.add(appo);
                    }
                }

                result.values = filteredList;
                result.count = filteredList.size();
            }
            return result;
        }
        @SuppressLint("NotifyDataSetChanged")
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            if (filterResults.values != null) {
                mAppointmentData = (ArrayList<Appointment>) filterResults.values;
                notifyDataSetChanged();
            }
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView historyTV;
        public ViewHolder(View itemView) {
            super(itemView);
            historyTV = itemView.findViewById(R.id.historyTV);
        }

    }
}
