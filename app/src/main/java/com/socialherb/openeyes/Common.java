package com.socialherb.openeyes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;

/**
 * Written by J.S. Oh on 2016-03-30.
 */
public class Common {

    static Snackbar snack;
    static void showSnackBar(View view,String msg) {

        snack = Snackbar.make(view, msg, Snackbar.LENGTH_LONG);
        snack.setActionTextColor(Color.WHITE);

        ViewGroup group = (ViewGroup) snack.getView();
        group.getBackground().setAlpha(100);
        // group.setAlpha(0.5f);
        snack.setAction("Action",null).show();

    }

    static Dialog PopupDialog(final Activity activity, String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle("OpenSource License");
        builder.setMessage(str);
        builder.setPositiveButton(activity.getString(R.string.close), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // nextScene(activity,cls);
            }
        });
        builder.create().show();
        return null;
    }

    static void nextScene(Activity activity, Class<?> cls) {
        try {
            //activity.finish();
            Intent i = new Intent(activity, cls);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(i);
            //activity.overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

}
