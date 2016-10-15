package net.refy.android.mstcc.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * Created by yutaka on 10/15/2016.
 */
public class PermissionRequiredDialogActivity extends AppCompatDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle("Permission Required")
                .setMessage("The App requires Write System Settings Permission.")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        startActivity(i);
                    }
                })
                .create();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }
}
