/**
 * ConfirmDeleteDialogFragment - A DialogFragment that prompts the user to confirm the deletion of a mood event.
 *
 * Purpose:
 * - Displays a confirmation dialog with options to "Delete" or "Cancel".
 * - Sends a result back to the parent fragment when the user confirms the deletion.
 * - The parent fragment can then take appropriate action, such as removing the mood event from the list.
 *
 * Key Features:
 * - Custom dialog with a title, message, and buttons for user confirmation.
 * - Uses FragmentResult API to send the deletion confirmation result back to the parent fragment.
 * - Modifies button text color for a customized user interface (UI) experience.
 *
 * Outstanding Issues:
 * - There is no feedback given to the user after pressing "Delete" to confirm the action (e.g., showing a loading spinner or toast message).
 * - The dialog currently only handles deletion confirmation, but additional confirmation details or error handling might be required.
 * - No handling of scenarios where the mood event to be deleted no longer exists or has been modified in the database since the user initiated deletion.
 */
package com.example.unemployedavengers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.unemployedavengers.models.MoodEvent;

public class ConfirmDeleteDialogFragment extends DialogFragment {

    public static ConfirmDeleteDialogFragment newInstance(String moodId) {
        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        Bundle args = new Bundle();
        args.putString("moodId", moodId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Delete Mood Event")
                .setMessage("Are you sure you want to delete this mood event?")
                .setPositiveButton("Delete", (dialogInterface, which) -> {
                    // send result to the dashboard indicating that the mood should be deleted
                    Bundle result = new Bundle();
                    result.putBoolean("DeleteConfirmed", true);
                    getParentFragmentManager().setFragmentResult("delete_mood_event", result);
                })
                .setNegativeButton("Cancel", null)
                .create();

        // Change the text color of the buttons to pink
        dialog.setOnShowListener(dialogInterface -> {
            // Get the buttons from the dialog
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            // Set the text color to pink
            positiveButton.setTextColor(Color.parseColor("#F08080")); // Pink color
            negativeButton.setTextColor(Color.parseColor("#F08080")); // Pink color
        });

        return dialog;
    }
}


