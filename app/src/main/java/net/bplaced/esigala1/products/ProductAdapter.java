package net.bplaced.esigala1.products;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.bplaced.esigala1.products.data.ProductContract.ProductEntry;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Effie Sigala on 1/2/2018.
 */

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    /** Tag for the log messages. */
    private static final String LOG_TAG = "DEBUGGING " + ProductAdapter.class.getSimpleName();

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    private Toast mToast;

    // Use a Cursor so that the adapter can extract the weather information and display
    // them properly.
    private Cursor mCursor;

    /*
     * Below, we've defined an interface to handle clicks on items within this Adapter. In the
     * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
     * said interface. We store that instance in this variable to call the onClickItem method whenever
     * an item is clicked in the list.
     */
    final private ProductRVAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClickItem messages.
     */
    public interface ProductRVAdapterOnClickHandler {
        void onClickItem(int position, int productID);
    }

    /**
     * Constructs a new {@link ProductAdapter}.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public ProductAdapter(@NonNull Context context, ProductRVAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    /**
     * onCreateViewHolder is called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent    The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new {@link ProductViewHolder} that holds the View for each list item
     */
    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(LOG_TAG, "onCreateViewHolder()");
        return new ProductViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
    }

    /**
     * onBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the item
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder     The ViewHolder which should be updated to represent the
     *                   contents of the item at the given position in the data set.
     * @param position   The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Log.d(LOG_TAG, "onBindViewHolder(): Position = " + position);

        if (mCursor == null){
            return;
        }

        holder.bind(position);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our list of items
     */
    @Override
    public int getItemCount() {
        Log.d(LOG_TAG, "getItemCount()");
        try{
            /**
             * If there are no items to display, then display the Empty View!!
             */
            if (mCursor == null || mCursor.getCount() == 0){
                Log.d(LOG_TAG, "getItemCount(): No items to display.");
                // Display the empty view.
                CatalogActivity.mEmptyView.setVisibility(View.VISIBLE);
                return 0;
            }
            /**
             * If there are some items to display, then hide the Empty View and display the items!!
             */
            Log.d(LOG_TAG, String.format("getItemCount(): There are %d items to display.", mCursor.getCount()));
            // Hide the empty view.
            CatalogActivity.mEmptyView.setVisibility(View.GONE);
            return mCursor.getCount();
        }
        catch (NullPointerException ex){
            Log.e(LOG_TAG, "getItemCount(): NullPointerException caught: " + ex);
            return 0;
        }
    }

    /**
     * Swaps the cursor used by the Adapter for its data. This method is called by MainActivity
     * after a load has finished, as well as when the Loader responsible for loading
     * the data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        Log.d(LOG_TAG, "swapCursor()");
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a list item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        /** Tag for the log messages. */
        private final String LOG_TAG = "DEBUGGING " + ProductViewHolder.class.getSimpleName();

        private final Button saleButton;
        private final TextView nameTextView;
        private final TextView priceTextView;
        private final TextView quantityTextView;
        private final int idIndex;
        private final int nameIndex;
        private final int priceIndex;
        private final int quantityIndex;

        private int productID;
        private String productName;
        private int productQTY;

        ProductViewHolder(View view) {
            super(view);

            // Get the TextViews from the ViewHolder object and set the texts.
            saleButton = (Button) view.findViewById(R.id.product_sale);
            nameTextView = (TextView) view.findViewById(R.id.product_name);
            priceTextView = (TextView) view.findViewById(R.id.product_price);
            quantityTextView = (TextView) view.findViewById(R.id.product_quantity);

            // Find the columns of product attributes that we're interested in.
            idIndex = mCursor.getColumnIndexOrThrow(ProductEntry._ID);
            nameIndex = mCursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME);
            priceIndex = mCursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE);
            quantityIndex = mCursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QTY);

            // Set the Listeners.
            view.setOnClickListener(this);
            saleButton.setOnClickListener(this);
        }

        void bind(int listIndex) {
            Log.d(LOG_TAG, "bind(): Position = " + getAdapterPosition());

            // Move the cursor to the passed in position. If "moveToPosition()" returns false, then return.
            if (!mCursor.moveToPosition(listIndex)){
                return;
            }

            // Read the product attributes from the Cursor for the current product.
            productID = mCursor.getInt(idIndex);
            productName = mCursor.getString(nameIndex);
            productQTY = mCursor.getInt(quantityIndex);
            final double productPrice = mCursor.getDouble(priceIndex);

            // Get the TextView from the ViewHolder object and set the the product name.
            nameTextView.setText(productName);

            // Set the currency.
            NumberFormat fmt = NumberFormat.getCurrencyInstance(Locale.ITALY);
            // Get the TextView from the ViewHolder object and set the the price.
            priceTextView.setText(fmt.format(productPrice).toString());

            // Display the quantity of the product.
            displayQTY(mContext, productQTY);
        }

        /**
         * Display the current quantity of the product.
         **/
        private void displayQTY(Context context, int quantity){
            // If the quantity is zero, then...
            if (quantity == 0) {
                // Get the TextView from the ViewHolder object and set the text "Out of stock".
                quantityTextView.setText(context.getString(R.string.out_of_stock));
            }
            else {
                // Get the TextView from the ViewHolder object and set the the quantity.
                quantityTextView.setText(context.getString(R.string.quantity) + " #" + quantity);
            }
        }

        // ***************************************************************************************** //
        // Implementation for the "View.OnClickListener" interface
        // ***************************************************************************************** //

        /**
         * This gets called by the child views during a click.
         *
         * @param view the View that was clicked
         */
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                // If the button "SALE" was clicked, then...
                case R.id.product_sale:
                    // Sale the product.
                    saleProduct();
                    break;
                // If the above case does not match, then the rest of the space in the row was clicked, so...
                default:
                    // Edit the product.
                    editProduct();
                    break;
            }
        }

        /**
         * Method to edit a product.
         */
        private void editProduct(){
            Log.d(LOG_TAG, "editProduct()");
            // Return the Adapter position of the item represented by the ViewHolder,
            // which is the position of the clicked item.
            int adapterPosition = getAdapterPosition();
            // Move the cursor to the position of the clicked item.
            mCursor.moveToPosition(adapterPosition);
            // Call the onClickItem() method of the custom interface.
            mClickHandler.onClickItem(adapterPosition, productID);
        }

        /**
         * Method to sell a product.
         */
        private void saleProduct(){
            Log.d(LOG_TAG, "saleProduct(): Sale button clicked => " + productName);
            // Variable to put the message to display corresponding to availability.
            String message = "";
            // If the item is not out of stock, then...
            if (productQTY > 0) {
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

                Log.d(LOG_TAG, "URI = " + currentProductUri);

                // Update the new quantity of the product in the SQLite Database.
                int rowsAffected = mContext.getContentResolver().update(currentProductUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    message = mContext.getResources().getString(R.string.editor_update_product_failed);
                }
                else {
                    // Display the new quantity of the product.
                    displayQTY(mContext, productQTY - 1);
                    // Otherwise, the update was successful and we can display a toast.
                    message = mContext.getResources().getString(R.string.msg_item_sold, productName);
                }
            }
            else {
                message = mContext.getResources().getString(R.string.msg_item_out_of_stock, productName);
            }

            // If the "mToast" object is not null, then cancel it.
            if (mToast != null){
                mToast.cancel();
            }
            // Instantiate the Toast object.
            mToast = Toast.makeText(
                    mContext,           // Context
                    message,            // Message
                    Toast.LENGTH_SHORT  // Duration
            );
            // Show the toast message.
            mToast.show();
        }
    }
}
