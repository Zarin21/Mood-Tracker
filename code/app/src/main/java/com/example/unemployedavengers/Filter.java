package com.example.unemployedavengers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class Filter extends DialogFragment {
    private CheckBox  filterReason;
    private EditText editReasonFilter;

    public interface FilterListener{
        void onFilterApplied(boolean filterReason, String reasonText);
    }

    private FilterListener listener;

    public void setEditReasonFilter(FilterListener listener){
        this.listener = listener;
    }

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter, null);
        filterReason = view.findViewById(R.id.filterReason);
        editReasonFilter = view.findViewById(R.id.editReasonFilter);
        builder.setView(view)
                .setTitle("Filter Options")
                .setPositiveButton("Apply", null) //set null for the action here so the button doesn't close the dialog when invalid text is entered
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            //set button text to pink
            positiveButton.setTextColor(Color.parseColor("#F08080"));
            negativeButton.setTextColor(Color.parseColor("#F08080"));

            //implement the logic for positive button
            positiveButton.setOnClickListener(v -> {
                if(filterReason.isChecked()) {
                    //if the text is only 1 word and not empty
                    if (editReasonFilter.getText().toString().trim().split("\\s+").length == 1 && !editReasonFilter.getText().toString().trim().isEmpty()) {
                        if (listener != null) {

                            listener.onFilterApplied(
                                    filterReason.isChecked(),
                                    editReasonFilter.getText().toString().trim()
                            );
                        }
                        dialog.dismiss();
                    } else {
                        //show error
                        editReasonFilter.setError("Must contain only 1 word and cannot be empty!");
                    }
                }
            });
        });

        return dialog;

    }
}
