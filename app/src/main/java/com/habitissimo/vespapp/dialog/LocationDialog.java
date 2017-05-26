package com.habitissimo.vespapp.dialog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.habitissimo.vespapp.Constants;
import com.habitissimo.vespapp.R;

import java.util.regex.Pattern;

/**
 * Created by Simo on 20/05/2017.
 */

public class LocationDialog extends DialogFragment {

    public static LocationDialog newInstance() {
        LocationDialog frag = new LocationDialog();
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());

        builder.setMessage(getString(R.string.dialog_location_message))
                .setTitle(getString(R.string.dialog_location_title))
                .setPositiveButton(getString(R.string.dialog_location_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }
}
