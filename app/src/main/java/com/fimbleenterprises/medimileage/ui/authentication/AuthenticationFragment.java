package com.fimbleenterprises.medimileage.ui.authentication;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.fimbleenterprises.medimileage.BuildConfig;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.MediUser;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.MyProgressDialog;
import com.fimbleenterprises.medimileage.MySettingsHelper;
import com.fimbleenterprises.medimileage.MySqlDatasource;
import com.fimbleenterprises.medimileage.Queries;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.Requests;
import com.fimbleenterprises.medimileage.RestResponse;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cz.msebera.android.httpclient.Header;

public class AuthenticationFragment extends Fragment {

    public static final String TRUE = "true";
    public static final String FALSE = "false";
    private static final String TAG = "AuthenticationFragment";
    private AuthenticationViewModel authenticationViewModel;
    Button btnLogin;
    MySettingsHelper options;
    Context context;
    EditText txtUsername;
    EditText txtPassword;
    RequestHandle requestHandle;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        authenticationViewModel =
                ViewModelProviders.of(this).get(AuthenticationViewModel.class);

        View root = inflater.inflate(R.layout.fragment_authentication, container, false);

        context = getContext();
        // progress = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        options = new MySettingsHelper(context);
        context = getContext();
        txtUsername = root.findViewById(R.id.text_username);
        txtPassword = root.findViewById(R.id.text_password);
        btnLogin = root.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btnLogin.getText().equals("Logout")) {
                    options.setCachedPassword(null);
                    options.setCachedUsername(null);
                    configureViews();
                    return;
                }

                options.setCachedUsername(txtUsername.getText().toString());
                options.setCachedPassword(txtPassword.getText().toString());

                final MyProgressDialog dialog = new MyProgressDialog(getContext(), "Verifying credentials...");
                dialog.show();

                userCanAuthenticate(new MyInterfaces.YesNoResult() {
                    @Override
                    public void onYes(@Nullable Object object) {
                        Toast.makeText(context, "Successfully authenticated.\nGetting user details...", Toast.LENGTH_SHORT).show();
                        if (requestHandle != null && requestHandle.isCancelled()) {
                            dialog.dismiss();
                            return;
                        }
                        dialog.dismiss();
                        getUser(txtUsername.getText().toString());
                    }

                    @Override
                    public void onNo(@Nullable Object object) {
                        Toast.makeText(context, "Failed to authenticate.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });

        if (BuildConfig.DEBUG) {
            txtUsername.setText("matt.weber@medistimusa.com");
            txtPassword.setText("R3dst4ff^^");
        } else {
            txtUsername.setText(options.getCachedUsername());
            txtPassword.setText(options.getCachedPassword());
        }

        options.authenticateFragIsVisible(true);

        return root;
    }

    @Override
    public void onStart() {
        options.authenticateFragIsVisible(true);
        super.onStart();

    }

    @Override
    public void onStop() {
        options.authenticateFragIsVisible(false);
        super.onStop();
    }

    void configureViews() {
        txtUsername.setEnabled((options.getCachedUsername() != null && options.getCachedPassword() != null));
        txtUsername.setEnabled((options.getCachedUsername() != null && options.getCachedPassword() != null));

        if ((options.getCachedUsername() == null || options.getCachedPassword() == null)) {
            btnLogin.setText("Login");
        } else {
            btnLogin.setText("Logout");
        }
    }

    public void userCanAuthenticate(final MyInterfaces.YesNoResult result) {
        if (requestHandle != null && requestHandle.isCancelled()) {
            return;
        }

        final MyProgressDialog dialog = new MyProgressDialog(getContext(), "Checking credentials...");

        Requests.Request request = new Requests.Request(Requests.Request.Function.CAN_AUTHENTICATE);
        request.arguments.add(new Requests.Argument(null, txtUsername.getText().toString()));
        request.arguments.add(new Requests.Argument(null, txtPassword.getText().toString()));
        Crm crm = new Crm();
        try {
            crm.makeCrmRequest(context, request, new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                    String strResponse = new String(responseBody);
                    Log.d(TAG, "onSuccess " + strResponse);

                    // Added 1.5 - was authenticating everyone prior
                    if (strResponse != null && strResponse.equals(TRUE)) {
                        result.onYes(null);
                    } else if (strResponse != null && strResponse.equals(FALSE)) {
                        result.onNo(null);
                    } else {
                        result.onNo(null);
                    }
                    dialog.dismiss();

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getMessage());
                    Toast.makeText(context, "Failed to get user!\n" + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    result.onNo(error.getMessage());
                    options.clearCachedCredentials();
                    dialog.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "onClick: " + e.getMessage());
            dialog.dismiss();
        }
    }

    public void getUser(String email) {
        String query = Queries.Users.getUser(email);
        Requests.Request request = new Requests.Request(Requests.Request.Function.GET);
        request.arguments.add(new Requests.Argument(null, query));
        Crm crm = new Crm();
        final MyProgressDialog progressDialog = new MyProgressDialog(getContext(), "Getting your user information...");
        progressDialog.show();

        try {
            crm.makeCrmRequest(getContext(), request, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers,
                                      byte[] responseBody) {
                    progressDialog.dismiss();
                    String strResponse = new String(responseBody);
                    RestResponse response = new RestResponse(strResponse);
                    MediUser user = new MediUser(response);
                    user.save(context);
                    MySqlDatasource db = new MySqlDatasource(context);
                    MediUser retreivedUser = db.getMe();
                    Log.d(TAG, "onSuccess " + strResponse);

                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_HomeSecondFragment_to_HomeFragment);

                    Toast.makeText(getContext(), "Successfully logged in.", Toast.LENGTH_SHORT).show();

                    configureViews();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers,
                                      byte[] responseBody, Throwable error) {
                    Log.w(TAG, "onFailure: " + error.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to get details\n" + error.getMessage()
                            , Toast.LENGTH_SHORT).show();
                    options.clearCachedCredentials();
                    configureViews();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "onClick: " + e.getMessage());
            progressDialog.dismiss();
        }
    }
}
