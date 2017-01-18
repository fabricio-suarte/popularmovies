package com.udacity.suarte.popularmovies.domain;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Helper for building Movie's videos urls
 */

public final class VideoUrlHelper {

    private static final String VIDEO_SITE_YOUTUBE = "youtube";
    private static final Uri YOUTUBE_BASE_URI = Uri.parse("https://www.youtube.com/watch");

    /**
     * Returns the absolute link to a video
     * @param key the key representing the desired video
     * @param site the website where the video is hosted
     * @return Uri
     */
    public static Uri getVideoUri(String key, String site) {
        if(TextUtils.isEmpty(key))
            return null;

        if(TextUtils.isEmpty(site))
            site = VIDEO_SITE_YOUTUBE; //default

        Uri videoUri = null;

        switch (site.toLowerCase()){
            case VIDEO_SITE_YOUTUBE:
                videoUri = YOUTUBE_BASE_URI
                        .buildUpon()
                        .appendQueryParameter("v", key)
                        .build();
                break;

            default:
                throw new UnsupportedOperationException("Unknown Video site: " + site);
        }

        return videoUri;
    }

}
