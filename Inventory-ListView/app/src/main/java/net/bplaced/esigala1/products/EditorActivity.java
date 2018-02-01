package net.bplaced.esigala1.products;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.bplaced.esigala1.products.data.BitmapUtility;
import net.bplaced.esigala1.products.data.ProductContract.ProductEntry;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener{

    /** Tag for the log messages. */
    private static final String LOG_TAG = "DEBUGGING " + EditorActivity.class.getSimpleName();

    /** Key for quantity on SaveInstanceState */
    private static final String STATE_QUANTITY = "state_quantity";

    /** Identifier for the product data loader */
    private static final int EXISTING_PET_LOADER = 0;

    /** Identifier for the intent to pick an image from the device */
    private static final int IMAGE_PICKER = 1;

    /** Content URI for the existing product (null if it's a new product) */
    private Uri mCurrentProductUri;

    /** EditText field to enter the product's name */
    private EditText mNameEditText;

    /** EditText field to enter the product's cup */
    private EditText mCpuEditText;

    /** EditText field to enter the product's price */
    private EditText mPriceEditText;

    /** Spinner to set the product's operating system */
    private Spinner mOSSpinner;

    /** The image of the product */
    private ImageView mProductImageView;

    /**
     * Gender of the product. The possible valid values are in the ProductContract.java file:
     * {@link ProductEntry#OS_UNKNOWN}, {@link ProductEntry#OS_ANDROID}, or
     * {@link ProductEntry#OS_IOS}.
     */
    private int operatingSystem = ProductEntry.OS_UNKNOWN;

    /** Boolean flag that keeps track of whether the product has been edited (true) or not (false) **/
    private boolean hasProductChanged = false;

    /** Boolean flag that keeps track of whether the instance is saved **/
    private boolean isSavedInstance = false;

    /** Variable to keep the product's quantity **/
    private int quantity = 0;

    /** Variable to keep the URI of the selected image from the device **/
    private Uri selectedImageUri;

    private Toast mToast;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the hasProductChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            hasProductChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        mCurrentProductUri = getIntent().getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Hide the FAB.
            findViewById(R.id.fab_email).setVisibility(View.GONE);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);

            // Setup FAB to open EditorActivity
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_email);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v(LOG_TAG, "Floating Action Button pressed...");
                    // Send email to supplier
                    orderMore();
                }
            });
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mCpuEditText = (EditText) findViewById(R.id.edit_product_cpu);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mOSSpinner = (Spinner) findViewById(R.id.spinner_operating_system);
        mProductImageView = (ImageView) findViewById(R.id.image_view_product);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mCpuEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mOSSpinner.setOnTouchListener(mTouchListener);

        // Set the OnClick listeners.
        mProductImageView.setOnClickListener(this);
        (findViewById(R.id.button_decrease_qty)).setOnClickListener(this);
        (findViewById(R.id.button_increase_qty)).setOnClickListener(this);

        // Setup the dropdown spinner.
        setupSpinner();

        // If there is a saved state, then...
        if (savedInstanceState != null) {
            // Set the boolean variable to true, because there is a saved instance.
            isSavedInstance = true;
            // Get the quantity from the saved state.
            quantity = savedInstanceState.getInt(STATE_QUANTITY);
            // Display the given quantity value on the screen.
            displayQuantity();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(LOG_TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        // Save the current value of the quantity.
        outState.putInt(STATE_QUANTITY, quantity);
    }

    /**
     * Display a Toast message.
     *
     * @param message is the text to display.
     */
    private void displayToastMessage(String message){
        // If the "mToast" object is not null, then cancel it.
        if (mToast != null){
            mToast.cancel();
        }
        // Instantiate the Toast object.
        mToast = Toast.makeText(
                this,               // Context
                message,            // Message
                Toast.LENGTH_SHORT  // Duration
        );
        // Show the toast message.
        mToast.show();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the product.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_os_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mOSSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mOSSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.os_android))) {
                        operatingSystem = ProductEntry.OS_ANDROID; // Male
                    } else if (selection.equals(getString(R.string.os_ios))) {
                        operatingSystem = ProductEntry.OS_IOS; // Female
                    } else {
                        operatingSystem = ProductEntry.OS_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                operatingSystem = ProductEntry.OS_UNKNOWN;; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" MenuItem.
        if (mCurrentProductUri == null) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return true;
    }

    /**
     * Select an image from device.
     */
    private void selectImageFromDevice(){
        Log.v(LOG_TAG, "Select an image from the device...");
        // ACTION_GET_CONTENT is the intent to read/import data via the system's file browser.
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Filter to show only images, using the image MIME data type.
        intent.setType("image/*");
        // The intent should only return data that is on the local device.
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        // Start an activity to select an image from the device
        startActivityForResult(intent, IMAGE_PICKER);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // IMAGE_PICKER. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.
                // Pull that URI of the selected file using resultData.getData().
                selectedImageUri = resultData.getData();

                Log.i(LOG_TAG, "Uri: " + selectedImageUri.toString());

                /** Set the Image to the ImageView **/
                Picasso.with(this)
                        .load(String.valueOf(selectedImageUri)) // The path to load the image.
                        .fit() // Reduce the image size to the dimensions of the ImageView.
                        .centerInside() // Scale the image to the requested bounds.
                        .into(mProductImageView); // The ImageView to set the image.

                /** Display a successfully message **/
                displayToastMessage(getString(R.string.msg_image_selected_successfully));
            }
            else
            {
                /** Display an unsuccessful message **/
                displayToastMessage(getString(R.string.msg_image_selected_unsuccessfully));
            }
        }
        else
        {
            /** Display a cancel image picker message **/
            displayToastMessage(getString(R.string.msg_image_picker_canceled));
        }
    }

    /**
     * Create a new product or update an existing one (Get user input from editor and save
     * product into database).
     */
    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String cpuString = mCpuEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        // If the following values are not valid then display a message and return early
        // (Note: The method TextUtils.isEmpty() checks whether a string is made up of all whitespace or nothing at all).
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) || operatingSystem == ProductEntry.OS_UNKNOWN) {
            displayToastMessage(getString(R.string.editor_fill_in_all_values));
            return;
        }

        // If the price is not provided by the user, don't try to parse the string into a
        // double value. Use 0 by default.
        double price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_CPU, cpuString);
        values.put(ProductEntry.COLUMN_PRODUCT_OS, operatingSystem);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QTY, quantity);
        if (selectedImageUri != null)
        {
            values.put(ProductEntry.COLUMN_IMAGE_VALUE,
                    BitmapUtility.getImageFromURIAsByteArray(this, selectedImageUri));
        }

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not.
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                displayToastMessage(getString(R.string.editor_insert_product_failed));
            }
            else
            {
                // Otherwise, the insertion was successful and we can display a toast.
                displayToastMessage(getString(R.string.editor_insert_product_successful));
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentProductUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentProductUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                displayToastMessage(getString(R.string.editor_update_product_failed));
            }
            else
            {
                // Otherwise, the update was successful and we can display a toast.
                displayToastMessage(getString(R.string.editor_update_product_successful));
            }
        }
        // Exit activity
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion.
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!hasProductChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes.
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press.
        if (!hasProductChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG, "onCreateLoader()");
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table.
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_CPU,
                ProductEntry.COLUMN_PRODUCT_OS,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QTY,
                ProductEntry.COLUMN_IMAGE_VALUE};

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,     // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i(LOG_TAG, "onLoadFinished()");
        // Bail early if the cursor is null or there is less than 1 row in the cursor.
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor).
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in.
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int cpuColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CPU);
            int osColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_OS);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageValueColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE_VALUE);

            // Extract out the value from the Cursor for the given column index.
            String name = cursor.getString(nameColumnIndex);
            String cpu = cursor.getString(cpuColumnIndex);
            int os = cursor.getInt(osColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            byte[] imgByte = cursor.getBlob(imageValueColumnIndex);

            // If there is not a saved instance, then get the quantity from the cursor.
            if(!isSavedInstance){
                int qtyColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QTY);
                quantity = cursor.getInt(qtyColumnIndex);
            }

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mCpuEditText.setText(cpu);
            mPriceEditText.setText(Double.toString(price));

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (os) {
                case ProductEntry.OS_ANDROID:
                    mOSSpinner.setSelection(1);
                    break;
                case ProductEntry.OS_IOS:
                    mOSSpinner.setSelection(2);
                    break;
                default:
                    mOSSpinner.setSelection(0);
                    break;
            }

            // Display the given quantity value on the screen.
            displayQuantity();

            // If the array of bytes is not null, then convert it to a Bitmap and display it.
            if (imgByte != null)
            {
                mProductImageView.setImageBitmap(BitmapUtility.getBitmapFromByteArray(imgByte));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG, "onLoaderReset()");
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mCpuEditText.setText("");
        mPriceEditText.setText("");
        mOSSpinner.setSelection(0); // Select "Unknown" gender
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes.
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
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

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the product at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentProductUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                displayToastMessage(getString(R.string.editor_delete_product_failed));
            }
            else
            {
                // Otherwise, the delete was successful and we can display a toast.
                displayToastMessage(getString(R.string.editor_delete_product_successful));
            }
        }
        // Close the activity
        finish();
    }

    /**
     * Decrease product's quantity.
     */
    private void decreaseQTY() {
        // If the current quantity is zero, then display message and exit early.
        if (quantity == 0){
            displayToastMessage(getString(R.string.msg_negative_stock));
            return;
        }
        hasProductChanged = true;
        // Decrease the quantity.
        quantity--;
        // Display the given quantity value on the screen.
        displayQuantity();
    }

    /**
     * Increase product's quantity.
     */
    private void increaseQTY() {
        hasProductChanged = true;
        // Increase the quantity.
        quantity++;
        // Display the given quantity value on the screen.
        displayQuantity();
    }

    /**
     * Display the given quantity value on the screen.
     */
    private void displayQuantity(){
        ((TextView) findViewById(R.id.text_view_quantity)).setText(String.valueOf(quantity));
    }

    /**
     * Send email to order more from the supplier.
     */
    private void orderMore(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.email_subject, mNameEditText.getText().toString()));
        intent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.email_text, quantity, mNameEditText.getText().toString()));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.image_view_product:
                selectImageFromDevice();
                break;
            case R.id.button_decrease_qty:
                decreaseQTY();
                break;
            case R.id.button_increase_qty:
                increaseQTY();
                break;
            default:
                break;
        }
    }

}