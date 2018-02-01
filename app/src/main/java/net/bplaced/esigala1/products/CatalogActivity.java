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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import net.bplaced.esigala1.products.data.BitmapUtility;
import net.bplaced.esigala1.products.data.ProductContract.ProductEntry;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    /** Tag for the log messages. */
    private static final String LOG_TAG = "DEBUGGING " + CatalogActivity.class.getSimpleName();

    /** Identifier for the product data loader */
    private static final int PET_LOADER = 0;

    /** Button to add a new product **/
    FloatingActionButton fabAddNewProduct;

    /** Adapter for the ListView */
    ProductAdapter mProductAdapter;

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

        // Find the ListView which will be populated with the product data
        ListView productListView = (ListView) findViewById(R.id.list_view);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of product data in the Cursor.
        // There is no product data yet (until the loader finishes) so pass in null for the Cursor.
        mProductAdapter = new ProductAdapter(this, null);
        productListView.setAdapter(mProductAdapter);

        // Setup the item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.v(LOG_TAG, "Clicked item at position " + position);
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific product that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link ProductEntry#CONTENT_URI}.
                // For example, the URI would be "content://net.bplaced.esigala1.products/products/2"
                // if the product with ID 2 was clicked on.
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent.
                intent.setData(currentProductUri);

                // Launch the {@link EditorActivity} to display the data for the current product.
                startActivity(intent);
            }
        });

        // Set the OnScroll listener to hide the FAB button when the list is scrolled to the bottom.
        productListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                switch (view.getId()){
                    case R.id.list_view:
                        // If all the items of the list are more than the visible items, then...
                        if (totalItemCount > visibleItemCount){
                            // Determine if the last item is fully visible.
                            final int lastItem = firstVisibleItem + visibleItemCount;
                            // If the last item is visible, then hide the fab button.
                            if(lastItem == totalItemCount)
                            {
                                fabAddNewProduct.setVisibility(View.INVISIBLE);
                            }
                            else
                            {
                                fabAddNewProduct.setVisibility(View.VISIBLE);
                            }
                        }
                        return;
                    default:
                        Log.v(LOG_TAG, "No list find with id = " + view.getId());
                        return;
                }
            }
        });

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
        // Update {@link ProductAdapter} with this new cursor containing updated product data.
        mProductAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted.
        mProductAdapter.swapCursor(null);
    }
}
