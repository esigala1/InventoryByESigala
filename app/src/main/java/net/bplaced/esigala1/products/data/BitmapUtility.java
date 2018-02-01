package net.bplaced.esigala1.products.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Helper methods related to convert Bitmaps to Byte Arrays and vice versa.
 *
 * Created by Effie Sigala on 23/6/2017.
 */

public class BitmapUtility {

    /** Tag for the log messages. */
    private static final String LOG_TAG = "DEBUGGING " + BitmapUtility.class.getSimpleName();

    // To prevent someone from accidentally instantiating the utility class,
    // give it an empty constructor.
    private BitmapUtility() {
    }

    /**
     * Get a byte array from an image from the resources.
     */
    public static byte[] getImageFromResourcesAsByteArray(Context context, int resourceFile){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceFile);
        return getBitmapAsByteArray(bitmap);
    }

    /**
     * Get a byte array from an image from a URI.
     */
    public static byte[] getImageFromURIAsByteArray(Context context, Uri imageURI){
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageURI);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not get the bitmap from the URI: " + e);
        }

        // Max desired dimensions.
        int maxWidth = 80;
        int maxHeight = 90;

        // Scale the bitmap if its dimensions are greater than the desired...
        if (bitmap.getWidth() > maxWidth || bitmap.getHeight() > maxHeight){
            Matrix m = new Matrix();
            m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, maxWidth, maxHeight), Matrix.ScaleToFit.CENTER);
            Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            // Return the scaled bitmap.
            return getBitmapAsByteArray(scaledBitmap);
        }

        // Return the unscaled bitmap.
        return getBitmapAsByteArray(bitmap);
    }

    /**
     * Get a byte array from bitmap.
     */
    private static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    /**
     * Get a bitmap from byte array.
     */
    public static Bitmap getBitmapFromByteArray(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
