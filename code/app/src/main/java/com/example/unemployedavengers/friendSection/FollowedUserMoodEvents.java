package com.example.unemployedavengers.friendSection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.R;
import com.example.unemployedavengers.arrayadapters.FollowedUserMoodEventAdapter;
import com.example.unemployedavengers.databinding.FollowedUserMoodEventsBinding;
import com.example.unemployedavengers.models.MoodEvent;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

public class FollowedUserMoodEvents extends Fragment {
    private FollowedUserMoodEventsBinding binding;
    private FirebaseFirestore db;
    private String followedUserId;
    private String followedUsername;
    private ArrayList<MoodEvent> followedUserMoodEvents;
    private FollowedUserMoodEventAdapter moodAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FollowedUserMoodEventsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize FirebaseFirestore
        db = FirebaseFirestore.getInstance();

        // Initialize mood events list and adapter
        followedUserMoodEvents = new ArrayList<>();
        moodAdapter = new FollowedUserMoodEventAdapter(getContext(), followedUserMoodEvents);
        binding.followedUsersListView.setAdapter(moodAdapter);

        // Get the passed user ID and username
        Bundle args = getArguments();
        if (args != null) {
            followedUserId = args.getString("followedUserId");
            followedUsername = args.getString("followedUsername");

            if (followedUserId != null && followedUsername != null) {
                // Update UI to show the friend's username
                binding.tvFriendsMoodTitle.setText(followedUsername + "'s Mood History");

                // Load mood events for this user
                loadFollowedUsersMoodEvents();
            }
        }

        // Setup buttons
        binding.addFriendButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_followedUserMoodEventsFragment_to_userSearchFragment)
        );

        binding.mapButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Map view coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.filterButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Filter functionality coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Loads mood events from the followed user
     */
    private void loadFollowedUsersMoodEvents() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyStateMessage.setVisibility(View.GONE);

        db.collection("users")
                .document(followedUserId)
                .collection("moods")
                .get()
                .addOnSuccessListener(moodSnapshot -> {
                    followedUserMoodEvents.clear();

                    for (DocumentSnapshot doc : moodSnapshot.getDocuments()) {
                        MoodEvent moodEvent = doc.toObject(MoodEvent.class);
                        if (moodEvent != null) {
                            // Prefix the trigger with the username to show who posted it
                            if (moodEvent.getTrigger() != null) {
                                moodEvent.setTrigger(followedUsername + ": " + moodEvent.getTrigger());
                            } else {
                                moodEvent.setTrigger(followedUsername + ": ");
                            }
                            followedUserMoodEvents.add(moodEvent);
                        }
                    }

                    // Update UI based on results
                    if (followedUserMoodEvents.isEmpty()) {
                        binding.emptyStateMessage.setText("No mood events from " + followedUsername);
                        binding.emptyStateMessage.setVisibility(View.VISIBLE);
                        binding.followedUsersListView.setVisibility(View.GONE);
                    } else {
                        // Sort by time (most recent first)
                        Collections.sort(followedUserMoodEvents,
                                (e1, e2) -> Long.compare(e2.getTime(), e1.getTime()));

                        moodAdapter.notifyDataSetChanged();
                        binding.emptyStateMessage.setVisibility(View.GONE);
                        binding.followedUsersListView.setVisibility(View.VISIBLE);
                    }

                    binding.progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.emptyStateMessage.setText("Error loading mood events");
                    binding.emptyStateMessage.setVisibility(View.VISIBLE);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}