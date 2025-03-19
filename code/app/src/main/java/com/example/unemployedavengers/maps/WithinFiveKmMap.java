package com.example.unemployedavengers.maps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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