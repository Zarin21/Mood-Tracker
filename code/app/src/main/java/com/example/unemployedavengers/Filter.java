package com.example.unemployedavengers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

public class Filter extends DialogFragment {
    private CheckBox filterMood, filterReason , filterWeek;
    private Spinner spinner;
    private EditText editReasonFilter;

    public interface FilterListener{
        void onFilterApplied(boolean filterMood, boolean filterReason, boolean filterRecentWeek,
                    String reasonText, String spinnerSelection, boolean seeAll);
    }

    private FilterListener listener;

    public void setFilterListener(FilterListener listener){
        this.listener = listener;
    }

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter, null);


        filterReason = view.findViewById(R.id.filterReason);
        filterMood = view.findViewById(R.id.filterMood);
        filterWeek = view.findViewById(R.id.filterWeek);
        spinner = view.findViewById(R.id.spinner);
        editReasonFilter = view.findViewById(R.id.editReasonFilter);
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
        spinner.setAdapter(adapter);



        builder.setView(view)
                .setTitle("Filter Options")
                .setPositiveButton("Apply", null) //set null for the action here so the button doesn't close the dialog when invalid text is entered
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("See All",null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            //set button text to pink
            positiveButton.setTextColor(Color.parseColor("#F08080"));
            negativeButton.setTextColor(Color.parseColor("#F08080"));
            neutralButton.setTextColor(Color.parseColor("#F08080"));

            //implement the logic for positive button
            positiveButton.setOnClickListener(v -> {
                if(filterReason.isChecked()) {
                    String inputText = editReasonFilter.getText().toString().trim();

                    if (inputText.split("\\s+").length == 1 && !inputText.isEmpty()) {
                        if (listener != null) {
                            Log.d("Filter Dialog", "passed");
                            listener.onFilterApplied(
                                    filterMood.isChecked(),
                                    filterReason.isChecked(),
                                    filterWeek.isChecked(),
                                    inputText,
                                    spinner.getSelectedItem().toString(),
                                    false
                            );
                        }
                        dialog.dismiss();
                    } else {
                        Log.d("Filter Dialog", "did not pass");
                        editReasonFilter.setError("Must contain only 1 word and cannot be empty!");
                    }
                } else {
                        if (listener != null) {
                            Log.d("Filter Dialog", "passed");

                            listener.onFilterApplied(
                                    filterMood.isChecked(),
                                    filterReason.isChecked(),
                                    filterWeek.isChecked(),
                                    editReasonFilter.getText().toString().trim(),
                                    spinner.getSelectedItem().toString(),
                                    false
                            );

                        dialog.dismiss();
                    }
                }
            });
            neutralButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFilterApplied(false, false, false, "", "", true);
                }
                dialog.dismiss();
            });
        });

        return dialog;

    }
}
