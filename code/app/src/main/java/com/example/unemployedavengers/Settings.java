/**
 * Settings Fragment - Provides the user interface for the settings page in the Unemployed Avengers application.
 *
 * This fragment is responsible for:
 * - Inflating the settings layout (`settings.xml`) and displaying the settings options for the user.
 *
 * Features:
 * - Displays various user settings options (e.g., notification preferences, privacy settings, etc.).
 * - Currently, no specific settings functionality has been implemented within this fragment.
 *
*/

package com.example.unemployedavengers;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Settings extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings, container, false);
    }
}
