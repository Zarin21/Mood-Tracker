package com.example.unemployedavengers;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unemployedavengers.databinding.DashboardBinding;
import com.example.unemployedavengers.databinding.InputDialogBinding;
import com.example.unemployedavengers.models.MoodEvent;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InputDialog#newInstance} factory method to
 * create an instance of this fragment.
 */

/*
This class interacts with the input dialog xml and retrieve inputted information.
It also has the adapter for the spinner as well as colouring the text for the mood
After user input info they can either confirm which will send a new moodEvent back to dashboard or
cancel and go right back to dashboard
 */
public class InputDialog extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private InputDialogBinding binding; //binding

    private MoodEvent moodEvent;
    private String source;

    /**
     * A empty constructor needed
     */
    public InputDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InputDialog.
     */
    // TODO: Rename and change types and number of parameters
    public static InputDialog newInstance(String param1, String param2) {
        InputDialog fragment = new InputDialog();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //create the binding
        binding = InputDialogBinding.inflate(inflater, container, false);

        //get the spinner
        Spinner spinnerEmotion = binding.spinnerEmotion;

        //create the adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.spinner_items,
                android.R.layout.simple_spinner_item
        );

        // Customize the dropdown view and spinner item appearance
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // Style for the dropdown view

        // Override getView to customize the selected item view
        adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.spinner_items)) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Change the text color based on the item position
                switch (position) {
                    case 0: // Anger
                        textView.setTextColor(Color.RED);
                        break;
                    case 1: // Confusion

                        textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.orange));

                        break;
                    case 2: // Disgust
                        textView.setTextColor(Color.GREEN);
                        break;
                    case 3: // Fear
                        textView.setTextColor(Color.BLUE);
                        break;
                    case 4: // Happiness
                        textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.baby_blue));
                        break;
                    case 5: // Sadness
                        textView.setTextColor(Color.GRAY);
                        break;
                    case 6: // Shame
                        textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.yellow));
                        break;
                    case 7: // Surprise
                        textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.pink));
                        break;
                    default:
                        textView.setTextColor(Color.BLACK);
                        break;
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                // You can change the color for the dropdown items as well
                switch (position) {
                    case 0:
                        textView.setTextColor(Color.RED);
                        break;
                    case 1:
                        textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.orange));
                        break;
                    case 2:
                        textView.setTextColor(Color.GREEN);
                        break;
                    case 3:
                        textView.setTextColor(Color.BLUE);
                        break;
                    case 4:
                        textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.baby_blue));
                        break;
                    case 5:
                        textView.setTextColor(Color.GRAY);
                        break;
                    case 6:
                        textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.yellow));
                        break;
                    case 7:
                        textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.pink));
                        break;
                    default:
                        textView.setTextColor(Color.BLACK);
                        break;
                }
                return view;
            }
        };

        //set the adapter to the spinner
        spinnerEmotion.setAdapter(adapter);

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //get the selected MoodEvent passed from Dashboard
        if (getArguments() != null) {
            moodEvent = (MoodEvent) getArguments().getSerializable("selected_mood_event");
            source = getArguments().getString("source");
            if (moodEvent != null) {
                //populate the fields with the data from the selected MoodEvent
                EditText triggerEditText = view.findViewById(R.id.editTrigger);
                EditText situationEditText = view.findViewById(R.id.editSocialSituation);
                EditText reasonEditText = view.findViewById(R.id.editReason);
                Spinner spinner =  view.findViewById(R.id.spinnerEmotion);

                //using getter function from model to get text
                triggerEditText.setText(moodEvent.getTrigger());
                situationEditText.setText(moodEvent.getSituation());
                reasonEditText.setText(moodEvent.getReason());

                if (Objects.equals(moodEvent.getRadioSituation(), "Alone")) {
                    ((RadioButton) view.findViewById(R.id.radioAlone)).setChecked(true);
                } else if (Objects.equals(moodEvent.getRadioSituation(), "Two or Several")) {
                    ((RadioButton) view.findViewById(R.id.radioTwoSeveral)).setChecked(true);
                } else if (Objects.equals(moodEvent.getRadioSituation(), "A Crowd")) {
                    ((RadioButton) view.findViewById(R.id.radioCrowd)).setChecked(true);
                }


                //using the adapter in onCreateview
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();

                String selectedMood = moodEvent.getMood();

                //match the position to the adapter position and set it to that mood
                int position = adapter.getPosition(selectedMood);
                if (position != -1) {
                    spinner.setSelection(position);
                }

            }
        }


        //when user clicks confirm
        binding.buttonConfirm.setOnClickListener(v -> {
            if (binding.editReason.getText().toString().split(" ").length <= 3) {
                //get all relevant information
                String mood = (String) binding.spinnerEmotion.getSelectedItem();
                String reason = binding.editReason.getText().toString();
                String trigger = binding.editTrigger.getText().toString();
                String situation = binding.editSocialSituation.getText().toString();
                String radioSituation = ((RadioButton) v.getRootView().findViewById(binding.radioGroupSocial.getCheckedRadioButtonId())).getText().toString();

                long time = System.currentTimeMillis();

                reason = reason.trim();
                trigger = trigger.trim();
                situation = situation.trim();

                //if moodEvent exists, update it otherwise create a new one
                if (moodEvent != null) {
                    moodEvent.setMood(mood);
                    moodEvent.setReason(reason);
                    moodEvent.setTrigger(trigger);
                    moodEvent.setSituation(situation);
                    moodEvent.setRadioSituation(radioSituation);
                    //no need to change the time because we are editing the existing event
                } else {
                    moodEvent = new MoodEvent(mood, reason, trigger, situation, time, radioSituation);
                }
                //pass the updated MoodEvent back to dashboard
                Bundle result = new Bundle();
                result.putSerializable("mood_event_key", moodEvent);
                getParentFragmentManager().setFragmentResult("input_dialog_result", result);

                if (source=="dashboard") {
                    Navigation.findNavController(v)
                            .navigate(R.id.action_inputDialog_to_dashboardFragment);
                }else if (source=="history"){
                    Navigation.findNavController(v)
                            .navigate(R.id.action_inputDialog_to_historyFragment);
                }
            } else {
                binding.editReason.setError("Reason must be 3 words or less!");
            }
        });


        //go right back to dashboard
        binding.buttonCancel.setOnClickListener(v ->{
            Toast.makeText(getContext(), "Action Cancelled", Toast.LENGTH_SHORT).show();
            if (source=="dashboard") {
                Navigation.findNavController(v)
                        .navigate(R.id.action_inputDialog_to_dashboardFragment);
            }else if (source=="history"){
                Navigation.findNavController(v)
                        .navigate(R.id.action_inputDialog_to_historyFragment);
            }
        });
    }
}