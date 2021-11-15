package com.puteffort.sharenshop.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.puteffort.sharenshop.R;

public class SortDialogFragment extends DialogFragment {
    private OnSortClick callback;
    private final int checkedItem;

    public SortDialogFragment(int checkedSort) {
        this.checkedItem = checkedSort;
    }

    public interface OnSortClick {
        void onSortClicked(String sortBy);
    }

    public void setOnSortClick(OnSortClick callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        String[] sort_tags = getResources().getStringArray(R.array.sort_tags);

        builder.setTitle("Sort by")
                .setSingleChoiceItems(R.array.sort_tags, checkedItem,null)
                .setPositiveButton("OK", (dialog, id) -> {
                    int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                    if(callback != null) {
                        dialog.dismiss();
                        callback.onSortClicked(sort_tags[selectedPosition]);
                    }
                })
                .setNegativeButton("DEFAULT", (dialog, id) -> {
                    if(callback != null) {
                        dialog.dismiss();
                        callback.onSortClicked("Last Activity");
                    }
                });
        return builder.create();
    }
}