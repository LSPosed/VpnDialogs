package com.android.internal.app;

import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;

public class AlertController {
    public Button getButton(int whichButton) {
        throw new RuntimeException("Stub!");
    }

    public static class AlertParams {
        public CharSequence mTitle;
        public CharSequence mPositiveButtonText;
        public DialogInterface.OnClickListener mPositiveButtonListener;
        public CharSequence mNegativeButtonText;
        public DialogInterface.OnClickListener mNegativeButtonListener;
        public CharSequence mNeutralButtonText;
        public DialogInterface.OnClickListener mNeutralButtonListener;
        public boolean mCancelable;
        public View mView;
    }
}
