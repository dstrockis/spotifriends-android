package com.strockisdev.spotifriends;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.content.Context;

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


public class LoginActivity extends FragmentActivity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "5d96370e485f4fc88a64eeed442d265e";
    private static final String REDIRECT_URI = "spotifriends-login://callback";

    private Player mPlayer;
    private FbLoginFragment fbFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

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

//        SpotifyAuthentication.openAuthWindow(CLIENT_ID, "token", REDIRECT_URI,
//                new String[]{"user-read-private", "streaming"}, null, this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null) {
            Log.d("MainActivity", "Access Token: " + uri.getQueryParameter("access_token"));

////            AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
////            Spotify spotify = new Spotify(response.getAccessToken());
//
//            mPlayer = spotify.getPlayer(this, "Strockis Dev", this,
//                    new Player.InitializationObserver() {
//
//                        @Override
//                        public void onInitialized() {
//                            mPlayer.addConnectionStateCallback(MainActivity.this);
//                            mPlayer.addPlayerNotificationCallback(MainActivity.this);
//                        }
//
//                        @Override
//                        public void onError(Throwable throwable) {
//                            Log.e("MainActivity", "Could not initialize player: " +
//                                    throwable.getMessage());
//                        }
//                    });
        }
    }

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
    public void onPlaybackEvent(EventType eventType) {
        Log.d("MainActivity", "Playback event recieved: " + eventType.name());
    }

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

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    public void checkPlaying(View view) {
//        Log.d("MainActivity", "Is Playing?" + mPlayer.isPlaying());

        Uri uri = Uri.parse(getString(R.string.backend_base_url) + "/login");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isIntentSafe = activities.size() > 0;

        if (isIntentSafe) {
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

}
