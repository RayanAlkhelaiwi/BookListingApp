package com.example.android.booklistingapp;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    public static final String LOG_TAG = MainActivity.class.getName();
    //Loader ID
    private static final int BOOKS_LOADER_ID = 1;
    private static final String SAVED_INSTANCE = "BookListing";
    /**
     * JSON response for a Google Books API
     */
    private static final String JSON_RESPONSE_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private ListView booksListView;
    private View progressBar;
    private String jsonUrl;
    private EditText editText;
    private Button searchButton;
    private List<Book> bookList;
    private TextView emptyTextView;
    private InputMethodManager inputManager;
    private ConnectivityManager cm;
    private NetworkInfo activeNetwork;
    private LoaderManager loaderManager;
    /**
     * Adapter for the list of books
     */
    private BookAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Initializing the variables inside the onCreate method
         *
         */

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //Get connection information
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //Initializing progress bar with its ID
        progressBar = findViewById(R.id.progress_bar);

        //Initializing edit text with its ID
        editText = (EditText) findViewById(R.id.search_edit_text);

        // Find a reference to the ListView in the layout
        booksListView = (ListView) findViewById(R.id.list);

        //Assigning List to an ArrayList of tybe Book
        bookList = new ArrayList<Book>();

        // Create a new adapter that takes an empty list of books as input
        mAdapter = new BookAdapter(this, bookList);

        // Set the adapter on the ListView so the list can be populated in the user interface
        booksListView.setAdapter(mAdapter);

        //Set empty for the listView if there is no results
        booksListView.setEmptyView(findViewById(android.R.id.empty));

        //Set the search button to be clickable to do the search for the entered word
        searchButton = (Button) findViewById(R.id.search_button);

        // Get a reference to the LoaderManager, in order to interact with loaders.
        loaderManager = getLoaderManager();
        loaderManager.initLoader(BOOKS_LOADER_ID, null, MainActivity.this);

        //To get the saved instance of the app, that keep its data upon rotation
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_INSTANCE)) {
            bookList = savedInstanceState.getParcelableArrayList(SAVED_INSTANCE);
        } else {

            //Populate the data
            bookList = new ArrayList<Book>();
            mAdapter = new BookAdapter(this, bookList);
            booksListView.setAdapter(mAdapter);
        }

        //Set onClick for the search button to initiate the search in the query
        searchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //Set progress bar visible to indicate the search is in process
                progressBar.setVisibility(View.VISIBLE);

                //To hide the keyboard when the Search button is clicked (Better UX?)
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                //Add the user's search to the JSON response
                jsonUrl = JSON_RESPONSE_URL + editText.getText().toString().trim().replace(" ", "+");

                try {
                    activeNetwork = cm.getActiveNetworkInfo();
                    //Check if there is connection
                    if (activeNetwork != null && activeNetwork.isConnected()) {

                        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
                        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
                        // because this activity implements the LoaderCallbacks interface).
                        loaderManager.restartLoader(BOOKS_LOADER_ID, null, MainActivity.this);
                    } else {

                        //Clear the adapter when an attempt to re-search is an option
                        mAdapter.clear();

                        View progressBar = findViewById(R.id.progress_bar);
                        progressBar.setVisibility(View.GONE);

                        emptyTextView = (TextView) findViewById(android.R.id.empty);
                        emptyTextView.setText(R.string.no_internet);
                    }
                } catch (NetworkOnMainThreadException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //To save the instance of the app, to keep its data upon rotation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVED_INSTANCE, (ArrayList<? extends Parcelable>) bookList);
    }

    //Used loader instead of AsyncTask to prevent the continuous fetch of the data from JSON response and avoid memory leaks
    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        return new BooksLoader(this, jsonUrl);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> data) {

        progressBar.setVisibility(View.GONE);

        emptyTextView = (TextView) findViewById(android.R.id.empty);
        emptyTextView.setText(R.string.empty_view_text);

        // Clear the adapter of previous book data
        mAdapter.clear();

        // If there is a valid list of Books, then add them to the adapter's data set. This will trigger the ListView to update.
        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
            emptyTextView.setText(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        mAdapter.clear();
    }


    private static class BooksLoader extends AsyncTaskLoader<List<Book>> {

        /**
         * Query URL
         */
        private String mUrl;

        public BooksLoader(Context context, String url) {
            super(context);
            mUrl = url;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        public List<Book> loadInBackground() {
            if (mUrl == null) {
                return null;
            }

            List<Book> result = QueryUtils.fetchBookData(mUrl);
            return result;
        }

    }
}
