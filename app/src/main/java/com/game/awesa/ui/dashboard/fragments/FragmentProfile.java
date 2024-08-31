package com.game.awesa.ui.dashboard.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.codersworld.awesalibs.beans.CommonBean;
import com.codersworld.awesalibs.beans.user.UserBean;
import com.codersworld.awesalibs.listeners.OnConfirmListener;
import com.codersworld.awesalibs.listeners.OnPageChangeListener;
import com.codersworld.awesalibs.listeners.OnResponse;
import com.codersworld.awesalibs.mediapicker.ImagePicker;
import com.codersworld.awesalibs.mediapicker.listener.ImagePickerResultListener;
import com.codersworld.awesalibs.mediapicker.model.ImageProvider;
import com.codersworld.awesalibs.mediapicker.model.PickExtension;
import com.codersworld.awesalibs.mediapicker.model.PickerType;
import com.codersworld.awesalibs.mediapicker.ui.bottomsheet.SSPickerOptionsBottomSheet;
import com.codersworld.awesalibs.rest.ApiCall;
import com.codersworld.awesalibs.rest.RetrofitUtils;
import com.codersworld.awesalibs.rest.UniversalObject;
import com.codersworld.awesalibs.storage.UserSessions;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.codersworld.awesalibs.utils.Tags;
import com.game.awesa.R;
import com.game.awesa.databinding.FragmentProfileBinding;
import com.game.awesa.ui.LoginActivity;
import com.game.awesa.ui.mediapicker.PickerOptions;
import com.game.awesa.utils.Global;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class FragmentProfile extends Fragment implements View.OnClickListener,
        SSPickerOptionsBottomSheet.ImagePickerClickListener, OnConfirmListener,
        ImagePickerResultListener, OnResponse<UniversalObject> {
    ApiCall mApiCall = null;

    ImagePicker mImagePicker;
    @NotNull
    public static final String TAG = FragmentProfile.class.getSimpleName();

    public FragmentProfile() {
        //if required
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    FragmentProfileBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        binding = DataBindingUtil.bind(view);
        mImagePicker = new ImagePicker(this, requireActivity());
        initApiCall();

        UserBean mBeanUser = UserSessions.getUserInfo(requireActivity());
        if (mBeanUser != null) {
            binding.etFirstname.setText(CommonMethods.isValidString(mBeanUser.getFirstname()) ? mBeanUser.getFirstname() : "");
            binding.etLastname.setText(CommonMethods.isValidString(mBeanUser.getLastname()) ? mBeanUser.getLastname() : "");
            binding.etEmail.setText(CommonMethods.isValidString(mBeanUser.getEmail()) ? mBeanUser.getEmail() : "");
            binding.etPhone.setText(CommonMethods.isValidString(mBeanUser.getPhone()) ? mBeanUser.getPhone() : "");
            binding.etUsername.setText(CommonMethods.isValidString(mBeanUser.getUsername()) ? mBeanUser.getUsername() : "");
            if (CommonMethods.isValidString(mBeanUser.getImage())) {
                CommonMethods.loadImage(requireActivity(), mBeanUser.getImage(), binding.imgProfile);
            }
        }
        binding.btnSubmit.setOnClickListener(this);
        binding.imgEdit.setOnClickListener(this);
        return view;
    }

    public void initApiCall() {
        if (mApiCall == null) {
            mApiCall = new ApiCall(requireActivity());
        }
    }

    private void openPickerOptions() {
        SSPickerOptionsBottomSheet fragment = new SSPickerOptionsBottomSheet();
        fragment.show(getChildFragmentManager(), SSPickerOptionsBottomSheet.BOTTOM_SHEET_TAG);
    }

    @NotNull
    public static Fragment newInstance() {
        return new FragmentProfile();
    }

    OnPageChangeListener mListener = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPageChangeListener) context;
            if (mListener != null) {
                mListener.onPageChange("profile");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSubmit) {
            String strFName = binding.etFirstname.getText().toString();
            String strLName = binding.etLastname.getText().toString();
            String strEmail = binding.etEmail.getText().toString();
            String strUsername = binding.etUsername.getText().toString();
            String strPhone = binding.etPhone.getText().toString();
            if (!CommonMethods.isValidString(strFName)) {
                CommonMethods.setError(binding.etFirstname,requireActivity(),getString(R.string.first_name_required),getString(R.string.first_name_required));
            } else if (!CommonMethods.isValidString(strLName)) {
                CommonMethods.setError(binding.etLastname,requireActivity(),getString(R.string.last_name_required),getString(R.string.last_name_required));
            } else if (!CommonMethods.isValidString(strEmail)) {
                CommonMethods.setError(binding.etEmail,requireActivity(),getString(R.string.email_required),getString(R.string.email_required));
            } else if (CommonMethods.checkEmail(strEmail, requireActivity()) != -1) {
                CommonMethods.setError(binding.etEmail,requireActivity(),getString(R.string.error_email_invalid),getString(R.string.error_email_invalid));
            } else if (!CommonMethods.isValidString(strPhone)) {
                CommonMethods.setError(binding.etPhone,requireActivity(),getString(R.string.phone_required),getString(R.string.phone_required));
            } else if (CommonMethods.checkPhone(strPhone, requireActivity()) != 0) {
                CommonMethods.setError(binding.etPhone,requireActivity(),getString(R.string.error_phone_invalid),getString(R.string.error_phone_invalid));
            } else if (!CommonMethods.isValidString(strUsername)) {
                CommonMethods.setError(binding.etUsername,requireActivity(),getString(R.string.error_username),getString(R.string.error_username));
            } else {
                RequestBody user_id = new RetrofitUtils().createPartFromString(UserSessions.getUserInfo(requireActivity()).getId()+"");
                RequestBody fname = new RetrofitUtils().createPartFromString(strFName+"");
                RequestBody lname = new RetrofitUtils().createPartFromString(strLName+"");
                RequestBody email = new RetrofitUtils().createPartFromString(strEmail+"");
                RequestBody username = new RetrofitUtils().createPartFromString(strUsername+"");
                RequestBody phone = new RetrofitUtils().createPartFromString(strPhone+"");
                MultipartBody.Part part =null;
                if (CommonMethods.isValidString(strFilePath)) {
                    part = new RetrofitUtils().createFilePart("image", strFilePath, MediaType.parse("image/jpeg"));
                }
                //firstname,lastname, user_id,phone,username,email
                mApiCall.updateProfile(this, fname,lname,user_id,phone,username,email,CommonMethods.isValidString(strFilePath)?part:null);
            }
        } else if (v.getId() == R.id.imgEdit) {
            if (hasStoragePermission()) {
                openPickerOptions();
            }
        }
    }

    private PickerOptions pickerOptions = new PickerOptions(PickerType.GALLERY, true, true, false, 15, 5.5f, PickExtension.ALL, true, true, false, false, false);


    @Override
    public void onImageProvider(ImageProvider provider) {
        if (provider == ImageProvider.GALLERY) {
            pickerOptions = new PickerOptions(PickerType.GALLERY, true, true, false, 15, 5.5f, PickExtension.ALL, true, true, false, false, false);
            openImagePicker();
        } else if (provider == ImageProvider.CAMERA) {
            pickerOptions = new PickerOptions(PickerType.CAMERA, true, true, false, 15, 5.5f, PickExtension.ALL, true, true, false, false, false);
            openImagePicker();
        } else if (provider == ImageProvider.NONE) {
            //User has pressed cancel show anything or just leave it blank.
        }
    }

    private void openImagePicker() {
        mImagePicker
                .title(getString(R.string.app_name))
                .multipleSelection(pickerOptions.getAllowMultipleSelection(), pickerOptions.getMaxPickCount())
                .showCountInToolBar(pickerOptions.getShowCountInToolBar())
                .showFolder(pickerOptions.getShowFolders())
                .cameraIcon(pickerOptions.getShowCameraIconInGallery())
                .doneIcon(pickerOptions.isDoneIcon())
                .allowCropping(pickerOptions.getOpenCropOptions())
                .compressImage(pickerOptions.getCompressImage(), 100)
                .maxImageSize(pickerOptions.getMaxPickSizeMB())
                .extension(pickerOptions.getPickExtension());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mImagePicker.systemPicker(pickerOptions.getOpenSystemPicker());
        }
        mImagePicker.open(pickerOptions.getPickerType());
    }

    String strFilePath = "";

    @Override
    public void onImagePick(Uri uri) {
        strFilePath = "";
        binding.imgProfile.setImageURI(uri);
        //String strFilePath1 = getPath(uri);
        strFilePath = getRealPathFromURI(uri);
        //  uri.let { updateImageList(listOf(it)) }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            ;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireActivity().managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onMultiImagePick(List<? extends Uri> uris) {
        //  uri.let { updateImageList(listOf(it)) }
    }

    // val requestMultiplePermissions =  registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {permissions ->
    private ActivityResultLauncher<String[]> requestMultiplePermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
        if (isGranted.containsValue(false)) {
            //requestMultiplePermissions.launch(PERMISSIONS);
        }
    });

    private Boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            if (ActivityCompat.checkSelfPermission(requireActivity(),
                    Manifest.permission.MEDIA_CONTENT_CONTROL
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.READ_MEDIA_VIDEO
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMultiplePermissions.launch(
                        new String[]{
                                Manifest.permission.MEDIA_CONTENT_CONTROL,
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO,
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO});
                //multiplePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMultiplePermissions.launch(
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                        }
                );
                //multiplePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
                return false;
            }
        }
        return true;
    }

    @Override
    public void onSuccess(UniversalObject response) {
        try {
            if (response != null) {
                if (response.getMethodName() == Tags.SB_UPDATE_PROFILE_API) {
                    CommonBean mCommonBean = (CommonBean) response.getResponse();
                    if (mCommonBean.getStatus() == 1) {
                        UserSessions.saveUserInfo(requireActivity(),mCommonBean.getInfo());
                        // makeLogout();
                    }else if(mCommonBean.getStatus() == 99){
                        UserSessions.clearUserInfo(requireActivity());
                        new Global().makeConfirmation(mCommonBean.getMsg(),requireActivity(),this);
                    } else if (CommonMethods.isValidString(mCommonBean.getMsg())) {
                        errorMsg(mCommonBean.getMsg());
                    } else {
                        errorMsg(getString(R.string.something_wrong));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            errorMsg(getString(R.string.something_wrong));
        }
    }

    @Override
    public void onError(String type, String error) {
        errorMsg(getString(R.string.something_wrong));
    }

    public void errorMsg(String strMsg) {
        CommonMethods.errorDialog(requireContext(), strMsg, getResources().getString(R.string.app_name), getResources().getString(R.string.lbl_ok));
    }

    @Override
    public void onConfirm(Boolean isTrue, String type) {
        if (isTrue){
            if (type.equalsIgnoreCase("99")){
                UserSessions.clearUserInfo(requireActivity());
                startActivity(new Intent(requireActivity(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                requireActivity().finishAffinity();
            }
        }
    }
}