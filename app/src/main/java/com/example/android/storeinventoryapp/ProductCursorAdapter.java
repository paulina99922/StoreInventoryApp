package com.example.android.storeinventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storeinventoryapp.data.ProductContract.ProductEntry;


public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        TextView productName = view.findViewById(R.id.product_name);
        TextView productPrice = view.findViewById(R.id.product_price);
        TextView productQuantity = view.findViewById(R.id.product_quantity);

        String name = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_NAME));
        final int price = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
        final Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, cursor.getInt(cursor
                .getColumnIndex(ProductEntry._ID)));

        productName.setText(name);
        productPrice.setText(context.getString(R.string.price_label) + " " + price);
        productQuantity.setText(quantity + " " + context.getString(R.string.quantity_label));

        Button saleButton = view.findViewById(R.id.sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    int newQuantity = quantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, newQuantity);
                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    Toast.makeText(context, "Product out of stock", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}