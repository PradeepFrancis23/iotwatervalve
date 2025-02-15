package com.example.smartwatervalve;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.ProgressDialog;
import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodCall;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.home.sdk.callback.IThingGetHomeListCallback;
import com.thingclips.smart.home.sdk.bean.HomeBean;
import com.thingclips.smart.android.user.bean.User; 

import com.thingclips.smart.android.user.api.IRegisterCallback;
import com.thingclips.smart.android.user.api.ILoginCallback;
import android.widget.Toast;
import android.app.AlertDialog;

public class MainActivity extends FlutterActivity implements TuyaAuthmanager.AuthorizeCallback {
    private static final String CHANNEL = "com.example.smartwatervalve";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private TuyaAuthmanager tuyaAuthManager;
    private long homeId;
    private ProgressDialog progressDialog;

    // Show a Toast message
    public void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show()
        );
    }

    // Show an Alert Dialog
    public void showAlert(String title, String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
        });
    }

    // Show a Loading Dialog
    public void showLoading(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setCancelable(false);
            }
            progressDialog.setMessage(message);
            progressDialog.show();
        });
    }

    // Hide Loading Dialog
    public void hideLoading() {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }


    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler((call, result) -> {
                if (call.method.equals("registerUser")) {
                    String countryCode = call.argument("countryCode");
                    String email = call.argument("email");
                    String password = call.argument("password");
                    registerUser(countryCode, email, password, result);
                } else if (call.method.equals("loginUser")) {
                    String loginCountryCode = call.argument("countryCode");
                    String loginEmail = call.argument("email");
                    String loginPassword = call.argument("password");
                    loginUser(loginCountryCode, loginEmail, loginPassword, result);
                } else if (call.method.equals("searchDevices")) {
                    
                    searchDevices();
                } else {
                    result.notImplemented();
                }
            });
    }

    private boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                },
                PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                },
                PERMISSION_REQUEST_CODE);
        }
    }

    @Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSION_REQUEST_CODE) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permissions granted, proceed with device search
//            searchDevices(resul);
        } else {
            // Permissions denied, show a message to the user
            showToast("Permissions denied. Cannot search for devices.");
        }
    }
}

    private void registerUser(String countryCode, String email, String password, MethodChannel.Result result) {
        showLoading("Registering...");
        ThingHomeSdk.getUserInstance().registerAccountWithEmail(countryCode, email, password, new IRegisterCallback() {

            public void onSuccess(User user) {
                hideLoading();
                showToast("Registration Successful!");
                result.success("Registration successful");
            }

            public void onError(String code, String error) {
                hideLoading();
                showAlert("Registration Failed", "Error: " + error);
                result.error(code, error, null);
            }
        });
    }

    private void loginUser(String countryCode, String email, String password, MethodChannel.Result result) {
        showLoading("Logging in...");
        ThingHomeSdk.getUserInstance().loginWithEmail(countryCode, email, password, new ILoginCallback() {
            public void onSuccess(User user) {
                hideLoading();
                showToast("Login Successful!");
                result.success("Login successful");
            }


            public void onError(String code, String error) {
                hideLoading();
                showAlert("Login Failed", "Error: " + error);
                result.error(code, error, null);
            }
        });
    }

    @Override
    public void onLoginSuccess(long homeId) {
        Log.d("TuyaAuth", "✅ Login successful, Home ID: " + homeId);
        this.homeId = homeId;
    }

    @Override
    public void onError(String error) {
        Log.e("TuyaAuth", "❌ Login/Register error: " + error);
    }

//    private void searchTuyaDevices(MethodChannel.Result result) {
//        if(!hasPermissions()){
//            requestPermissions();
//            return;
//        }
//        // Show loading indicator
//        showLoading("Searching for devices...");
//
//        // Log the start of the search
//        Log.d("Tuya", "🔍 Starting device search...");
//
//        // Initialize the device search
//        TuyaHomeSdk.getActivatorInstance().newSearch().start(new ITuyaActivatorListener() {
//
//            public void onError(String errorCode, String errorMsg) {
//                // Hide loading indicator
//                hideLoading();
//
//                // Log and show error message
//                Log.e("Tuya", "❌ Search error: " + errorMsg);
//                showAlert("Search Error", errorMsg);
//
//                // Return error to Flutter
//                result.error(errorCode, errorMsg, null);
//            }
//
//
//            public void onActiveSuccess(DeviceBean deviceBean) {
//                // Hide loading indicator
//                hideLoading();
//
//                // Log and show success message
//                Log.d("Tuya", "✅ Device found: " + deviceBean.getName());
//                showToast("Device found: " + deviceBean.getName());
//
//                // Return success to Flutter
//                result.success(deviceBean.getName());
//            }
//
//
//            public void onStep(String step, Object data) {
//                // Handle each step of the activation process if needed
//                Log.d("Tuya", "Step: " + step + ", Data: " + data);
//            }
//        });
//
//        // Stop the search after a predefined timeout (e.g., 10 seconds)
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            // Stop the device search
//            // ThingHomeSdk.getActivatorInstance().stopSearch();
//            TuyaHomeSdk.getActivatorInstance().newSearch().stop();
//
//
//            // Hide loading indicator
//            hideLoading();
//
//            // Inform the user that the search has ended
//            showToast("Device search completed.");
//
//            // Log the end of the search
//            Log.d("Tuya", "🔍 Device search completed.");
//
//            // Return result to Flutter
//            result.success("Search completed");
//        }, 10000); // 10,000 milliseconds = 10 seconds
//    }
//

    private void searchDevices() {
        showLoading("Searching for devices...");
        Log.d("Tuya", "🔍 Starting Wi-Fi/BLE search...");

        // ThingSearchDevice.getInstance().startSearchDevice(new IThingSearchDeviceListener() {
        //     @Override
        //     public void onSearchDeviceSuccess(List<SearchDeviceBean> list) {
        //         hideLoading();
        //         if (list.isEmpty()) {
        //             showToast("No Tuya devices found.");
        //         } else {
        //             Log.d("Tuya", "🔍 Found devices: " + list.size());
        //             for (SearchDeviceBean device : list) {
        //                 Log.d("Tuya", "🔍 Device: " + device.getDeviceName());
        //             }
        //             showToast("Devices found: " + list.size());
        //         }
        //         result.success(list.size());
        //     }

        //     @Override
        //     public void onSearchDeviceError(String errorCode, String errorMsg) {
        //         hideLoading();
        //         Log.e("Tuya", "❌ Search error: " + errorMsg);
        //         showAlert("Search Error", errorMsg);
        //         result.error(errorCode, errorMsg, null);
        //     }
        // });

        // Stop the search after a predefined timeout (e.g., 10 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // ThingSearchDevice.getInstance().stopSearch();
            hideLoading();
            showToast("Tuya Device Not Found");

        }, 10000); // 10,000 milliseconds = 10 seconds
    }
}
