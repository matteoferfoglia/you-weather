package it.units.youweather.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Utility class for pictures.
 * Adapted from <a href="https://stackoverflow.com/a/14066265/17402378">here</a>.
 */
public abstract class ImagesHelper {

    /**
     * TAG for logger.
     */
    private static final String TAG = ImagesHelper.class.getSimpleName();

    /**
     * Most phone cameras are landscape, meaning if you
     * take the photo in portrait, the resulting photos
     * will be rotated 90 degrees. This method tries to
     * straight the image and return it.
     *
     * @param inputImageUri The {@link Uri} to the Bitmap image.
     * @return The straighten image.
     */
    public static Bitmap straightImage(@NonNull Uri inputImageUri) {
        try {
            String inputBitmapPath = Objects.requireNonNull(inputImageUri).getPath();
            ExifInterface ei = new ExifInterface(inputBitmapPath);
            int imageOrientation = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,      // tag for the desired attribute (we want to know the actual image orientation)
                    ExifInterface.ORIENTATION_UNDEFINED // default returned value if the specified tag in unavailable
            );
            Bitmap inputBitmap = BitmapFactory.decodeFile(inputBitmapPath);

            if (inputBitmap != null) {

                Bitmap straightenImage;
                switch (imageOrientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        straightenImage = rotateImage(inputBitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        straightenImage = rotateImage(inputBitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        straightenImage = rotateImage(inputBitmap, 270);
                        break;

                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        straightenImage = inputBitmap;
                }
                return straightenImage;
            } else {
                Log.e(TAG, "Returning null because input image cannot be decoded.");
                return null;
            }
        } catch (IOException exception) {
            Log.e(TAG, "Returning null due to an IOException", exception);
            return null;
        }
    }

    /**
     * Compresses the given input image such that it will have at most the
     * provided number of pixels (intended as outputWidthPx * outputHeightPx)
     *
     * @param inputBitmap    The input {@link Bitmap} image to compress.
     * @param maxNumOfPixels The maximum number of pixels
     *                       (intended as outputWidthPx * outputHeightPx)
     *                       of the output image.
     * @return The compressed image.
     */
    public static Bitmap compressImageAndGet(@NonNull Bitmap inputBitmap, int maxNumOfPixels) {

        Bitmap outputBitmap = Objects.requireNonNull(inputBitmap);
        final int inputImageWidth = inputBitmap.getWidth();
        final int inputImageHeight = inputBitmap.getHeight();

        if (maxNumOfPixels > 0 && inputImageWidth > 0 && inputImageHeight > 0) {

            // Compute the compression ratio
            final double widthToHeightRatio = inputImageWidth / (double) inputImageHeight;
            final double compressionRatio =
                    Math.sqrt(maxNumOfPixels / (widthToHeightRatio)) / inputImageHeight;

            assert compressionRatio > 0;

            if (compressionRatio < 1) {
                // If compression ration is greater than 1, it would mean to enlarge the input image, hence avoided

                final int outputImageWidth = (int) Math.round(compressionRatio * inputImageWidth);
                final int outputImageHeight = (int) Math.round(compressionRatio * inputImageHeight);

                // Actual compression
                outputBitmap = Bitmap.createScaledBitmap(inputBitmap, outputImageWidth, outputImageHeight, true);
            }
        }

        return outputBitmap;
    }

    /**
     * Rotates the given {@link Bitmap} image.
     *
     * @param source The image to rotate.
     * @param angle  The angle for ratation.
     */
    public static Bitmap rotateImage(@NonNull Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(
                Objects.requireNonNull(source),
                0, 0,   // x and y coord of the first pixel in source image
                source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * Wrapper for {@link Bitmap}s, to make them {@link Serializable}.
     * The image is serialized with the Base64 encoding.
     */
    public static class SerializableBitmap implements Serializable {

        private String base64Bitmap;
        private int widthPx;
        private int heightPx;
        private String bitmapConfigName;

        public SerializableBitmap(Bitmap bitmap) {
            final int MAX_NUM_OF_PIXELS = 1_000_000;
            if (bitmap == null) {
                // Create empty bitmap
                // Adapted from https://stackoverflow.com/a/5664047/17402378

                int width = 1, height = 1;
                Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
                bitmap = Bitmap.createBitmap(width, height, conf); // this creates a MUTABLE bitmap
            }
            Bitmap compressedBitmap = compressImageAndGet(bitmap, MAX_NUM_OF_PIXELS);
            this.base64Bitmap = Base64Helper.encode(convertToByteArray(compressedBitmap));
            this.bitmapConfigName = compressedBitmap.getConfig().name();  // adapted from https://stackoverflow.com/a/34165515/17402378
            this.widthPx = compressedBitmap.getWidth();
            this.heightPx = compressedBitmap.getHeight();
        }

        private SerializableBitmap() {  // needed no-args constructor
        }

        public String getBase64Bitmap() {
            return base64Bitmap;
        }

        public int getWidthPx() {
            return widthPx;
        }

        public int getHeightPx() {
            return heightPx;
        }

        public String getBitmapConfigName() {
            return bitmapConfigName;
        }

        @Exclude    // Firebase tries to serialiaze every getter
        public Bitmap getBitmap() {
            byte[] imageAsByteArray = Base64Helper.decode(base64Bitmap);
            Bitmap.Config configBmp = Bitmap.Config.valueOf(bitmapConfigName);
            Bitmap bitmap_tmp = Bitmap.createBitmap(widthPx, heightPx, configBmp);
            ByteBuffer buffer = ByteBuffer.wrap(imageAsByteArray);
            bitmap_tmp.copyPixelsFromBuffer(buffer);
            return bitmap_tmp;
        }

        /**
         * Adapted from <a href="https://stackoverflow.com/a/34165515/17402378">here</a>.
         *
         * @param bitmap The {@link Bitmap} instance to convert to byte array.
         * @return the byte array for this instance.
         */
        private static byte[] convertToByteArray(@NonNull Bitmap bitmap) {
            int size = bitmap.getRowBytes() * bitmap.getHeight();
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            bitmap.copyPixelsToBuffer(byteBuffer);
            return byteBuffer.array();
        }
    }
}
