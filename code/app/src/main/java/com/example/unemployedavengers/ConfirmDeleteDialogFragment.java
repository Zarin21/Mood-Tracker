package com.example.unemployedavengers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

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
        return new AlertDialog.Builder(requireContext())
                .setTitle("Delete Mood Event")
                .setMessage("Are you sure you want to delete this mood event?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    //send result to the dashboard indicating that the mood should be deleted
                    Bundle result = new Bundle();
                    result.putBoolean("DeleteConfirmed", true);
                    getParentFragmentManager().setFragmentResult("delete_mood_event", result);
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}


