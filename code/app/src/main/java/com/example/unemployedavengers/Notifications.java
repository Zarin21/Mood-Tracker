/*
 * Notifications Fragment - Displays follow requests for the current user in the Unemployed Avengers application.
 *
 * This fragment handles:
 * - Fetching follow requests from Firestore based on the current user's ID.
 * - Displaying the follow requests in a ListView using a custom adapter (`FollowRequestAdapter`).
 * - Managing the state of the notifications list and updating the UI accordingly.
 *
 * Features:
 * - Retrieves a list of follow requests from Firestore by querying the user's subcollection of "requests".
 * - Displays the list using a `FollowRequestAdapter` to show each follow request in the list.
 * - Handles user authentication via FirebaseAuth and retrieves user details from FirebaseFirestore.
 *
 * Outstanding Issues/Improvements:
 * - Following someone that you followed already is possible
 */

package com.example.unemployedavengers;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.arrayadapters.FollowRequestAdapter;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.example.unemployedavengers.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Notifications extends Fragment {
    private ListView notificationsList;
    private IUserDAO userDAO;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FollowRequestAdapter adapter;
    private List<User> followRequests;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications, container, false);
        notificationsList = view.findViewById(R.id.notifications_list);
        userDAO = new UserDAOImplement();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        followRequests = new ArrayList<>();

        adapter = new FollowRequestAdapter(requireContext(), followRequests, "");
        notificationsList.setAdapter(adapter);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            loadFollowRequests();
        } else {
            Log.e("Notifications", "No user is logged in");
        }

        return view;
    }

    private void loadFollowRequests() {
        if (currentUserId == null) {
            Log.e("Notifications", "currentUserId is null");
            return;
        }

        CollectionReference requestsRef = db.collection("users").document(currentUserId).collection("requests");
        requestsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            followRequests.clear();

            if (queryDocumentSnapshots.isEmpty()) {
                Log.d("Notifications", "No follow requests found");
                adapter.notifyDataSetChanged();
                return;
            }

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String requesterId = document.getId();
                Log.d("Notifications", "Found follow request from: " + requesterId);

                db.collection("users").document(requesterId).get()
                        .addOnSuccessListener(userDoc -> {
                            if (userDoc.exists()) {
                                User user = userDoc.toObject(User.class);
                                if (user != null) {
                                    followRequests.add(user);
                                    adapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.e("Notifications", "User document does not exist: " + requesterId);
                            }
                        })
                        .addOnFailureListener(e -> Log.e("Notifications", "Error fetching user data", e));
            }

            adapter = new FollowRequestAdapter(requireContext(), followRequests, currentUserId);
            notificationsList.setAdapter(adapter);
        }).addOnFailureListener(e ->
                Log.e("Notifications", "Failed to load follow requests", e)
        );
    }
}
