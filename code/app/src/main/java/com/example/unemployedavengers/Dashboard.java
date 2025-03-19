/*
 * Dashboard Fragment
 * --------------------
 * Purpose:
 * - Display the main dashboard of the Unemployed Avengers app.
 * - Load and show mood events from Firestore using MoodEventArrayAdapter.
 * - Provide navigation to other screens (e.g., friends history, profile, input dialog).
 * - Handle user interactions such as selecting mood events, editing, and deleting.
 */
package com.example.unemployedavengers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dashboard extends Fragment{
    private DashboardBinding binding;
    private ArrayList<MoodEvent> moodList;
    //private ListView moodListView;
    private MoodEventArrayAdapter moodAdapter;
    private FirebaseFirestore db;
    private CollectionReference moodEventRef;
    private IUserDAO userDAO;
    private String userID;
    private String username;

    private MoodEvent selectedMoodForDeletion;



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
                        .navigate(R.id.action_dashboardFragment_to_userSearchFragment)
        );
        binding.profileButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboardFragment_to_profileFragment)
        );
        binding.notificationsButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_dashboardFragment_to_notificationsFragment)
        );

        //Navigates to the input dialog
        binding.addMoodButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("source", "dashboard");
            Navigation.findNavController(v)
                    .navigate(R.id.action_dashboardFragment_to_inputDialog, args);
        });

        //register the listener for the result from InputDialog (Only once)
        getParentFragmentManager().setFragmentResultListener("input_dialog_result", getViewLifecycleOwner(), new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                //retrieve the MoodEvent object
                MoodEvent moodEvent = (MoodEvent) result.getSerializable("mood_event_key");

                //existed is true if we are updating a mood, else we are adding a mood
                if ((moodEvent != null)&&(moodEvent.getExisted()==false)) {

                    addMoodEvent(moodEvent); //store the mood event in Firestore
                }else{
                    updateMoodEvent(moodEvent);
                }
            }
        });

        //register the result listener for the delete confirmation
        getParentFragmentManager().setFragmentResultListener("delete_mood_event", getViewLifecycleOwner(), new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                boolean deleteConfirmed = result.getBoolean("DeleteConfirmed", false);
                if (deleteConfirmed) {
                    //proceed with the deletion
                    onDeleteConfirmed(selectedMoodForDeletion);
                    loadMoodEvents(); //reload the mood events after deletion
                }
            }
        });


    }

    //adds a new mood event to the database

    public void addMoodEvent(MoodEvent moodEvent) {
        moodEvent.setExisted(true); //mark it as an existing mood event

        if (moodEventRef != null) {
            //add the mood event without the ID first
            moodEventRef.add(moodEvent)
                    .addOnSuccessListener(documentReference -> {
                        //get the unique ID generated by Firestore
                        String id = documentReference.getId();

                        //update the document with the ID field
                        documentReference.update("id", id)
                                .addOnSuccessListener(aVoid -> {

                                    //set the id in the local object
                                    moodEvent.setId(id);

                                    //update user
                                    //NEED TO RELOAD DATABASE CAUSE FIREBASE IS AN IDIOT (crashes if you add a moodEvent and tries to update it right away cause "cannot find id")

                                    updateMoodEvent(moodEvent);
                                    Toast.makeText(getContext(), "Mood added successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to update ID in Firestore", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to add mood", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Database reference is null", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateMoodEvent(MoodEvent moodEvent) {
        //NEED TO RELOAD DATABASE CAUSE FIREBASE IS AN IDIOT (crashes if you add a moodEvent and tries to update it right away cause "cannot find id")
        loadMoodEvents();

        String moodEventId = moodEvent.getId();

        //get document via id
        DocumentReference moodEventDocRef = moodEventRef.document(moodEventId);

        //set the new values for the document
        moodEventDocRef.set(moodEvent)  //use set() to update or create the document if it doesn't exist
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Mood updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update mood", Toast.LENGTH_SHORT).show();
                });
    }

    //delete function
    public void onDeleteConfirmed(MoodEvent moodEvent) {
        moodEventRef.document(moodEvent.getId()).delete() //using the id to delete
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Mood deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete mood", Toast.LENGTH_SHORT).show();
                });

        loadMoodEvents(); //reload mood events
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
                            Bundle args = new Bundle();
                            args.putSerializable("selected_mood_event", selectedMoodEvent);
                            args.putString("source", "dashboard");

                            //navigate to inputdialog and pass the selected mood event
                            Navigation.findNavController(view).navigate(R.id.action_dashboardFragment_to_inputDialog, args);
                        });
                        //long click to delete
                        binding.activityList.setOnItemLongClickListener((parent, view, position, id) -> {
                            selectedMoodForDeletion = recentMoodEvents.get(position);

                            ConfirmDeleteDialogFragment dialog = ConfirmDeleteDialogFragment.newInstance(selectedMoodForDeletion.getId());
                            dialog.show(getParentFragmentManager(), "ConfirmDeleteDialog");


                            return true; //indicate the event was handled
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