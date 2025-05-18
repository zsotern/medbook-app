package com.example.medbook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medbook.adapter.DoctorAdapter;
import com.example.medbook.model.Doctor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class DoctorListActivity extends AppCompatActivity {
    private static final String LOG_TAG = DoctorListActivity.class.getName();
    private static final int SECRET_KEY = 99;
    private FirebaseUser user;

    private RecyclerView mRecyclerView;
    private ArrayList<Doctor> mDoctorData;
    private DoctorAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mDoctor;

    String specialtyId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != SECRET_KEY) {
            finish();
        }

        //lekérjük a user adtait aki be van jelentkezve
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Log.d(LOG_TAG, "Authenticated user");
        } else {
            Log.d(LOG_TAG, "Unauthenticated user");
            finish();
        }

        // Intentből az adatok fogadása
        specialtyId = getIntent().getStringExtra("specialty_id");
        String specialtyName = getIntent().getStringExtra("specialty_name");

        if (specialtyId == null || specialtyName == null) {
            Log.e("DoctorListActivity", "Nincs megfelelő adat az intentben!");
            finish();  // Bezárjuk az Activity-t, ha nincsenek adatok
            return;
        }

        Log.d("DoctorListActivity", "Received specialty ID: " + specialtyId);
        Log.d("DoctorListActivity", "Received specialty name: " + specialtyName);


        mRecyclerView = findViewById(R.id.recyclerViewDoc);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mDoctorData = new ArrayList<>();
        mAdapter = new DoctorAdapter(this, mDoctorData);
        mRecyclerView.setAdapter(mAdapter);


        mFirestore = FirebaseFirestore.getInstance();
        mDoctor = mFirestore.collection("Doctor");

        queryData();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void queryData() {
        mDoctorData.clear();

        mDoctor.whereEqualTo("specialtyId", specialtyId)
                .orderBy("name", Query.Direction.ASCENDING)  // Név szerint növekvő
                .limit(10)  // Maximum 10 orvos
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        Doctor doctor = document.toObject(Doctor.class);
                        mDoctorData.add(doctor);
                        Log.d(LOG_TAG, "Doctor added: " + doctor.getName());
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error parsing document: " + document.getId(), e);
                    }
                }
                if(mDoctorData.isEmpty()){
                    initializeData();
                    queryData();
                }
            } else {
                Log.d(LOG_TAG, "No data found in Firestore.");
                initializeData();
                queryData();
            }
            mAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e(LOG_TAG, "Error loading data from Firestore: ", e);
        });
    }

    // Ha nincs az adatbázisban adat, akkor feltötjük
    private void initializeData() {
        String[] doctorsName = getResources().getStringArray(R.array.doctor_names);
        String[] doctorsBio = getResources().getStringArray(R.array.doctor_bios);
        String[] specialtyIds = getResources().getStringArray(R.array.specialty_id);
        TypedArray doctorImageResource = getResources().obtainTypedArray(R.array.doctor_images);

        //mItemList.clear();


        for (int i = 0; i < doctorsName.length; i++) {
            int resid = i % 2;
            mDoctor.add(new Doctor(
                    Integer.toString(i), // id
                    doctorsName[i],
                    specialtyIds[i],
                    doctorsBio[i],
                    doctorImageResource.getResourceId(resid, 0)));
        }

        doctorImageResource.recycle();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.mb_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.log_out_button) {
            Log.d(LOG_TAG, "Logout clicked!");
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;

        }  else if (id == R.id.profile_button) {
            Log.d(LOG_TAG, "Profile clicked!");
            startProfile();
            return true;

        } else if (id == R.id.home_button) {
            Log.d(LOG_TAG, "Home clicked!");
            startHome();
            return true;

        } else if (id == R.id.history_button) {
            Log.d(LOG_TAG, "History clicked!");
            startHistroy();
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected void startProfile(){
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    protected void startHome(){
        Intent intent = new Intent(this, SpecialtyListActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    protected void startHistroy(){
        Intent intent = new Intent(this, HistoryListActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DoctorListActivity", "Activity resumed");
    }

}

