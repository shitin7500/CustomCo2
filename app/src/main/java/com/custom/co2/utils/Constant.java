package com.custom.co2.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.custom.co2.R;
import com.custom.co2.activity.LoginActivity;

public class Constant {

    public static String spUsername = "username";
    public static String spEmail = "email";
    public static String spPassword = "password";
    public static String spConatact = "contact";
    public static String spDob = "dob";


    public static void setShaedPref(Context context, String key, String value) {
        SharedPreferences sharedpreferences = context.getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getShaedPref(Context context, String key) {
        SharedPreferences sharedpreferences = context.getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);

        return sharedpreferences.getString(key, "");
    }

    public static void clearShaedPref(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static void showAlertDailogBox(Context context, String msg) {

        new AlertDialog.Builder(context)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })
                // .setNegativeButton(android.R.string.no, null)
                .show();
    }


    public static Dialog showProgressDialog(Context context) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_progress);
        int width = (int)(context.getResources().getDisplayMetrics().widthPixels*0.90);

        dialog.getWindow().setLayout(width, LinearLayout.LayoutParams.WRAP_CONTENT);


        return dialog;
    }

}
