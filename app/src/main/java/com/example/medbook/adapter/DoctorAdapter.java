package com.example.medbook.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medbook.AppointmentActivity;
import com.example.medbook.DoctorListActivity;
import com.example.medbook.R;
import com.example.medbook.model.Doctor;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> implements Filterable {
    private ArrayList<Doctor> mDoctorData;
    private List<Doctor> mDoctorDataAll;
    private Context mContext;
    private int lastPosition = -1;

    public DoctorAdapter(Context context, ArrayList<Doctor> doctorData) {
        this.mContext = context;
        this.mDoctorData = doctorData;
        this.mDoctorDataAll = new ArrayList<>(doctorData);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor currentDoctor = mDoctorData.get(position);
        holder.bindTo(currentDoctor);

        // Animáció
        if (holder.getBindingAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getBindingAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mDoctorData.size();
    }

    @Override
    public Filter getFilter() {
        return doctorFilter;
    }

    private final Filter doctorFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Doctor> filteredList = new ArrayList<>();
            FilterResults result = new FilterResults();

            if (charSequence == null || charSequence.length() == 0) {
                result.values = new ArrayList<>(mDoctorDataAll);
                result.count = mDoctorDataAll.size();
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (Doctor doc : mDoctorDataAll) {
                    if (doc.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(doc);
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
                mDoctorData = (ArrayList<Doctor>) filterResults.values;
                notifyDataSetChanged();
            }
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView doctorImageView;
        TextView docNameTextView;
        TextView docBioTextView;
        TextView specTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            doctorImageView = itemView.findViewById(R.id.doctorImage);
            docNameTextView = itemView.findViewById(R.id.doctorName);
            docBioTextView = itemView.findViewById(R.id.bioD);
            specTextView = itemView.findViewById(R.id.specialtyD);


            // Új: A CardView-ra kattintás kezelése
            itemView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Doctor currentDoctor = mDoctorData.get(position);
                    if (currentDoctor != null) {
                        Log.d("DoctorAdapter", "CardView megnyomva: " + currentDoctor.getName());

                        // AppointmentActivity indítása az adatokkal
                        Intent intent = new Intent(mContext, AppointmentActivity.class);
                        intent.putExtra("SECRET_KEY", 99);  // Hozzáadva a szükséges kulcs
                        intent.putExtra("specialty_id", currentDoctor.getSpecialtyId());
                        intent.putExtra("doctor_id", currentDoctor.getId());
                        intent.putExtra("doctor_name", currentDoctor.getName());
                        mContext.startActivity(intent);
                    } else {
                        Log.e("DoctorAdapter", "A Doctor objektum null!");
                    }
                }
            });
        }

        public void bindTo(Doctor currentDoctor) {
            int imageRes = currentDoctor.getImageResource();
            Log.d("DoctorAdapter", "Loading image with resource ID: " + imageRes);
            Glide.with(itemView.getContext())
                    .load(currentDoctor.getImageResource())
                    .into(doctorImageView);
            docNameTextView.setText(currentDoctor.getName());
            docBioTextView.setText(currentDoctor.getBio());
            specTextView.setText(currentDoctor.getSpecialtyId());
        }
    }
}
