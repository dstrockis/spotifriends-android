package com.strockisdev.spotifriends;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;


public class MainActivity extends Activity {

    private UiLifecycleHelper uiLifecycleHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiLifecycleHelper = new UiLifecycleHelper(this, callback);
        uiLifecycleHelper.onCreate(savedInstanceState);

//        Session.getActiveSession().getAccessToken()
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

    public void onResume() {
        super.onResume();

        //Weird fb session state change notification not called scenarios
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiLifecycleHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiLifecycleHelper.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiLifecycleHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiLifecycleHelper.onSaveInstanceState(outState);
    }

    /////////////////////////////////////
    /////// Facebook Auth
    /////////////////////////////////////

    private void onSessionStateChange(Session session, SessionState state, Exception exception)
    {
        if (state.isOpened()) {
            Log.i("MainActivity", "Logged in...");
        }
        if (state.isClosed()) {
            Log.i("MainActivity", "Logged out...");
        }
    }
}
