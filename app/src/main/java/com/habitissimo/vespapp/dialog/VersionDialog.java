package com.habitissimo.vespapp.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.habitissimo.vespapp.R;

/**
 * Created by Simó on 18/07/2016.
 */
public class VersionDialog extends DialogFragment {

    public static VersionDialog newInstance(String message) {
        VersionDialog frag = new VersionDialog();
        Bundle args = new Bundle();
        args.putString("code", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String message = getArguments().getString("code");

        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());

        builder.setMessage(message)
                .setTitle(getString(R.string.dialog_title))
                .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_to_play_store)));
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
