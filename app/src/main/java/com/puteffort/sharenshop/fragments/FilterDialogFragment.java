package com.puteffort.sharenshop.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputLayout;
import com.puteffort.sharenshop.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterDialogFragment extends DialogFragment {

    private OnFilterClick callback;
    private final Set<Integer> lastActivityChips;
    private final List<String> fromAndTos;

    public FilterDialogFragment(Set<Integer> lastActivityChips, List<String> fromAndTos) {
        this.lastActivityChips = lastActivityChips;
        this.fromAndTos = fromAndTos;
    }

    public interface OnFilterClick {
        void onFilterClicked(Set<Integer> lastActivityChips, List<String> fromAndTos);
    }

    public void setOnFilterClick(OnFilterClick callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        // TODO(make context of fragment)
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        final View layout = getLayoutInflater().inflate(R.layout.custom_filter_dialog, null);

        EditText amountFrom = ((TextInputLayout)layout.findViewById(R.id.filterAmountFrom)).getEditText();
        EditText amountTo = ((TextInputLayout)layout.findViewById(R.id.filterAmountTo)).getEditText();
        EditText peopleFrom = ((TextInputLayout)layout.findViewById(R.id.filterPeopleFrom)).getEditText();
        EditText peopleTo = ((TextInputLayout)layout.findViewById(R.id.filterPeopleTo)).getEditText();

        EditText[] editTexts = {amountFrom, amountTo, peopleFrom, peopleTo};
        Chip[] chips = {layout.findViewById(R.id.lessThan1Month), layout.findViewById(R.id.oneMonthTo6Months),
                layout.findViewById(R.id.sixMonthsTo1Year), layout.findViewById(R.id.greaterThan1Year)};

        for (int i = 0; i < 4; i++) {
            editTexts[i].setText(fromAndTos.get(i));
            if (lastActivityChips.contains(chips[i].getId())) {
                chips[i].setChecked(true);
            }
        }

        builder.setView(layout);
        builder.setPositiveButton("OK", (dialog, id) -> {
                    Set<Integer> chipsSelected = new HashSet<>();
                    List<String> values = new ArrayList<>();
                    for (int i = 0; i < 4; i++) {
                        if (chips[i].isChecked())
                            chipsSelected.add(chips[i].getId());
                        values.add(editTexts[i].getText().toString());
                    }

                    if(callback != null) {
                        callback.onFilterClicked(chipsSelected, values);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("CLEAR FILTERS", (dialog, id) -> {
                    if(callback != null) {
                        callback.onFilterClicked(new HashSet<>(), Arrays.asList("0", "", "0", ""));
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}