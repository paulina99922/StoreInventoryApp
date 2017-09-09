package com.example.android.storeinventoryapp;

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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storeinventoryapp.data.ProductContract.ProductEntry;


public class EditorActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private final static int SELECT_PICTURE = 200;

    private Uri mCurrentProductUri;
    private Uri mImageUri;

    private String mName;
    private int mPrice;
    private int mQuantity;

    private Button mIncreaseButton1;
    private Button mDecreaseButton1;
    private Button mIncreaseButton2;
    private Button mDecreaseButton2;
    private Button mPictureButton;
    private ImageView mProductPicture;
    private EditText mNameEditText;
    private TextView mPriceTextView;
    private TextView mQuantityTextView;
    private Button orderButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.add_new_product));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_existing_product));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
        initialiseView();
        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mPriceTextView = (TextView) findViewById(R.id.edit_price);
        mQuantityTextView = (TextView) findViewById(R.id.edit_quantity);
        mProductPicture = (ImageView) findViewById(R.id.picture);

    }

    private void initialiseView() {

        orderButton = (Button) findViewById(R.id.order);
        orderButton.setVisibility(View.VISIBLE);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = getString(R.string.message_email) +
                        "\n" + mName;
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:supplier@onlinestore.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.subject_email));
                intent.putExtra(Intent.EXTRA_TEXT, message);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mPriceTextView = (TextView) findViewById(R.id.edit_price);
        mQuantityTextView = (TextView) findViewById(R.id.edit_quantity);

        mIncreaseButton1 = (Button) findViewById(R.id.button_plus_p);
        mIncreaseButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrice++;
                mPriceTextView.setText(String.valueOf(mPrice));
            }
        });

        mDecreaseButton1 = (Button) findViewById(R.id.button_minus_p);
        mDecreaseButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPrice > 0) {
                    mPrice--;
                    mPriceTextView.setText(String.valueOf(mPrice));
                } else {
                    Toast.makeText(EditorActivity.this, "Invalid price", Toast.LENGTH_SHORT).show();
                }
            }
        });


        mIncreaseButton2 = (Button) findViewById(R.id.button_plus_q);
        mIncreaseButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuantity++;
                mQuantityTextView.setText(String.valueOf(mQuantity));
            }
        });

        mDecreaseButton2 = (Button) findViewById(R.id.button_minus_q);
        mDecreaseButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantity > 0) {
                    mQuantity--;
                    mQuantityTextView.setText(String.valueOf(mQuantity));
                } else {
                    Toast.makeText(EditorActivity.this, "Invalid quantity", Toast.LENGTH_SHORT)
                            .show();

                }
            }
        });

        mProductPicture = (ImageView) findViewById(R.id.image);
        mPictureButton = (Button) findViewById(R.id.button_picture);
        mPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mImageUri = resultData.getData();
                mProductPicture.setImageURI(mImageUri);
            }
        }
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    private void saveProduct() {

        boolean nameNotSelected = checkFieldEmpty(mNameEditText.getText().toString().trim());
        boolean priceNotSelected = checkFieldEmpty(mPriceTextView.getText().toString().trim());
        boolean quantityNotSelected = checkFieldEmpty(mQuantityTextView.getText().toString().trim
                ());

        if (nameNotSelected || priceNotSelected || quantityNotSelected || mImageUri == null) {
            Toast.makeText(this, getString(R.string.invalid_product), Toast.LENGTH_SHORT).show();
        } else {
            String nameString = mNameEditText.getText().toString().trim();
            String priceString = mPriceTextView.getText().toString().trim();
            String quantityString = mQuantityTextView.getText().toString().trim();
            String imageString = mImageUri.toString();

            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_NAME, nameString);
            values.put(ProductEntry.COLUMN_PRICE, priceString);
            values.put(ProductEntry.COLUMN_QUANTITY, quantityString);
            values.put(ProductEntry.COLUMN_PICTURE, imageString);

            if (mCurrentProductUri == null) {

                Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
                if (newUri == null) {
                    Toast.makeText(this, getString(R.string.insert_product_failed), Toast
                            .LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.insert_product_successful), Toast
                            .LENGTH_SHORT).show();
                }
                finish();
            } else {
                int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null,
                        null);


                if (rowsAffected == 0) {
                    Toast.makeText(this, getString(R.string.update_product_failed), Toast
                            .LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.update_product_successful), Toast
                            .LENGTH_SHORT).show();
                }
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveProduct();
                return true;
            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PICTURE};

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            mName = data.getString(data.getColumnIndex(ProductEntry.COLUMN_NAME));
            mPrice = data.getInt(data.getColumnIndex(ProductEntry.COLUMN_PRICE));
            mQuantity = data.getInt(data.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
            mImageUri = Uri.parse(data.getString(data.getColumnIndex(ProductEntry.COLUMN_PICTURE)));

            mNameEditText.setText(mName);
            mPriceTextView.setText(String.valueOf(mPrice));
            mQuantityTextView.setText(String.valueOf(mQuantity));
            mProductPicture.setImageURI(mImageUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener
                                                  discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.finish_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_product_failed), Toast
                        .LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_product_successful), Toast
                        .LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private boolean checkFieldEmpty(String string) {
        return TextUtils.isEmpty(string) || string.equals("");
    }

}
