package com.udacity.suarte.popularmovies.fragment;

import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.suarte.popularmovies.R;
import com.udacity.suarte.popularmovies.core.RepositoryManager;
import com.udacity.suarte.popularmovies.domain.MovieRepository;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An RecyclerView.Adapter implementation for listing videos of an specific Movie
 */

public class VideosAdapter extends RecyclerViewCursorAdapter {

    //region Attributes

    private View.OnClickListener videoPlayClickListener;

    //endregion

    //region constructors

    /**
     * Instantiates a regular instance of this adapter
     */
    public VideosAdapter() {

    }

    //endregion

    //region inner classes
    /**
     * The ViewHolder implementation for this Adapter
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textView_VideoName) TextView textViewName;
        @BindView(R.id.imageButton_VideoPlay) AppCompatImageButton buttonPlay;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    //endregion

    //region overrides of base class methods
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_video_item, parent, false);

        RecyclerView.ViewHolder vh = new ViewHolder(item);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(this.cursor == null)
            return;

        if(this.cursor.moveToPosition(position)) {

            MovieRepository repository = RepositoryManager.getInstance()
                    .getRepository(MovieRepository.class);

            MovieRepository.VideoCursorWrapper wrapper
                    = repository.createVideoCursorWrapper(this.cursor);

            ViewHolder myHolder = (ViewHolder) holder;
            myHolder.textViewName.setText( wrapper.getName() );

            myHolder.buttonPlay.setTag(R.id.videoPlayKeyTag, wrapper.getKey());
            myHolder.buttonPlay.setTag(R.id.videoPlaySiteTag, wrapper.getSite());

            //Set the click on the video play button
            if(this.videoPlayClickListener != null) {
                myHolder.buttonPlay.setOnClickListener(this.videoPlayClickListener);
            }
        }
    }

    //endregion

    /**
     * Set the OnClickListener for listening on "video play" button clicks
     * @param listener View.OnClickListener implementation
     */
    public void setVideoPlayOnClickListener(View.OnClickListener listener) {
        this.videoPlayClickListener = listener;
    }
}
