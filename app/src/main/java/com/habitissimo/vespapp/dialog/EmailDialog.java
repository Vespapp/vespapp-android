package com.habitissimo.vespapp.dialog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.habitissimo.vespapp.Constants;
import com.habitissimo.vespapp.R;

import java.util.regex.Pattern;

/**
 * Created by Simó on 18/07/2016.
 */
public class EmailDialog extends DialogFragment {

    public static EmailDialog newInstance() {
        EmailDialog frag = new EmailDialog();
//        Bundle args = new Bundle();
//        args.putBundle("code", activity.get);
//        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

//        String message = getArguments().getString("code");

        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.dialog_email_view, null);

        SharedPreferences prefs =
                getActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

        String email = prefs.getString("email", "");

//        if (email.equals("")) {
//            email = getEmailFromAccount();
//        }

        final EditText new_email = (EditText) v.findViewById(R.id.editText_mail);
        new_email.setText(email);
        builder.setView(v);

        builder.setMessage(getString(R.string.dialog_email_message))
                .setTitle(getString(R.string.dialog_email_title))
                .setPositiveButton(getString(R.string.dialog_email_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences prefs =
                                getActivity().getSharedPreferences(Constants.PREFERENCES,Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("email", new_email.getText().toString());
                        editor.commit();

                        if (!new_email.getText().toString().equals(""))
                            Toast.makeText(getContext(), getString(R.string.dialog_email_toast), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_email_negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return builder.create();
    }

    private String getEmailFromAccount() {
        String mail = "";
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(getActivity().getApplicationContext()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                mail = account.name;
            }
        }
        return mail;
    }
}
