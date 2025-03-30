/**
 * FriendsMap is a Fragment that displays a map with mood event markers for friends.
 * It retrieves mood events from a shared ViewModel and places custom markers
 * on the map based on the events' locations and moods.
 *
 * Key Features:
 * - Observes mood events from a ViewModel and updates the map with markers.
 * - Each marker represents a mood event with mood text and username.
 * - Custom markers are created based on the mood of the event.
 * - The map camera zooms to the first event’s location if available.
 *
 * Methods:
 * - onViewCreated: Observes the mood events and triggers the map update.
 * - onMapReady: Adds markers for mood events on the map.
 * - createCustomMarker: Creates a custom bitmap marker with the mood and username.
 */

package com.example.unemployedavengers.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.FriendsMapBinding;
import com.example.unemployedavengers.models.FriendMoodEventsViewModel;
import com.example.unemployedavengers.models.MoodEvent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class FriendsMap extends Fragment implements OnMapReadyCallback {
    private FriendsMapBinding binding;
    private ArrayList<MoodEvent> moodEvents;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FriendsMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("MapDebug", "reached");
        // Get the shared ViewModel from the Activity
        FriendMoodEventsViewModel vm = new ViewModelProvider(requireActivity()).get(FriendMoodEventsViewModel.class);

        // Observe the mood events LiveData
        vm.getMoodEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                Log.d("MapDebug", "reached");
                // Update the local list (creating a copy if needed)
                moodEvents = new ArrayList<>(events);
                // Refresh the map markers by requesting the map asynchronously
                SupportMapFragment mapFragment = (SupportMapFragment)
                        getChildFragmentManager().findFragmentById(R.id.map2_fragment);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(this);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d("MapDebug", "reached");
        // Loop through moodEvents and add markers for those with locations
        if (moodEvents != null) {
            for (MoodEvent event : moodEvents) {
                if (event != null && event.getHasLocation()) {
                    double lat = event.getLatitude();
                    double lng = event.getLongitude();
                    Log.d("MapDebug", "Placing marker at lat=" + lat + ", lng=" + lng);

                    // Create a custom marker bitmap with the mood string
                    Bitmap markerBitmap = createCustomMarker(getContext(), event.getMood(),event.getUserName());

                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap)));
                }
            }
            // Optionally adjust the camera to the first event's location
            if (!moodEvents.isEmpty() && moodEvents.get(0).getHasLocation()) {
                double lat = moodEvents.get(0).getLatitude();
                double lng = moodEvents.get(0).getLongitude();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10));
            }
        }
    }


    private Bitmap createCustomMarker(Context context, String mood,String userName) {
        // Inflate with a dummy parent to help with layout measurement
        FrameLayout dummyParent = new FrameLayout(context);
        View markerView = LayoutInflater.from(context)
                .inflate(R.layout.friends_map_marker, dummyParent, false);

        // Set the mood text on the TextView
        TextView markerText = markerView.findViewById(R.id.marker_text);
        TextView userNamePlace = markerView.findViewById(R.id.user_name);
        markerText.setText(mood);
        String lowerMood = mood.toLowerCase();
        if (lowerMood.contains("anger")) markerText.setTextColor(Color.RED);
        if (lowerMood.contains("confusion")) markerText.setTextColor( ContextCompat.getColor(context, R.color.orange));
        if (lowerMood.contains("disgust")) markerText.setTextColor( Color.GREEN);
        if (lowerMood.contains("fear")) markerText.setTextColor(Color.BLUE);
        if (lowerMood.contains("happiness")) markerText.setTextColor( ContextCompat.getColor(context, R.color.baby_blue));
        if (lowerMood.contains("sadness")) markerText.setTextColor(Color.GRAY);
        if (lowerMood.contains("shame")) markerText.setTextColor( ContextCompat.getColor(context, R.color.yellow));
        if (lowerMood.contains("surprise")) markerText.setTextColor( ContextCompat.getColor(context, R.color.pink));
        userNamePlace.setText(userName);
        // Measure and layout the view properly
        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        markerView.measure(widthSpec, heightSpec);
        int measuredWidth = markerView.getMeasuredWidth();
        int measuredHeight = markerView.getMeasuredHeight();
        markerView.layout(0, 0, measuredWidth, measuredHeight);

        // Create the bitmap and draw the view into the canvas
        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);
        return bitmap;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Prevent memory leaks by nullifying the binding
        binding = null;
    }

}
