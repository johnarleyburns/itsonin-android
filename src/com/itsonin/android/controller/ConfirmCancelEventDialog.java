package com.itsonin.android.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.itsonin.android.R;
import com.itsonin.android.api.ItsoninAPI;
import com.itsonin.android.entity.ApiError;
import com.itsonin.android.model.LocalEvent;

/**
* Created with IntelliJ IDEA.
* User: johnarleyburns
* Date: 4/27/14
* Time: 2:56 PM
* To change this template use File | Settings | File Templates.
*/
public class ConfirmCancelEventDialog extends DialogFragment {

    private static final String TAG = ConfirmCancelEventDialog.class.getSimpleName();
    private static final boolean DEBUG = true;

    private LocalEvent localEvent;
    private TextView cancelButton;
    private TextView saveButton;
    protected View overlay;
    protected ProgressBar progressBar;
    protected ItsoninAPI itsoninAPI;

    public ConfirmCancelEventDialog() {
    }

    public ConfirmCancelEventDialog(LocalEvent e) {
        localEvent = e;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (localEvent == null) {
            return null;
        }

        final View view = getActivity().getLayoutInflater().inflate(R.layout.cancel_event_layout, null);
        cancelButton = (TextView)view.findViewById(R.id.event_dialog_action_cancel_button);
        saveButton = (TextView)view.findViewById(R.id.event_dialog_action_save_button);
        overlay = view.findViewById(R.id.overlay);
        progressBar = (ProgressBar)view.findViewById(R.id.progress);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        itsoninAPI = ItsoninAPI.instance(activity.getApplicationContext());
        itsoninAPI.unregisterReceiver(apiReceiver);
        itsoninAPI.registerReceiver(apiReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (itsoninAPI != null) {
            itsoninAPI.unregisterReceiver(apiReceiver);
            itsoninAPI = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itsoninAPI == null) {
                    Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_SHORT).show();
                }
                else {
                    overlay.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    itsoninAPI.cancelEvent(localEvent);
                }
            }
        });
    }

    protected BroadcastReceiver apiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            overlay.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            int statusCode = intent.getIntExtra(ItsoninAPI.ITSONIN_API_STATUS_CODE, 0);
            String path = intent.getStringExtra(ItsoninAPI.ITSONIN_API_PATH);
            String response = intent.getStringExtra(ItsoninAPI.ITSONIN_API_RESPONSE);
            if (DEBUG) Log.i(TAG, "received " + statusCode + ": " + response);

            ItsoninAPI.REST rest = ItsoninAPI.REST.valueOfPath(path);
            switch(rest) {
                case CANCEL_EVENT:
                    if (isError(statusCode, response)) {
                        notifyAuthenticationError(context, response);
                    }
                    else {
                        dismiss();
                    }
                    break;
                default:
                    if (DEBUG) Log.i(TAG, "ignored rest api: " + rest);
                    dismiss();
                    break;
            }

        }

        protected boolean isError(int statusCode, String response) {
            return response == null || response.isEmpty() || statusCode != 200;
        }

        protected void notifyAuthenticationError(Context context) {
            Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
        }

        protected void notifyAuthenticationError(Context context, String response) {
            if (response == null || response.trim().length() == 0) {
                notifyAuthenticationError(context);
            }
            else {
                try {
                    ApiError apiError = ItsoninAPI.mapper.readValue(response, ApiError.class);
                    Toast.makeText(context, apiError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                catch (Exception e) {
                    Log.e(TAG, "error handling error", e);
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                }
            }
        }

    };

}
