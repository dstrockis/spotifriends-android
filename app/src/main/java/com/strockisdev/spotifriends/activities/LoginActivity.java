package com.strockisdev.spotifriends.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;
import com.spotify.sdk.android.Spotify.*;


import com.facebook.*;
import com.strockisdev.spotifriends.fragments.FbLoginFragment;
import com.strockisdev.spotifriends.R;
import com.strockisdev.spotifriends.persistence.SpTokenCache;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends FragmentActivity implements
        PlayerNotificationCallback, ConnectionStateCallback  {

    private static final String CLIENT_ID = "5d96370e485f4fc88a64eeed442d265e";
    private static final String REDIRECT_URI = "spotifriends-login://callback";

    private FbLoginFragment fbFragment;
    private SpTokenCache spCache;

///////////////////////////////////////////////////////
///// Activity Lifecycle Methods
///////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        spCache = new SpTokenCache(this);

        // Add FB Login Fragment
        if (savedInstanceState == null) {
            fbFragment = new FbLoginFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fbFragment).commit();
        } else {
            fbFragment = (FbLoginFragment) getSupportFragmentManager()
                    .findFragmentById(android.R.id.content);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If we have both tokens, send them to server & start the app
        String spToken = spCache.getAccessToken();
        String fbToken = Session.getActiveSession().getAccessToken();
        if (spToken != null && fbToken != null && fbToken.length() > 0) {

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(
                    Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new PostAccessTokensTask().execute(
                        getString(R.string.backend_base_url)
                                + getString(R.string.backend_token_path), fbToken, spToken);
            } else {
                Intent intent = new Intent(this, NoConnectionActivity.class);
                intent.putExtra("last_activity", this.getClass());
                startActivity(intent);
                return;
            }

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

/////////////////////////////////////////////////////
///// Event Listeners
/////////////////////////////////////////////////////

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            Log.d("MainActivity", "SP Access Token Query: " + uri.toString());
            AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
            spCache.cacheAccessToken(response.getAccessToken());
        }
    }

    public void onSpotifyLoginButtonClick(View view) {

        if (spCache.getAccessToken() == null) {
            Uri uri = Uri.parse(getString(R.string.backend_base_url) + "/login/spotify");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
            boolean isIntentSafe = activities.size() > 0;
            if (isIntentSafe) {
                startActivity(intent);
            }
        } else {
            //TODO: Spotify Logout?
            spCache.clearTokenCache();
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

    @Override
    public void onPlaybackError(ErrorType eventType, String errorDetails) {
        Log.d("MainActivity", "Playback Error" + errorDetails);
    }

    ///////////////////////////////////////////
    /////// Spotify Helper Methods
    ///////////////////////////////////////////

    ///////////////////////////////////////////
    /////// Network Connectivity
    //////////////////////////////////////////

    private class PostAccessTokensTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... inputs) {
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("fbToken", inputs[1]));
                params.add(new BasicNameValuePair("spToken", inputs[2]));
                return sendRequest(inputs[0], params);
            } catch (IOException e) {
                return "Error!"; //TODO
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO: We don't really care what happens, do we?
            // Infinite activity redirects (think hotel wifi sign in)
            Log.d("LoginActivity", "Server Response: " + result);
        }

        private String sendRequest(String urlString, List<NameValuePair> params) throws IOException {

            BufferedOutputStream oStream = null;
            BufferedInputStream iStream = null;
            HttpURLConnection conn = null;

            try {
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod(HttpMethod.POST.toString());
                conn.setDoInput(true);

                // Add POST parameters
                conn.setDoOutput(true);
                conn.setChunkedStreamingMode(0);
                oStream = new BufferedOutputStream(conn.getOutputStream());
                writeOutputStream(oStream, params);


                conn.connect();
                int response = conn.getResponseCode();
                Log.d("LoginActivity", "Token POST response: " + response);

                // Read POST response as String
                iStream = new BufferedInputStream(conn.getInputStream());
                return readInputStream(iStream, 500);

            } finally {
              if (iStream != null)
                  iStream.close();
              if (oStream != null)
                  oStream.close();
              conn.disconnect();
            }
        }

        private String readInputStream(BufferedInputStream is, int len)
                throws IOException {

            BufferedReader iReader = new BufferedReader(
                    new InputStreamReader(is, "UTF-8"));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = iReader.readLine()) != null) {
                total.append(line);
            }
            return total.toString();
        }

        private void writeOutputStream(BufferedOutputStream os, List<NameValuePair> params)
                throws IOException {

            BufferedWriter oWriter = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            oWriter.write(constructQuery(params));
            oWriter.flush();
            oWriter.close();
            return;
        }

        private String constructQuery(List<NameValuePair> params)
                throws UnsupportedEncodingException {

            StringBuilder result = new StringBuilder();
            boolean first = true;

            for (NameValuePair pair : params) {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            }

            return result.toString();
        }
    }

}