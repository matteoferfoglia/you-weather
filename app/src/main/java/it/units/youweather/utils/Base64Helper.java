package it.units.youweather.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.common.util.Base64Utils;

import java.util.Objects;

public abstract class Base64Helper {

    /**
     * Encodes the input byte array into a Base64-encoded string.
     *
     * @param bytes The bytes to be encoded.
     * @return The Base64-encoded string.
     */
    public static String encode(@NonNull byte[] bytes) {
        return Base64Utils.encode(Objects.requireNonNull(bytes));
    }

    /**
     * Encodes the input byte array into a Base64-encoded string.
     *
     * @param base64Encoded The Base64-encoded string.
     * @return The decoded bytes.
     */
    public static byte[] decode(@NonNull String base64Encoded) {
        return Base64Utils.decode(Objects.requireNonNull(base64Encoded));
    }

}
