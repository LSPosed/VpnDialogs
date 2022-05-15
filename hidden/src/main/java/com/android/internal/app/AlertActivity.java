package com.android.internal.app;

import android.app.Activity;
import android.content.DialogInterface;

public abstract class AlertActivity extends Activity implements DialogInterface {
    protected AlertController mAlert;
    protected AlertController.AlertParams mAlertParams;

    public void cancel() {
        throw new RuntimeException("Stub!");
    }

    public void dismiss() {
        throw new RuntimeException("Stub!");
    }

    protected void setupAlert() {
        throw new RuntimeException("Stub!");
    }
}
