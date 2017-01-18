package com.udacity.suarte.popularmovies.domain;

/**
 * A Converter for a "Starts Rating", providing converting functionality
 * from a given original scale to the desired "Stars Rating" scale.
 */
public class StarsRatingConverter {

    private float originalScaleMaxRating;
    private float starsScaleMaxRating;

    /**
     * Creates an instance of this class.
     * @param originalScaleMaxRating the max rating of the original scale (from where you want to convert to your stars scale)
     * @param starsScaleMaxRating the max rating of your stars scale
     */
    public StarsRatingConverter(float originalScaleMaxRating, float starsScaleMaxRating) {
        this.originalScaleMaxRating = originalScaleMaxRating;
        this.starsScaleMaxRating = starsScaleMaxRating;
    }

    /**
     * Returns the stars rating value for the given original scale rating value
     * @param originalRating The rating value of the original scale rating.
     * @return A float representing the rating value in your stars rating scale.
     */
    public float getStarsRating(float originalRating) {

        float originalPercent = (originalRating * 100) / originalScaleMaxRating;
        float starsRating = (originalPercent * starsScaleMaxRating) / 100;

        return starsRating;
    }
}
