package com.example.smartwatervalve;

import com.thingclips.smart.home.sdk.ThingHomeSdk;
import android.util.Log;
import com.thingclips.smart.android.user.api.IRegisterCallback;
import com.thingclips.smart.android.user.api.ILoginCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.sdk.api.IThingSearchDeviceListener;
import java.util.List;




public class TuyaAuthmanager {
    private static final String TAG = "TuyaAuthmanager";
    private AuthorizeCallback authorizeCallback;
    private MainActivity mainActivity;

    // Constructor receives MainActivity reference
    public TuyaAuthmanager(MainActivity mainActivity, AuthorizeCallback authorizeCallback) {
        this.mainActivity = mainActivity;
        this.authorizeCallback = authorizeCallback;
    }

    public TuyaAuthmanager(AuthorizeCallback authorizeCallback) {
        this.authorizeCallback = authorizeCallback;
    }

    public interface AuthorizeCallback {
        void onLoginSuccess(long homeId);
        void onError(String error);
    }

    

    // register new user
    public void registerUser(String countryCode, String email, String password) {
        mainActivity.showLoading("Registering...");
        ThingHomeSdk.getUserInstance().registerAccountWithEmail(
            countryCode, email, password,
            new IRegisterCallback() {
                
                public void onSuccess(User user) {
                    Log.d(TAG, "✅ Registration successful: " + user.getUid());
                    mainActivity.hideLoading();
                    mainActivity.showToast("✅ Registration Successful!");
                    loginUser(countryCode, email, password); // Auto-login after registration
                }

               
                public void onError(String code, String error) {
                    mainActivity.hideLoading();
                    mainActivity.showAlert("Registration Failed", "❌ Error: " + error);
                    Log.e(TAG, "❌ Registration failed: " + error);
                    authorizeCallback.onError(error);
                }
            }
        );
    }

    // login user
    public void loginUser(String countryCode, String email, String password) {
        mainActivity.showLoading("Logging in...");
        ThingHomeSdk.getUserInstance().loginWithEmail(
            countryCode, email, password,
            new ILoginCallback() {
               
                public void onSuccess(User user) {
                    Log.d(TAG, "✅ Login successful: " + user.getUid());
                    mainActivity.hideLoading();
                    mainActivity.showToast("✅ Login Successful!");
                    // getHomeId();
                }

               
                public void onError(String code, String error) {
                    mainActivity.hideLoading();
                    mainActivity.showAlert("Login Failed", "❌ Error: " + error);
                    Log.e(TAG, "❌ Login failed: " + error);
                    authorizeCallback.onError(error);
                }
            }
        );
    }

    
}
