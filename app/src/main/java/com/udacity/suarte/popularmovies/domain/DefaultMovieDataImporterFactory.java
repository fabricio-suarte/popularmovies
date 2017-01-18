package com.udacity.suarte.popularmovies.domain;


/**
 * A default implementation for {@link MovieDataImporterFactory}
 */
public class DefaultMovieDataImporterFactory implements MovieDataImporterFactory{

    //Singleton support
    private static DefaultMovieDataImporterFactory instance;

    //Instance members
    private MovieDataImporter jsonDataImporter;

    //Private constructor (singleton implementation)
    private DefaultMovieDataImporterFactory() {}

    /**
     * Get the singleton instance of this class
     * @return A instance of DefaultMovieDataImporterFactory
     */
    public static DefaultMovieDataImporterFactory getInstance() {

        if(instance == null)
            instance = new DefaultMovieDataImporterFactory();

        return instance;
    }

    // *** MovieDataImporterFactory interface implementation ***

    @Override
    /**
     * Creates or returns the current instance (if it is already created) for the Json data importer
     */
    public MovieDataImporter createJsonDataImporter() {

        //Creates an instance of TMDbImporter (themoviedb.org service)
        if(this.jsonDataImporter == null)
            this.jsonDataImporter = new TheMovieDbImporter();

        return this.jsonDataImporter;
    }

    // *** End of MovieDataImporterFactory interface implementation ***
}
