package com.puteffort.sharenshop.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.puteffort.sharenshop.R;

import java.util.ArrayList;

public class FilterDialogFragment extends DialogFragment {

    private OnFilterClick callback;
    private ArrayList<Integer> checked = new ArrayList<>();

    public FilterDialogFragment(ArrayList<Integer> filterSelected) {
        this.checked = filterSelected;
    }

    public interface OnFilterClick {
        void onFilterClicked(ArrayList<Integer> checked, ArrayList<Integer> type);
    }

    public void setOnFilterClick(OnFilterClick callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        ArrayList<Integer> type = new ArrayList<>();

        final View customLayout = getLayoutInflater().inflate(R.layout.custom_filter_dialog, null);
        CheckBox[] checkBoxes = new CheckBox[12];
        Integer[] checkBoxIds = {R.id.amountCheckBox1, R.id.amountCheckBox2, R.id.amountCheckBox3, R.id.amountCheckBox4, R.id.peopleCheckBox1, R.id.peopleCheckBox2, R.id.peopleCheckBox3, R.id.peopleCheckBox4, R.id.lastActivityCheckBox1, R.id.lastActivityCheckBox2, R.id.lastActivityCheckBox3, R.id.lastActivityCheckBox4};
        for(Integer i : this.checked){
            CheckBox chkBoxes = (CheckBox)customLayout.findViewById(checkBoxIds[i]);
            chkBoxes.setChecked(true);
        }

        builder.setView(customLayout);

        builder.setTitle("Filter by")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        checked.clear();
                        type.clear();
                        for(int i = 0; i < checkBoxIds.length; i++) {
                            checkBoxes[i] = (CheckBox) customLayout.findViewById(checkBoxIds[i]);
                            if(checkBoxes[i].isChecked()) {
                                if(i < 4)
                                    type.add(1);
                                else if(i < 9)
                                    type.add(2);
                                else
                                    type.add(3);
                                checked.add(i);
                            }
                        }
                        if(callback != null) {
                            callback.onFilterClicked(checked, type);
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton("CLEAR FILTERS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if(callback != null) {
                            callback.onFilterClicked(checked, type);
                            dialog.dismiss();
                        }
                    }
                });
        return builder.create();
    }
}