package it.units.youweather.ui.logged_in_area;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentTakeAPhotoBinding;
import it.units.youweather.utils.PermissionsHelper;
import it.units.youweather.utils.PicturesHelper;
import it.units.youweather.utils.ResourceHelper;

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

        // Used in the intent to take a photo
        Uri imageUri = requireActivity().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues()/*for additional data*/);

        // ActivityResultLauncher to handle the Intent to take a photo
        //  with the camera and put it into the view.
        final ActivityResultLauncher<Intent> takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> { // ActivityResultCallback<ActivityResult>#onActivityResult(ActivityResult)
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        String imageRealUrl = getRealPathFromUri(imageUri);
                        viewBinding.photo.setImageBitmap(PicturesHelper.straightImage(Uri.parse(imageRealUrl)));
                        viewBinding.photo.setVisibility(View.VISIBLE);
                    }
                });

        viewBinding.takePhotoButton.setOnClickListener(view_ -> {
            PermissionsHelper.requestPermissionsForActivityIfNecessary(permissionsToUseCamera, requireActivity());
            if (PermissionsHelper.arePermissionsGrantedForActivity(permissionsToUseCamera, requireActivity())) {
                Intent takeAPhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  // Intent to take a photo
                if (takeAPhotoIntent.resolveActivity(requireActivity().getPackageManager()) != null) { // Return the Activity component that should be used to handle this intent or null
                    if (imageUri != null) {
                        takeAPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    } else {
                        Log.e(TAG, "getContentResolver().insert(..) returned null.");
                    }
                    takePhotoLauncher.launch(takeAPhotoIntent);
                } else {
                    Toast.makeText(requireContext(), ResourceHelper.getResString(R.string.no_app_for_action), Toast.LENGTH_LONG)
                            .show();
                }
            } else {
                Log.e(TAG, "Missing permissions to use the camera");
            }
        });

        return viewBinding.getRoot();
    }

    /**
     * Get the real path of an image taken thanks to an Intent.
     * Adapted from <a href="https://stackoverflow.com/a/10382217/17402378">here</a>
     * and <a href="https://stackoverflow.com/a/12714830/17402378"></a>.
     *
     * @param imageUri The {@link Uri} used to take a photo with the {@link Intent}.
     * @return the {@link String} with the real path for the given {@link Uri}.
     */
    private String getRealPathFromUri(Uri imageUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().getContentResolver().query(imageUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

}