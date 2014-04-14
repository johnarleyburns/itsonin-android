package com.itsonin.android.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;
import com.itsonin.android.R;
import com.itsonin.android.model.SavedPreference;

/**
 * Created with IntelliJ IDEA.
 * User: johnarleyburns
 * Date: 4/14/14
 * Time: 7:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeletePreference extends DialogPreference {

    public DeletePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setTitle(R.string.delete_information); // This will override ListPreference Title
        setDialogMessage(R.string.delete_information_summary_verify);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(android.R.drawable.ic_dialog_alert);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            SavedPreference.clearAll(getContext());
            Toast.makeText(getContext(), R.string.deleted_information, Toast.LENGTH_SHORT).show();
        }
    }

}
