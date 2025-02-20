package com.example.unemployedavengers.arrayadapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.example.unemployedavengers.models.User;

import java.util.ArrayList;

public class UserArrayAdapter extends ArrayAdapter<User> {
    private Context context;
    private ArrayList<User> users;

    public UserArrayAdapter(Context context, ArrayList<User> users){
        super(context, 0, users);
        this.users = users;
        this.context = context;
    }

}
