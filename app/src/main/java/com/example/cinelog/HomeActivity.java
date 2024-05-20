package com.example.cinelog;

import static android.view.View.GONE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cinelog.databinding.ActivityHomeBinding;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private HomeAdapter adapter;
    private List<Movie> movies;
    private String query;
    private String genre;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    if (data != null) {
                        Map<String, Object> response = (Map<String, Object>) data.getSerializableExtra("query");
                        filter(response);
                    }
                }
            });

    ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    getAllMovies();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new HomeAdapter(movie -> {
            Intent i = new Intent(this, DetailActivity.class);
            i.putExtra("movieId", movie.id);

            detailLauncher.launch(i);
        });
        binding.homeRv.setAdapter(adapter);
        binding.homeRv.setLayoutManager(new LinearLayoutManager(this));

        binding.homeFab.setOnClickListener(view -> {
            Intent i = new Intent(HomeActivity.this, AddActivity.class);
            detailLauncher.launch(i);
        });
        binding.homeSwipe.setOnRefreshListener(() -> {
            getAllMovies();
            binding.homeSwipe.setRefreshing(false);
        });
        binding.btnFilter.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, FilterActivity.class);
            intent.putExtra("title", query);
            intent.putExtra("genre", genre);
            launcher.launch(intent);
        });
        getAllMovies();

    }

    private void getAllMovies() {
        query = null;
        genre = null;

        binding.homeRv.setVisibility(View.VISIBLE);
        binding.tvNotFound.setVisibility(GONE);

        MoviesDatabaseHelper databaseHelper = MoviesDatabaseHelper.getInstance(this);

        movies = databaseHelper.getAllMovies();

        if (movies.isEmpty()) {
            binding.noMovieData.setVisibility(View.VISIBLE);
        } else {
            binding.noMovieData.setVisibility(View.GONE);
        }

        adapter.setMovies(movies);
    }

    private void filter(Map<String, Object> query) {
        this.query = ((String) query.get("title")).toLowerCase();
        this.genre = (String) query.get("genre");

        List<Movie> filtered;
        if (Objects.equals(genre, "All")) {
            filtered = movies.stream().filter(movie ->
                    movie.title.toLowerCase().contains(this.query)).collect(Collectors.toList());
        } else {
            filtered = movies.stream().filter(movie ->
                    movie.title.toLowerCase().contains(this.query) &&
                            Objects.equals(movie.genre, genre)).collect(Collectors.toList());
        }

        if(filtered.isEmpty()) {
            binding.homeRv.setVisibility(GONE);
            binding.tvNotFound.setVisibility(View.VISIBLE);
            binding.noMovieData.setVisibility(View.GONE);
        } else {
            binding.homeRv.setVisibility(View.VISIBLE);
            binding.tvNotFound.setVisibility(GONE);
        }

        adapter.setMovies(filtered);
    }
}