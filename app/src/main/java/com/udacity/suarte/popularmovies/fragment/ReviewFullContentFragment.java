package com.udacity.suarte.popularmovies.fragment;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.suarte.popularmovies.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment to show up the full content of a review
 */
public class ReviewFullContentFragment extends Fragment {

    public static final String ARGUMENT_CONTENT_ID = "reviewContentKey";

    private static final String REVIEW_FULL_CONTENT_STACK = "reviewFullContent";

    private String reviewContent;

    @BindView(R.id.textView_ReviewFullContent)
    TextView textViewContent;

    public ReviewFullContentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getArguments() != null) {
            this.reviewContent = this.getArguments().getString(ARGUMENT_CONTENT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_review_full_content, container, false);

        ButterKnife.bind(this, root);

        this.textViewContent.setText(this.reviewContent);

        return root;
    }

    /**
     * Creates an instance of {@link ReviewFullContentFragment} and replaces it on the given container id.
     * @param reviewContent the full content of a review to be passed to the fragment instance
     * @param context The {@link AppCompatActivity} that is going to add an instance of {@link ReviewFullContentFragment}
     * @param containerId The container Id for replacing.
     */
    public static void createAndReplaceOn(String reviewContent, AppCompatActivity context, int containerId) {

        Bundle args = new Bundle();
        args.putString(ReviewFullContentFragment.ARGUMENT_CONTENT_ID, reviewContent);

        Fragment frag = new ReviewFullContentFragment();
        frag.setArguments(args);

        context.getSupportFragmentManager()
                .beginTransaction()
                .replace(containerId, frag)
                .addToBackStack(REVIEW_FULL_CONTENT_STACK)
                .commit();
    }
}
