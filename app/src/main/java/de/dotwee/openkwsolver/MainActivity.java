/*
 * Copyright 2015 Lukas "dotwee" Wolfsteiner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dotwee.openkwsolver;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.concurrent.ExecutionException;

import de.dotwee.openkwsolver.Fragments.ConfirmFragment;
import de.dotwee.openkwsolver.Fragments.SettingsFragment;
import de.dotwee.openkwsolver.Fragments.SolverFragment;
import de.dotwee.openkwsolver.Tools.DownloadContentTask;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {
    public static final String URL_9WK = "http://www.9kw.eu:80/index.cgi";
    public static final String URL_PARAMETER_NOCAPTCHA = "&nocaptcha=1";
    public static final String URL_PARAMETER_CAPTCHA_NEW = "?action=usercaptchanew";

    public static final String URL_PARAMETER_SOURCE = "&source=androidopenkws";
    public static final String URL_PARAMETER_CAPTCHA_SKIP = "?action=usercaptchaskip";
    private final static String LOG_TAG = "MainActivity";
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new cFragmentAdapter(getFragmentManager()));

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab().setText("Solver").setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText("Settings").setTabListener(this));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem linkItem = menu.add("Source");
        linkItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        linkItem.setIcon(R.drawable.ic_bookmark_outline_white_36dp);
        linkItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent linkIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/dotWee/OpenKWSolver"));
                startActivity(linkIntent);
                finish();
                return false;
            }
        });
        
        MenuItem exitItem = menu.add("Exit");
        exitItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        exitItem.setIcon(R.drawable.ic_close_circle_outline_white_36dp)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        finish();
                        return true;
                    }
                });
        return true;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition(), true);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }
    
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public class cFragmentAdapter extends FragmentPagerAdapter {
        private String LOG_TAG = "FragmentAdapter";

        public cFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) fragment = new SolverFragment();
            if (position == 1) fragment = new ConfirmFragment();
            if (position == 2) fragment = new SettingsFragment();

            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public static String requestCaptchaID(Context context, Boolean LOOP, int TYPE) {
        String CAPTCHA_URL = (URL_9WK + URL_PARAMETER_CAPTCHA_NEW + getApiKey(context) +
                URL_PARAMETER_SOURCE + getExternalParameter(context, TYPE) + URL_PARAMETER_NOCAPTCHA);

        Log.i(LOG_TAG, "ID Request URL: " + CAPTCHA_URL);
        String CAPTCHA_ID = "";
        if (LOOP)
            try {
                CAPTCHA_ID = new DownloadContentTask().execute(CAPTCHA_URL, "captchaid").get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        else try {
            CAPTCHA_ID = new DownloadContentTask().execute(CAPTCHA_URL, "").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Log.i(LOG_TAG, "ID Request RETURN: " + CAPTCHA_ID);

        return CAPTCHA_ID;
    }

    public static void skipCaptchaByID(Context context, String CAPTCHA_ID) {
        String CAPTCHA_URL = (URL_9WK + URL_PARAMETER_CAPTCHA_SKIP + "&id=" + CAPTCHA_ID +
                getApiKey(context) + URL_PARAMETER_SOURCE);
        Log.i(LOG_TAG, "SKIP Request URL: " + CAPTCHA_URL);
        new DownloadContentTask().execute(CAPTCHA_URL);
    }

    public static String getApiKey(Context context) {
        SharedPreferences pref_apikey = PreferenceManager
                .getDefaultSharedPreferences(context);
        String apikey = pref_apikey.getString("pref_api_key", null);
        Log.i(LOG_TAG, "API-Key: " + apikey);
        if (apikey != null)
            return "&apikey=" + apikey;
        else return "";
    }

    public static String getExternalParameter(Context context, int type) {
        String s = "", d = "", t = "";
        String CONFIRM = "&confirm=";
        String CLICK = "&mouse=";

        switch (type) {
            case 0: // confirm
                t = CONFIRM + "1" + CLICK + "0";
                break;

            case 1: // Todo click
                t = CONFIRM + "0" + CLICK + "1";
                break;

            case 2: // none
                t = CONFIRM + "0" + CLICK + "0";
                break;
        }

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        Boolean prefSelfonly = prefs.getBoolean("pref_api_selfonly", false);
        Boolean prefDebug = prefs.getBoolean("pref_api_debug", false);
        if (prefSelfonly) s = "&selfonly=1";
        if (prefDebug) d = "&debug=1";
        return s + d + t;
    }

    public static boolean networkAvailable(Context context) {
        Log.i("isNetworkAvailable", "Called");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}