package com.strockisdev.spotifriends;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;

import com.facebook.widget.LoginButton;
import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;

import com.facebook.*;
import com.facebook.model.*;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LoginActivity extends FragmentActivity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "5d96370e485f4fc88a64eeed442d265e";
    private static final String REDIRECT_URI = "spotifriends-login://callback";

    private Player mPlayer;
    private FbLoginFragment fbFragment;
    private String spToken;

///////////////////////////////////////////////////////
///// Activity Lifecycle Methods
///////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add FB Login Fragment
        if (savedInstanceState == null) {
            fbFragment = new FbLoginFragment();
            Log.d("MainActivity", "Adding Fragment...");
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fbFragment).commit();
        } else {
            fbFragment = (FbLoginFragment) getSupportFragmentManager()
                    .findFragmentById(android.R.id.content);
            Log.d("MainActivity", "Existing Fragment...");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If we already have both tokens...
        SpTokenCache tokenCache = new SpTokenCache(this);
        spToken = tokenCache.getAccessToken();
        Session fbSesh = Session.getActiveSession();
        if (spToken != null && fbSesh != null && fbSesh.isOpened()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }

        Button spButton = (Button) findViewById(R.id.sp_auth_button);
        TextView stepTextView = (TextView) findViewById(R.id.sp_auth_step_2);
        ImageView doneImageView = (ImageView) findViewById(R.id.sp_complete_check);

        if (spToken != null) {
            spButton.setText(R.string.sp_auth_button_logged_in);
            stepTextView.setVisibility(View.INVISIBLE);
            doneImageView.setVisibility(View.VISIBLE);
        } else {
            spButton.setText(R.string.sp_auth_button_log_in);
            stepTextView.setVisibility(View.VISIBLE);
            doneImageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

//////////////////////////////////////////
///// Action Bar Methods
//////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

/////////////////////////////////////////////////////
///// Event Listeners
/////////////////////////////////////////////////////

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            Log.d("MainActivity", "SP Access Token QP: " + uri.toString());

            AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
            SpTokenCache tokenCache = new SpTokenCache(this);
            tokenCache.cacheAccessToken(response.getAccessToken());
            spToken = response.getAccessToken();

//            if (Session.getActiveSession().isOpened())
//            {
//                TimerTask task = new TimerTask() {
//                    @Override
//                    public void run() {
//                        Intent newIntent = new Intent(LoginActivity.this, MainActivity.class);
//                        startActivity(newIntent);
//                    }
//                };
//                Timer t = new Timer();
//                t.schedule(task, 5000);
//            }
        }
    }

    public void onSpotifyLoginButtonClick(View view) {

        if (spToken == null) {
            Uri uri = Uri.parse(getString(R.string.backend_base_url) + "/login/spotify");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
            boolean isIntentSafe = activities.size() > 0;
            if (isIntentSafe) {
                startActivity(intent);
            }
        } else {
            SpTokenCache tokenCache = new SpTokenCache(this);
            tokenCache.clearTokenCache();
            spToken = null;

            Button spButton = (Button) findViewById(R.id.sp_auth_button);
            TextView stepTextView = (TextView) findViewById(R.id.sp_auth_step_2);
            ImageView doneImageView = (ImageView) findViewById(R.id.sp_complete_check);
            spButton.setText(R.string.sp_auth_button_log_in);
            stepTextView.setVisibility(View.VISIBLE);
            doneImageView.setVisibility(View.INVISIBLE);
        }
    }


/////////////////////////////////////////////////
///////// Spotify Implementations
////////////////////////////////////////////////////

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User Logged In.");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User Logged Out.");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary Error Occured");
    }

    @Override
    public void onNewCredentials(String s) {
        Log.d("MainActivity", "User credentials blob received.");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event recieved: " + eventType.name());
    }

    @Override
    public void onLoginFailed(Throwable throwable) {
        Log.d("MainActivity", "Login Failed");
    }


////////////////////////////////////////////////
/////// Spotify Helper Methods
////////////////////////////////////////////////

}
