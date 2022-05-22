package it.units.youweather.ui.logged_in_area;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentTakeAPhotoBinding;
import it.units.youweather.utils.PermissionsHelper;

public class TakeAPhotoFragment extends Fragment {

    /**
     * TAG for logger.
     */
    private static final String TAG = TakeAPhotoFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentTakeAPhotoBinding viewBinding = FragmentTakeAPhotoBinding.inflate(getLayoutInflater());

        String[] permissionsToUseCamera = new String[]{Manifest.permission.CAMERA};

        // Show layout if permission to use the camera are granted
        PermissionsHelper.requestPermissionsForActivityIfNecessary(permissionsToUseCamera, requireActivity());
        viewBinding.getRoot().setVisibility(
                PermissionsHelper.arePermissionsGrantedForActivity(permissionsToUseCamera, requireActivity())
                        ? View.VISIBLE
                        : View.GONE);

        // ActivityResultLauncher to handle the Intent to take a photo
        //  with the camera and put it into the view.
        final ActivityResultLauncher<Void> takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                capturedBitmapImage -> {
                    if (capturedBitmapImage != null) {
                        Log.d(TAG, "Photo taken");
                        viewBinding.photo.setImageBitmap(capturedBitmapImage);
                        viewBinding.photo.setVisibility(View.VISIBLE);
                        viewBinding.takePhotoButton.setText(R.string.take_another_photo);
                    } else {
                        Log.d(TAG, "Unable to take the photo.");
                    }
                });

        viewBinding.takePhotoButton.setOnClickListener(view_ -> {
            PermissionsHelper.requestPermissionsForActivityIfNecessary(permissionsToUseCamera, requireActivity());
            if (PermissionsHelper.arePermissionsGrantedForActivity(permissionsToUseCamera, requireActivity())) {
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                takePhotoLauncher.launch(null);
            } else {
                Log.e(TAG, "Missing permissions to use the camera");
            }
        });

        return viewBinding.getRoot();
    }

}