package com.example.cinelog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cinelog.databinding.ActivityEditBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class EditActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private ActivityEditBinding binding;
    private Uri pickedImage;
    private Long pickedDateMillis;
    private String selectedGenre;

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MoviesDatabaseHelper db = MoviesDatabaseHelper.getInstance(this);
        List<Movie> movies = db.getAllMovies();

        int movieId = getIntent().getIntExtra("movieId", -1);
        Movie movie = movies.stream().filter(movie1 ->
                movie1.id == movieId).collect(Collectors.toList()).get(0);

        selectedGenre = movie.genre;
        pickedDateMillis = movie.date;

        Bitmap bitmap = BitmapFactory.decodeByteArray(movie.cover, 0, movie.cover.length);
        binding.ib.setImageBitmap(bitmap);
        binding.titleEt.setText(movie.title);
        binding.check.setChecked(movie.rewatch == 1);
        binding.yearEt.setText(String.valueOf(movie.year));
        binding.reviewEt.setText(movie.review);
        binding.rating.setRating(movie.rate);

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String watchDate = format.format(new Date(movie.date));
        binding.dateEt.setText(watchDate);

        binding.dateEt.setOnClickListener(view -> {
            DatePicker picker = new DatePicker();
            picker.show(getSupportFragmentManager(), "PICK DATE");
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.genres, R.layout.spinner_item);
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

        String[] genres = getResources().getStringArray(R.array.genres);

        for (int i = 0; i < genres.length; i++) {
            if (Objects.equals(genres[i], movie.genre)) {
                binding.spGenre.setSelection(i);
                break;
            }
        }

        binding.ib.setOnClickListener(view -> ImagePicker.with(EditActivity.this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start());

        binding.btnSave.setOnClickListener(view -> {
            String title = binding.titleEt.getText().toString();
            String year = binding.yearEt.getText().toString();
            String review = binding.reviewEt.getText().toString();

            if (!title.isEmpty() &&
                    !year.isEmpty() &&
                    !review.isEmpty() &&
                    binding.rating.getRating() > 0) {

                movie.rewatch = binding.check.isChecked() ? 1 : 0;
                movie.rate = binding.rating.getRating();
                movie.year = Integer.valueOf(year);
                movie.review = review;
                movie.title = title;
                movie.date = pickedDateMillis;
                movie.genre = selectedGenre;

                if (pickedImage != null) {
                    Bitmap pickedBitmap = null;

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            pickedBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), pickedImage));
                        } else {
                            pickedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pickedImage);
                        }
                    } catch (Exception e) {
                        Log.d("TAG", e.getMessage());
                        finish();
                        Toast.makeText(this, "Cannot add log", Toast.LENGTH_SHORT).show();
                    }

                    if (pickedBitmap != null) {
                        movie.cover = getBytes(pickedBitmap);
                    }
                }

                db.updateMovie(movie);

                Intent i = new Intent(EditActivity.this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);

                Toast.makeText(EditActivity.this, "Movie updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Uri uri;

            if (data != null) {
                uri = data.getData();

                if (uri != null) {
                    pickedImage = uri;
                    binding.ib.setImageURI(pickedImage);
                }
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateSet(android.widget.DatePicker datePicker, int i, int i1, int i2) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR, i);
        mCalendar.set(Calendar.MONTH, i1);
        mCalendar.set(Calendar.DAY_OF_MONTH, i2);
        String selectedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(mCalendar.getTime());

        pickedDateMillis = mCalendar.getTimeInMillis();
        binding.dateEt.setText(selectedDate);
    }
}