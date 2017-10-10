package com.example.android.booklistingapp;

/**
 * Created by Rean on 10/8/2017.
 */

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.booklistingapp.MainActivity.LOG_TAG;

public final class QueryUtils {

    //Create an empty constructor to avoid creating an object of QueryUtils
    private QueryUtils() {
    }

    //A method to convert a string entry to a URL object, if it's valid
    private static URL createURL(String strURL) {

        URL url = null;

        try {
            url = new URL(strURL);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    //A method to implement the HTTP connection
    private static String makeHTTPrequest(URL url) throws IOException {

        String jsonResponse = "";

        if (url == null) return jsonResponse;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {

                Log.e("QueryUtils", "Error, with JSON response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
            if (inputStream != null) inputStream.close();
        }

        return jsonResponse;
    }

    /**
     * Query the Google books dataset and return a list of Books objects.
     */
    public static List<Book> fetchBookData(String requestUrl) {

        // Create URL object
        URL url = createURL(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHTTPrequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link books}s
        List<Book> books = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link books}
        return books;
    }

    //A method to append the string to have a string builder, to then read the input stream
    private static String readFromStream(InputStream inputStream) throws IOException {

        StringBuilder strOutput = new StringBuilder();

        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                strOutput.append(line);
                line = reader.readLine();
            }
        }

        return strOutput.toString();
    }

    //A method to output a multiple authors, if there is more than one author
    public static String formatListOfAuthors(JSONArray authorsList) throws JSONException {

        String authorsAppender = null;

        if (authorsList == null) {
            return "No authors specified";
        }

        for (int i = 0; i < authorsList.length(); i++) {

            if (i == 0) {
                //To avoid having an extra comma at the beginning of the first name
                authorsAppender = authorsList.getString(0);

            } else {
                authorsAppender += ", " + authorsList.getString(i);
            }
        }

        return authorsAppender;
    }

    /**
     * Return a list of Book objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Book> extractFeatureFromJson(String bookJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        List<Book> books = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            // Extract the JSONArray associated with the key called "features",
            // which represents a list of features (or books).
            JSONArray bookArray = baseJsonResponse.getJSONArray("items");

            // For each book in the bookArray, create an {@link book} object
            for (int i = 0; i < bookArray.length(); i++) {

                // Get a single book at position i within the list of books
                JSONObject currentBooks = bookArray.getJSONObject(i);

                // For a given book, extract the JSONObject associated with the
                // key called "properties", which represents a list of all properties
                // for that book.
                JSONObject volumeInfo = currentBooks.getJSONObject("volumeInfo");

                // Extract the value for the key called title
                String title = volumeInfo.getString("title");

                // Extract the value for the key called "place"
                JSONArray authorsArray = volumeInfo.getJSONArray("authors");

                //Extract the author(s) name(s)
                String author = formatListOfAuthors(authorsArray);

                // Create a new {@link Book} object with the magnitude, location, time,
                // and url from the JSON response.
                books.add(new Book(author, title));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Return the list of books
        return books;
    }
}
