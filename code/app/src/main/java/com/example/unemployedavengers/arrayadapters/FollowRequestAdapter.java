/**
 * FollowRequestAdapter - A custom ArrayAdapter for handling follow requests.
 *
 * Purpose:
 * - Displays a list of follow requests in a `ListView`, where each request contains a user's username and options to either accept or ignore the request.
 * - Interacts with the `IUserDAO` interface to process follow requests (accept or reject) using the `UserDAOImplement` class.
 * - Dynamically updates the UI by removing the request from the list once it is accepted or ignored.
 * - Utilizes `Toast` messages to provide feedback to the user when a request is processed.
 *
 * Design Pattern:
 * - Implements the `ArrayAdapter` design pattern to efficiently manage a list of `User` objects (representing follow requests) and display them in a `ListView`.
 * - Provides custom buttons (`addButton` and `ignoreButton`) to accept or reject follow requests, which trigger actions in the underlying database via the `IUserDAO`.
 * - Ensures real-time UI updates when follow requests are processed by removing accepted or ignored requests from the list and notifying the adapter of the change.
 *
 * Outstanding Issues:
 * - The adapter could be further optimized to handle large lists of requests by improving the data update and UI refresh mechanisms (e.g., using `DiffUtil` for list comparison).
 * - There may be a potential performance issue if many follow requests are handled simultaneously, as it requires network calls to interact with the database.
 * - No confirmation dialogues or additional user feedback are provided before the request is accepted or ignored; this could be added to enhance user experience.
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
