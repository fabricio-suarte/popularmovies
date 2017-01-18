package com.udacity.suarte.popularmovies.fragment;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.udacity.suarte.popularmovies.R;
import com.udacity.suarte.popularmovies.core.RepositoryManager;
import com.udacity.suarte.popularmovies.domain.DefaultMovieDataImporterFactory;
import com.udacity.suarte.popularmovies.domain.MovieDataImporter;
import com.udacity.suarte.popularmovies.domain.MovieRepository;

/**
 * A specific {@link CursorAdapter} specialization for loading movies data from a cursor.
 */

public class MoviesAdapter extends CursorAdapter {

    private String imagesRootPath;

    public MoviesAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);

        MovieDataImporter importer = DefaultMovieDataImporterFactory.getInstance()
                .createJsonDataImporter();

        this.imagesRootPath = importer.getImagesRootPath();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        //Creates an view object for an Item in the grid view
        View view = LayoutInflater.from(context).inflate(R.layout.gridview_movie_item, viewGroup, false);

        /*
         Usually, it is a good practice to use a ViewHolder pattern here.
         However, in this case, the created View is the ImageView itself which is used
         for loading the movie poster and does not contains any other View elements inside it.
        */

        return view;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //Let's get the name of the Movie poster image

        MovieRepository.MovieCursorWrapper wrapper = RepositoryManager.getInstance()
                .getRepository(MovieRepository.class)
                .createMovieCursorWrapper(cursor);

        String poster = wrapper.getPoster();

        String posterUrl = Uri.parse(this.imagesRootPath)
                .buildUpon()
                .appendEncodedPath(poster)
                .toString();

        Picasso.with(context)
                .load(posterUrl)
                .placeholder(R.drawable.ic_movie_loading_place_holder)
                .error(R.drawable.ic_movie_error_place_holder)
                .into((ImageView) view);
    }
}
