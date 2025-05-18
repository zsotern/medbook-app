package com.example.medbook;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medbook.model.Appointment;
import com.example.medbook.model.Doctor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class AppointmentActivity extends AppCompatActivity {
    private static final String LOG_TAG = AppointmentActivity.class.getName();
    private static final int SECRET_KEY = 99;

    private FirebaseUser user;

    private FirebaseFirestore mFirestore;
    private CollectionReference mAppointment;

    private String year, month, day;
    private String selectedTime;
    private TextView textSpecialty;
    private TextView textDoctor;

    private String specialtyId;
    private String doctorId;
    private String userId;
    private String doctorName;

    private NotificationHandler mNotificationHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_appointment);

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if(secret_key != SECRET_KEY) {
            finish();
        }

        mFirestore = FirebaseFirestore.getInstance();
        mAppointment = mFirestore.collection("Appointment");

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
        doctorName = getIntent().getStringExtra("doctor_name");

        if (specialtyId == null || doctorId == null || doctorName == null) {
            Log.e("AppointmentActivity", "Nincs megfelelő adat az intentben!");
            finish();  // Bezárjuk az Activity-t, ha nincsenek adatok
            return;
        }

        Log.d("AppointmentActivity", "Received specialty ID: " + specialtyId);
        Log.d("AppointmentActivity", "Received doctor ID: " + doctorId);
        Log.d("AppointmentActivity", "Received doctor name: " + doctorName);

        // Dátum inicializálása az onCreate-ben
        Calendar calendar = Calendar.getInstance();
        year = String.valueOf(calendar.get(Calendar.YEAR));
        month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        DatePicker datePicker = findViewById(R.id.datePicker);
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), (view, yearSelected, monthOfYear, dayOfMonth) -> {
            year = String.valueOf(yearSelected);
            month = String.valueOf(monthOfYear + 1);
            day = String.valueOf(dayOfMonth);
            Log.d("DatePicker", "Selected date: " + year + "-" + month + "-" + day);
        });

        textSpecialty = findViewById(R.id.textSpecialty);
        setupSpecialtyName();

        textDoctor = findViewById(R.id.textDoctor);
        textDoctor.setText(doctorName);


        RadioGroup radioGroupTime = findViewById(R.id.radioGroupTime);
        radioGroupTime.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadio = findViewById(checkedId);
            if (selectedRadio != null) {
                selectedTime = selectedRadio.getText().toString();
                Toast.makeText(this, "Kiválasztott időpont: " + selectedTime, Toast.LENGTH_SHORT).show();
            } else {
                selectedTime = "10:00"; // Alapértelmezett időpont
                Toast.makeText(this, "Alapértelmezett időpont: " + selectedTime, Toast.LENGTH_SHORT).show();
            }
        });

        // Foglalás gomb
        Button bookButton = findViewById(R.id.buttonBook);
        bookButton.setOnClickListener(v -> bookAppointment());

        mNotificationHandler = new NotificationHandler(this);

    }

    private void setupSpecialtyName() {
        Log.d(LOG_TAG, "Received specialty ID: " + specialtyId);

        // Firestore lekérdezés, ahol az "id" mezőt vizsgáljuk
        mFirestore.collection("Specialty")
                .whereEqualTo("id", specialtyId) // Itt keresünk az "id" mezőre
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String specialtyName = document.getString("name");
                            if (specialtyName != null) {
                                textSpecialty.setText(specialtyName);
                                Log.d(LOG_TAG, "Specialty name loaded: " + specialtyName);
                            } else {
                                Log.e(LOG_TAG, "Specialty name is null");
                                textSpecialty.setText("Nincs szakterület");
                            }
                        }
                    } else {
                        Log.e(LOG_TAG, "Specialty document not found");
                        textSpecialty.setText("Szakterület nem található");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "Error retrieving specialty: " + e.getMessage());
                    textSpecialty.setText("Hiba történt");
                });
    }


    @SuppressLint("ScheduleExactAlarm")
    private void bookAppointment() {
        Log.d(LOG_TAG, selectedTime);

        if (selectedTime == null) {
            Toast.makeText(this, "Válasszon időpontot!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (year == null || month == null || day == null) {
            Toast.makeText(this, "Válasszon dátumot!", Toast.LENGTH_SHORT).show();
            return;
        }

        final int min = 1;
        final int max = 100;
        final int random = new Random().nextInt((max - min) + 1) + min;
        String appointmentId = "A" + random;

        Appointment appointment = new Appointment(appointmentId, specialtyId, doctorId, userId, year, month, day, selectedTime);

        mAppointment.add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AppointmentActivity.this, "Foglalás sikeres!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AppointmentActivity.this, BookingSuccessActivity.class);

                    intent.putExtra("SECRET_KEY", SECRET_KEY);
                    intent.putExtra("specialty_id", specialtyId);
                    intent.putExtra("doctor_id", doctorId);
                    intent.putExtra("year", year);
                    intent.putExtra("month", month);
                    intent.putExtra("day", day);
                    intent.putExtra("time", selectedTime);

                    String summary = "Sikeres foglalás!\n\nDátum: " + year + ". " + month + " " + day + ".\nIdőpont: " + selectedTime;
                    mNotificationHandler.send(summary);

                    // Értesítés időzítése (teszt - 5 perc múlva)
                    long currentTime = System.currentTimeMillis();
                    long testInterval = 5 * 60 * 1000L;  // 5 perc (teszt)
                    long triggerTime = currentTime + testInterval;
                    setAlarm(triggerTime, "Emlékeztető", "5 nap múlva van az időpont!");

                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AppointmentActivity.this, "Foglalás sikertelen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void setAlarm(long triggerAtMillis, String title, String message) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            Log.d("AlarmManager", "Értesítés beállítva: " + triggerAtMillis);
        }
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
