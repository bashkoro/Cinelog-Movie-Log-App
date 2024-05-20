package com.example.cinelog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.example.cinelog.databinding.ActivityAddBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Calendar;

public class AddActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private ActivityAddBinding binding;
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

        binding = ActivityAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        binding.ib.setOnClickListener(view -> ImagePicker.with(AddActivity.this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start());
        binding.btnSave.setOnClickListener(view -> {
            String title = binding.titleEt.getText().toString();
            String year = binding.yearEt.getText().toString();
            String review = binding.reviewEt.getText().toString();

            if (pickedImage != null &&
                    !title.isEmpty() &&
                    !year.isEmpty() &&
                    pickedDateMillis != null &&
                    !review.isEmpty() &&
                    selectedGenre != null &&
                    binding.rating.getRating() > 0) {

                Bitmap bitmap = null;

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), pickedImage));
                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pickedImage);
                    }
                } catch (Exception e) {
                    Log.d("TAG", e.getMessage());
                    Toast.makeText(this, "Cannot add log", Toast.LENGTH_SHORT).show();
                }

                if (bitmap != null) {
                    MoviesDatabaseHelper db = MoviesDatabaseHelper.getInstance(AddActivity.this);
                    Movie movie = new Movie();
                    movie.rewatch = binding.check.isChecked() ? 1 : 0;
                    movie.rate = binding.rating.getRating();
                    movie.year = Integer.valueOf(year);
                    movie.review = review;
                    movie.title = title;
                    movie.cover = getBytes(bitmap);
                    movie.date = pickedDateMillis;
                    movie.genre = selectedGenre;

                    db.addMovie(movie);

                    Intent i = new Intent();
                    setResult(RESULT_OK, i);
                    finish();

                    Toast.makeText(AddActivity.this, "Movie added!", Toast.LENGTH_SHORT).show();
                }

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