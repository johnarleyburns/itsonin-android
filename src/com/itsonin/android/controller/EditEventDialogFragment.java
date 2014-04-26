package com.itsonin.android.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.itsonin.android.R;
import com.itsonin.android.api.ItsoninAPI;
import com.itsonin.android.entity.Event;
import com.itsonin.android.model.Category;
import com.itsonin.android.model.Host;
import com.itsonin.android.model.LocalEvent;
import com.itsonin.android.model.Place;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 4/23/14
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditEventDialogFragment extends CreateEventDialogFragment {

    protected static final String TAG = EditEventDialogFragment.class.toString();
    protected static final boolean DEBUG = true;

    protected static final int DIALOG_LAYOUT_ID = R.layout.edit_event_layout;

    @Override
    protected void createDialog(View view) {
        dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    protected View inflateLayout() {
        return getActivity().getLayoutInflater().inflate(DIALOG_LAYOUT_ID, null);
    }

    protected void setListeners() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Cancel event!!!", Toast.LENGTH_SHORT).show();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localEvent = extractEvent();
                if (DEBUG) Log.i(TAG, localEvent.toString());
                Context context = getActivity();
                if (context != null) {
                    persistToPrefs(context);
                }

                if (itsoninAPI == null) {
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                }
                else {
                    overlay.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    itsoninAPI.updateEvent(localEvent);
                }
            }
        });
    }

}
