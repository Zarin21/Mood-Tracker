package com.example.unemployedavengers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.databinding.DashboardBinding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.example.unemployedavengers.models.MoodEvent;
import com.example.unemployedavengers.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Dashboard extends Fragment {
    private DashboardBinding binding;

    private ArrayList<MoodEvent> moodList;
    private ListView moodListView;

    // private MoodEventArrayAdapter moodAdapter; Still need adapter
    private FirebaseFirestore db;
    private CollectionReference moodEventRef;
    private IUserDAO userDAO;

    private String userID;

    private String username;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DashboardBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userDAO = new UserDAOImplement();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);  // Default to null if not found
        userID = sharedPreferences.getString("userID", null);  // Default to null if not found

        if (username != null) {
            Log.d("Dashboard", "Username retrieved: " + username);
        } else {
            Log.e("Dashboard", "No username found in SharedPreferences");
        }


        binding.friendsButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboardFragment_to_friendsHistoryFragment)
        );
        binding.profileButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboardFragment_to_profileFragment)
        );


        //database
        db = FirebaseFirestore.getInstance();
        moodEventRef = db.collection("users").document(userID).collection("moods");


        //Navitages to the input dialog
        binding.addMoodButton.setOnClickListener(v -> {
            Navigation.findNavController(v)
                    .navigate(R.id.action_dashboardFragment_to_inputDialog);


        });

        // Register the listener for the result from InputDialog (Only once)
        getParentFragmentManager().setFragmentResultListener("input_dialog_result", getViewLifecycleOwner(), new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                // Retrieve the MoodEvent object
                MoodEvent moodEvent = (MoodEvent) result.getSerializable("mood_event_key");
                if (moodEvent != null) {

                    addMoodEvent(moodEvent); // Store the mood event in Firestore
                }
            }
        });





    }

    //addMoodEvent function
    public void addMoodEvent(MoodEvent moodEvent) {
        if (moodEventRef != null) {
            moodEventRef.add(moodEvent)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Mood added successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to add mood", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Database reference is null", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
