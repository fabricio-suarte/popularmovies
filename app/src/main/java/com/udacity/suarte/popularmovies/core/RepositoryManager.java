package com.udacity.suarte.popularmovies.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps Domain repository interfaces to its implementation and manages repository instantiation.
 */

public final class RepositoryManager {

    private static RepositoryManager instance;

    //region members

    private Map<Object, Object> repositoryMaps = new HashMap<>();

    //endregion

    //region constructor
    private RepositoryManager() {

    }

    //endregion

    //region public methods

    /**
     * Returns the singleton instance of this class
     * @return RepositoryManager
     */
    public static RepositoryManager getInstance() {
        if( instance == null) {
            instance = new RepositoryManager();
        }

        return instance;
    }

//    /**
//     * Initializes the instance passing the Android application context
//     * @param context Android application context
//     */
//    public void init(Context context) {
//        ArgumentHelper.validateNull(context, "context");
//
//        this.context = context;
//    }

    /**
     * Maps a repository contract class (interface) to an implementation instance
     * @param contractClass
     * @param repoImplementation
     */
    public <C> void addRepositoryMap(Class<C> contractClass, C repoImplementation) {
        ArgumentHelper.validateNull(contractClass, "repoContract");
        ArgumentHelper.validateNull(repoImplementation, "repoImplementation");

        this.repositoryMaps.put(contractClass, repoImplementation);
    }

    /**
     * Get the current implementation for the given repository interface class
     * @param contractClass The repository contract (interface) class
     * @param <C> The generics type of the interface
     * @return C
     */
    public <C> C getRepository(Class<C> contractClass) {

        Object obj = this.repositoryMaps.get(contractClass);
        if(obj == null)
            return null;

        return (C) obj;
    }

    //endregion

    //region private aux methods

    /*private void validateInitialization() {

        if(this.context == null)
            throw new RuntimeException("This instance has not been initialized properly! Call init(Context) before trying to use this method.");
    }*/

    //endregion
}
