package com.example.cinelog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinelog.databinding.ActivityFilterBinding;

import java.util.HashMap;
import java.util.Objects;

public class FilterActivity extends AppCompatActivity {
    private ActivityFilterBinding binding;

    private String selectedGenre = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFilterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String title = getIntent().getStringExtra("title");
        String genre = getIntent().getStringExtra("genre");

        if (title != null) {
            binding.homeEt.setText(title);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_genres, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spGenre.setAdapter(adapter);
        binding.spGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedGenre = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        if (genre != null) {
            String[] genres = getResources().getStringArray(R.array.filter_genres);

            for (int i = 0; i < genres.length; i++) {
                if (Objects.equals(genres[i], genre)) {
                    binding.spGenre.setSelection(i);
                    break;
                }
            }
        }

        binding.btnApply.setOnClickListener(view -> {
            HashMap<String, Object> query = new HashMap<String, Object>() {{
                put("title", binding.homeEt.getText().toString());
                put("genre", selectedGenre);
            }};

            Intent i = new Intent();
            i.putExtra("query", query);
            setResult(RESULT_OK, i);
            finish();
        });
    }
}