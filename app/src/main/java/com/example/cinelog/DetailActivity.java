package com.example.cinelog;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cinelog.databinding.ActivityDetailBinding;

import java.util.List;
import java.util.stream.Collectors;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int movieId = getIntent().getIntExtra("movieId", -1);
        if (movieId == -1) {
            finish();
            Toast.makeText(this, "Cannot get detail", Toast.LENGTH_SHORT).show();
        }

        MoviesDatabaseHelper db = MoviesDatabaseHelper.getInstance(this);
        List<Movie> movies = db.getAllMovies();
        Movie movie = movies.stream().filter(movie1 ->
                movie1.id == movieId).collect(Collectors.toList()).get(0);

        Bitmap bitmap = BitmapFactory.decodeByteArray(movie.cover, 0, movie.cover.length);
        binding.cover.setImageBitmap(bitmap);

        binding.title.setText(movie.title);
        binding.year.setText(String.valueOf(movie.year));
        binding.rating.setText(String.valueOf(movie.rate));
        binding.genre.setText(movie.genre);

        if (movie.rewatch == 0) {
            binding.rewatch.setVisibility(View.GONE);
        } else {
            binding.rewatch.setVisibility(View.VISIBLE);
        }

        binding.review.setText(movie.review);
        binding.btnDelete.setOnClickListener(view ->
                showDeleteDialog(movie));
        binding.btnUpdate.setOnClickListener(view -> {
            Intent i = new Intent(DetailActivity.this, EditActivity.class);
            i.putExtra("movieId", movieId);
            startActivity(i);
        });
    }

    private void showDeleteDialog(Movie movie) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Delete");
        alert.setMessage("Are you sure you want to delete?");
        alert.setPositiveButton("Yes", (dialog, which) -> {
            MoviesDatabaseHelper db = MoviesDatabaseHelper.getInstance(DetailActivity.this);
            db.deleteMovie(movie);

            Intent i = new Intent();
            setResult(RESULT_OK, i);
            finish();
        });

        alert.setNegativeButton("No", (dialog, which) ->
                dialog.dismiss());

        alert.show();
    }
}