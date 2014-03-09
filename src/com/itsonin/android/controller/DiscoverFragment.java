package com.itsonin.android.controller;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import com.itsonin.android.R;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 3/1/14
 * Time: 8:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class DiscoverFragment extends Fragment {

    private static final String TAG = DiscoverFragment.class.getSimpleName();
    private static final boolean DEBUG = false;

    DiscoverPagerAdapter mPagerAdapter;
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_pager, container, false);
        mViewPager = (ViewPager) v.findViewById(R.id.pager);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        mPagerAdapter =
                new DiscoverPagerAdapter(
                        getActivity().getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.discover_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.add_event:
                new AddEventDialogFragment().show(getFragmentManager(), TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class DiscoverPagerAdapter extends FragmentStatePagerAdapter {

        private String[] mPageTitles;
        private String[] mPageDescriptions;
        private String[] mPageCategories;

        public DiscoverPagerAdapter(FragmentManager fm) {
            super(fm);
            mPageTitles = getResources().getStringArray(R.array.discover_page_titles);
            mPageDescriptions = getResources().getStringArray(R.array.discover_page_descriptions);
            mPageCategories = getResources().getStringArray(R.array.discover_page_event_categories);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DiscoverEventListFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putString(DiscoverEventListFragment.PAGE_TITLE, mPageTitles[i]);
            args.putString(DiscoverEventListFragment.PAGE_DESCRIPTION, mPageDescriptions[i]);
            args.putString(DiscoverEventListFragment.EVENT_CATEGORY, mPageCategories[i]);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return mPageTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPageTitles[position];
        }
    }

}

