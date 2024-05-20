package com.example.cinelog;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinelog.databinding.ListItemBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {
    protected static OnMovieSelectedListener onMovieSelectedListener;
    private final List<Movie> movies = new ArrayList<>();

    public HomeAdapter(OnMovieSelectedListener onMovieSelectedListener) {
        HomeAdapter.onMovieSelectedListener = onMovieSelectedListener;
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ListItemBinding binding = ListItemBinding.inflate(inflater, parent, false);

        return new HomeHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, int position) {
        holder.bind(movies.get(position));
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMovies(List<Movie> movies) {
        this.movies.clear();
        this.movies.addAll(movies);
        notifyDataSetChanged();
    }

    interface OnMovieSelectedListener {
        public void onClick(Movie movie);
    }

    protected static class HomeHolder extends RecyclerView.ViewHolder {
        private final ListItemBinding binding;

        public HomeHolder(@NonNull ListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Movie movie) {
            binding.itemTitle.setText(movie.title);
            binding.itemGenre.setText(movie.genre);
            binding.itemRating.setText(String.valueOf(movie.rate));
            binding.itemYear.setText(String.valueOf(movie.year));

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String watchDate = format.format(new Date(movie.date));
            binding.itemWatchDate.setText("Watch date: " + watchDate);

            if (movie.rewatch == 0) {
                binding.itemRewawtch.setVisibility(View.GONE);
            } else {
                binding.itemRewawtch.setVisibility(View.VISIBLE);
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(movie.cover, 0, movie.cover.length);
            binding.itemCover.setImageBitmap(bitmap);

            itemView.setOnClickListener(view -> onMovieSelectedListener.onClick(movie));
        }
    }
}
