package com.example.moodvisulized;

import android.os.Bundle;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class LoginActivity extends MainActivity {
    // Request code will be used to verify if result comes from the login activity.
    // Can be set to any integer.
    private static final int REQUEST_CODE = 1003;
    private static final String REDIRECT_URI = "moodvisualized://callback";
    private static final String CLIENT_ID = "0184658057ca400693856a596026419b";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

}
