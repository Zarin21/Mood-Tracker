/**
 * PersonalMap - A Fragment responsible for displaying the user's mood events on a Google Map.
 * This fragment fetches mood events from a shared ViewModel and shows them as custom markers on the map.
 * Each marker represents a mood event with location data, and its appearance is customized based on the mood of the event.
 *
 * Purpose:
 * - Displays the user's mood events on a map with custom markers that reflect the mood's nature.
 * - Fetches mood event data from a shared ViewModel, ensuring that the map updates when the data changes.
 * - Uses custom-designed markers where the color and text depend on the specific mood associated with the event.
 *
 * Key Features:
 * - Fetches mood events using a ViewModel and observes changes to the data (LiveData).
 * - Displays mood events as markers on a Google Map, where each marker's color and text represent the mood.
 * - Uses custom bitmaps for markers, where the mood influences the marker's design (e.g., color coding for different moods).
 * - Optionally adjusts the camera to the location of the first mood event on the map for an optimal user experience.
 *
 * Methods:
 * - onCreateView: Inflates the layout and sets up the binding for this Fragment.
 * - onViewCreated: Observes mood events from the ViewModel and updates the map markers accordingly.
 * - onMapReady: Adds the mood event markers to the map based on the locations provided.
 * - createCustomMarker: Creates a custom bitmap for each mood event marker, with the mood influencing the marker's design.
 * - onDestroyView: Nullifies the binding to prevent memory leaks when the view is destroyed.
 *
 * Known Issues:
 * - No explicit error handling is implemented for situations when mood events have no location data.
 * - The camera zoom behavior may not be ideal for larger datasets with widely scattered markers.
 * - Custom marker layouts could be more optimized to handle performance for large numbers of events.
 *
 * Design Patterns:
 * - **MVVM** (Model-View-ViewModel) pattern is employed, where mood events are managed by the ViewModel, and the Fragment observes and reacts to changes.
 * - **Observer** pattern is utilized via LiveData, ensuring the map is updated when mood events change.
 * - Custom markers use **View Binding** to bind the layout views efficiently.
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
import com.example.unemployedavengers.databinding.PersonalMapBinding;
import com.example.unemployedavengers.models.MoodEvent;
import com.example.unemployedavengers.models.MoodEventsViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class PersonalMap extends Fragment implements OnMapReadyCallback {

    private PersonalMapBinding binding;
    private ArrayList<MoodEvent> moodEvents;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = PersonalMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("MapDebug", "reached");
        // Get the shared ViewModel from the Activity
        MoodEventsViewModel vm = new ViewModelProvider(requireActivity()).get(MoodEventsViewModel.class);

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
                    Bitmap markerBitmap = createCustomMarker(getContext(), event.getMood());

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


    private Bitmap createCustomMarker(Context context, String mood) {
        // Inflate with a dummy parent to help with layout measurement
        FrameLayout dummyParent = new FrameLayout(context);
        View markerView = LayoutInflater.from(context)
                .inflate(R.layout.marker_layout, dummyParent, false);

        // Set the mood text on the TextView
        TextView markerText = markerView.findViewById(R.id.marker_text);
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