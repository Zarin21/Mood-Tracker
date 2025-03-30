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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.unemployedavengers.Filter;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.arrayadapters.FollowedUserMoodEventAdapter;
import com.example.unemployedavengers.databinding.FollowedUserMoodEventsBinding;
import com.example.unemployedavengers.models.FriendMoodEventsViewModel;
import com.example.unemployedavengers.models.MoodEvent;
import com.example.unemployedavengers.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowedUserMoodEvents extends Fragment {
    private FollowedUserMoodEventsBinding binding;
    private FirebaseFirestore db;
    private String currentUserId;
    private ArrayList<MoodEvent> followedUserMoodEvents;
    private FollowedUserMoodEventAdapter moodAdapter;
    private Map<String, String> userIdToUsernameMap;
    private boolean singleUserView;
    private String singleUserId;
    private String singleUsername;
    private ArrayList<MoodEvent> filteredMoodList;
    private FollowedUserMoodEventAdapter filteredMoodAdapter;

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
        userIdToUsernameMap = new HashMap<>();

        // Get current user ID from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("userID", null);

        // Initialize mood events list and adapter
        followedUserMoodEvents = new ArrayList<>();
        moodAdapter = new FollowedUserMoodEventAdapter(getContext(), followedUserMoodEvents);
        binding.followedUsersListView.setAdapter(moodAdapter);

        // Check if we're in single user view mode
        singleUserView = false;
        singleUserId = null;
        singleUsername = null;

        if (getArguments() != null) {
            singleUserView = getArguments().getBoolean("singleUserView", false);
            singleUserId = getArguments().getString("followedUserId");
            singleUsername = getArguments().getString("followedUsername");
        }

        // Update UI title based on view mode
        if (singleUserView && singleUsername != null) {
            binding.tvFriendsMoodTitle.setText(singleUsername + "'s Mood History");
        } else {
            binding.tvFriendsMoodTitle.setText("Following Mood History");
        }

        // If we're in single user view, load just that user's moods
        if (singleUserView && singleUserId != null) {
            // Add the username to our mapping
            if (singleUsername != null) {
                userIdToUsernameMap.put(singleUserId, singleUsername);
            }

            // Load only this user's mood events
            loadSingleUserMoods(singleUserId);
        } else {
            // Load mood events from all followed users
            loadFollowedUsers();
        }

        // Setup filter button
        binding.filterButton.setOnClickListener(v -> {
            Filter filterDialog = new Filter();
            filterDialog.setFilterListener((mood, reason, recentWeek, reasonText, spinnerSelection, seeAll) -> {
                ArrayList<MoodEvent> filterList = new ArrayList<>(followedUserMoodEvents);
                if (seeAll) {
                    loadFollowedUsers();
                } else {
                    if (mood) {
                        ArrayList<MoodEvent> filteredByMood = new ArrayList<>();
                        for (MoodEvent event : filterList) {
                            if (event.getMood() != null && event.getMood().contains(spinnerSelection)) {
                                filteredByMood.add(event);
                            }
                        }
                        filterList = filteredByMood;
                    }
                    if (reason) {
                        ArrayList<MoodEvent> filteredByReason = new ArrayList<>();
                        for (MoodEvent event : filterList) {
                            if (event.getReason().contains(reasonText)) {
                                filteredByReason.add(event);
                            }
                        }
                        filterList = filteredByReason;
                    }
                    if (recentWeek) {
                        long currentTime = System.currentTimeMillis();
                        long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000;
                        ArrayList<MoodEvent> filteredByWeek = new ArrayList<>();
                        for (MoodEvent event : filterList) {
                            if (event.getTime() >= (currentTime - sevenDaysMillis)) {
                                filteredByWeek.add(event);
                            }
                        }
                        filterList = filteredByWeek;
                    }
                    filteredFollowedMoodEvents.clear();
                    filteredFollowedMoodEvents.addAll(filterList);
                    FriendMoodEventsViewModel vm = new ViewModelProvider(requireActivity()).get(FriendMoodEventsViewModel.class);
                    vm.setMoodEvents(filteredFollowedMoodEvents);
                    FollowedUserMoodEventAdapter filteredAdapter = new FollowedUserMoodEventAdapter(getContext(), filteredFollowedMoodEvents);
                    binding.followedUsersListView.setAdapter(filteredAdapter);
                    filteredAdapter.notifyDataSetChanged();
                }
            });
            filterDialog.show(getParentFragmentManager(), "FilterDialog");
        });

        // Setup back button to return to friends list
        binding.backButton.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_followedUserMoodEventsFragment_to_friendsHistoryFragment)
        );

        // Setup item click to navigate to mood detail view with comments
        binding.followedUsersListView.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position >= 0 && position < followedUserMoodEvents.size()) {
                MoodEvent selectedMoodEvent = followedUserMoodEvents.get(position);

                // Make sure the mood event has a username set
                if (selectedMoodEvent.getUserName() == null && selectedMoodEvent.getUserId() != null) {
                    selectedMoodEvent.setUserName(userIdToUsernameMap.get(selectedMoodEvent.getUserId()));
                }

                // Create bundle and add the selected mood event
                Bundle args = new Bundle();
                args.putSerializable("selected_mood_event", selectedMoodEvent);

                // Navigate to the mood detail fragment
                Navigation.findNavController(view)
                        .navigate(R.id.action_followedUserMoodEventsFragment_to_moodDetailFragment, args);
            }
        });
    }

    /**
     * Loads mood events from a single user
     * Only shows public mood events
     * @param userId The user ID to load mood events for
     */
    private void loadSingleUserMoods(String userId) {
        if (binding == null) {
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyStateMessage.setVisibility(View.GONE);

        followedUserMoodEvents.clear();

        // Get the user's mood events
        db.collection("users")
                .document(userId)
                .collection("moods")
                .orderBy("time", Query.Direction.DESCENDING) // Sort by time descending (newest first)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Check if the fragment is still active
                    if (binding == null) {
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        MoodEvent moodEvent = doc.toObject(MoodEvent.class);

                        // Only add public mood events
                        // If publicStatus doesn't exist or is true, show the mood
                        Boolean isPublic = doc.contains("publicStatus") ?
                                doc.getBoolean("publicStatus") : true;

                        if (isPublic != null && isPublic) {
                            // Set the user ID so we can display the username
                            moodEvent.setUserId(userId);

                            // Set username for the mood event for easier access later
                            if (userIdToUsernameMap.containsKey(userId)) {
                                moodEvent.setUserName(userIdToUsernameMap.get(userId));
                            }

                            followedUserMoodEvents.add(moodEvent);
                        }
                    }

                    // Sort all mood events by time in reverse chronological order
                    Collections.sort(followedUserMoodEvents, (event1, event2) ->
                            Long.compare(event2.getTime(), event1.getTime())
                    );

                    updateUI();
                })
                .addOnFailureListener(e -> {
                    // Check if the fragment is still active
                    if (binding == null) {
                        return;
                    }

                    binding.progressBar.setVisibility(View.GONE);
                    binding.emptyStateMessage.setText("Error loading mood events");
                    binding.emptyStateMessage.setVisibility(View.VISIBLE);

                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Loads the list of users that the current user follows
     */
    private void loadFollowedUsers() {
        if (binding == null) {
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyStateMessage.setVisibility(View.GONE);

        if (currentUserId == null) {
            if (binding == null) {
                return;
            }

            binding.progressBar.setVisibility(View.GONE);
            binding.emptyStateMessage.setText("Please log in to view following");
            binding.emptyStateMessage.setVisibility(View.VISIBLE);
            return;
        }

        db.collection("users")
                .document(currentUserId)
                .collection("following")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Check if the fragment is still active
                    if (binding == null) {
                        return;
                    }

                    List<String> followedUserIds = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String followedId = document.getString("followedId");
                        if (followedId != null) {
                            followedUserIds.add(followedId);
                        }
                    }

                    if (followedUserIds.isEmpty()) {
                        if (binding == null) return;

                        binding.progressBar.setVisibility(View.GONE);
                        binding.emptyStateMessage.setText("You're not following anyone yet");
                        binding.emptyStateMessage.setVisibility(View.VISIBLE);
                        binding.followedUsersListView.setVisibility(View.GONE);
                    } else {
                        // Load usernames for all followed users
                        loadUsernames(followedUserIds);
                    }
                })
                .addOnFailureListener(e -> {
                    // Check if the fragment is still active
                    if (binding == null) {
                        return;
                    }

                    binding.progressBar.setVisibility(View.GONE);
                    binding.emptyStateMessage.setText("Error loading following data");
                    binding.emptyStateMessage.setVisibility(View.VISIBLE);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Loads usernames for all followed users
     * @param userIds List of user IDs to load usernames for
     */
    private void loadUsernames(List<String> userIds) {
        int[] completedCount = {0}; // Use array to allow modification in lambda

        for (String userId : userIds) {
            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        // Check if the fragment is still active
                        if (binding == null) {
                            return;
                        }

                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getUsername() != null) {
                            // Store username mapping
                            userIdToUsernameMap.put(userId, user.getUsername());
                        }

                        completedCount[0]++;

                        // When all usernames are loaded, load the mood events
                        if (completedCount[0] >= userIds.size()) {
                            loadMoodEvents(userIds);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Check if the fragment is still active
                        if (binding == null) {
                            return;
                        }

                        completedCount[0]++;

                        // Continue loading even if some usernames fail
                        if (completedCount[0] >= userIds.size()) {
                            loadMoodEvents(userIds);
                        }
                    });
        }
    }

    /**
     * Loads mood events from all followed users
     * Only shows public mood events
     * @param userIds List of user IDs to load mood events for
     */
    private void loadMoodEvents(List<String> userIds) {
        // Check if the fragment is still active
        if (binding == null) {
            return;
        }

        followedUserMoodEvents.clear();
        int[] completedCount = {0}; // Use array to allow modification in lambda

        for (String userId : userIds) {
            // Get all mood events and filter client-side if needed
            db.collection("users")
                    .document(userId)
                    .collection("moods")
                    .orderBy("time", Query.Direction.DESCENDING) // Sort by time descending (newest first)
                    .limit(10) // Get more than we need in case some are private
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        // Check if the fragment is still active
                        if (binding == null) {
                            return;
                        }

                        int count = 0;
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            MoodEvent moodEvent = doc.toObject(MoodEvent.class);

                            // Only add public mood events
                            // If publicStatus doesn't exist or is true, show the mood
                            Boolean isPublic = doc.contains("publicStatus") ?
                                    doc.getBoolean("publicStatus") : true;

                            if (isPublic != null && isPublic) {
                                // Set the user ID so we can display the username
                                moodEvent.setUserId(userId);

                                // Set username for the mood event for easier access later
                                if (userIdToUsernameMap.containsKey(userId)) {
                                    moodEvent.setUserName(userIdToUsernameMap.get(userId));
                                }

                                followedUserMoodEvents.add(moodEvent);
                                count++;

                                // Only take the 3 most recent public moods
                                if (count >= 3) {
                                    break;
                                }
                            }
                        }

                        completedCount[0]++;

                        // When all mood events are loaded, update the UI
                        if (completedCount[0] >= userIds.size()) {
                            // Sort all mood events by time in reverse chronological order
                            Collections.sort(followedUserMoodEvents, new Comparator<MoodEvent>() {
                                @Override
                                public int compare(MoodEvent event1, MoodEvent event2) {
                                    // Compare in reverse order for descending sort (newest first)
                                    return Long.compare(event2.getTime(), event1.getTime());
                                }
                            });

                            updateUI();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Check if the fragment is still active
                        if (binding == null) {
                            return;
                        }

                        completedCount[0]++;

                        // Continue updating UI even if some mood events fail to load
                        if (completedCount[0] >= userIds.size()) {
                            updateUI();
                        }
                    });
        }
    }

    /**
     * Updates the UI with loaded mood events
     */
    private void updateUI() {
        // Check if the fragment is still active
        if (binding == null) {
            return;
        }

        binding.progressBar.setVisibility(View.GONE);

        if (followedUserMoodEvents.isEmpty()) {
            if (singleUserView && singleUsername != null) {
                binding.emptyStateMessage.setText(singleUsername + " has no public mood events");
            } else {
                binding.emptyStateMessage.setText("No public mood events from followed users");
            }
            binding.emptyStateMessage.setVisibility(View.VISIBLE);
            binding.followedUsersListView.setVisibility(View.GONE);
        } else {
            // Update the adapter with username mappings
            moodAdapter.setUserIdToUsernameMap(userIdToUsernameMap);
            moodAdapter.notifyDataSetChanged();

            binding.emptyStateMessage.setVisibility(View.GONE);
            binding.followedUsersListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
