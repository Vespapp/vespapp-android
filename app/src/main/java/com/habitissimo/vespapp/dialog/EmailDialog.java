package com.habitissimo.vespapp.dialog;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.habitissimo.vespapp.Constants;
import com.habitissimo.vespapp.R;
import com.habitissimo.vespapp.menu.LOPDActivity;
import com.habitissimo.vespapp.sighting.PicturesActions;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by Simó on 18/07/2016.
 */
public class EmailDialog extends DialogFragment {

    private static EmailDialog frag;
    private static final int READ_PHONE_PERMISSION = 14;
    private String mPhoneNumber = "";

    public static EmailDialog getInstance() {
        if (frag == null) {
            frag = new EmailDialog();
        }
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

//        String message = getArguments().getString("code");

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.dialog_email_view, null);

        SharedPreferences prefs =
                getActivity().getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

        String email = prefs.getString("email", "");
        String name = prefs.getString("name", "");
        String phone = prefs.getString("phone", "");

        if (email.equals("")) {
            email = getEmailFromAccount();
        }
        if (phone.equals("")) {
//            phone = getPhoneNumber();
        }

        final EditText new_email = (EditText) v.findViewById(R.id.editText_mail);
        final EditText new_name = (EditText) v.findViewById(R.id.editText_name);
        final EditText new_phone = (EditText) v.findViewById(R.id.editText_phone);

        new_email.setText(email);
        new_name.setText(name);
        new_phone.setText(phone);

        builder.setView(v);

        builder.setTitle(getString(R.string.dialog_email_title))
                .setPositiveButton(getString(R.string.dialog_email_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences prefs =
                                getActivity().getSharedPreferences(Constants.PREFERENCES,Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("email", new_email.getText().toString());
                        editor.putString("name", new_name.getText().toString());
                        editor.putString("phone", new_phone.getText().toString());
                        editor.putString("permission", "yes");
                        editor.commit();

                        if (!new_email.getText().toString().equals("") ||
                                !new_name.getText().toString().equals("") ||
                                !new_phone.getText().toString().equals(""))
                            Toast.makeText(getContext(), getString(R.string.dialog_email_toast), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_email_negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(getString(R.string.dialog_email_neutral_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent i = new Intent(getContext(), LOPDActivity.class);
                        startActivity(i);
                    }
                });
        return builder.create();
    }

    private String getPhoneNumber() {
        Log.d("[MainActivity]","getPhoneNumber");
        int permissionCheck_Phone = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_PHONE_STATE);

        mPhoneNumber = "";
        if (permissionCheck_Phone == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneNumber = tMgr.getLine1Number();
            if (mPhoneNumber == null)
                return "";
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    READ_PHONE_PERMISSION);
        }
        return mPhoneNumber;
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_PHONE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    TelephonyManager tMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
                    mPhoneNumber = tMgr.getLine1Number();
                    if (mPhoneNumber == null)
                        mPhoneNumber = "";

                } else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                    Log.d("[MainActivity]","Permission to write to external storage not granted");
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE)) {
                        //Show permission explanation dialog...
                        Toast.makeText(getContext(), getString(R.string.permission_warning_storage_1), Toast.LENGTH_LONG);
                    } else {
                        //Never ask again selected, or device policy prohibits the app from having that permission.
                        //So, disable that feature, or fall back to another situation...
                        Toast.makeText(getContext(), getString(R.string.permission_warning_storage_2), Toast.LENGTH_LONG);
                    }
                }
                return;
        }
    }
}
