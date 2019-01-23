/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.util;//

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

public class DialogUtil {

    public static interface DialogCallback {

        public void confirmed();
        public void canceled();
    }

    public static void confirmDialog(
            Activity act,
            String title,
            String message,
            String cancelButtonText,
            String confirmButtonText,
            final DialogCallback dialogCallback) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(act);
        alert.setTitle(title);

        alert.setMessage(message);

        alert.setPositiveButton( confirmButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                dialogCallback.confirmed();
            }
        });

        alert.setNegativeButton( cancelButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                alert.setOnCancelListener( null);
                dialogCallback.canceled();
            }
        });

        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                dialogCallback.canceled();
            }
        });

        alert.show();
    }
}
