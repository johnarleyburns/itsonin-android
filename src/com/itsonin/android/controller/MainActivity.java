package com.itsonin.android.controller;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.*;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.itsonin.android.R;
import com.itsonin.android.enums.ItsoninWebRoot;
import com.itsonin.android.model.ItsoninWebUri;
import com.itsonin.android.model.LocalEvent;

import java.util.*;

public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final CharSequence DEFAULT_LOCATION = "DÜSSELDORF";

    private String[] mDrawerArray;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mDrawerSubtitle;
    private CharSequence mDrawerLastTitle;
    private CharSequence mTitle;
    private CharSequence mSubtitle;
    private SimpleAdapter mDrawerAdapter;
    private int[] mDrawerDrawables = {
        R.drawable.binoculars,
        R.drawable.men,
        R.drawable.door_open,
        R.drawable.email,
        R.drawable.gear,
        R.drawable.speech_bubble_ellipsis,
        R.drawable.money_bag
    };

    private static final String ROW_ID = "rowid";
    private static final String TEXT = "description";
    private static final String DRAWABLE_ID = "drawableid";

    private static final String[] adapterFrom = {
            ROW_ID,
            TEXT,
            DRAWABLE_ID
    };

    private static final int[] adapterTo = {
            R.id.drawer_list_item,
            R.id.drawer_list_item_text,
            R.id.drawer_list_item_icon
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        createDrawer();
        handleIntent(getIntent());
    }

    //private boolean hasFragment() {
    //    FrameLayout frame = (FrameLayout)findViewById(R.id.content_frame);
    //    return frame != null && frame.getChildCount() > 0;
    //}

   private void clearBackStack() {
       FragmentManager manager = getSupportFragmentManager();
       if (manager.getBackStackEntryCount() > 0) {
           FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
           manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
       }
   }

   private void showEventListFragment(Uri dataUri) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new EventListFragment(dataUri), dataUri.toString())
                .commit();
    }

    private void showSettingsFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsFragment())
                .commit();
    }

    private CharSequence getSubtitle() {
        return DEFAULT_LOCATION; // harcoded for now
    }

    private void createDrawer() {
        mDrawerArray = getResources().getStringArray(R.array.drawer_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mTitle = mDrawerTitle = mDrawerLastTitle = getTitle();
        mDrawerSubtitle = getSubtitle();
        mSubtitle = getSubtitle();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_navigation_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                getActionBar().setSubtitle(mSubtitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                getActionBar().setSubtitle(mDrawerSubtitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < mDrawerArray.length; i++) {
            String drawerText = mDrawerArray[i];
            int drawableId = mDrawerDrawables[i];
            if (DEBUG) Log.i(TAG, "row=" + i + " description=" + drawerText + " drawableId=" + drawableId);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(ROW_ID, "" + i);
            map.put(TEXT, drawerText);
            map.put(DRAWABLE_ID, "" + drawableId);
            fillMaps.add(map);
        }
        mDrawerAdapter = new SimpleAdapter(MainActivity.this, fillMaps, R.layout.drawer_list_item, adapterFrom, adapterTo) {
            @Override
            public boolean isEnabled(int position) {
                return true;
            }
        };
        mDrawerAdapter.setViewBinder(mViewBinder);
        mDrawerList.setAdapter(mDrawerAdapter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.event_list_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        else {
            switch (item.getItemId()) {
                case R.id.add_event:
                    CreateEventDialogFragment d = new CreateEventDialogFragment();
                    d.show(getSupportFragmentManager(), TAG);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private static final int DISCOVER_POSITION = 0;
    private static final int PRIVATE_POSITION = 1;
    private static final int SETTINGS_POSITION = 2;
    private static final int SEND_FEEDBACK_POSITION = 3;
    private static final int PURCHASE_POSITION = 4;

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

        // Create a new fragment and specify the planet to show based on position
        clearBackStack();
        switch (position) {
            default:
            case DISCOVER_POSITION:
                showEventListFragment(LocalEvent.Events.EVENTS_CONTENT_URI);
                setDrawerSelected(position);
                break;
            case PRIVATE_POSITION:
                showEventListFragment(LocalEvent.Events.EVENTS_PRIVATE_CONTENT_URI);
                setDrawerSelected(position);
                break;
            case SETTINGS_POSITION:
                showSettingsFragment();
                setDrawerSelected(position);
                break;
            case SEND_FEEDBACK_POSITION:
                SendFeedback.email(this);
                break;
            case PURCHASE_POSITION:
                Toast.makeText(this, R.string.not_implemented, Toast.LENGTH_SHORT).show();
                break;

        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void setDrawerSelected(int position) {
        mDrawerList.setItemChecked(position, true);
        CharSequence subtitle = position == DISCOVER_POSITION ? mDrawerSubtitle : null;
        setTitle(mDrawerArray[position], subtitle);
        mDrawerLastTitle = mDrawerTitle;
    }

    @Override
    public void setTitle(CharSequence title) {
        setTitle(title, getSubtitle());
    }

    private void setTitle(CharSequence title, CharSequence subtitle) {
        if (DEBUG) Log.i(TAG, "setTitle() title=" + title + " sub=" + subtitle);
        mTitle = title;
        mSubtitle = subtitle;
        if (mSubtitle == null) {
            getActionBar().setSubtitle(null);
        }
        else {
            getActionBar().setSubtitle(mSubtitle);
        }
        getActionBar().setTitle(mTitle);
    }

    private SimpleAdapter.ViewBinder mViewBinder = new SimpleAdapter.ViewBinder() {
        private int pickSelector(String item) {
            int selector;
            if (item != null && item.equals(mDrawerLastTitle))
                selector = R.drawable.drawer_list_selector_checked_bg;
            else
                selector = R.drawable.drawer_list_selector_inverse_bg;
            return selector;
        }
        public boolean setViewValue(View view, Object data, String textRepresentation) {
            if (view.getId() != R.id.drawer_list_item)
                return false;
            // find item
            int pos = Integer.valueOf((String)data);
            Map<String, String> item = (Map<String, String>)mDrawerAdapter.getItem(pos);
            String drawerText = item.get(TEXT);
            int drawableId = Integer.valueOf(item.get(DRAWABLE_ID));
            //int selector = pickSelector(drawerText);
            //FrameLayout child = (FrameLayout)view.findViewById(R.id.frame_child);
            //Drawable selectorDrawable = getLayoutInflater().getContext().getResources().getDrawable(selector);
            //child.setForeground(selectorDrawable);

            // set title state
            ImageView icon = (ImageView)view.findViewById(R.id.drawer_list_item_icon);
            TextView text = (TextView)view.findViewById(R.id.drawer_list_item_text);
            TextView title = (TextView)view.findViewById(R.id.drawer_list_item_title);
            TextView detail = (TextView)view.findViewById(R.id.drawer_list_item_detail);
            View divider = view.findViewById(R.id.drawer_list_item_divider);

            if (drawableId > 0) {
                title.setText("");
                detail.setText("");
                text.setText(drawerText);
                icon.setVisibility(View.VISIBLE);
                text.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
                detail.setVisibility(View.GONE);
            }

            return true;
        }
    };

    protected void onNewIntent(final Intent intent) {
        handleIntent(intent);
    }

    protected void handleIntent(final Intent intent) {
        if (!handleSpecificIntent(intent)) {
            clearBackStack();
            showEventListFragment(LocalEvent.Events.EVENTS_CONTENT_URI);
            setDrawerSelected(DISCOVER_POSITION);
        }
    }

    protected boolean handleSpecificIntent(final Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, "Searching for " + query, Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            final List<String> segments = intent.getData().getPathSegments();
            ItsoninWebUri uri = ItsoninWebUri.parse(segments);
            if (DEBUG) Log.i(TAG, "parsed event uri=" + uri);
            switch (uri.pathRoot) {
                case EVENT:
                //    return true;
                case INVITE:
                    Uri dataUri = LocalEvent.Events.EVENTS_ID_CONTENT_URI
                            .buildUpon()
                            .appendPath(String.valueOf(uri.eventId))
                            .build();
                    Uri listUri = LocalEvent.Events.EVENTS_CONTENT_URI;
                    Fragment backFrag = getSupportFragmentManager().findFragmentByTag(listUri.toString());
                    if (backFrag == null) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, new EventListFragment(listUri), listUri.toString());
                        ft.commit();
                    }
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.add(R.id.content_frame, new EventInfoFragment(dataUri), dataUri.toString());
                    ft.addToBackStack(dataUri.toString());
                    ft.commit();
                    return true;
                case WELCOME:
                case OTHER:
                default:
                    return false;
            }
        }
        return false;
    }

    /*
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof EditText) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            Log.d("Activity", "Touch event "+event.getRawX()+","+event.getRawY()+" "+x+","+y+" rect "+w.getLeft()+","+w.getTop()+","+w.getRight()+","+w.getBottom()+" coords "+scrcoords[0]+","+scrcoords[1]);
            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }
        return ret;
    }
    */

}