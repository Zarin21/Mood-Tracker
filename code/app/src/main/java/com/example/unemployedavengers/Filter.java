package com.example.unemployedavengers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class Filter extends DialogFragment {
    private CheckBox  filterMood, filterReason, filterWeek;
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

        filterMood = view.findViewById(R.id.filterMood);
        filterReason = view.findViewById(R.id.filterReason);
        filterWeek = view.findViewById(R.id.filterWeek);
        spinner = view.findViewById(R.id.spinner);
        editReasonFilter = view.findViewById(R.id.editReasonFilter);

        String[] moodOptions = new String[]{"Happy", "Sadness", "Angry", "Confused","Scared","Shame","Disgust","Surprise"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, moodOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
                    listener.onFilterApplied(false,"",true);
                }
                dialog.dismiss();
            });
        });

        return dialog;

    }
}
