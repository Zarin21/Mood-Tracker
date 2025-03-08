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
import com.example.unemployedavengers.arrayadapters.MoodEventArrayAdapter;
import com.example.unemployedavengers.databinding.DashboardBinding;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.example.unemployedavengers.models.MoodEvent;
import com.example.unemployedavengers.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dashboard extends Fragment {
    private DashboardBinding binding;
    private ArrayList<MoodEvent> moodList;
    //private ListView moodListView;
    private MoodEventArrayAdapter moodAdapter;
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

        //database
        db = FirebaseFirestore.getInstance();
        moodEventRef = db.collection("users").document(userID).collection("moods");

        moodList = new ArrayList<>();
        moodAdapter = new MoodEventArrayAdapter(requireContext(), moodList);
        binding.activityList.setAdapter(moodAdapter);

        //load mood event function
        loadMoodEvents();


        binding.friendsButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboardFragment_to_friendsHistoryFragment)
        );
        binding.profileButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboardFragment_to_profileFragment)
        );




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


    private void loadMoodEvents() {
        //get all moodevent from firebase
        moodEventRef.get()
                .addOnCompleteListener(task -> {
                    //convert each document to
                    if (task.isSuccessful()) {
                        List<MoodEvent> moodEvents = new ArrayList<>();
                        //each documentSnapsho is a document fethced from task.getResult()
                        for (DocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class); //convert to MoodEvent class
                            moodEvents.add(moodEvent); //add to array
                        }

                        //sort the mood events by time in descending order (most recent first)
                        Collections.sort(moodEvents, (e1, e2) -> Long.compare(e2.getTime(), e1.getTime()));

                        //limit the list to the most recent 7 mood events
                        List<MoodEvent> recentMoodEvents = new ArrayList<>();

                        //store the most recent 7 moodevent or smaller.
                        for (int i = 0; i < Math.min(7, moodEvents.size()); i++) {
                            recentMoodEvents.add(moodEvents.get(i));
                        }

                        //set the adapter with the recent 7 mood events
                        MoodEventArrayAdapter adapter = new MoodEventArrayAdapter(getContext(), recentMoodEvents);
                        binding.activityList.setAdapter(adapter);

                        //set item click listener
                        binding.activityList.setOnItemClickListener((parent, view, position, id) -> {
                            //get the selected MoodEvent based on position
                            MoodEvent selectedMoodEvent = recentMoodEvents.get(position);

                            //create a bundle and put the selected MoodEvent in it
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("selected_mood_event", selectedMoodEvent);

                            //navigate to inputdialog and pass the selected mood event
                            Navigation.findNavController(view).navigate(R.id.action_dashboardFragment_to_inputDialog, bundle);
                        });
                    } else {
                        Log.e("Dashboard", "Error fetching mood events", task.getException());
                    }
                });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
