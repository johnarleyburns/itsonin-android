package com.itsonin.android.controller;

import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.TextView;
import com.itsonin.android.R;
import com.itsonin.android.model.Event;
import com.itsonin.android.view.EventCard;

/**
* Created with IntelliJ IDEA.
* User: johnarleyburns
* Date: 3/1/14
* Time: 11:19 PM
* To change this template use File | Settings | File Templates.
*/
public class EventListFragment extends Fragment {

    public static final String PAGE_TITLE = "pageTitle";
    public static final String PAGE_DESCRIPTION = "pageDescription";
    public static final String EVENT_CATEGORY = "eventCategory";

    private static final int EVENTS_LOADER = 0;

    private String[] mProjection = Event.Events.COLUMNS;
    private Uri mDataUri;
    private String mEventCategory;
    private ListView mListView;
    private CursorAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.event_list_fragment, container, false);
        Bundle args = getArguments();
        //((TextView) rootView.findViewById(R.id.title)).setText(args.getString(PAGE_TITLE));
        //((TextView) rootView.findViewById(R.id.title_desc)).setText(args.getString(PAGE_DESCRIPTION));

        mListView = (ListView)rootView.findViewById(R.id.list_view);
        mAdapter = new SimpleCursorAdapter(container.getContext(), EventCard.list_item_layout, null,
                Event.Events.COLUMNS, EventCard.VIEW_IDS,
                Adapter.NO_SELECTION);
        mListView.setAdapter(mAdapter);

        mDataUri = Event.Events.CONTENT_URI;
        mEventCategory = args.getString(EVENT_CATEGORY);
        getLoaderManager().initLoader(EVENTS_LOADER, null, mLoaderCallbacks);

        return rootView;
    }

    LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case EVENTS_LOADER:
                    return new CursorLoader(
                            getActivity(),   // Parent activity context
                            mDataUri,        // Table to query
                            mProjection,     // Projection to return
                            null,            // No selection clause
                            null,            // No selection arguments
                            null             // Default sort order
                    );
                default:
                    // An invalid id was passed in
                    return null;
            } }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    };

}
