package com.example.medbook;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.medbook.adapter.SpecialtyAdapter;
import com.example.medbook.model.Specialty;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;

public class SpecialtyListActivity extends AppCompatActivity {
    private static final String LOG_TAG = SpecialtyListActivity.class.getName();
    private static final int SECRET_KEY = 99;
    private FirebaseUser user;

    private RecyclerView mRecyclerView;
    private ArrayList<Specialty> mSpecialtyData;
    private SpecialtyAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mSpecialties;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specialty_list);

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != SECRET_KEY) {
            Log.d(LOG_TAG, Integer.toString(secret_key));
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

        mRecyclerView = findViewById(R.id.recyclerViewSpec);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mSpecialtyData = new ArrayList<>();
        mAdapter = new SpecialtyAdapter(this, mSpecialtyData);
        mRecyclerView.setAdapter(mAdapter);


        mFirestore = FirebaseFirestore.getInstance();
        mSpecialties = mFirestore.collection("Specialty");

        queryData();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void queryData() {
        mSpecialtyData.clear();

        mSpecialties.orderBy("id").limit(10).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        Specialty specialty = document.toObject(Specialty.class);
                        mSpecialtyData.add(specialty);
                        Log.d(LOG_TAG, "Specialty added: " + specialty.getName());
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "Error parsing document: " + document.getId(), e);
                    }
                }
                if(mSpecialtyData.isEmpty()){
                    Log.d(LOG_TAG, "Üres a lekerdezes?");
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
        String[] specialtiesName = getResources().getStringArray(R.array.specialty_names);

        TypedArray specialtiesImageResource = getResources().obtainTypedArray(R.array.specialty_images);

        //mItemList.clear();

        for (int i = 0; i < specialtiesName.length; i++) {
            mSpecialties.add(new Specialty(
                    Integer.toString(i), // id
                    specialtiesName[i],
                    specialtiesImageResource.getResourceId(i, 0)));
        }

        specialtiesImageResource.recycle();
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    protected void startHistroy(){
        Intent intent = new Intent(this, HistoryListActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        //queryData();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(LOG_TAG, "onPause: Releasing input channels");
        //mRecyclerView.setAdapter(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("NotificationPermission", "Engedély megadva");
            } else {
                Log.d("NotificationPermission", "Engedély megtagadva");
            }
        }
    }
}
