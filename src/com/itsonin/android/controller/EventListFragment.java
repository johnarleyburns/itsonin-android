package com.itsonin.android.controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
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
import com.itsonin.android.model.LocalEvent;
import com.itsonin.android.providers.EventsContentProvider;
import com.itsonin.android.view.EventCard;
import org.json.JSONException;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: johnarleyburns
* Date: 3/1/14
* Time: 11:19 PM
* To change this template use File | Settings | File Templates.
*/
public class EventListFragment extends Fragment {


    public static final String EVENT_DATA_URI = "eventDataUri";

    private static final String TAG = EventListFragment.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final int EVENTS_LOADER = 0;

    private String[] mProjection = LocalEvent.Events.COLUMNS;
    private Uri mDataUri;
    private AbsListView mListView;
    private SimpleCursorAdapter mAdapter;
    private View mEmptyView;
    private ItsoninAPI itsoninAPI;
    private Handler handler;
    private PullToRefreshLayout mPullToRefreshLayout;

    public EventListFragment() {
        super();
    }

    public EventListFragment(Uri dataUri) {
        super();
        Bundle b = getArguments();
        if (b == null) {
            b = new Bundle();
        }
        b.putString(EVENT_DATA_URI, dataUri.toString());
        setArguments(b);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.event_list_fragment, container, false);
        Bundle args = getArguments();

        mListView = (AbsListView)rootView.findViewById(R.id.list_view);
        mAdapter = new SimpleCursorAdapter(container.getContext(), EventCard.list_item_layout, null,
                LocalEvent.Events.COLUMNS, EventCard.VIEW_IDS,
                Adapter.NO_SELECTION);
        mAdapter.setViewBinder(new EventCard.EventViewBinder());
        mListView.setAdapter(mAdapter);
        mEmptyView = rootView.findViewById(R.id.empty_message);
        mPullToRefreshLayout = (PullToRefreshLayout)rootView.findViewById(R.id.pull_to_refresh);
        mDataUri = Uri.parse(args.getString(EVENT_DATA_URI));

        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(pullToRefreshListener)
                .setup(mPullToRefreshLayout);

        reloadEvents();

        return rootView;
    }

    private void reloadEvents() {
        mPullToRefreshLayout.setRefreshing(true);
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
        itsoninAPI = new ItsoninAPI(activity.getApplicationContext());
        itsoninAPI.registerReceiver(apiReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        handler = new Handler();
    }

    @Override
    public void onStop() {
        super.onStop();
        handler = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (itsoninAPI != null) {
            itsoninAPI.unregisterReceiver(apiReceiver);
            itsoninAPI.onDestroy();
            itsoninAPI = null;
        }
    }

    private BroadcastReceiver apiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mPullToRefreshLayout.setRefreshComplete();
            int statusCode = intent.getIntExtra(ItsoninAPI.ITSONIN_API_STATUS_CODE, 0);
            String path = intent.getStringExtra(ItsoninAPI.ITSONIN_API_PATH);
            String response = intent.getStringExtra(ItsoninAPI.ITSONIN_API_RESPONSE);
            if (DEBUG) Log.i(TAG, "received " + statusCode + ": " + response);

            if (response == null || response.isEmpty() || statusCode != 200) {
                Log.e(TAG, "Empty response statusCode=" + statusCode);
                notifyAuthenticationError(context);
                return;
            }

            ItsoninAPI.REST rest = ItsoninAPI.REST.valueOfPath(path);
            switch(rest) {
                case LIST_EVENTS:
                    handleListEvents(context, response);
                    break;
                case CREATE_EVENT:
                    reloadEvents();
                    break;
                default:
                    if (DEBUG) Log.i(TAG, "ignored rest api: " + rest);
                    break;
            }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.event_list_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.add_event:
                CreateEventDialogFragment d = new CreateEventDialogFragment();
                d.show(getFragmentManager(), TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);
            if (mListView.getEmptyView() == null)
                mListView.setEmptyView(mEmptyView);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }
    };

}
