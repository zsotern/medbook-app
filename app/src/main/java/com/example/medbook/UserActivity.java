package com.example.medbook;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.medbook.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity{
    private static final String LOG_TAG = UserActivity.class.getName();
    private static final int SECRET_KEY = 99;
    private static final int REQUEST_IMAGE_CAPTURE = 1;


    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private String userId;

    private EditText profileName, profileUserName, profilePhone, profileEmail;
    private ImageView profilImage;
    private Button saveButton, cancelButton;
    private Bitmap imageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != SECRET_KEY) {
            finish();
        }

        profileName = findViewById(R.id.profileName);
        profileUserName = findViewById(R.id.profileUserName);
        profilePhone = findViewById(R.id.profilePhone);
        profileEmail = findViewById(R.id.profileEmail);
        profileEmail.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        //lekérjük a user adtait aki be van jelentkezve
        user = mAuth.getCurrentUser();
        userId = user.getUid();

        if (user != null) {
            Log.d(LOG_TAG, "Authenticated user");
        } else {
            Log.d(LOG_TAG, "Unauthenticated user");
            finish();
        }

        loadUserData();

        profilImage = findViewById(R.id.profilImage);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        Button captureButton = findViewById(R.id.buttonUpdatePic);

        // Kamera gomb megnyomása
        captureButton.setOnClickListener(v -> {
            // Ellenőrizzük a kamera engedélyt
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Ha nincs engedély, kérjük meg
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 102);
            } else {
                // Ha van engedély, nyissuk meg a kamerát
                openCamera();
            }
        });

        // Mentés gomb
        saveButton.setOnClickListener(v -> saveImage());

        // Mégse gomb
        cancelButton.setOnClickListener(v -> cancelImage());

        Button updateButton = findViewById(R.id.buttonUpdate);
        updateButton.setOnClickListener(v-> updateUserData());

        Button deleteButton = findViewById(R.id.buttonDelete);
        deleteButton.setOnClickListener(v -> deleteUser());

    }

    private void loadUserData() {
        DocumentReference docRef = mFirestore.collection("User").document(userId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String userName = documentSnapshot.getString("userName");
                String phoneNumber = documentSnapshot.getString("phoneNumber");
                String email = documentSnapshot.getString("email");

                String base64Image = documentSnapshot.getString("profileImage");
                profileName.setText(name);
                profileUserName.setText(userName);
                profilePhone.setText(phoneNumber);
                profileEmail.setText(email);

                if (base64Image != null) {
                    Bitmap bitmap = base64ToBitmap(base64Image);
                    profilImage.setImageBitmap(bitmap);
                    Log.d("ProfileActivity", "Image loaded from Firestore");
                }

            }
        }).addOnFailureListener(e -> {
            profileName.setText("Hiba az adatok betöltésekor");
        });
    }

    public void deleteUser() {
        // Figyelmeztető ablak létrehozása
        new AlertDialog.Builder(this)
                .setTitle("Fiók törlése")
                .setMessage("Biztosan törölni szeretné a felhasználói fiókját? Ez a művelet nem visszavonható.")
                .setPositiveButton("Igen", (dialog, which) -> {

                    // Törlés a Firestore-ból
                    DocumentReference docRef = mFirestore.collection("User").document(userId);
                    docRef.delete().addOnSuccessListener(aVoid -> {
                        Log.d(LOG_TAG, "User data deleted from Firestore.");

                        // Törlés az Authentication rendszerből
                        mAuth.getCurrentUser().delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(LOG_TAG, "User account deleted.");
                                logOut();
                            } else {
                                Log.e(LOG_TAG, "Error deleting user account.", task.getException());
                                profileName.setText("Hiba a felhasználói fiók törlésekor");
                            }
                        });

                    }).addOnFailureListener(e -> {
                        Log.e(LOG_TAG, "Error deleting user data.", e);
                        profileName.setText("Hiba az adatok törlésekor");
                    });
                })
                .setNegativeButton("Mégse", (dialog, which) -> {
                    // Ha a felhasználó a "Mégse" gombra kattint, bezárjuk az ablakot
                    dialog.dismiss();
                    Log.d(LOG_TAG, "Felhasználói fiók törlése megszakítva.");
                })
                .show();
    }

    public void updateUserData(){
        String name = profileName.getText().toString();
        String userName = profileUserName.getText().toString();
        String phoneNumber = profilePhone.getText().toString();
        String email = profileEmail.getText().toString();

        // Adatok ellenőrzése
        if (name.isEmpty() || userName.isEmpty() || phoneNumber.isEmpty() || email.isEmpty()) {
            profileName.setText("Töltse ki az összes mezőt!");
            return;
        }

        // Frissítendő adatok egy térképben
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("userName", userName);
        updates.put("phoneNumber", phoneNumber);
        updates.put("email", email);

        // Firestore frissítése
        DocumentReference docRef = mFirestore.collection("User").document(userId);
        docRef.update(updates).addOnSuccessListener(aVoid -> {
            Log.d(LOG_TAG, "User data updated successfully.");
            Toast.makeText(UserActivity.this,
                    "Az adatok frissítése sikeresen megtörtént!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e(LOG_TAG, "Error updating user data.", e);
            Toast.makeText(UserActivity.this,
                    "Hiba az adatok frissítésekor!", Toast.LENGTH_SHORT).show();
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
            logOut();
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

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            profilImage.setImageBitmap(imageBitmap);
            toggleButtonsVisibility(true);  // Gombok megjelenítése
        }
    }

    // Mentés gomb funkció
    private void saveImage() {
        profilImage.buildDrawingCache();
        Bitmap bitmap = profilImage.getDrawingCache();
        saveImageToFirestore(bitmap);
        Log.d("ProfileActivity", "Profilkép elmentve az adatbázisba.");
        toggleButtonsVisibility(false);  // Gombok elrejtése
        loadUserData();
    }

    // Mégse gomb funkció
    private void cancelImage() {
        DocumentReference docRef = mFirestore.collection("User").document(userId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String base64Image = documentSnapshot.getString("profileImage");
                if (base64Image != null && !base64Image.equals("null")) {
                    Bitmap bitmap = base64ToBitmap(base64Image);
                    profilImage.setImageBitmap(bitmap);
                    Log.d("ProfileActivity", "Profilkép visszaállítva az adatbázisból");
                } else {
                    profilImage.setImageResource(R.drawable.default_profil_pic);
                    Log.d("ProfileActivity", "Nincs mentett kép, alapértelmezett kép használata");
                }
            } else {
                profilImage.setImageResource(R.drawable.default_profil_pic);
                Log.d("ProfileActivity", "Nincs felhasználói adat, alapértelmezett kép használata");
            }
            toggleButtonsVisibility(false);  // Gombok elrejtése
        }).addOnFailureListener(e -> {
            profilImage.setImageResource(R.drawable.default_profil_pic);
            Log.e("ProfileActivity", "Hiba a kép visszaállításakor", e);
            toggleButtonsVisibility(false);  // Gombok elrejtése
        });
    }

    // Gombok láthatóságának kezelése
    private void toggleButtonsVisibility(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        saveButton.setVisibility(visibility);
        cancelButton.setVisibility(visibility);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Kamera engedély megtagadva!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void logOut(){
        mAuth.signOut();
        Intent intent = new Intent(UserActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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

    private void saveImageToFirestore(Bitmap bitmap) {
        String base64Image = bitmapToBase64(bitmap);

        DocumentReference userRef = mFirestore.collection("User").document(userId);

        userRef.update("profileImage", base64Image)
                .addOnSuccessListener(aVoid -> Log.d("ProfileActivity", "Image saved to Firestore"))
                .addOnFailureListener(e -> Log.e("ProfileActivity", "Error saving image", e));
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LOG_TAG, "onRestart");
    }

    @Override
    protected void onPause(){
        super.onPause();
        saveUserInput();  // Elmenti az aktuális beírt adatokat
        Log.d("UserActivity", "Felhasználói adatok mentése onPause-ban.");
    }

    private void saveUserInput() {
        SharedPreferences preferences = getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("profileName", profileName.getText().toString());
        editor.putString("profilePhone", profilePhone.getText().toString());
        editor.putString("profilUserName", profileUserName.getText().toString());
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
