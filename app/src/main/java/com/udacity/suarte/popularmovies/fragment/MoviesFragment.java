package com.udacity.suarte.popularmovies.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.udacity.suarte.popularmovies.R;
import com.udacity.suarte.popularmovies.core.RepositoryManager;
import com.udacity.suarte.popularmovies.domain.MovieRepository;
import com.udacity.suarte.popularmovies.domain.UserProfile;

/**
 * A Movies {@link Fragment} for listing movies posters in a thumbnails fashion.
 * Activities that contain this fragment must implement the
 * {@link CallBackListener} interface
 * to handle interaction events.
 */
public class MoviesFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //region constants

    private static final String TAG = MoviesFragment.class.getCanonicalName();
    private static int MOVIES_LOADER = 1;
    private static final String GRIDVIEW_CURRENT_POS_KEY = "gridview_current_pos_key";

    //endregion

    private CallBackListener listener;
    private MoviesAdapter adapter;
    private GridView gridView;
    private int gridViewCurrentPos;

    private String mostPopularSettingsValue;
    private String highestRatedSettingsValue;

    public MoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

        //Initializes these members so that it is not necessary to get this from resources
        //every time a reload is made due to a settings change...
        this.mostPopularSettingsValue = this.getString(R.string.pref_sort_criteria_most_popular_value);
        this.highestRatedSettingsValue = this.getString(R.string.pref_sort_criteria_highest_rated_value);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        //Creates the instance of the adapter (at first set to no cursor),
        //just for being able to attach it to the Grid View
        this.adapter = new MoviesAdapter(this.getActivity(), null, 0);

        this.gridView = (GridView) rootView.findViewById(R.id.gridView_movies);
        this.gridView.setAdapter(this.adapter);

        //Set the item click listener for the gridview
        this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                gridViewCurrentPos = position;

                if(listener != null) {
                    listener.onMovieSelected(id);
                }
            }
        });

        //Loads specific state data from a saved instance
        if( savedInstanceState != null)
            this.loadFromSavedInstance(savedInstanceState);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //Let's set the loader
        this.getLoaderManager().initLoader(MOVIES_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if(this.gridViewCurrentPos != ListView.INVALID_POSITION )
        {
            outState.putInt(GRIDVIEW_CURRENT_POS_KEY, this.gridViewCurrentPos);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CallBackListener) {
            this.listener = (CallBackListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CallBackListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
        this.gridView = null;
        this.adapter = null;
    }

    //region 'LoaderManager.LoaderCallbacks<Cursor>' implementation


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.d(TAG, "onCreateLoader is running...");

        MovieRepository repository
                = RepositoryManager.getInstance().getRepository(MovieRepository.class);

        //Let's get the current sort criteria setting
        MovieRepository.SortOrder sortOrder;

        UserProfile profile = new UserProfile(this.getActivity());
        String currentValue = profile.getCurrentSortOrderCriteriaValue();
        boolean onlyFavorites = profile.isOnlyFavoritesChecked();

        //Let's assume "" as default to Most popular...
        if(currentValue.equals("") || currentValue.equals(this.mostPopularSettingsValue)) {

            sortOrder = MovieRepository.SortOrder.MOST_POPULAR;
        }
        else if(currentValue.equals(this.highestRatedSettingsValue)) {

            sortOrder = MovieRepository.SortOrder.HIGHEST_RATED;
        }
        else {
            throw new RuntimeException("Unknown sort criteria: " + currentValue);
        }

        CursorLoader loader
                = repository.createCursorLoaderForAvailableMovies(sortOrder, onlyFavorites);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(loader.getId() == MOVIES_LOADER) {

            Log.d(TAG, "onLoadFinished for Movies loader...");

            //Get the loaded cursor and set it to the adapter
            this.adapter.swapCursor(data);

            //Check if we have a current position
            if (this.gridViewCurrentPos != GridView.INVALID_POSITION)
                this.gridView.smoothScrollToPosition(this.gridViewCurrentPos);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.adapter.swapCursor(null);
    }

    //endregion

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface CallBackListener {
        void onMovieSelected(long movieId);
    }

    //region public methods

    /**
     * Used to notify this instance about movies loading criteria changes
     */
    public void onLoadingCriteriaChanged() {

        this.getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
    }

    //endregion

    //region private aux methods

    //Loads specific data which has been saved on "onSaveInstanceState"
    private void loadFromSavedInstance(Bundle savedInstance) {

        //The current grid position
        if(savedInstance.containsKey(GRIDVIEW_CURRENT_POS_KEY))
            this.gridViewCurrentPos = savedInstance.getInt(GRIDVIEW_CURRENT_POS_KEY);
    }

    //endregion
}
