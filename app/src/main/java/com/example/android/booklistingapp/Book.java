package com.example.android.booklistingapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rean on 10/08/2017.
 */

public class Book implements Parcelable {

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
    private String mAuthor;
    private String mTitle;

    public Book(String author, String title) {

        mAuthor = author;
        mTitle = title;
    }

    protected Book(Parcel in) {
        mAuthor = in.readString();
        mTitle = in.readString();
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mAuthor);
        parcel.writeString(mTitle);
    }
}
