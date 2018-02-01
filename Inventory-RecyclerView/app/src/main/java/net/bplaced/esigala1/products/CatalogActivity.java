package net.bplaced.esigala1.products;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.bplaced.esigala1.products.data.BitmapUtility;
import net.bplaced.esigala1.products.data.ProductContract.ProductEntry;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        ProductAdapter.ProductRVAdapterOnClickHandler {

    /** Tag for the log messages. */
    private static final String LOG_TAG = "DEBUGGING " + CatalogActivity.class.getSimpleName();

    /** Identifier for the product data loader */
    private static final int PET_LOADER = 0;

    /** Button to add a new product **/
    FloatingActionButton fabAddNewProduct;

    private ProductAdapter mProductAdapter;
    private RecyclerView mRecyclerView;

    public static View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        fabAddNewProduct = (FloatingActionButton) findViewById(R.id.fab_add_product);
        fabAddNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(LOG_TAG, "Floating Action Button pressed...");
                startActivity(new Intent(CatalogActivity.this, EditorActivity.class));
            }
        });

        /**
         * A view to display as the empty view.
         */
        mEmptyView = findViewById(R.id.empty_view);

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. In our case, we want a vertical list, so we pass in the constant from the
         * LinearLayoutManager class for vertical lists, LinearLayoutManager.VERTICAL.
         *
         * There are other LayoutManagers available to display your data in uniform grids,
         * staggered grids, and more! See the developer documentation for more details.
         *
         * The third parameter (shouldReverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        /*
         * Add decoration between the items of the RecyclerView.
         */
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        /*
         * The RecyclerViewAdapter is responsible for linking our data with the Views that
         * will end up displaying our data.
         */
        mProductAdapter = new ProductAdapter(this, this);

        mRecyclerView.setAdapter(mProductAdapter);

        // Kick off the loader
        getLoaderManager().initLoader(PET_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded product data into the database. For debugging purposes only.
     */
    private void insertProduct() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "LG G5");
        values.put(ProductEntry.COLUMN_PRODUCT_CPU, "2200 MHz");
        values.put(ProductEntry.COLUMN_PRODUCT_OS, ProductEntry.OS_ANDROID);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 364f);
        values.put(ProductEntry.COLUMN_PRODUCT_QTY, 4);
        values.put(ProductEntry.COLUMN_IMAGE_VALUE,
                BitmapUtility.getImageFromResourcesAsByteArray(this, R.drawable.phone_lg_g5));
        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link ProductEntry#CONTENT_URI} to indicate that we want to insert
        // into the products database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "OnePlus 3T");
        values.put(ProductEntry.COLUMN_PRODUCT_OS, ProductEntry.OS_ANDROID);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 420f);
        values.put(ProductEntry.COLUMN_IMAGE_VALUE,
                BitmapUtility.getImageFromResourcesAsByteArray(this, R.drawable.phone_one_plus_3t));
        getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "iPhone SE");
        values.put(ProductEntry.COLUMN_PRODUCT_CPU, "1840 MHz");
        values.put(ProductEntry.COLUMN_PRODUCT_OS, ProductEntry.OS_IOS);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 380.35f);
        values.put(ProductEntry.COLUMN_PRODUCT_QTY, 2);
        getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all products in the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + " rows deleted from product database");
    }

    /**
     * Prompt the user to confirm that they want to delete all products.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all the products.
                deleteAllProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QTY};

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,      // Parent activity context
                ProductEntry.CONTENT_URI,  // Provider content URI to query
                projection,                // Columns to include in the resulting Cursor
                null,                      // No selection clause
                null,                      // No selection arguments
                null);                     // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ProductLVAdapter} with this new cursor containing updated product data.
        mProductAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted.
        mProductAdapter.swapCursor(null);
    }

    /**********************************************************************************************
     * Implementation for the {@link ProductAdapter.ProductRVAdapterOnClickHandler} interface
     *********************************************************************************************/

    /**
     * This callback is invoked when you click on an item in the list.
     **/
    @Override
    public void onClickItem(int position, int productID) {
        Log.v(LOG_TAG, "Clicked item at position " + position + " Product ID = " + productID);
        // Create new intent to go to {@link EditorActivity}
        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

        // Form the content URI that represents the specific product that was clicked on,
        // by appending the "id" (passed as input to this method) onto the
        // {@link ProductEntry#CONTENT_URI}.
        // For example, the URI would be "content://net.bplaced.esigala1.products/products/2"
        // if the product with ID 2 was clicked on.
        Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productID);

        // Set the URI on the data field of the intent.
        intent.setData(currentProductUri);

        // Launch the {@link EditorActivity} to display the data for the current product.
        startActivity(intent);
    }


}
