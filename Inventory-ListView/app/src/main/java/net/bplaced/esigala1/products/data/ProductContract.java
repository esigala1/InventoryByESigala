package net.bplaced.esigala1.products.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import net.bplaced.esigala1.products.BuildConfig;

/**
 *
 * Note: This class is set to "final" because it only provides constants, so there is no need
 * to extend or implement anything for the outer class.
 *
 * Created by Effie Sigala on 2/6/2017.
 */

public final class ProductContract {

    /** Tag for the log messages. */
    private static final String LOG_TAG = "DEBUGGING " + ProductContract.class.getSimpleName();

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ProductContract() {

    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     *
     * Note: A way to access the package name is through the constant "APPLICATION_ID"
     * of the "BuildConfig" class.
     */
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     * Note: "content://" is the scheme.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path => Database table (appended to base content URI for possible URI's)
     * For instance, content://net.bplaced.esigala1.products/products/ is a valid path for
     * looking at product data. content://net.bplaced.esigala1.products/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCTS = "products";

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static final class ProductEntry implements BaseColumns {

        /** Tag for the log messages. */
        private static final String LOG_TAG = "DEBUGGING " + ProductEntry.class.getSimpleName();

        /**
         * The content URI to access the product data in the provider
         *
         * Append the BASE_CONTENT_URI (which contains the scheme and the content authority)
         * to the path segment =>  content://net.bplaced.esigala1.products/products
         **/
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /** Name of database table for products */
        public final static String TABLE_NAME = "products";

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="name";

        /**
         * CPU of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_CPU = "cpu";

        /**
         * Operation System of the product.
         *
         * The only possible values are {@link #OS_UNKNOWN}, {@link #OS_ANDROID},
         * or {@link #OS_IOS}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_OS = "operation_system";

        /**
         * Price of the product.
         *
         * Type: REAL
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Quantity of the product.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QTY = "quantity";

        /**
         * Image of the product.
         *
         * Type: BITMAP
         */
        public final static String COLUMN_IMAGE_VALUE = "image_value";

        /**
         * Possible values for the gender of the product.
         */
        public static final int OS_UNKNOWN = 0;
        public static final int OS_ANDROID = 1;
        public static final int OS_IOS = 2;

        /**
         * Returns whether or not the given operating system is {@link #OS_UNKNOWN},
         * {@link #OS_ANDROID}, or {@link #OS_IOS}.
         */
        public static boolean isValidOS(int operatingSystem) {
            return operatingSystem == OS_UNKNOWN || operatingSystem == OS_ANDROID || operatingSystem == OS_IOS;
        }
    }

}
