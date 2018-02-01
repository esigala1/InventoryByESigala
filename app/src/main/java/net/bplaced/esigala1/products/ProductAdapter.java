package net.bplaced.esigala1.products;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import net.bplaced.esigala1.products.data.ProductContract.ProductEntry;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Effie Sigala on 22/6/2017.
 */

public class ProductAdapter extends CursorAdapter {

    /** Tag for the log messages. */
    private static final String LOG_TAG = "DEBUGGING " + ProductAdapter.class.getSimpleName();

    private Toast mToast;

    /**
     * Constructs a new {@link ProductAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context.
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to.
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, final Cursor cursor, ViewGroup parent) {
        Log.v(LOG_TAG, "newView => Position = " + cursor.getPosition());

        // Inflate a list item view using the layout specified in list_item.xml
        View row = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

        // Create an instance of the holder.
        ViewHolder holder = new ViewHolder();

        // Get the TextViews from the ViewHolder object and set the texts.
        holder.saleButton = (Button) row.findViewById(R.id.product_sale);
        holder.nameTextView = (TextView) row.findViewById(R.id.product_name);
        holder.priceTextView = (TextView) row.findViewById(R.id.product_price);
        holder.quantityTextView = (TextView) row.findViewById(R.id.product_quantity);

        // Find the columns of product attributes that we're interested in.
        holder.idIndex = cursor.getColumnIndexOrThrow(ProductEntry._ID);
        holder.nameIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME);
        holder.priceIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE);
        holder.quantityIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QTY);

        // Store each of the component views inside the tag field of the Layout.
        row.setTag(holder);

        // Return the blank list item view.
        return row;
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method.
     * @param context app context.
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        Log.v(LOG_TAG, "bindView => Position = " + cursor.getPosition());

        // Declare and initialize a ViewHolder object using the existing tag field of the Layout.
        final ViewHolder holder = (ViewHolder) view.getTag();

        // Read the product attributes from the Cursor for the current product.
        final int productID = cursor.getInt(holder.idIndex);
        final String productName = cursor.getString(holder.nameIndex);
        final double productPrice = cursor.getDouble(holder.priceIndex);
        final int productQTY = cursor.getInt(holder.quantityIndex);

        // Get the TextView from the ViewHolder object and set the the product name.
        holder.nameTextView.setText(productName);

        // Set the currency.
        NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.ITALY);
        // Get the TextView from the ViewHolder object and set the the price.
        holder.priceTextView.setText(fmt.format(productPrice).toString());

        // Display the quantity of the product.
        displayQTY(context, holder, productQTY);

        // Set the OnClick listener - SALE button.
        holder.saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(LOG_TAG, "Sale button clicked => " + productName);
                // Variable to put the message to display corresponding to availability.
                String message = "";
                // If the item is not out of stock, then...
                if (productQTY > 0)
                {
                    // Create a ContentValues object where column names are the keys,
                    // and product attributes from the editor are the values.
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QTY, productQTY - 1);

                    // Form the content URI that represents the specific product that was clicked on,
                    // by appending the "id" (passed as input to this method) onto the
                    // {@link ProductEntry#CONTENT_URI}.
                    // For example, the URI would be "content://net.bplaced.esigala1.products/products/2"
                    // if the product with ID 2 was clicked on.
                    Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productID);

                    Log.v(LOG_TAG, "URI = " + currentProductUri);

                    // Update the new quantity of the product in the SQLite Database.
                    int rowsAffected = context.getContentResolver().update(currentProductUri, values, null, null);

                    // Show a toast message depending on whether or not the update was successful.
                    if (rowsAffected == 0) {
                        // If no rows were affected, then there was an error with the update.
                        message = context.getResources().getString(R.string.editor_update_product_failed);
                    }
                    else
                    {
                        // Display the new quantity of the product.
                        displayQTY(context, holder, productQTY - 1);
                        // Otherwise, the update was successful and we can display a toast.
                        message = context.getResources().getString(R.string.msg_item_sold, productName);
                    }
                }
                else
                {
                    message = context.getResources().getString(R.string.msg_item_out_of_stock, productName);
                }

                // If the "mToast" object is not null, then cancel it.
                if (mToast != null){
                    mToast.cancel();
                }
                // Instantiate the Toast object.
                mToast = Toast.makeText(
                        context,            // Context
                        message,            // Message
                        Toast.LENGTH_SHORT  // Duration
                );
                // Show the toast message.
                mToast.show();
            }
        });
    }

    /**
     * Display the current quantity of the product.
     **/
    private void displayQTY(Context context, ViewHolder holder, int quantity){
        // If the quantity is zero, then...
        if (quantity == 0) {
            // Get the TextView from the ViewHolder object and set the text "Out of stock".
            holder.quantityTextView.setText(context.getString(R.string.out_of_stock));
        }
        else
        {
            // Get the TextView from the ViewHolder object and set the the quantity.
            holder.quantityTextView.setText(context.getString(R.string.quantity) + " #" + quantity);
        }
    }

    /**
     * A {@link ViewHolder} object contains all the Views and SQLite database column indices
     * related to a single list item object.
     */
    private static class ViewHolder {
        Button saleButton;
        TextView nameTextView;
        TextView priceTextView;
        TextView quantityTextView;
        int idIndex;
        int nameIndex;
        int priceIndex;
        int quantityIndex;
    }
}
