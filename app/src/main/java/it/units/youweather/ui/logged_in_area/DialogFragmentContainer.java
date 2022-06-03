package it.units.youweather.ui.logged_in_area;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;

import it.units.youweather.databinding.FragmentDialogContainerBinding;

/**
 * This {@link DialogFragment} can wrap a {@link Fragment}.
 *
 * @author Matteo Ferfoglia
 */
public class DialogFragmentContainer extends DialogFragment {

    /**
     * The {@link Fragment} contained in this instance.
     */
    private final Fragment containedFragment;

    /**
     * @param containedFragment The {@link Fragment} to be contained in this instance.
     */
    public DialogFragmentContainer(@NonNull Fragment containedFragment) {
        this.containedFragment = Objects.requireNonNull(containedFragment);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDialogContainerBinding viewBinding = FragmentDialogContainerBinding.inflate(inflater);

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.add(viewBinding.fragmentContainerView.getId(), containedFragment).commitNow();

        return viewBinding.getRoot();
    }
}