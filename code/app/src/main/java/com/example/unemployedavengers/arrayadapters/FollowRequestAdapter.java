/**
 * FollowRequestAdapter - A custom ArrayAdapter for handling follow requests.
 *
 * Purpose:
 * - Displays a list of follow requests in a ListView.
 * - Provides UI elements (buttons) for accepting or ignoring follow requests.
 * - Uses an IUserDAO implementation to interact with the database.
 * - Updates the UI dynamically when a request is processed.
 *
 */
package com.example.unemployedavengers.arrayadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.unemployedavengers.DAO.IUserDAO;
import com.example.unemployedavengers.R;
import com.example.unemployedavengers.implementationDAO.UserDAOImplement;
import com.example.unemployedavengers.models.User;

import java.util.List;

public class FollowRequestAdapter extends ArrayAdapter<User> {
    private final IUserDAO userDAO;
    private final List<User> requests;
    private final String currentUserId;

    public FollowRequestAdapter(Context context, List<User> requests, String currentUserId) {
        super(context, R.layout.follow_item, requests);
        this.requests = requests;
        this.userDAO = new UserDAOImplement();
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.follow_item, parent, false);
        }

        User requester = requests.get(position);

        TextView usernameText = convertView.findViewById(R.id.username_text);
        Button addButton = convertView.findViewById(R.id.add_button);
        Button ignoreButton = convertView.findViewById(R.id.ignore_button);

        usernameText.setText(requester.getUsername());

        addButton.setOnClickListener(v -> {
            userDAO.acceptFollowRequest(requester.getUserId(), currentUserId)
                    .addOnSuccessListener(aVoid -> {
                        requests.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(getContext(), "Follow request accepted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        ignoreButton.setOnClickListener(v -> {
            userDAO.rejectFollowRequest(requester.getUserId(), currentUserId)
                    .addOnSuccessListener(aVoid -> {
                        requests.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(getContext(), "Follow request ignored", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        return convertView;
    }
}
