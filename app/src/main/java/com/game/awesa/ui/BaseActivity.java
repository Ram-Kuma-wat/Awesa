package com.game.awesa.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import com.codersworld.awesalibs.storage.UserSessions;
import com.game.awesa.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;


public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appUpdateManager = AppUpdateManagerFactory.create(this);
    }

    String strVar;

    @Override
    protected void onResume() {
        super.onResume();
        //checkBackButton();
        appUpdateManager = AppUpdateManagerFactory.create(this);
        if (UserSessions.getUpdate(BaseActivity.this) == 0 && appUpdateManager != null) {
            checkUpdate();
        } else {
            checkUpdate();
        }
    }

    AppUpdateManager appUpdateManager;

    public void checkUpdate() {
        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        // Checks that the platform will allow the specified type of update.
        // Log.d("ASDD", "Checking for updates");
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    UserSessions.saveUpdate(BaseActivity.this, 1);
                    //   Log.d("ASDD", "Update available");
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(BaseActivity.this);
                    builder.setTitle((CharSequence) getString(R.string.app_name));
                    builder.setCancelable(false);
                    builder.setMessage((CharSequence) getString(R.string.app_update_msg));
                    builder.setPositiveButton((CharSequence) getString(R.string.lbl_update_now), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String packageName = getPackageName();
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                                finish();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                                    finish();
                                } catch (Exception ex1) {
                                    ex1.printStackTrace();
                                }
                            }
                        }
                    });
                    //builder.setNegativeButton((CharSequence) "Later", (DialogInterface.OnClickListener) null);
                    builder.show();
                } else {
                    // Log.d("ASDD", "No Update available");
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

}