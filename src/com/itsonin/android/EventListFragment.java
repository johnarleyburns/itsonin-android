package com.itsonin.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

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

    ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(
                R.layout.discover_list_fragment, container, false);
        Bundle args = getArguments();
        ((TextView) rootView.findViewById(R.id.title)).setText(args.getString(PAGE_TITLE));
        ((TextView) rootView.findViewById(R.id.title_desc)).setText(args.getString(PAGE_DESCRIPTION));
        mListView = (ListView)rootView.findViewById(R.id.list_view);
        return rootView;
    }
}
