package com.itsonin.android;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    private String[] mDrawerArray;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mDrawerLastTitle;
    private CharSequence mTitle;
    private SimpleAdapter mDrawerAdapter;
    private int[] mDrawerDrawables = {
        R.drawable.men,
        R.drawable.men,
        R.drawable.men,
        R.drawable.men,
        R.drawable.gear,
        R.drawable.speech_bubble_ellipsis,
        R.drawable.money_bag
    };

    private static final String ROW_ID = "rowid";
    private static final String TEXT = "text";
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
    }

    private void createDrawer() {
        mDrawerArray = getResources().getStringArray(R.array.drawer_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mTitle = mDrawerTitle = mDrawerLastTitle = getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
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
            if (DEBUG) Log.i(TAG, "row=" + i + " text=" + drawerText + " drawableId=" + drawableId);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private static final int DISCOVER_POSITION = 0;
    private static final int EVENTS_POSITION = 1;
    private static final int CALENDAR_POSITION = 2;
    private static final int LOCATIONS_POSITION = 3;
    private static final int SETTINGS_POSITION = 4;
    private static final int SEND_FEEDBACK_POSITION = 5;
    private static final int PURCHASE_POSITION = 6;

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {

        // Create a new fragment and specify the planet to show based on position
        switch (position) {
            default:
            case DISCOVER_POSITION:
                showDiscoverFragment(position);
                break;
            case EVENTS_POSITION:
                Toast.makeText(this, R.string.not_implemented, Toast.LENGTH_SHORT).show();
                break;
            case CALENDAR_POSITION:
                Toast.makeText(this, R.string.not_implemented, Toast.LENGTH_SHORT).show();
                break;
            case LOCATIONS_POSITION:
                Toast.makeText(this, R.string.not_implemented, Toast.LENGTH_SHORT).show();
                break;
            case SETTINGS_POSITION:
                Toast.makeText(this, R.string.not_implemented, Toast.LENGTH_SHORT).show();
                break;
            case SEND_FEEDBACK_POSITION:
                SendFeedback.email(this);
                break;
            case PURCHASE_POSITION:
                Toast.makeText(this, R.string.not_implemented, Toast.LENGTH_SHORT).show();
                break;

        }
        showDiscoverFragment(position);

        // Highlight the selected item, update the title, and close the drawer

        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerArray[position]);
        mDrawerLastTitle = mDrawerTitle;
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void showDiscoverFragment(int position) {
        Fragment fragment = new DiscoverFragment();
        //Bundle args = new Bundle();
        //args.putInt(DiscoverFragment.ARG_PLANET_NUMBER, position);
        //fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

    }



    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
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

}