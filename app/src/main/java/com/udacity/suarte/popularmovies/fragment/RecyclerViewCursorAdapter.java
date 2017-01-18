package com.udacity.suarte.popularmovies.fragment;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

/**
 * A very simple abstraction of an CursorAdapter for using with RecyclerView
 */

public abstract class RecyclerViewCursorAdapter extends RecyclerView.Adapter {

    //region Attributes

    protected Cursor cursor;
    private Observer observer;

    //endregion

    //region constructor

    public RecyclerViewCursorAdapter() {
        this.observer = new Observer();
    }

    //endregion

    //region inner classes

    private class Observer extends DataSetObserver {

        @Override
        public void onChanged() {
            //Attention! this method comes from the main class "RecyclerView.Adapter"
            notifyDataSetChanged();

            super.onChanged();
        }

        @Override
        public void onInvalidated() {

            //Attention! this method comes from the base class "RecyclerView.Adapter"
            notifyDataSetChanged();

            super.onInvalidated();
        }
    }

    //endregion

    //region overrides of base class methods

    @Override
    public int getItemCount() {
        int count = 0;

        if(this.cursor != null)
            count = this.cursor.getCount();

        return count;
    }

    //endregion

    //region methods interface of this class

    /**
     * Swaps data from the given cursor to this adapter
     * @param cursor a cursor containing videos data
     */
    public void swapToCursor(Cursor cursor) {

        //First, let's ensure that any previous cursor instance is treated properly.
        if(this.cursor != null) {
            this.cursor.close();
            this.unregisterObserver();

            this.cursor = null;
        }

        this.cursor = cursor;
        this.registerObserver();

        //Notify this adapter that data changed...
        this.notifyDataSetChanged();
    }

    //endregion

    //region private aux methods

    private void unregisterObserver() {
        if(this.cursor != null && this.observer != null)
            this.cursor.unregisterDataSetObserver(this.observer);
    }

    private void registerObserver() {
        if(this.cursor != null && this.observer != null)
            this.cursor.registerDataSetObserver(this.observer);
    }

    //endregion
}
