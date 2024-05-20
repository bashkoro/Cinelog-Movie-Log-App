package com.example.cinelog;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MoviesDatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "moviesDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_MOVIES = "movies";

    // Movie Table Columns
    private static final String KEY_MOVIE_ID = "id";
    private static final String KEY_MOVIE_TITLE = "title";
    private static final String KEY_MOVIE_YEAR = "year";
    private static final String KEY_MOVIE_GENRE = "genre";
    private static final String KEY_MOVIE_DATE = "date";
    private static final String KEY_MOVIE_RATE = "rate";
    private static final String KEY_MOVIE_REVIEW = "review";
    private static final String KEY_MOVIE_REWATCH = "rewatch";
    private static final String KEY_MOVIE_COVER = "cover";

    private static MoviesDatabaseHelper sInstance;

    private MoviesDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized MoviesDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MoviesDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_MOVIES +
                "(" +
                KEY_MOVIE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Define a primary key
                KEY_MOVIE_TITLE + " TEXT, " +
                KEY_MOVIE_GENRE + " TEXT, " +
                KEY_MOVIE_RATE + " REAL, " +
                KEY_MOVIE_DATE + " INTEGER, " +
                KEY_MOVIE_YEAR + " INTEGER, " +
                KEY_MOVIE_REWATCH + " INTEGER, " +
                KEY_MOVIE_REVIEW + " TEXT, " +
                KEY_MOVIE_COVER + " BLOB" +
                ")";

        db.execSQL(CREATE_POSTS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOVIES);
            onCreate(db);
        }
    }

    @SuppressLint("Range")
    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();

        String POSTS_SELECT_QUERY =
                String.format("SELECT * FROM %s", TABLE_MOVIES);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Movie movie = new Movie();
                    movie.id = cursor.getInt(cursor.getColumnIndex(KEY_MOVIE_ID));
                    movie.title = cursor.getString(cursor.getColumnIndex(KEY_MOVIE_TITLE));
                    movie.year = cursor.getInt(cursor.getColumnIndex(KEY_MOVIE_YEAR));
                    movie.genre = cursor.getString(cursor.getColumnIndex(KEY_MOVIE_GENRE));
                    movie.rate = cursor.getInt(cursor.getColumnIndex(KEY_MOVIE_RATE));
                    movie.date = cursor.getLong(cursor.getColumnIndex(KEY_MOVIE_DATE));
                    movie.rewatch = cursor.getInt(cursor.getColumnIndex(KEY_MOVIE_REWATCH));
                    movie.review = cursor.getString(cursor.getColumnIndex(KEY_MOVIE_REVIEW));
                    movie.cover = cursor.getBlob(cursor.getColumnIndex(KEY_MOVIE_COVER));

                    movies.add(movie);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return movies;
    }

    public void addMovie(Movie movie) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_MOVIE_TITLE, movie.title);
            values.put(KEY_MOVIE_COVER, movie.cover);
            values.put(KEY_MOVIE_DATE, movie.date);
            values.put(KEY_MOVIE_GENRE, movie.genre);
            values.put(KEY_MOVIE_RATE, movie.rate);
            values.put(KEY_MOVIE_REVIEW, movie.review);
            values.put(KEY_MOVIE_REWATCH, movie.rewatch);
            values.put(KEY_MOVIE_YEAR, movie.year);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_MOVIES, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }

    public int updateMovie(Movie movie) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MOVIE_TITLE, movie.title);
        values.put(KEY_MOVIE_COVER, movie.cover);
        values.put(KEY_MOVIE_DATE, movie.date);
        values.put(KEY_MOVIE_GENRE, movie.genre);
        values.put(KEY_MOVIE_RATE, movie.rate);
        values.put(KEY_MOVIE_REVIEW, movie.review);
        values.put(KEY_MOVIE_REWATCH, movie.rewatch);
        values.put(KEY_MOVIE_YEAR, movie.year);

        return db.update(TABLE_MOVIES, values, KEY_MOVIE_ID + " = ?", new String[]{String.valueOf(movie.id)});

    }

    public void deleteMovie(Movie movie) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MOVIES, KEY_MOVIE_ID + " = ?", new String[]{String.valueOf(movie.id)});
    }
}