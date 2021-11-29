package com.puteffort.sharenshop.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.puteffort.sharenshop.R;

public class DefaultFragment extends Fragment {
    private String message;

    public DefaultFragment() {
        // Required empty public constructor
    }

    public DefaultFragment(String message) {
        this.message = message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_default, container, false);

        if (message != null)
            ((TextView)view.findViewById(R.id.rightPaneMessage)).setText(message);

        return view;
    }
}