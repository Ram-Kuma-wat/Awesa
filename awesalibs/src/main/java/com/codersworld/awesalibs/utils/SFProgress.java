package com.codersworld.awesalibs.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;

 import com.codersworld.awesalibs.R;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class SFProgress {

    private static ProgressDialog progressDialog;
    public static void showProgressDialog(Activity context,Boolean isCancelable) {
       try {
           if (progressDialog !=null && progressDialog.isShowing()){
               progressDialog.dismiss();
           }
           if (!((Activity) context).isFinishing()) {
               WeakReference<Context> weakActivity = new WeakReference<>(context);
               progressDialog = ProgressDialog.show(context, null, null, false, isCancelable);
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                   Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
               }
               progressDialog.setCanceledOnTouchOutside(isCancelable);
               progressDialog.setCancelable(isCancelable);
               progressDialog.setContentView(R.layout.progress_bar);
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }
    public static ProgressDialog showTempProgressDialog(Activity context,Boolean isCancelable) {
        ProgressDialog progressDialog  = ProgressDialog.show(context, null, null, false, isCancelable);
        try {
           if (!((Activity) context).isFinishing()) {
               WeakReference<Context> weakActivity = new WeakReference<>(context);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                   Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
               }
               progressDialog.setCanceledOnTouchOutside(isCancelable);
               progressDialog.setCancelable(isCancelable);
               progressDialog.setContentView(R.layout.progress_bar);
           }
       }catch (Exception e){
           e.printStackTrace();
       }
        return progressDialog;
    }
    public static void showMsgProgressDialog(Activity context,Boolean isCancelable,String strMsg) {
       try {
           if (progressDialog !=null && progressDialog.isShowing()){
               progressDialog.dismiss();
           }
           if (!((Activity) context).isFinishing()) {
               WeakReference<Context> weakActivity = new WeakReference<>(context);
               progressDialog = ProgressDialog.show(context, null, null, false, isCancelable);
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                   Objects.requireNonNull(progressDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
               }
               if (CommonMethods.isValidString(strMsg)) {
                   progressDialog.setMessage(strMsg);
               }
               progressDialog.setCanceledOnTouchOutside(isCancelable);
               progressDialog.setCancelable(isCancelable);
               progressDialog.setContentView(R.layout.progress_bar);
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    public static void printLog(String message) {

    }

    public static void hideProgressDialog(Context context) {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception ex) {
            printLog(ex.getMessage());
        }
    }
}
