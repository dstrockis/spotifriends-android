package com.strockisdev.spotifriends.persistence;

import android.content.Context;
import android.content.SharedPreferences;

import com.strockisdev.spotifriends.R;

/**
 * Created by dastrock on 10/12/2014.
 */
public class SpTokenCache {

    private Context context;
    private SharedPreferences sharedPref;

    public SpTokenCache(Context ctx) {
        context = ctx;
        sharedPref = context.getSharedPreferences(
                ctx.getString(R.string.sp_token_cache_key), Context.MODE_PRIVATE);
    }

    public String getAccessToken() {
        return sharedPref.getString(context.getString(R.string.sp_token_cache_key), null);
    }

    public void cacheAccessToken(String token) {

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.sp_token_cache_key),token);
        editor.commit();
    }

    public void clearTokenCache() {

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.sp_token_cache_key), null);
        editor.commit();
    }
}
