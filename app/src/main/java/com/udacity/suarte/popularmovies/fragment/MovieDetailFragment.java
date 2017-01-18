package com.udacity.suarte.popularmovies.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.suarte.popularmovies.R;
import com.udacity.suarte.popularmovies.core.ArgumentHelper;
import com.udacity.suarte.popularmovies.core.RepositoryManager;
import com.udacity.suarte.popularmovies.domain.DefaultMovieDataImporterFactory;
import com.udacity.suarte.popularmovies.domain.MovieDataImporter;
import com.udacity.suarte.popularmovies.domain.MovieDataImporterFactory;
import com.udacity.suarte.popularmovies.domain.MovieRepository;
import com.udacity.suarte.popularmovies.domain.StarsRatingConverter;
import com.udacity.suarte.popularmovies.domain.VideoUrlHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A Movie details {@link Fragment} subclass.
 */
public class MovieDetailFragment extends Fragment
                                 implements LoaderManager.LoaderCallbacks<Cursor>,
                                            View.OnClickListener {
    //region constants

    private static final String LOG_TAG = MovieDetailFragment.class.getCanonicalName();

    private static final int MOVIE_LOADER = 1;
    private static final int VIDEOS_LOADER = 2;
    private static final int REVIEWS_LOADER = 3;
    private static final String ARGUMENT_MOVIE_ID = "movieID";
    private static final String CURRENT_Y_SCROLL_POSITION_ID = "y_scrollPositionId";

    //The display date format for "release date"
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    //endregion

    //region members

    private long currentMovieId;
    private CallBackListener listener;

    private View rootView;
    private int currentYScrollViewPosition;

    @BindView(R.id.textView_Title)  TextView textViewTitle;
    @BindView(R.id.imageView_Poster)  ImageView imageViewPoster;
    @BindView(R.id.textView_Synopsis)  TextView textViewSynopsis;
    @BindView(R.id.textView_UserRating)  TextView textViewUserRating;
    @BindView(R.id.ratingBar_UserRating)  RatingBar ratingBarUserRating;
    @BindView(R.id.textView_ReleaseDate)  TextView textViewReleaseDate;
    @BindView(R.id.imageButton_Favorite)  ImageButton imageButtonFavorite;

    private RecyclerView recyclerViewVideos;
    private RecyclerView recyclerViewReviews;

    //The "videos" and "reviews" Adapters
    private VideosAdapter videosAdapter;
    private ReviewsAdapter reviewsAdapter;

    //endregion

    //region constructor

    public MovieDetailFragment() {
        // Required empty public constructor
    }

    //endregion

    //region public interfaces

    /**
     * Callback interface for this Fragment callback events.
     */
    public interface CallBackListener {

        /**
         * User has clicked in the button for seeing a review content, fully.
         * @param fullContent
         */
        void onReviewFullContent(String fullContent);
    }

    //endregion

    //region overrides on the base class Fragment

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "Running 'onCreate'");

        if (this.getArguments() != null) {

            //set the current movie Id
            this.currentMovieId = this.getArguments().getLong(ARGUMENT_MOVIE_ID);

            if(savedInstanceState != null)
                this.currentYScrollViewPosition
                        = savedInstanceState.getInt(CURRENT_Y_SCROLL_POSITION_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG, "Running 'onCreateView'");

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        this.rootView = root;

        ButterKnife.bind(this, root);

        //Setting the Videos recycler view
        this.recyclerViewVideos = (RecyclerView) root.findViewById(R.id.recyclerView_Videos);
        this.recyclerViewVideos.setHasFixedSize(true);
        this.recyclerViewVideos.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        this.recyclerViewVideos.setItemAnimator(new DefaultItemAnimator());

        //This Fragment implements View.OnClickListener. It is given to "VideosAdapter" as the
        //"videoPlayClickListener". In other words, this fragment registers itself as a listener
        //for play video button clicks.
        this.videosAdapter = new VideosAdapter();
        this.videosAdapter.setVideoPlayOnClickListener(this);
        this.recyclerViewVideos.setAdapter(this.videosAdapter);

        //Setting the Reviews recycler view
        this.recyclerViewReviews = (RecyclerView) root.findViewById(R.id.recyclerView_Reviews);
        this.recyclerViewReviews.setHasFixedSize(true);
        this.recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        //this.recyclerViewReviews.setItemAnimator(new DefaultItemAnimator());

        //This Fragment implements View.OnClickListener. It is given to "ReviewsAdapter" as the
        //"viewFullReviewClickListener". In other words, this fragment register itself as a listener
        //for view ful review button clicks.
        this.reviewsAdapter = new ReviewsAdapter();
        this.reviewsAdapter.setViewFullReviewOnClickListener(this);
        this.recyclerViewReviews.setAdapter(this.reviewsAdapter);

        //The favorite start OnClickListener
        this.imageButtonFavorite.setOnClickListener(this);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        Log.d(LOG_TAG, "Running 'onActivityCreated'");

        //Let's set the loaders

        if(this.currentMovieId > 0) {

            this.getLoaderManager().initLoader(MOVIE_LOADER, null, this);
            this.getLoaderManager().initLoader(VIDEOS_LOADER, null, this);
            this.getLoaderManager().initLoader(REVIEWS_LOADER, null, this);

        }

        //Verifies if there was a previous vertical scroll position
        if(this.currentYScrollViewPosition > 0) {

            this.rootView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rootView.scrollTo(0, currentYScrollViewPosition);
                }
            }, 100);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof MovieDetailFragment.CallBackListener) {
            this.listener = (CallBackListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement CallBackListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        this.listener = null;
        this.videosAdapter = null;
        this.reviewsAdapter = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if(outState != null && this.rootView != null)
            outState.putInt(CURRENT_Y_SCROLL_POSITION_ID,
                            this.rootView.getVerticalScrollbarPosition());

       super.onSaveInstanceState(outState);
    }

    //endregion

    //region LoaderManager.LoaderCallbacks<Cursor> implementation

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //As there is only one loader in this fragment, it is not necessary checking the Id.
        MovieRepository repository = RepositoryManager.getInstance()
                .getRepository(MovieRepository.class);

        CursorLoader loader = null;

        switch (id) {
            case MOVIE_LOADER:

                loader = repository.createCursorLoaderForMovieDetail(this.currentMovieId);
                break;

            case VIDEOS_LOADER:
                loader = repository.createCursorLoaderForMovieVideos(this.currentMovieId);
                break;

            case REVIEWS_LOADER:
                loader = repository.createCursorLoaderForMovieReviews(this.currentMovieId);
                break;

            default:
                this.throwUnknownLoader(id);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data == null|| !data.moveToFirst())
            return;

        int loaderId = loader.getId();
        switch (loaderId) {
            case MOVIE_LOADER:

                this.onLoadFinishedForMovie(data);
                break;

            case VIDEOS_LOADER:
                this.onLoadFinishedForVideos(data);
                break;

            case REVIEWS_LOADER:
                this.onLoadFinishedForReviews(data);
                break;

            default:
                this.throwUnknownLoader(loaderId);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        int loaderId = loader.getId();
        if(loaderId == VIDEOS_LOADER) {
            this.videosAdapter.swapToCursor(null);
        }

    }

    //endregion

    //region View.OnClickListener implementation

    @Override
    public void onClick(View view) {

        if(view == null)
            return;

        int id = view.getId();
        switch (id) {
            case R.id.imageButton_VideoPlay:

                this.onPlayVideoClick(view);

                break;

            case R.id.imageButton_ReviewFullContent:

                this.onReviewFullContentClick(view);
                break;

            case R.id.imageButton_Favorite:

                this.onFavoriteClick(view);
                break;
        }
    }

    //endregion

    //region public methods

    /**
     * Creates a new instance of {@link MovieDetailFragment} for an specific movie
     * @param movieId the movie id
     * @return {@link MovieDetailFragment}
     */
    public static MovieDetailFragment newInstance(long movieId) {

        ArgumentHelper.validateLessOrEqualToN(movieId, "movieId", 0);

        Bundle args = new Bundle();
        args.putLong(ARGUMENT_MOVIE_ID, movieId);

        MovieDetailFragment frag = new MovieDetailFragment();
        frag.setArguments(args);

        return frag;
    }

    //endregion

    //region private aux methods

    private void onPlayVideoClick(View view ){

        String key = (String) view.getTag(R.id.videoPlayKeyTag);
        String site = (String) view.getTag(R.id.videoPlaySiteTag);

        Uri videoUri = null;

        try {
            videoUri = VideoUrlHelper.getVideoUri(key, site);
        }
        catch (Exception ex){
            Log.e(LOG_TAG, "It was not possible to get the video url", ex);
        }

        if(videoUri != null) {
            Intent videoIntent
                    = new Intent(Intent.ACTION_VIEW, videoUri);

            try {
                this.startActivity(videoIntent);
            }
            catch (ActivityNotFoundException ex) {
                Log.e(LOG_TAG, "It was not possible to play a video!", ex);
                Toast message = Toast
                        .makeText(this.getActivity(),
                                this.getString(R.string.cant_play_video_message),
                                Toast.LENGTH_LONG);

                message.show();
            }
        }
    }

    private void onReviewFullContentClick(View view ){

        //Let's get the review full content
        String fullContent = (String) view.getTag(R.id.reviewFullContentTag);

        if(this.rootView != null && this.rootView instanceof NestedScrollView) {
            this.currentYScrollViewPosition
                    = this.rootView.getScrollY();
        }

        if(this.listener != null)
            this.listener.onReviewFullContent(fullContent);
    }

    private void onFavoriteClick(View view) {

        if(this.currentMovieId <= 0)
            return;

        //Toggling in the repository...
        MovieRepository repository = RepositoryManager.getInstance()
                .getRepository(MovieRepository.class);

        repository.toggleMovieFavorite(this.currentMovieId);

        //Toggling the interface...
        ImageButton myFavoriteImageButton = (ImageButton) view;
        boolean isFavorite = (boolean) myFavoriteImageButton.getTag(R.id.favoriteCheckValueTag);

        if(isFavorite) {
            //Change to not favorite (toggling...)
            myFavoriteImageButton.setBackgroundResource(R.drawable.ic_not_favorite);
        }
        else {
            myFavoriteImageButton.setBackgroundResource(R.drawable.ic_favorite);
        }
    }

    private void onLoadFinishedForMovie(Cursor data) {

        MovieRepository.MovieCursorWrapper wrapper = RepositoryManager.getInstance()
                .getRepository(MovieRepository.class)
                .createMovieCursorWrapper(data);

        //Title
        this.textViewTitle.setText( wrapper.getTitle() );

        //Poster
        MovieDataImporterFactory importerFactory = DefaultMovieDataImporterFactory.getInstance();
        MovieDataImporter importer = importerFactory.createJsonDataImporter();

        String poster = wrapper.getPoster();
        String posterUrl = Uri.parse( importer.getImagesRootPath() )
                .buildUpon()
                .appendEncodedPath( poster )
                .toString();

        Picasso.with(this.getContext())
                .load(posterUrl)
                .placeholder(R.drawable.ic_movie_loading_place_holder)
                .error(R.drawable.ic_movie_error_place_holder)
                .into( this.imageViewPoster );

        //Synopsis
        this.textViewSynopsis.setText( wrapper.getSynopsis() );

        //User rating
        float userRating = wrapper.getUserRating();

        int originalMaxRating = this.getResources().getInteger(R.integer.TMDb_max_rating_scale);
        int starsMaxRating = this.getResources().getInteger(R.integer.rating_bar_default_stars);

        StarsRatingConverter converter = new StarsRatingConverter(originalMaxRating, starsMaxRating );
        float starsRating = converter.getStarsRating(userRating);

        this.textViewUserRating.setText( String.format(Locale.US, "%.2f/%d", userRating, originalMaxRating) );
        this.ratingBarUserRating.setRating( starsRating );

        //Favorite
        int favoriteValue = wrapper.getFavorite();
        if(favoriteValue == 1) {
            this.imageButtonFavorite.setBackgroundResource(R.drawable.ic_favorite);
            this.imageButtonFavorite.setTag(R.id.favoriteCheckValueTag, true);
        }
        else {
            this.imageButtonFavorite.setBackgroundResource(R.drawable.ic_not_favorite);
            this.imageButtonFavorite.setTag(R.id.favoriteCheckValueTag, false);
        }

        //Release date
        Date date = new Date( wrapper.getReleaseDate() );
        String releaseDate = DISPLAY_DATE_FORMAT.format(date);

        this.textViewReleaseDate.setText( releaseDate );

    }

    private void onLoadFinishedForVideos(Cursor data) {
        this.videosAdapter.swapToCursor(data);
    }

    private void onLoadFinishedForReviews(Cursor data) {
        this.reviewsAdapter.swapToCursor(data);
    }

    private void throwUnknownLoader(int loaderId) {
        throw new UnsupportedOperationException("Unknown loader id! " + String.valueOf(loaderId));
    }

    //endregion
}
