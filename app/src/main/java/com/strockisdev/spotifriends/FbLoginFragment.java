package com.strockisdev.spotifriends;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class FbLoginFragment extends Fragment {

    private UiLifecycleHelper uiLifecycleHelper;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.login_main, container, false);

        LoginButton fbAuthButton = (LoginButton) view.findViewById(R.id.fb_auth_button);
        fbAuthButton.setFragment(this);
        fbAuthButton.setReadPermissions(Arrays.asList("public_profile"));
        //TODO: Do I need to update UI here as well, or is onSessionStateChanged called?

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiLifecycleHelper = new UiLifecycleHelper(getActivity(), callback);
        uiLifecycleHelper.onCreate(savedInstanceState);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        TextView stepTextView = (TextView)getActivity().findViewById(R.id.fb_auth_step_1);
        ImageView doneImageView = (ImageView)getActivity().findViewById(R.id.fb_complete_check);
        if (state.isOpened()) {
            Log.i("MainFragment", "Logged in...");
            stepTextView.setVisibility(View.INVISIBLE);
            doneImageView.setVisibility(View.VISIBLE);

//            SpTokenCache tokenCache = new SpTokenCache(getActivity());
//            if (tokenCache.getAccessToken() != null) {
//                TimerTask task = new TimerTask() {
//                    @Override
//                    public void run() {
//                        Intent newIntent = new Intent(
//                                FbLoginFragment.this.getActivity(), MainActivity.class);
//                        startActivity(newIntent);
//                    }
//                };
//                Timer t = new Timer();
//                t.schedule(task, 5000);
//            }

        } else if (state.isClosed()) {
            stepTextView.setVisibility(View.VISIBLE);
            doneImageView.setVisibility(View.INVISIBLE);
            Log.i("MainFragment", "Logged out...");
        }
    }

    @Override
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

}
