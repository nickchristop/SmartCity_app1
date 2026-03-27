package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ρύθμιση του URL χειροκίνητα
        String databaseUrl = "https://smartcityui-12e16-default-rtdb.europe-west1.firebasedatabase.app/";

        // 2. Δημιουργία Instance και εγγραφή
        try {
            com.google.firebase.database.FirebaseDatabase db =
                    com.google.firebase.database.FirebaseDatabase.getInstance(databaseUrl);

            db.getReference("connection_test").setValue("Connected at " + System.currentTimeMillis());

            android.util.Log.d("FIREBASE_DEBUG", "Attempting to write to DB...");
        } catch (Exception e) {
            android.util.Log.e("FIREBASE_DEBUG", "Error: " + e.getMessage());
        }

        // Φορτώνουμε το Dashboard (Home) εξ ορισμού
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            // Εδώ αργότερα θα βάλουμε το switch για να αλλάζουν οι οθόνες
            return true;
        });
    }
    
}
