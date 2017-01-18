package com.udacity.suarte.popularmovies.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.udacity.suarte.popularmovies.R;
import com.udacity.suarte.popularmovies.core.RepositoryManager;
import com.udacity.suarte.popularmovies.domain.MovieRepository;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An RecyclerView.Adapter implementation for listing reviews of an specific Movie
 */

public class ReviewsAdapter extends RecyclerViewCursorAdapter {

    //region constants

    private static String AUTHOR_FORMAT = "by \"%s\"";
    private static String SHORT_CONTENT_FORMAT = "%s ...";
    private static int SHORT_CONTENT_MAX_LENGTH = 100;

    //endregion

    //region Attributes

    private View.OnClickListener viewFullReviewClickListener;

    //endregion

    /**
     * Instantiates a regular instance of this adapter
     */
    public ReviewsAdapter() {
    }

    //region inner classes
    /**
     * The ViewHolder implementation for this Adapter
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textView_ReviewContent)
        TextView textViewReviewContent;

        @BindView(R.id.textView_ReviewAuthor)
        TextView textViewReviewAuthor;

        @BindView(R.id.imageButton_ReviewFullContent)
        ImageButton imageButtonReviewFullContent;

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
                .inflate(R.layout.recyclerview_review_item, parent, false);

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

            MovieRepository.ReviewCursorWrapper wrapper
                    = repository.createReviewCursorWrapper(this.cursor);

            ViewHolder myHolder = (ViewHolder) holder;

            String fullContent = wrapper.getReviewContent();
            String shortContent = this.getReviewShortTex( fullContent );

            myHolder.textViewReviewContent.setText( shortContent );

            //Let's set the view tag property, so it can be hold the full content to be shown
            //if user clicks on "full review" button.
            myHolder.imageButtonReviewFullContent.setTag(R.id.reviewFullContentTag, fullContent);

            if(this.viewFullReviewClickListener != null)
                myHolder.imageButtonReviewFullContent.setOnClickListener(this.viewFullReviewClickListener);

            String formattedAuthor = String.format(AUTHOR_FORMAT, wrapper.getAuthor() );
            myHolder.textViewReviewAuthor.setText( formattedAuthor );
        }
    }

    //endregion

    //region methods interface of this class

    /**
     * Set the OnClickListener for listening on "view full review" button clicks
     * @param listener View.OnClickListener implementation
     */
    public void setViewFullReviewOnClickListener(View.OnClickListener listener) {
        this.viewFullReviewClickListener = listener;
    }

    //endregion

    //region private aux methods

    private String getReviewShortTex(String reviewContent) {

        if(reviewContent == null)
            return "";

        String shortContent;

        if(reviewContent.length() <= SHORT_CONTENT_MAX_LENGTH) {
            shortContent = reviewContent;
        }
        else {

            //Let's look for a space character...
            int finalIndex = SHORT_CONTENT_MAX_LENGTH -1;

            char c;
            while( (c = reviewContent.charAt(finalIndex)) != ' ' && finalIndex < reviewContent.length() -1 ) {
                finalIndex++;
            }

            if(finalIndex < reviewContent.length()-1 ) {
                shortContent = String.format(SHORT_CONTENT_FORMAT, reviewContent.substring(0, finalIndex));
            }
            else {
                //well... that is weird. It means that we could not find a space character in the text.
                //Let's cut at it anyway...
                shortContent = String.format(SHORT_CONTENT_FORMAT, reviewContent.substring(0, SHORT_CONTENT_MAX_LENGTH -1));
            }
        }

        return shortContent;
    }

    //endregion
}
