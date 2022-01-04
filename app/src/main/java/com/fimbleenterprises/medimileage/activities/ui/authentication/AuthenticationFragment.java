package com.fimbleenterprises.medimileage.activities.ui.authentication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.fimbleenterprises.medimileage.BuildConfig;
import com.fimbleenterprises.medimileage.Crm;
import com.fimbleenterprises.medimileage.objects_and_containers.MediUser;
import com.fimbleenterprises.medimileage.MyInterfaces;
import com.fimbleenterprises.medimileage.dialogs.MyProgressDialog;
import com.fimbleenterprises.medimileage.MyPreferencesHelper;
import com.fimbleenterprises.medimileage.MySqlDatasource;
import com.fimbleenterprises.medimileage.CrmQueries;
import com.fimbleenterprises.medimileage.R;
import com.fimbleenterprises.medimileage.objects_and_containers.Requests;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import cz.msebera.android.httpclient.Header;

public class AuthenticationFragment extends Fragment {

    public static final String TRUE = "true";
    public static final String FALSE = "false";
    private static final String TAG = "AuthenticationFragment";
    private AuthenticationViewModel authenticationViewModel;
    Button btnLogin;
    MyPreferencesHelper options;
    Context context;
    EditText txtUsername;
    EditText txtPassword;
    RequestHandle requestHandle;
    BroadcastReceiver testReceiver;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        authenticationViewModel =
                ViewModelProviders.of(this).get(AuthenticationViewModel.class);

        View root = inflater.inflate(R.layout.fragment_authentication, container, false);

        context = getContext();
        // progress = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        options = new MyPreferencesHelper(context);
        context = getContext();
        txtUsername = root.findViewById(R.id.text_username);
        txtPassword = root.findViewById(R.id.text_password);
        btnLogin = root.findViewById(R.id.btnLogin);


        /*******************************************************************************************
                                         TESTING
        *******************************************************************************************/

        /*CrmEntities.Tickets.Ticket ticket = new CrmEntities.Tickets.Ticket();
        ticket.ticketnumber = "CAS-24556-XYY";
        ticket.repFormatted = "John Plante";
        ticket.territoryFormatted = "10010";
        ticket.customerFormatted = "Some hospital";
        ticket.createdByFormatted = "Matt Weber";
        ticket.title = "PQ32 s/n 2333 does not work";
        ticket.createdon = DateTime.now();
        ticket.subjectFormatted = "Complaint (Probe)";
        ticket.modifiedByFormatted = "Matt Weber";
        ticket.modifiedon = DateTime.now();
        ticket.statusFormatted = "In progress";
        ticket.description = "John mailed this in.  Contact Kristy with the findings.";

        BasicEntity basicEntity = ticket.toGenericActivity();
        String gsonString = basicEntity.toGson();

        Intent intent = new Intent(getContext(), BasicEntityActivity.class);
        intent.putExtra(BasicEntityActivity.GSON_STRING, gsonString);
        startActivity(intent);

        if (1 == 1) {
            return null;
        }

        testReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int pos = intent.getIntExtra(BasicEntityActivity.LIST_POSITION, -1);
                String label = intent.getStringExtra(BasicEntityActivity.LABEL);
                String value = intent.getStringExtra(BasicEntityActivity.VALUE);

                Toast.makeText(context, "Label: " + label + ", Value: " + value + ", Position: " + pos, Toast.LENGTH_SHORT).show();
            }
        };

        getContext().registerReceiver(testReceiver, BasicEntityActivity.getIntentFilter());*/

        /*******************************************************************************************
                                        END TESTING
        *******************************************************************************************/

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

                Crm.userCanAuthenticate(txtUsername.getText().toString(), txtPassword.getText().toString(),
                        new MyInterfaces.AuthenticationResult() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(context, "Successfully authenticated.\nGetting user details...", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                getUser(txtUsername.getText().toString());
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(context, "Failed to authenticate!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                            @Override
                            public void onError(String msg, Throwable exception) {
                                Toast.makeText(context, "Failed to authenticate\n" + msg, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
            }
        });

        if (BuildConfig.DEBUG) {
            /*txtUsername.setText("matt.weber@medistimusa.com");
            txtPassword.setText("R3dst4ff^^");*/
            txtUsername.setText("matt.weber@medistimusa.com");
            txtPassword.setText("R3dst4ff****");
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

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    public void getUser(String email) {
        String query = CrmQueries.Users.getUser(email);
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
                    MediUser user = MediUser.createOne(strResponse);
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
