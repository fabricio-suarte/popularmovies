package com.udacity.suarte.popularmovies.domain;

/**
 * Basic interface for a Factory implementation that should create MovieDataImporter instances
 */
public interface MovieDataImporterFactory {

    /**
     * Creates the importer that supports json format
     * @return A instance of MovieDataImporter
     */
    MovieDataImporter createJsonDataImporter();
}