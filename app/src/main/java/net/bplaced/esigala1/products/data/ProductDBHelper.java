package net.bplaced.esigala1.products.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.bplaced.esigala1.products.data.ProductContract.ProductEntry;

/**
 * Database helper for Products app. Manages database creation and version management.
 *
 * Created by Effie Sigala on 22/6/2017.
 */

public class ProductDBHelper extends SQLiteOpenHelper {

    /** Tag for the log messages. */
    private static final String LOG_TAG = "DEBUGGING " + ProductDBHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 2;

    /** Include the IMAGE VALUE in the SQLite database **/
    private static final String DATABASE_ADD_IMAGE_VALUE = "ALTER TABLE "
            + ProductEntry.TABLE_NAME + " ADD COLUMN " + ProductEntry.COLUMN_IMAGE_VALUE + " BLOB;";

    /**
     * Constructs a new instance of {@link ProductDBHelper}.
     *
     * @param context of the app
     */
    public ProductDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(LOG_TAG, "onCreate()");
        // Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_CPU + " TEXT, "
                + ProductEntry.COLUMN_PRODUCT_OS + " INTEGER NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_QTY + " INTEGER NOT NULL DEFAULT 0, "
                + ProductEntry.COLUMN_IMAGE_VALUE + " BLOB );";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(LOG_TAG, "onUpgrade()");
        if (oldVersion < 2) {
            db.execSQL(DATABASE_ADD_IMAGE_VALUE);
        }
    }
}
