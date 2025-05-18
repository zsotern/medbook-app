package com.example.medbook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BookingSuccessActivity extends AppCompatActivity {
    private static final String LOG_TAG = BookingSuccessActivity.class.getName();
    private static final int SECRET_KEY = 99;

    private FirebaseFirestore mFirestore;
    private FirebaseUser user;
    private String userId;
    private String specialtyId;
    private String doctorId;
    private String specialtyName;
    private String doctorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_success);

        mFirestore = FirebaseFirestore.getInstance();

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if(secret_key != SECRET_KEY) {
            finish();
        }

        // lekérjük a user adatait aki be van jelentkezve
        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            userId = user.getUid();
            Log.d(LOG_TAG, "Authenticated user");
        } else {
            Log.d(LOG_TAG, "Unauthenticated user");
            finish();
        }

        specialtyId = getIntent().getStringExtra("specialty_id");
        doctorId = getIntent().getStringExtra("doctor_id");

        if (specialtyId == null || doctorId == null) {
            Log.e("AppointmentActivity", "Nincs megfelelő adat az intentben!");
            finish();  // Bezárjuk az Activity-t, ha nincsenek adatok
            return;
        }

        setupSpecialtyName();
        setupDoctorName();

    }

    public void goHome(View view) {
        Intent intent = new Intent(BookingSuccessActivity.this, SpecialtyListActivity.class);
        intent.putExtra("SECRET_KEY", 99);
        startActivity(intent);
    }

    private void setupSpecialtyName() {
        Log.d(LOG_TAG, "Received specialty ID: " + specialtyId);

        mFirestore.collection("Specialty")
                .whereEqualTo("id", specialtyId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            specialtyName = document.getString("name");
                            if (specialtyName != null) {
                                Log.d(LOG_TAG, "Specialty name loaded: " + specialtyName);
                                updateBookingSummary();  // Frissítsd a foglalás összegzését
                            } else {
                                Log.e(LOG_TAG, "Specialty name is null");
                            }
                        }
                    } else {
                        Log.e(LOG_TAG, "Specialty document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Error retrieving specialty: " + e.getMessage());
                });
    }

    private void setupDoctorName() {
        Log.d(LOG_TAG, "Received doctor ID: " + doctorId);

        mFirestore.collection("Doctor")
                .whereEqualTo("id", doctorId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            doctorName = document.getString("name");
                            if (doctorName != null) {
                                Log.d(LOG_TAG, "Doctor name loaded: " + doctorName);
                                updateBookingSummary();  // Frissítsd a foglalás összegzését
                            } else {
                                Log.e(LOG_TAG, "Doctor name is null");
                            }
                        }
                    } else {
                        Log.e(LOG_TAG, "Doctor document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Error retrieving doctor: " + e.getMessage());
                });
    }

    private void updateBookingSummary() {
        if (specialtyName == null || doctorName == null) {
            // Ha valamelyik még nincs meg, akkor ne folytassuk
            return;
        }

        // Az Intentből az adatok fogadása
        String year = getIntent().getStringExtra("year");
        String month = getIntent().getStringExtra("month");
        String day = getIntent().getStringExtra("day");
        String time = getIntent().getStringExtra("time");

        // Megjelenítés a TextView-ban
        TextView summaryText = findViewById(R.id.bookingSummary);
        String summary = "Sikeres foglalás!\n\nDátum: " + year + ". " + month + ". " + day + ".\nIdőpont: " + time
                + "\nSzakterület: " + specialtyName + "\nOrvos: " + doctorName;
        summaryText.setText(summary);

        Log.d(LOG_TAG, "Booking summary updated: " + summary);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
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
