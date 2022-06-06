package it.units.youweather.ui.logged_in_area;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

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
    private Fragment containedFragment;

    /**
     * @param containedFragment The {@link Fragment} to be contained in this instance.
     */
    public DialogFragmentContainer(@NonNull Fragment containedFragment) {
        this.containedFragment = Objects.requireNonNull(containedFragment);
    }

    public DialogFragmentContainer() { // public no-args constructor
    }

    public static DialogFragmentContainer newInstance() {
        Bundle args = new Bundle();
        DialogFragmentContainer fragment = new DialogFragmentContainer();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentDialogContainerBinding viewBinding = FragmentDialogContainerBinding.inflate(inflater);

        try {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(viewBinding.fragmentContainerView.getId(), containedFragment)
                    .commit();
        } catch (NullPointerException ignored) { // In some cases (e.g., if the user change the system language), views must be recreated and this can lead to NullPointerException
        }

        return viewBinding.getRoot();
    }
}