package com.example.medbook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medbook.adapter.HistoryAdapter;
import com.example.medbook.model.Appointment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HistoryListActivity extends AppCompatActivity {
    private static final String LOG_TAG = HistoryListActivity.class.getName();
    private static final int SECRET_KEY = 99;
    private FirebaseUser user;

    private RecyclerView mRecyclerView;
    private ArrayList<Appointment> mAppointmentData;
    private HistoryAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mAppointment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history_list);

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

        mFirestore = FirebaseFirestore.getInstance();
        mAppointmentData = new ArrayList<>();
        mAdapter = new HistoryAdapter(this, mAppointmentData);

        mRecyclerView = findViewById(R.id.recyclerViewHis);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        mRecyclerView.setAdapter(mAdapter);

        queryData();  // Lekérdezi a foglalásokat
    }

    @SuppressLint("NotifyDataSetChanged")
    private void queryData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mFirestore.collection("Appointment")
                .whereEqualTo("userId", userId)  // Csak a bejelentkezett felhasználó foglalásai
                .orderBy("year", Query.Direction.ASCENDING)
                .orderBy("month", Query.Direction.ASCENDING)
                .orderBy("day", Query.Direction.ASCENDING)
                .orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                Appointment appointment = document.toObject(Appointment.class);
                                mAppointmentData.add(appointment);
                                Log.d(LOG_TAG, "Appointment added: " + appointment.getAppointmentId());
                            } catch (Exception e) {
                                Log.e(LOG_TAG, "Error parsing document: " + document.getId(), e);
                            }
                        }
                        if (mAppointmentData.isEmpty()) {
                            Log.d(LOG_TAG, "No appointments found for this user.");
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(LOG_TAG, "No data found in Firestore.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Error loading data from Firestore: ", e);
                });
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
}
