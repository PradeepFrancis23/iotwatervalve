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
import com.thingclips.smart.sdk.api.IThingDataCallback;
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener;
import com.thingclips.smart.sdk.api.IThingActivatorGetToken;
import com.thingclips.smart.sdk.api.IThingActivator;
import com.thingclips.smart.sdk.api.IThingDataCallback;
import com.thingclips.smart.android.user.api.IRegisterCallback;
import com.thingclips.smart.android.user.api.ILoginCallback;
import com.thingclips.smart.sdk.enums.ActivatorModelEnum;
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder;

import android.widget.Toast;
import android.app.AlertDialog;


public class MainActivity extends FlutterActivity implements TuyaAuthmanager.AuthorizeCallback {
    private static final String CHANNEL = "com.example.smartwatervalve";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private TuyaAuthmanager tuyaAuthManager;
    private long homeId;
    private ProgressDialog progressDialog;
    // Declare mThingActivator at the class level
    private IThingActivator mThingActivator;
    private String regToken;


    private DeviceBean currentDeviceBean;   

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
                    searchTuyaDevice();
                    // if(!hasPermissions()) {
                    //     String token = call.argument("token");
                    // searchTuyaDevice();
                    // } else {
                    //     requestPermissions();
                    //     result.error("PERMISSION_DENIED", "Permissions denied, cannot search for devices.", null);


                    // }
                    
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
                new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                },
                PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                },
                PERMISSION_REQUEST_CODE);
        }
    }
    


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with device search
                String token = "your_token_here"; // Replace with actual token if needed
                searchTuyaDevice();
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
                createHome();
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

    // get token for device pairing to identify user
    private void getRegistrationToken(){
        ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId, new IThingActivatorGetToken() {
            
            public void onSuccess(String token) {
                Log.d("Tuya", "🔑 Registration token: " + token);
                searchTuyaDevice(token);
                // showToast("Registration token: " + token);
            }

          
            public void onFailure(String errorCode, String errorMsg) {
                Log.e("Tuya", "Error: " + errorCode + " - " + errorMsg);
                showToast("Token error: " + errorMsg);
            }
        });
    }


    // samele for demo
    private void searchDevices() {
        showLoading("Searching for devices...");
        Log.d("Tuya", "🔍 Starting Wi-Fi/BLE search...");

      

        // Stop the search after a predefined timeout (e.g., 10 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // ThingSearchDevice.getInstance().stopSearch();
            hideLoading();
            showToast("Tuya Device Not Found");

        }, 10000); // 10,000 milliseconds = 10 seconds
    }

     // create home
     

     private void createHome(String homeName,List<String> roomList){
        ThingHomeSdk.getHomeManagerInstance().createHome(homeName,
        120.52,
                30.40,
                "mumbai",
            
         roomList, new IThingHomeResultCallback() {
            
            public void onSuccess(HomeBean homeBean) {
                currentDeviceBean = homeBean;
                getRegistrationToken();
                Log.d("Tuya", "🏠 Home created: " + homeBean.toString());
                showToast("Home created: " + homeBean.getName());
            }

            
            public void onError(String errorCode, String errorMsg) {
                Log.e("Tuya", "Error: " + errorCode + " - " + errorMsg);
                showToast("Home creation error: " + errorMsg);
            }
        });

     }

    private void searchTuyaDevice(String token) {
        // Check if permissions are granted before starting the activation
        if (!hasPermissions()) {
            requestPermissions();
            return;
        }
    
        showLoading("Searching for devices...");
    
        // Adjusted timeout and added debugging logs
        ActivatorBuilder builder = new ActivatorBuilder()
            .setSsid("Pradeep")
            .setContext(this)
            .setPassword("123456789")
            .setToken(token)
            .setActivatorModel(ActivatorModelEnum.THING_AP)
            .setTimeOut(100)  // Increased timeout to 30 seconds
            .setListener(new IThingSmartActivatorListener() {
    
                @Override
                public void onError(String errorCode, String errorMsg) {
                    // Handle error
                    Log.e("TuyaDeviceSearch", "Error: " + errorCode + " - " + errorMsg);
                    showToast("Error: " + errorMsg);
                    hideLoading();
                }
    
                @Override
                public void onActiveSuccess(DeviceBean devResp) {
                    // Handle success
                    Log.d("TuyaDeviceSearch", "Device detected: " + devResp.toString());
                    showToast("Device Detection Successful");
                    currentDeviceBean = devResp;
                    System.out.println(currentDeviceBean);
                    hideLoading();
                    // You can add more code to handle the device pairing or other actions
                }
    
                @Override
                public void onStep(String step, Object data) {
                    // Log each step for debugging
                    Log.d("TuyaDeviceSearch", "Step: " + step);
                    if (data != null) {
                        Log.d("TuyaDeviceSearch", "Step data: " + data.toString());
                    }
                }
            });
    
        // Initialize and start the activator
        IThingActivator mThingActivator = ThingHomeSdk.getActivatorInstance().newActivator(builder);
    
        // Start the activation process
        mThingActivator.start();
    }

   
    

    @Override
protected void onDestroy() {
    super.onDestroy();
    
    if (mThingActivator != null) {
        mThingActivator.onDestroy();
    }
}

    

   

}
