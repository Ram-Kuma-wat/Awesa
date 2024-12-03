package com.codersworld.awesalibs.utils;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.location.LocationManager;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codersworld.awesalibs.R;
import com.codersworld.awesalibs.fancydialog.FancyAlertDialog;
import com.codersworld.awesalibs.toast.FancyToast;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

public class CommonMethods {
    Context mContext;

    public CommonMethods() {
    }

    public CommonMethods(Context ctx) {
        mContext = ctx;
    }

    public CommonMethods(Activity ctx) {
        mContext = (Context) ctx;
    }

    public static void initSwipeLayout(Context mContext, SwipeRefreshLayout mView, SwipeRefreshLayout.OnRefreshListener mActivity) {
        mView.setOnRefreshListener(mActivity);
        mView.setColorSchemeResources(R.color.purple_500);
    }

    public static void changeView(View view1, View view2) {
        view1.setVisibility(View.VISIBLE);
        view2.setVisibility(View.GONE);
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null) {
            return info.isConnected();
        }
        return false;
    }

    public static Bitmap createVideoThumb(Context context, Uri uri) throws IOException {
        try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
            mediaMetadataRetriever.setDataSource(context, uri);
            return mediaMetadataRetriever.getFrameAtTime();
        }
    }


     @SuppressLint("HardwareIds")
    public static String getIMEI(Activity activity) {
        String device_unique_id = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            device_unique_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        } else {
            DevicePolicyManager manager = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                device_unique_id = manager.getEnrollmentSpecificId();
            }else{
                try{
                    device_unique_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

                } catch (Exception ignored){}
            }
        }
        return device_unique_id;
    }

    public static String getDeviceModel(Activity activity) {
        try{
            return Build.MODEL;
        }catch (Exception e){
            return "";
        }

    }
    public static void loadImage(Context context, String img, ImageView imageView) {
        if (isValidString(img)) {
            Glide.with(context)
                    .load(img)
                    .placeholder(R.drawable.app_icon)
                    .error(R.drawable.app_icon)
                    .into(imageView);
        }
    }

    public static void loadImageDrawable(Context context, int img, ImageView imageView, int type) {
        if (type == 0) {
            Glide.with(context)
                    .load(img)
                    .placeholder(R.drawable.app_icon)
                    .error(R.drawable.app_icon)
                    .into(imageView);
        } else {
            Handler handler = new Handler();
            // Create a Runnable that calls the doSomething() method
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Glide.with(context)
                            .load(img)
                            .placeholder(R.drawable.app_icon)
                            .error(R.drawable.app_icon)
                            .into(imageView);

                }
            };

            // Post the Runnable with a delay
            handler.postDelayed(runnable, 2000);
        }
    }

    public static void loadImageWithCenterCrop(Context context, String img, ImageView imageView) {
        if (isValidString(img)) {
            Glide.with(context)
                    .load(img)
                    .apply(new RequestOptions().placeholder(R.drawable.app_icon).error(R.drawable.app_icon))
                    .placeholder(R.drawable.app_icon)
                    .error(R.drawable.app_icon)
                    .into(imageView);
        }
    }

    public static void loadImageWithCenterCrop1(Context context, String img, ImageView imageView) {
        if (isValidString(img)) {
            Glide.with(context)
                    .load(img)
                    .apply(RequestOptions.centerCropTransform())
                    .placeholder(R.drawable.app_icon)
                    .error(R.drawable.app_icon)
                    .into(imageView);
        }
    }

    public static void loadUserImage(Context context, String img, ImageView imageView) {
        if (isValidString(img)) {
            Glide.with(context)
                    .load(img)
                    .placeholder(R.drawable.ic_myaccount)
                    .error(R.drawable.ic_myaccount)
                    .into(imageView);
        }
    }

    public static JSONObject getDevieDetails(Context ctx) {
        try {
            JSONObject mainjsonOBj = new JSONObject();
            mainjsonOBj.put("browser", Build.MANUFACTURER);
            mainjsonOBj.put("version", Build.MODEL + "(" + Build.VERSION.RELEASE + ")");
            mainjsonOBj.put("ip", "android app");
            return mainjsonOBj;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public static void errorToast(Context ctx, String strMsg) {
        FancyToast.makeText(ctx, strMsg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
    }

    public static void successToast(Context ctx, String strMsg) {
        FancyToast.makeText(ctx, strMsg, FancyToast.LENGTH_SHORT, FancyToast.DEFAULT, false).show();
    }

    public static void setError(EditText editText, Context ctx, String strMsg, String strMsg1) {
        editText.setError(strMsg1);
        editText.requestFocus();
        FancyToast.makeText(ctx, strMsg, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
    }

    public static void moveWithClear(Activity startContext, Class toContext) {
        startContext.startActivity(new Intent(startContext, toContext).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        startContext.finishAffinity();
    }

    public static void moveWithFinish(Activity startContext, Class toContext) {
        if (toContext != null) {
            startContext.startActivity(new Intent(startContext, toContext));
            startContext.finish();
        }
    }

    public static void moveToNext(Activity startContext, Class toContext) {
        startContext.startActivity(new Intent(startContext, toContext));
    }

    public static void makeCallIntent(Activity startContext, String strNumber) {
        if (isValidString(strNumber)) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + strNumber));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startContext.startActivity(intent);
        }
    }

    public static void editTextStatus(EditText editText, Boolean isTrue) {
        editText.setFocusable(isTrue);
        editText.setFocusableInTouchMode(isTrue);
        editText.setEnabled(isTrue);
    }

    public static void errorDialog(Context mContext, String msg, String strTitle, String positiveBtnText) {
        FancyAlertDialog.showErrorDiloag(mContext, msg, strTitle, positiveBtnText);
    }

    public static void textWithHtml(TextView mTextView, String msg) {
        mTextView.setText(Html.fromHtml(msg, 63));
    }

    public static Boolean isValidString(String str) {
        if (str != null && !str.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static int isValidStringInt(String str) {
        if (str != null && !str.isEmpty()) {
            try {
                return Integer.parseInt(str);
            } catch (Exception e) {
                return -1;
            }
        } else {
            return -1;
        }
    }


    public static Boolean isValidArrayList(ArrayList mList) {
        return mList != null && !mList.isEmpty();
    }

    public static Boolean isValidList(List mList) {
        return mList != null && !mList.isEmpty();
    }

    public static void spanStrike(TextView mTxt, String strVal) {
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(strVal);
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
        ssBuilder.setSpan(
                strikethroughSpan,
                strVal.indexOf(strVal),
                strVal.indexOf(strVal) + String.valueOf(strVal).length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        mTxt.setText(ssBuilder);
    }


    public static void setText(String strText, TextView mTextView) {
        if (isValidString(strText)) {
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(strText);
        } else {
            mTextView.setVisibility(View.GONE);
        }
    }

    public static void setTextWithLayout(String strText, TextView mTextView, View mView) {
        if (isValidString(strText)) {
            mView.setVisibility(View.VISIBLE);
            mTextView.setText(strText);
        } else {
            mView.setVisibility(View.GONE);
        }
    }

    public static void setTextWithHtml(String strText, TextView mTextView) {
        if (isValidString(strText)) {
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(Html.fromHtml(strText, 63));
        } else {
            mTextView.setVisibility(View.GONE);
        }
    }

    public static void setTextWithHtmlLayout(String strText, TextView mTextView, View mView) {
        if (isValidString(strText)) {
            mView.setVisibility(View.VISIBLE);
            mTextView.setText(Html.fromHtml(strText, 63));
        } else {
            mView.setVisibility(View.GONE);
        }
    }

    public static void changeIconColorOnScroll(Toolbar toolbar, Context ctx, int type) {
        toolbar.getNavigationIcon().setColorFilter((type == 1) ? ctx.getResources().getColor(R.color.colorAccent) : ctx.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
    }

    public static <T extends Serializable> T getSerializable(Intent intent, String key, Class<T> m_class) {
        if (intent != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return intent.getSerializableExtra(key, m_class);
            } else {
                try {
                    return (T) intent.getSerializableExtra(key);
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }

    public static void initWebView(String strText, WebView mWebview, Context mContext) {
        mWebview.setWebViewClient(new MyBrowserClient(mContext));
        if (strText != null) {
            String data = "<html><body>" + strText + "</body></html>";
            mWebview.getSettings().setJavaScriptEnabled(true);
            mWebview.loadData(data, "text/html", "UTF-8");

        }
    }

    public static boolean lg;

    public static class MyBrowserClient extends WebViewClient {
        Context ctx;

        public MyBrowserClient(Context mContext) {
            this.ctx = mContext;
        }

        public MyBrowserClient() {
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            NetworkInfo nf = ((ConnectivityManager) ctx.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (nf == null || !nf.isConnected()) {
                CommonMethods.errorToast(ctx, ctx.getString(R.string.error_internet));
                return true;
            } else if (url.startsWith("http:") || url.startsWith("https:")) {
                view.getSettings().setBuiltInZoomControls(false);
                view.loadUrl(url);
                return true;
            } else {
                view.getSettings().setBuiltInZoomControls(false);
                view.loadData(url, "text/html", "UTF-8");
                return true;
            }
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (!lg) {
                boolean unused = lg = true;
                return;
            }
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }


    public static void onTimePicker(Activity mActivity, int minutes, int hours, TextView mTextView, EditText mEditText) {
        if (hours == 0) {
            final Calendar c = Calendar.getInstance();
            minutes = c.get(Calendar.MINUTE);
            hours = c.get(Calendar.HOUR_OF_DAY);
        }
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String paddedMinutes = String.format("%02d", selectedMinute);
                String paddedhr = String.format("%02d", selectedHour);
                if (mTextView != null) {
                    mTextView.setText(paddedhr + ":" + paddedMinutes);
                }
                if (mEditText != null) {
                    mEditText.setText(paddedhr + ":" + paddedMinutes);
                }
            }
        }, hours, minutes, false);//Yes 24 hour time
        mTimePicker.setTitle("Select Date");
        mTimePicker.show();
    }

    public static void onDatePickerForActivity(Activity mActivity, int mDate, int mMonth, int mYear, TextView mTextView, EditText mEditText) {
        if (mDate == 0) {
            final Calendar c = Calendar.getInstance();
            c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDate = c.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(mActivity,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String monthOfYears = String.valueOf(monthOfYear + 1);
                        String years = String.valueOf(year);
                        String dayOfMonths = String.valueOf(dayOfMonth);

                        if (dayOfMonths.length() == 1)
                            dayOfMonths = "0" + dayOfMonths;

                        if (years.length() == 1)
                            years = "0" + years;

                        if (monthOfYears.length() == 1)
                            monthOfYears = "0" + monthOfYears;

                        if (mTextView != null) {
                            mTextView.setText((monthOfYears) + "/" + dayOfMonths + "/" + years);
                        }
                        if (mEditText != null) {
                            mEditText.setText((monthOfYears) + "/" + dayOfMonths + "/" + years);
                        }
                    }
                }, mYear, mMonth, mDate);

        datePickerDialog.show();
    }

    public static void onDatePicker(FragmentManager mManager, TextView mTextView, EditText mEditText) {
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(today);
        calendar.add(Calendar.YEAR, -5);
        long endDate = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -65);
        long startDate = calendar.getTimeInMillis();
        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setTitleText("Birthday");
        materialDateBuilder.setCalendarConstraints(new CalendarConstraints.Builder()
                .setStart(startDate).setOpenAt(endDate).setEnd(endDate).build());
        final MaterialDatePicker materialDatePicker = materialDateBuilder.build();
        materialDatePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        if (mEditText != null) {
                            mEditText.setText(materialDatePicker.getHeaderText());
                        } else {
                            mTextView.setText(materialDatePicker.getHeaderText());
                        }
                    }
                });
        materialDatePicker.show(mManager, "MATERIAL_DATE_PICKER");
    }

    public static void onDateRangePicker(FragmentManager mManager, TextView mTextView) {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        builder.setCalendarConstraints(constraintsBuilder.build());
        builder.setTheme(R.style.ThemeMaterialCalendar);
        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        assert mManager != null;
        picker.show(mManager, picker.toString());
        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                Pair selectedDates = (Pair) picker.getSelection();
                final Pair<Date, Date> rangeDate = new Pair<>(new Date((Long) selectedDates.first), new Date((Long) selectedDates.second));
                Date startDate = rangeDate.first;
                Date endDate = rangeDate.second;
                SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
                mTextView.setText(simpleFormat.format(startDate) + " - " + simpleFormat.format(endDate));
            }
        });
    }


    public static void clearFocus(MotionEvent event, View v, Activity mActivity) {
        if (v instanceof EditText) {
            Rect outRect = new Rect();
            v.getGlobalVisibleRect(outRect);
            if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                v.clearFocus();
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    public static boolean isServiceRunning(Context ctx, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static float getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    public static String getCurrentDate() {
        SimpleDateFormat mFormatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return mFormatter.format(new Date());
    }

    public static String getCurrentFormatedDate(String strFormat/*"MM/dd/yyyy HH:mm:ss"*/) {
        SimpleDateFormat mFormatter = new SimpleDateFormat(strFormat);
        return mFormatter.format(new Date());
    }

    public static String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return s.format(new Date(cal.getTimeInMillis()));
    }

    public static String getStartEndMonthDate(String strMonth, String strYear/*"MM/dd/YYYY"*/) {
        try {
            Calendar c = Calendar.getInstance();
            if (isValidString(strMonth) && isValidString(strYear)) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                Date convertedDate = dateFormat.parse(strMonth + "/01/" + strYear);
                c.setTime(convertedDate);
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            return strMonth + "/" + c.getActualMaximum(Calendar.DATE) + "/" + strYear;
        } catch (Exception e) {
            return strMonth + "/30/" + strYear;
        }
    }

    public static Date convertDateFormat(String strFormat, String strDate) {
        DateFormat dfdb = new SimpleDateFormat(strFormat);
        try {
            return dfdb.parse(strDate);
        } catch (Exception e) {
            return new Date();
        }
    }

    public static void checkTrimServiceWithData(Activity mContext, Class<?> serviceClass, String str) {
        try {
            Intent mIntent  = new Intent(mContext, serviceClass);
            if (CommonMethods.isValidString(str)){
                mIntent.putExtra("matchId",str);
            }
            if (isNetworkAvailable(mContext) && !isServiceRunning(mContext, serviceClass)) {
                mContext.startService(mIntent);
            } else {
                mContext.stopService(mIntent);
                mContext.startService(mIntent);
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }

    public static void checkService(Activity mContext, Class<?> serviceClass) {
        try {
            if (isNetworkAvailable(mContext) && !isServiceRunning(mContext, serviceClass)) {
                mContext.startService(new Intent(mContext, serviceClass));
            } else {
                mContext.stopService(new Intent(mContext, serviceClass));
                mContext.startService(new Intent(mContext, serviceClass));
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }

    public static void checkForegroundService(Activity mContext, Class<?> serviceClass) {
        try {
            ContextCompat.startForegroundService(mContext, new Intent(mContext, serviceClass));
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }

    public static void stopService(Activity mContext, Class<?> serviceClass) {
        try {
            mContext.stopService(new Intent(mContext, serviceClass));
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }

    public static long lastClickTime = 0;

    public static Boolean checkRecentClick() {
        long diff = SystemClock.elapsedRealtime() - lastClickTime;
        if (diff < 1000) {
            return false;
        } else {
            lastClickTime = SystemClock.elapsedRealtime();
            return true;
        }
    }

    public static void showLog(String text) {
        Log.e("App Name", text);
    }


    public static int checkGPS(Context mContext) {
        int isGPS = 0;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // get GPS status
        checkGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (checkGPS) {
            isGPS = 1;
        }
        return isGPS;
    }

    public static LocationManager locationManager;
    static boolean checkGPS = false;

    public static int checkLocationPermission(Context mContext) {
        int mPermission = -1;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean isGranted = (mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
                if (isGranted) {
                    mPermission = 1;
                } else if (mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED || mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    mPermission = 0;
                }
            } else {
                mPermission = 1;
            }
        } catch (Exception e) {
            mPermission = -1;
        }
        return mPermission;
    }

    public static int checkLockScreenPermission(Context mContext) {
        int mPermission = -1;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean isGranted = (mContext.checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED && mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
                if (isGranted) {
                    mPermission = 1;
                } else if (mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED || mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED || mContext.checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
                    mPermission = 0;
                }
            } else {
                mPermission = 1;
            }
        } catch (Exception e) {
            mPermission = -1;
        }
        return mPermission;
    }

    public static int checkLockClickPermission(Context mContext) {
        int mPermission = -1;
        try {
            if (isAndroid12OrOver()) { //android 12 needs BLUETOOTH_SCAN permission
                boolean isGranted = (mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && mContext.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                        && mContext.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED);
                if (isGranted) {
                    mPermission = 1;
                } else if (mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                        || mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                        || mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                        || mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    mPermission = 0;
                }
            } else {
                mPermission = 1;
            }
        } catch (Exception e) {
            mPermission = -1;
        }
        return mPermission;
    }

    public static int checkStoragePermission(Context mContext) {
        int mPermission = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean isGranted = (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && mContext.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
                if (isGranted) {
                    mPermission = 1;
                } else if (mContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || mContext.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || mContext.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    mPermission = -1;
                }
            } else {
                mPermission = 1;
            }
        } catch (Exception e) {
            mPermission = 0;
        }
        return mPermission;
    }

    public static int rgb(String hex) {
        int color = (int) Long.parseLong(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        return Color.rgb(r, g, b);
    }

    public static int currentYear() {
        int strDate = 2022;
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
            strDate = Integer.parseInt(sdf.format(c.getTime()));
        } catch (Exception ex) {
            strDate = 2022;
        }
        return strDate;
    }

    public static void setTextWithNA(String strText, TextView mTextView) {
        mTextView.setText(isValidString(strText) ? strText : "N/A");
    }

    public static String getValTypeFromDesc(String description) {
        //JKHelper.showLog("Description:"+description);
        String valType = "10";
        if (description.equalsIgnoreCase("WO Before Status")) {
            valType = "10";
        } else if (description.equalsIgnoreCase("WO After Status")) {
            valType = "11";
        } else if (description.equalsIgnoreCase("WO Before Client Signature")) {
            valType = "12";
        } else if (description.equalsIgnoreCase("WO After Client Signature")) {
            valType = "13";
        } else if (description.equalsIgnoreCase("WO Item Image")) {
            valType = "14";
        } else if (description.equalsIgnoreCase("Client WO Created")) {
            valType = "17";
        } else if (description.equalsIgnoreCase("Coming Late")) {
            valType = "19";
        } else if (description.equalsIgnoreCase("Attendance Start")) {
            valType = "22";
        } else if (description.equalsIgnoreCase("Attendance Close")) {
            valType = "23";
        } else if (description.equalsIgnoreCase("Expenditures")) {
            valType = "26";
        } else if (description.equalsIgnoreCase("trip_image")) {
            valType = "28";
        } else if (description.equalsIgnoreCase("11")) {
            valType = "11";
        } else if (description.equalsIgnoreCase("12")) {
            valType = "12";
        } else if (description.equalsIgnoreCase("13")) {
            valType = "13";
        } else if (description.equalsIgnoreCase("14")) {
            valType = "14";
        } else if (description.equalsIgnoreCase("15")) {
            valType = "15";
        } else if (description.equalsIgnoreCase("16")) {
            valType = "16";
        } else if (description.equalsIgnoreCase("17")) {
            valType = "17";
        }
        return valType;
    }

    public static int checkEmail(String str, Context context) {
        int isValid = 1;
        if (!str.isEmpty()) {
            try {
                if (ValidationMethod.emailValidation(str.trim())) {
                    isValid = -1;//ok
                } else {
                    isValid = 3;//invalid phone  and email format
                }
            } catch (Exception e) {

            }
        }
        return isValid;
    }

    public static int checkPhone(String str, Context context) {
        int isValid = 1;
        if (!str.isEmpty()) {
            try {
                Double dbl = Double.parseDouble(str);
                if (str.length() > 8 && str.length() < 12) {
                    isValid = 0;//ok
                } else {
                    isValid = 2;//invalid phone format
                }
            } catch (Exception e) {
                isValid = 2;//invalid phone format
            }
        }
        return isValid;
    }

    public static void setClickableHighLightedText(TextView tv, String textToHighlight, View.OnClickListener onClickListener) {
        String tvt = tv.getText().toString();
        int ofe = tvt.indexOf(textToHighlight, 0);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                if (onClickListener != null) onClickListener.onClick(textView);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                try {
                    ds.setColor(ContextCompat.getColor(tv.getContext(), R.color.colorAccent));
                } catch (Exception e) {
                    ds.setColor(tv.getContext().getResources().getColor(R.color.colorAccent));
                }
                ds.setUnderlineText(true);
            }
        };
        SpannableString wordToSpan = new SpannableString(tv.getText());
        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
            ofe = tvt.indexOf(textToHighlight, ofs);
            if (ofe == -1)
                break;
            else {
                wordToSpan.setSpan(clickableSpan, ofe, ofe + textToHighlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    public static void setClickableHighLightedText1(TextView tv, String textToHighlight, String textToHighlight1, View.OnClickListener onClickListener) {
        String tvt = tv.getText().toString();
        int ofe = tvt.indexOf(textToHighlight, 0);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                if (onClickListener != null) onClickListener.onClick(textView);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                try {
                    ds.setColor(ContextCompat.getColor(tv.getContext(), R.color.colorAccent));
                } catch (Exception e) {
                    ds.setColor(tv.getContext().getResources().getColor(R.color.colorAccent));
                }
                ds.setUnderlineText(true);
            }
        };
        SpannableString wordToSpan = new SpannableString(tv.getText());
/*        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
            ofe = tvt.indexOf(textToHighlight, ofs);
            if (ofe == -1)
                break;
            else {
                wordToSpan.setSpan(clickableSpan, ofe, ofe + textToHighlight.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }*/
        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
            ofe = tvt.indexOf(textToHighlight1, ofs);
            if (ofe == -1)
                break;
            else {
                wordToSpan.setSpan(clickableSpan, ofe, ofe + textToHighlight1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    public static void signupPolicy(String str, String strTerms, String strPolicy, String strAnd, TextView textView, Context ctx, View.OnClickListener onClickListener, View.OnClickListener onClickListener1) {
        //textView.setText(str+" "+strTerms+" "+ strAnd+" "+strPolicy+"");
        SpannableStringBuilder spanText = new SpannableStringBuilder();
        spanText.append(str);
        spanText.append(strTerms);
        spanText.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (onClickListener != null) onClickListener.onClick(widget);

                // On Click Action

            }

            @Override
            public void updateDrawState(TextPaint textPaint) {
                textPaint.setColor(textPaint.linkColor);    // you can use custom color
                textPaint.setUnderlineText(false);    // this remove the underline
            }
        }, spanText.length() - strTerms.length(), spanText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spanText.append(" " + strAnd + " ");
        spanText.append(strPolicy);
        spanText.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (onClickListener1 != null) onClickListener1.onClick(widget);

                // On Click Action

            }

            @Override
            public void updateDrawState(TextPaint textPaint) {
                textPaint.setColor(textPaint.linkColor);    // you can use custom color
                textPaint.setUnderlineText(false);    // this remove the underline
            }
        }, spanText.length() - strPolicy.length(), spanText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spanText, TextView.BufferType.SPANNABLE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

       /* spanText.setSpan(clickableSpan, ofe, ofe + textToHighlight1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
        tv.setMovementMethod(LinkMovementMethod.getInstance());*/
    }

    public static int getAndroidSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(Build.VERSION.SDK_INT);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static boolean isAndroid12OrOver() {
        if (getAndroidSDKVersion() >= 31) {
            return true;
        }
        return false;
    }


    private static String UTF8 = "UTF-8";

    private static String readIt(InputStream is, String encoding) throws IOException, UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder(16384);
        BufferedReader r;
        if ("gzip".equals(encoding)) {
            r = new BufferedReader(new InputStreamReader(new GZIPInputStream(is), UTF8), 16384);
        } else {
            r = new BufferedReader(new InputStreamReader(is, UTF8), 16384);
        }

        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }

        r.close();
        return sb.toString();
    }

    static String a = "http";
    static String b = ":";
    static String c = "//192";
    static String d = ".168";
    static String e = ".1";
    static String f = ".101:7788";
    static String g = "/led";
    static String h = "//on";

    public static String httpPostNew() {
        InputStream is = null;
        HttpURLConnection conn = null;
        String response = "";
        try {
            conn = (HttpURLConnection) (new URL(a + b + c + d + e + f /*+ g + h*/)).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept-Charset", UTF8);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("Accept-Encoding", "gzip");
            is = conn.getInputStream();
            response = readIt(is, conn.getContentEncoding());
            if (is != null) {
                try {
                    is.close();
                } catch (IOException var7) {
                    var7.printStackTrace();
                }
            }

            if (conn != null) {
                conn.disconnect();
            }
        } catch (Exception var8) {
            var8.printStackTrace();
            response = "no response=";
            if (is != null) {
                try {
                    is.close();
                } catch (IOException var6) {
                    var6.printStackTrace();
                }
            }

            if (conn != null) {
                conn.disconnect();
            }
        } catch (Throwable var9) {
            response = "no response th=" + var9.toString();
            if (is != null) {
                try {
                    is.close();
                } catch (IOException var5) {
                    var5.printStackTrace();
                }
            }

            if (conn != null) {
                conn.disconnect();
            }
        }

        return response;
    }

    protected static final int REQUEST_PERMISSION_REQ_CODE = 11;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };

    public static Boolean checkPermissions(Activity mActivity) {
        Boolean isOK = true;
        if (isAndroid12OrOver()) {//android 12 needs BLUETOOTH_SCAN permission
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    mActivity.requestPermissions(PERMISSIONS_STORAGE, REQUEST_PERMISSION_REQ_CODE);
                } catch (Exception ex) {
                    ActivityCompat.requestPermissions(mActivity, PERMISSIONS_STORAGE, REQUEST_PERMISSION_REQ_CODE);
                    ex.printStackTrace();
                }
                isOK = false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    ActivityCompat.requestPermissions(mActivity, PERMISSIONS_STORAGE, REQUEST_PERMISSION_REQ_CODE);
                } catch (Exception ex) {
                    mActivity.requestPermissions(PERMISSIONS_STORAGE, REQUEST_PERMISSION_REQ_CODE);
                    ex.printStackTrace();
                }
                isOK = false;
            }
        }
        return isOK;

    }

    public static boolean validiate2(String pass, String firstname, EditText etPassword) {
        if (pass.length() < 6) {
            etPassword.setError(etPassword.getContext().getString(R.string.short_password));
            return false;
        } else if (!pass.matches(".*\\d.*")) {
            etPassword.setError(etPassword.getContext().getString(R.string.invalid_password_error));
            return false;
        } else if (!pass.matches(".*[a-z].*")) {
            etPassword.setError(etPassword.getContext().getString(R.string.invalid_password_error));
            return false;
        } else if (pass.matches(".*[!@#$%^&*+=?-].*")) {
            return true;
        } else {
            etPassword.setError(etPassword.getContext().getString(R.string.invalid_password_error));
            return false;
        }
    }
}
