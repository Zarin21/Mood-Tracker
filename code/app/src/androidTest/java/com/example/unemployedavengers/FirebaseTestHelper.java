package com.example.unemployedavengers;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.net.HttpURLConnection;
import java.net.URL;

public class FirebaseTestHelper {
    public static void setupEmulator() {
        // Android emulator uses 10.0.2.2 to access localhost
        String androidLocalhost = "10.0.2.2";

        // Firestore emulator port
        int firestorePort = 4000; // Using port 4000 as configured in your environment

        // Auth emulator port
        int authPort = 9099; // Default port for Auth emulator

        // Storage emulator port
        int storagePort = 9199; // Default port for Storage emulator

        // Configure Firebase emulators
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, firestorePort);
        FirebaseAuth.getInstance().useEmulator(androidLocalhost, authPort);
        FirebaseStorage.getInstance().useEmulator(androidLocalhost, storagePort);

        Log.i("FirebaseEmulator", "Firebase emulators configured successfully");
    }

    public static void clearDatabase() {
        // Your Firebase project ID - replace with your actual project ID
        String projectId = "unemployeedavenger";

        try {
            // Clear Firestore data
            URL firestoreUrl = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
            HttpURLConnection firestoreConnection = (HttpURLConnection) firestoreUrl.openConnection();
            firestoreConnection.setRequestMethod("DELETE");
            int firestoreResponse = firestoreConnection.getResponseCode();
            Log.i("FirebaseEmulator", "Firestore clear response: " + firestoreResponse);
            firestoreConnection.disconnect();

            // Clear Storage data
            URL storageUrl = new URL("http://10.0.2.2:9199/emulator/v1/projects/" + projectId + "/buckets");
            HttpURLConnection storageConnection = (HttpURLConnection) storageUrl.openConnection();
            storageConnection.setRequestMethod("DELETE");
            int storageResponse = storageConnection.getResponseCode();
            Log.i("FirebaseEmulator", "Storage clear response: " + storageResponse);
            storageConnection.disconnect();

        } catch (Exception e) {
            Log.e("ClearDB Error", e.getMessage(), e);
        }
    }
}