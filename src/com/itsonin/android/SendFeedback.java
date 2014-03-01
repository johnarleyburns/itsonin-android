package com.itsonin.android;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

public class SendFeedback {
    
    public static boolean email(final FragmentActivity activity) {
        String email = activity.getString(R.string.contact_email_address);
        Uri uri = Uri.fromParts("mailto", email, null);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.app_name));
        activity.startActivity(
                Intent.createChooser(intent, activity.getString(R.string.send_email)));
        return true;
    }
}
