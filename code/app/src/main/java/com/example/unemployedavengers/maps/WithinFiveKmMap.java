/**
 * WithinFiveKmMap - A Fragment responsible for displaying mood events on a Google Map, specifically showing events within a 5 km radius of the user's location.
 * This fragment fetches mood events from a shared ViewModel and displays them as custom markers on the map.
 * Each marker represents a mood event with location data, and its appearance is customized based on the mood of the event.
 * The markers are placed on the map based on the latitude and longitude of the mood events, with an added filter to only display events within a 5 km radius of the user.
 *
 * Purpose:
 * - Displays mood events within a 5 km radius of the user's location on a Google Map.
 * - Fetches mood event data from a shared ViewModel, and the map is updated whenever the data changes.
 * - Customizes the marker appearance based on the mood of each event, including text and color.
 *
 * Key Features:
 * - Fetches mood events using a ViewModel and observes changes to the data (LiveData).
 * - Displays mood events as markers on the map, with each marker being customized based on the mood.
 * - Uses custom bitmaps for markers where the mood influences the marker's text and color.
 * - Optionally adjusts the camera to the location of the first mood event for a more focused view.
 * - Ensures that only mood events within a 5 km radius of the user are displayed on the map.
 *
 * Methods:
 * - onCreateView: Inflates the layout using view binding and prepares the view for displaying the map.
 * - onViewCreated: Observes mood events from the WithinFiveKmViewModel and updates the map markers accordingly.
 * - onMapReady: Adds markers to the map for mood events that have location data, ensuring only events within 5 km of the user are shown.
 * - createCustomMarker: Creates a custom bitmap for each mood event marker, with the mood influencing the marker's text and color.
 * - onDestroyView: Nullifies the binding to prevent memory leaks when the view is destroyed.
 *
 * Known Issues:
 * - No explicit checks or user feedback if the user's location cannot be fetched or if there are no events within 5 km.
 * - Marker density could cause performance issues if a large number of mood events are within the 5 km radius.
 * - No mechanism to dynamically update the user's location if it changes after the map is rendered.
 *
 * Design Patterns:
 * - **MVVM** (Model-View-ViewModel) pattern is employed, where mood events are managed by the ViewModel, and the Fragment observes and reacts to changes.
 * - **Observer** pattern is utilized via LiveData, ensuring the map is updated when mood events change.
 */


package com.example.unemployedavengers.maps;

import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.unemployedavengers.R;
import com.example.unemployedavengers.databinding.PersonalMapBinding;
import com.example.unemployedavengers.databinding.WithinFiveKmMapBinding;
import com.example.unemployedavengers.models.FriendMoodEventsViewModel;
import com.example.unemployedavengers.models.MoodEvent;
import com.example.unemployedavengers.models.MoodEventsViewModel;
import com.example.unemployedavengers.models.WithinFiveKmViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

public class WithinFiveKmMap extends Fragment implements OnMapReadyCallback {

    private WithinFiveKmMapBinding binding;
    private ArrayList<MoodEvent> moodEvents;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = WithinFiveKmMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("MapDebug", "reached");
        // Get the shared ViewModel from the Activity
        WithinFiveKmViewModel vm = new ViewModelProvider(requireActivity()).get(WithinFiveKmViewModel.class);


        // Observe the mood events LiveData
        vm.getMoodEvents().observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                Log.d("MapDebug", "reached");
                // Update the local list (creating a copy if needed)
                moodEvents = new ArrayList<>(events);
                // Refresh the map markers by requesting the map asynchronously
                SupportMapFragment mapFragment = (SupportMapFragment)
                        getChildFragmentManager().findFragmentById(R.id.map3_fragment);
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


                // key verification for within 5km
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