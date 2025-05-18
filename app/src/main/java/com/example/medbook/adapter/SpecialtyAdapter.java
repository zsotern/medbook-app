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
import com.example.medbook.DoctorListActivity;
import com.example.medbook.R;
import com.example.medbook.model.Specialty;

import java.util.ArrayList;
import java.util.List;

public class SpecialtyAdapter extends RecyclerView.Adapter<SpecialtyAdapter.ViewHolder> implements Filterable {
    private ArrayList<Specialty> mSpecialtyData;
    private List<Specialty> mSpecialtiesDataAll;
    private Context mContext;
    private int lastPosition = -1;

    public SpecialtyAdapter(Context context, ArrayList<Specialty> specialtyData) {
        this.mContext = context;
        this.mSpecialtyData = specialtyData;
        this.mSpecialtiesDataAll = new ArrayList<>(specialtyData);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_specialty, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Specialty currentSpecialty = mSpecialtyData.get(position);
        holder.bindTo(currentSpecialty);

        // Animáció
        if (holder.getBindingAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getBindingAdapterPosition();
        }
    }
    @Override
    public int getItemCount() {
        return mSpecialtyData.size();
    }

    @Override
    public Filter getFilter() {
        return specialtyFilter;
    }

    private final Filter specialtyFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Specialty> filteredList = new ArrayList<>();
            FilterResults result = new FilterResults();

            if (charSequence == null || charSequence.length() == 0) {
                result.values = new ArrayList<>(mSpecialtiesDataAll);
                result.count = mSpecialtiesDataAll.size();
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();

                for (Specialty spec : mSpecialtiesDataAll) {
                    if (spec.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(spec);
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
                mSpecialtyData = (ArrayList<Specialty>) filterResults.values;
                notifyDataSetChanged();
            }
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView specialtyImageView;
        TextView nameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            specialtyImageView = itemView.findViewById(R.id.specialtyImage);
            nameTextView = itemView.findViewById(R.id.specialtyName);

            itemView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Specialty currentSpecialty = mSpecialtyData.get(position);
                    if (currentSpecialty != null) {
                        Log.d("SpecialtyAdapter", "Gomb megnyomva: " + currentSpecialty.getName());

                        // DoctorActivity indítása az adatokkal
                        Intent intent = new Intent(mContext, DoctorListActivity.class);
                        intent.putExtra("SECRET_KEY", 99);  // Hozzáadva a szükséges kulcs
                        intent.putExtra("specialty_id", currentSpecialty.getId());
                        intent.putExtra("specialty_name", currentSpecialty.getName());
                        mContext.startActivity(intent);

                    } else {
                        Log.e("SpecialtyAdapter", "A Specialty objektum null!");
                    }
                }
            });
        }

        public void bindTo(Specialty currentSpecialty) {
            Glide.with(itemView.getContext())
                    .load(currentSpecialty.getImageResource())
                    .placeholder(R.drawable.foglalj_dokit_icon)  // Helyettesítő kép
                    .error(R.drawable.error_image)              // Hiba esetén kép
                    .into(specialtyImageView);
            nameTextView.setText(currentSpecialty.getName());

            Log.d("SpecialtyAdapter", "Image resource ID: " + currentSpecialty.getImageResource());

        }
    }

}
