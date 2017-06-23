package com.tomas.ampmechallenge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GraphRequest.Callback, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getName();

    private static final int RC_SIGN_IN = 9001;

    RecyclerView musicList;
    CallbackManager callbackManager;

    List<String> musicians;

    LoginButton loginButton;

    SignInButton signInButton;
    GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        // initializing class fields
        loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        musicList = (RecyclerView) findViewById(R.id.music_list);
        callbackManager = CallbackManager.Factory.create();
        musicians = new ArrayList<>();

        loginButton.setReadPermissions(Arrays.asList("user_likes"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // get likes from user.
                fetchUserMusicLikes(loginResult, "/%s/music");
            }

            @Override
            public void onCancel() {
                showToast("Facebook login cancelled...");
            }

            @Override
            public void onError(FacebookException error) {
                showToast("An error occurred");
            }
        });

        signInButton = (SignInButton) findViewById(R.id.yt_login_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/youtube.readonly"))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this , this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void fetchUserMusicLikes(LoginResult loginResult, String url) {
        // need to do this to access callback in anonymous function
        final GraphRequest.Callback callback = this;

        new GraphRequest(
                loginResult.getAccessToken(),
                String.format(url, loginResult.getAccessToken().getUserId()),
                null,
                HttpMethod.GET,
                callback
        ).executeAsync();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onCompleted(GraphResponse response) {
        try {
            JSONArray jsonArray = response.getJSONObject().getJSONArray("data");
            int count = jsonArray.length();

            // if there are liked music pages, the add the names in the list.
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    musicians.add(jsonObj.getString("name"));
                }

                GraphRequest nextPage = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);

                // also check if there are more liked pages.
                if (nextPage != null) {
                    nextPage.setCallback(this);
                    nextPage.executeAsync();
                } else {
                    //to delete
                    for (int i = 0; i < musicians.size(); i++) {
                        System.out.println(musicians.get(i));
                    }

                    // started a new thread since the "blockingConnect" method cannot execute in the
                    // main thread or the app crashes
                    /*new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // check if user is signed in to google (which he should)
                            // the fetch playlist
                            ConnectionResult connResult = mGoogleApiClient.blockingConnect();
                            if (connResult.isSuccess()) {
                                OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                                if (opr.isDone()) {
                                    // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                                    // and the GoogleSignInResult will be available instantly.
                                    Log.d(TAG, "Got cached sign-in");
                                    GoogleSignInResult result = opr.get();
                                    handleSignInResult(result);
                                } else {
                                    // If the user has not previously signed in on this device or the sign-in has expired,
                                    // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                                    // single sign-on will occur in this branch.
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showProgressDialog();
                                        }
                                    });

                                    opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                                        @Override
                                        public void onResult(GoogleSignInResult googleSignInResult) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    hideProgressDialog();
                                                }
                                            });
                                            handleSignInResult(googleSignInResult);
                                        }
                                    });
                                }
                            }
                        }
                    }).start();*/
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showToast("Failed to connect to Google!");
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            System.out.println(acct.getDisplayName());
        } else {
            // Signed out, show unauthenticated UI.
            // updateUI(false);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}



/*
TODO:
    - create booleans for each login and set it to true when they are logged in
    - at the end of the signing in process (fb or yt) check if both are logged in and preform the fetching
    - then check for silent sign-ins and perform the fetch if both are logged in from the start.
 */