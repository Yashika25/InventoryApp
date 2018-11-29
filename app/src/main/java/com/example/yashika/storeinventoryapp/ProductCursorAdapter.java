package com.example.yashika.storeinventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yashika.storeinventoryapp.data.ProductContract;
import com.example.yashika.storeinventoryapp.data.ProductContract.ProductEntry;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {


    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        Button button = view.findViewById(R.id.sale);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        String productQuantity = cursor.getString(quantityColumnIndex);

        if (TextUtils.isEmpty(productQuantity)) {
            productQuantity = context.getString(R.string.unknown_quantity);
        }

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        summaryTextView.setText("₹ " + productPrice);
        quantityTextView.setText(productQuantity + context.getString(R.string.quantity_in_stock));

        //On clicking sale button quantity should decrease by 1
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View parentRow = (View) view.getParent();
                ListView listView = (ListView) parentRow.getParent();

                //Position is the position of button which starts from 0
                int position = listView.getPositionForView(parentRow);

                int keyColumnIndex = cursor.getColumnIndex(ProductEntry._ID);

                try {
                    cursor.moveToFirst();
                } catch (IllegalStateException e) {
                    Log.e(LOG_TAG, "Attempt failed to re-open an already-closed cursor object");
                    return;
                }

                long key = 0;

                //Gets the row ID or PRIMARY_KEY of database into key Int variable.
                for (int i = 1; i <= position + 1; i++) {
                    key = cursor.getLong(keyColumnIndex);
                    cursor.moveToNext();
                }

                Log.v(LOG_TAG, "Current row ID or Primary key of database is: " + key);

                //Moving the cursor to the position where the button was clicked.
                cursor.moveToPosition(position);
                int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);

                //currentQuantity
                String currentQuantity = cursor.getString(quantityColumnIndex);
                int currentQuantityInt = Integer.parseInt(currentQuantity);

                if (currentQuantityInt > 0) {
                    currentQuantityInt -= 1;
                    Log.v(LOG_TAG, "Current quantity is: " + currentQuantityInt + " and changing to: " + currentQuantityInt);
                    ContentValues values = new ContentValues();
                    values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, currentQuantityInt);
                    Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, key);
                    context.getContentResolver().update(currentProductUri, values, null, null);
                } else {
                    Log.v(LOG_TAG, "Quantity cannot be decreased further.");
                }
            }
        });

    }
}