package com.itsonin.android.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.itsonin.android.R;
import com.itsonin.android.api.ItsoninAPI;
import com.itsonin.android.entity.Event;
import com.itsonin.android.model.Device;
import com.itsonin.android.model.LocalEvent;
import com.itsonin.android.providers.EventsContentProvider;
import com.itsonin.android.view.EventListCard;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: johnarleyburns
* Date: 3/1/14
* Time: 11:19 PM
* To change this template use File | Settings | File Templates.
*/
public class EventListFragment extends Fragment {

    public static final String EVENT_LIST_DATA_URI = "eventListDataUri";

    private static final String TAG = EventListFragment.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final int EVENTS_LOADER = 0;

    private String[] mProjection = LocalEvent.Events.COLUMNS;
    private Uri mDataUri;
    private AbsListView mListView;
    private SimpleCursorAdapter mAdapter;
    private View mEmptyView;
    private View mWelcomeScreen;
    private ItsoninAPI itsoninAPI;
    private Handler handler;
    private WeakReference<PullToRefreshLayout> mPullToRefreshLayout;
    private boolean scheduleReload;

    public EventListFragment() {
        super();
    }

    public EventListFragment(Uri dataUri) {
        super();
        Bundle b = getArguments();
        if (b == null) {
            b = new Bundle();
        }
        b.putString(EVENT_LIST_DATA_URI, dataUri.toString());
        setArguments(b);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
        setRetainInstance(true);
        scheduleReload = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (DEBUG) Log.i(TAG, "onCreateView()");
        View rootView = inflater.inflate(R.layout.event_list_fragment, container, false);
        Bundle args = getArguments();

        mListView = (AbsListView)rootView.findViewById(R.id.list_view);
        mListView.setOnItemClickListener(eventInfoListener);
        mEmptyView = rootView.findViewById(R.id.empty_message);
        mWelcomeScreen = rootView.findViewById(R.id.welcome_screen);

        String token = Device.load(getActivity()).token;
        if (DEBUG) Log.i(TAG, "token:" + token);
        //if (Device.load(getActivity()).token != null) {
            displayWelcomeScreen();
        //}

        mAdapter = new SimpleCursorAdapter(container.getContext(), EventListCard.list_item_layout, null,
                LocalEvent.Events.COLUMNS, EventListCard.VIEW_IDS,
                Adapter.NO_SELECTION);
        mAdapter.setViewBinder(new EventListCard.EventViewBinder());
        mListView.setAdapter(mAdapter);
        mPullToRefreshLayout = new WeakReference<PullToRefreshLayout>(
                (PullToRefreshLayout)rootView.findViewById(R.id.pull_to_refresh));
        mDataUri = Uri.parse(args.getString(EVENT_LIST_DATA_URI));

        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(pullToRefreshListener)
                .setup(mPullToRefreshLayout.get());

        if (scheduleReload) {
            scheduleReload = false;
            reloadEvents();
        }

        return rootView;
    }

    private void reloadEvents() {
        if (DEBUG) Log.i(TAG, "reloadEvents()");
        if (mPullToRefreshLayout.get() != null) {
            mPullToRefreshLayout.get().setRefreshing(true);
        }
        itsoninAPI.listEvents();
    }

    private OnRefreshListener pullToRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            reloadEvents();
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        itsoninAPI = ItsoninAPI.instance(activity.getApplicationContext());
        itsoninAPI.unregisterReceiver(apiReceiver);
        itsoninAPI.registerReceiver(apiReceiver);
    }

    private void displayWelcomeScreen() {
        mWelcomeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getActionBar().show();
                mWelcomeScreen.setVisibility(View.GONE);
            }
        });
        getActivity().getActionBar().hide();
        mWelcomeScreen.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (DEBUG) Log.i(TAG, "onStart()");
        handler = new Handler();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) Log.i(TAG, "onStop()");
        handler = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (itsoninAPI != null) {
            itsoninAPI.unregisterReceiver(apiReceiver);
            itsoninAPI = null;
        }
    }

    private BroadcastReceiver apiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPullToRefreshLayout.get() != null) {
                mPullToRefreshLayout.get().setRefreshComplete();
            }
            int statusCode = intent.getIntExtra(ItsoninAPI.ITSONIN_API_STATUS_CODE, 0);
            String path = intent.getStringExtra(ItsoninAPI.ITSONIN_API_PATH);
            String response = intent.getStringExtra(ItsoninAPI.ITSONIN_API_RESPONSE);
            if (DEBUG) Log.i(TAG, "received " + statusCode + ": " + response);

            ItsoninAPI.REST rest = ItsoninAPI.REST.valueOfPath(path);
            switch(rest) {
                case LIST_EVENTS:
                    if (isError(statusCode, response)) {
                        mListView.setEmptyView(mEmptyView);
                        notifyAuthenticationError(context);
                    }
                    else {
                        handleListEvents(context, response);
                    }
                    break;
                case CREATE_EVENT:
                    if (!isError(statusCode, response)) {
                        reloadEvents();
                    }
                    break;
                default:
                    if (DEBUG) Log.i(TAG, "ignored rest api: " + rest);
                    break;
            }
        }

        private boolean isError(int statusCode, String response) {
            return response == null || response.isEmpty() || statusCode != 200;
        }

        private void handleListEvents(final Context context, final String response) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Event> events = ItsoninAPI.mapper.readValue(response, new TypeReference<List<Event>>(){});
                        if (DEBUG) Log.i(TAG, "handleListEvents() events count=" + events.size());
                        EventsContentProvider.setDiscoverEvents(events);
                        if (handler != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getLoaderManager().restartLoader(EVENTS_LOADER, null, mLoaderCallbacks);
                                }
                            });
                        }
                    } catch (JsonParseException e) {
                        Log.e(TAG, "Cannot parse JSON results", e);
                        notifyAuthenticationError(context);
                    } catch (JsonMappingException e) {
                        Log.e(TAG, "Cannot map JSON results", e);
                        notifyAuthenticationError(context);
                    } catch (IOException e) {
                        Log.e(TAG, "Cannot read JSON results", e);
                        notifyAuthenticationError(context);
                    }
                }
            }).start();
        }

        private void notifyAuthenticationError(final Context context) {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    };

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
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    };

    private AdapterView.OnItemClickListener eventInfoListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String eventId = String.valueOf(id); // temporary
            Toast.makeText(getActivity(), "event id=" + eventId, Toast.LENGTH_SHORT).show();
            Uri dataUri = LocalEvent.Events.EVENTS_ID_CONTENT_URI.buildUpon().appendPath(eventId).build();
            Fragment fragment = new EventInfoFragment(dataUri);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .addToBackStack(dataUri.toString())
                    .add(R.id.content_frame, fragment)
                    .commit();
        }
    };

    /*
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Get a layout inflater (inflater from getActivity() or getSupportActivity() works as well)
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newView = inflater.inflate(R.layout.event_list_fragment, null);
        // This just inflates the view but doesn't add it to any thing.
        // You need to add it to the root view of the fragment
        ViewGroup rootView = (ViewGroup) getView();
        // Remove all the existing views from the root view.
        // This is also a good place to recycle any resources you won't need anymore
        rootView.removeAllViews();
        rootView.addView(newView);
        // Viola, you have the new view setup
    }
    */
}
