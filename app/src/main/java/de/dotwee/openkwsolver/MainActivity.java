package de.dotwee.openkwsolver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import de.dotwee.openkwsolver.Tools.DownloadContentTask;
import de.dotwee.openkwsolver.Tools.DownloadImageTask;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // declare main widgets
        final Button buttonPull = (Button) findViewById(R.id.buttonPull);
        final Button buttonSkip = (Button) findViewById(R.id.buttonSkip);
        final Button buttonSend = (Button) findViewById(R.id.buttonSend);
        final TextView textViewBalance = (TextView) findViewById(R.id.textViewBalance);
        final ImageView imageViewCaptcha = (ImageView) findViewById(R.id.imageViewCaptcha);
        final EditText editTextAnswer = (EditText) findViewById(R.id.editTextAnswer);

        // fix edittext width
        editTextAnswer.setMaxWidth(editTextAnswer.getWidth());

        // init prefs
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        final Boolean prefLoop = prefs.getBoolean("pref_automation_loop", false);

        Boolean prefNotification = prefs.getBoolean("pref_notification", false);
        Boolean prefSound = prefs.getBoolean("pref_notification_sound", false);
        final Boolean prefVibrate = prefs.getBoolean("pref_notification_vibrate", false);

        // start showing balance if network and apikey is available
        if (isNetworkAvailable()) {
            if (!pullKey().equals("")) balanceThread();
        }

        buttonPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("OnClickPull", "Click recognized");
                if (isNetworkAvailable()) {
                    String CaptchaID = null;

                    if (prefLoop) {
                        while (prefLoop) {
                            CaptchaID = requestCaptchaID();
                            Log.i("WhileCapchaIDLoop", CaptchaID);
                            if (!CaptchaID.equals("")) break;
                        }
                    } else CaptchaID = requestCaptchaID();

                    Boolean currentCapt = false;
                    currentCapt = pullCaptchaPicture(CaptchaID);


                    final ProgressBar ProgressBar = (ProgressBar) findViewById(R.id.progressBar);
                    buttonPull.setEnabled(false);


                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (prefVibrate)
                        if (currentCapt)
                            vibrator.vibrate(500);

                    final int[] i = {0};
                    final CountDownTimer CountDownTimer;
                    CountDownTimer = new CountDownTimer(26000, 1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {
                            i[0]++;
                            ProgressBar.setProgress(i[0]);
                        }

                        @Override
                        public void onFinish() {
                        }
                    };

                    CountDownTimer.start();
                    buttonPull.performClick();

                    Button buttonSend = (Button) findViewById(R.id.buttonSend);
                    final String finalCaptchaID = CaptchaID;
                    buttonSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String CaptchaAnswer = editTextAnswer.getText().toString();
                            sendCaptchaAnswer(CaptchaAnswer, finalCaptchaID);

                            CountDownTimer.cancel();
                            Log.i("OnClickSend", "Timer killed");
                            ProgressBar.setProgress(0);

                            imageViewCaptcha.setImageDrawable(null);
                            editTextAnswer.setText(null);

                            if (prefLoop) {
                                Log.i("OnClickSend", "Loop-Mode");
                                buttonPull.performClick();
                            } else buttonPull.setEnabled(true);
                        }
                    });

                    Button buttonSkip = (Button) findViewById(R.id.buttonSkip);
                    buttonSkip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i("OnClickSkip", "Click recognized");
                            EditText EditTextCaptchaAnswer = (EditText) findViewById(R.id.editTextAnswer);
                            EditTextCaptchaAnswer.setText(null);
                            skipCaptcha(finalCaptchaID);

                            CountDownTimer.cancel();
                            ProgressBar.setProgress(0);

                            ImageView ImageView = (ImageView) findViewById(R.id.imageViewCaptcha);
                            ImageView.setImageDrawable(null);

                            buttonPull.setEnabled(true);
                        }
                    });

                } else {
                    DialogNetwork();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_global, menu);
        Log.i("onCreateOptionsMenu", "Return: " + true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_stop:
                finish();
                System.exit(0);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Request CaptchaID
    public String requestCaptchaID() {
        String CaptchaURL = (SourceConfig.URL + SourceConfig.URL_PARAMETER_CAPTCHA_NEW + pullKey() + SourceConfig.URL_PARAMETER_SOURCE + SourceConfig.URL_PARAMETER_TYPE_CONFIRM + SourceConfig.URL_PARAMETER_NOCAPTCHA + readState());
        Log.i("requestCaptchaID", "URL: " + CaptchaURL);
        String CaptchaID = "";

        try {
            CaptchaID = new DownloadContentTask().execute(CaptchaURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (CaptchaID.equalsIgnoreCase("")) {
            Log.i("requestCaptchaID", "CaptchaID is empty");
        } else Log.i("requestCaptchaID", "Received ID: " + CaptchaID);

        return CaptchaID;
    }

    // Send Captcha answer
    public void sendCaptchaAnswer(String CaptchaAnswer, String CaptchaID) {

        Log.i("sendCaptchaAnswer", "Received answer: " + CaptchaAnswer);
        Log.i("sendCaptchaAnswer", "Received ID: " + CaptchaID);

        String CaptchaURL = (SourceConfig.URL + SourceConfig.URL_PARAMETER_CAPTCHA_ANSWER + SourceConfig.URL_PARAMETER_SOURCE + readState() + "&antwort=" + CaptchaAnswer + "&id=" + CaptchaID + pullKey());

        // remove Spaces from URL
        CaptchaURL = CaptchaURL.replaceAll(" ", "%20");
        Log.i("sendCaptchaAnswer", "Answer-URL: " + CaptchaURL);

        String Status = null;

        try {
            Status = new DownloadContentTask().execute(CaptchaURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Log.i("sendCaptchaAnswer", "Code: " + Status);

    }

    // Pull Captcha picture and display it
    public boolean pullCaptchaPicture(String CaptchaID) {
        String CaptchaPictureURL = (SourceConfig.URL + SourceConfig.URL_PARAMETER_CAPTCHA_SHOW + SourceConfig.URL_PARAMETER_SOURCE + readState() + "&id=" + CaptchaID + pullKey());
        Log.i("pullCaptchaPicture", "URL: " + CaptchaPictureURL);
        ImageView ImageV = (ImageView) findViewById(R.id.imageViewCaptcha);
        try {
            Bitmap returnBit = new DownloadImageTask(ImageV).execute(CaptchaPictureURL).get();
            if (returnBit != null) return true; // true = new image
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Skip Captcha
    public void skipCaptcha(String CaptchaID) {
        String CaptchaSkipURL = (SourceConfig.URL + SourceConfig.URL_PARAMETER_CAPTCHA_SKIP + "&id=" + CaptchaID + pullKey() + SourceConfig.URL_PARAMETER_SOURCE + readState());
        Log.i("skipCaptcha", "URL: " + CaptchaSkipURL);
        String r = null;

        try {
            r = new DownloadContentTask().execute(CaptchaSkipURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Log.i("skipCaptcha", "Result: " + r);
    }

    // Read API-Key from Dialog
    private String pullKey() {
        String r;
        SharedPreferences pref_apikey = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        String k = pref_apikey.getString("pref_api_key", null);
        Log.i("pullKey", "Readed key: " + k);

        if (k != null) {
            r = ("&apikey=" + k);
            return r;
        } else return "";
    }

    // Read written states
    public String readState() {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        Boolean prefSelfonly = prefs.getBoolean("pref_api_selfonly", false);
        Boolean prefDebug = prefs.getBoolean("pref_api_debug", false);
        String s = "";
        String d = "";

        if (prefSelfonly) s = "&selfonly=1";
        if (prefDebug) d = "&debug=1";

        return s + d;
    }

    // Check if network is available
    private boolean isNetworkAvailable() {
        Log.i("isNetworkAvailable", "Called");
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Notify the user about now working network
    public void DialogNetwork() {
        Log.i("DialogNetwork", "Called");
        AlertDialog.Builder Dialog = new AlertDialog.Builder(this);

        Dialog.setTitle("No network available");
        Dialog.setMessage("Please connect to the internet!");

        Dialog.setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("DialogNetwork", "OK");
            }
        });

        Dialog.setNegativeButton(getString(R.string.action_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("DialogNetwork", "Canceled");
            }
        });
        Dialog.show();
    }

    // BalanceThread: Update the balance every 5 seconds
    public void balanceThread() {
        final Thread BalanceUpdate;
        BalanceUpdate = new Thread() {

            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        Thread.sleep(5000); // 5000ms = 5s
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textViewBalance = (TextView) findViewById(R.id.textViewBalance);
                            Log.i("balanceThread", "Called");

                            String BalanceURL = (SourceConfig.URL + SourceConfig.URL_PARAMETER_SERVER_BALANCE + SourceConfig.URL_PARAMETER_SOURCE + pullKey());
                            Log.i("balanceThread", "BalanceURL: " + BalanceURL);

                            String tBalance = null;

                            try {
                                tBalance = new DownloadContentTask().execute(BalanceURL).get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }

                            Log.i("balanceThread", "Balance: " + tBalance);
                            textViewBalance.setText(tBalance);

                        }
                    });
                }

            }
        };

        // check if thread isn't already running.
        if (BalanceUpdate.isAlive()) {
            BalanceUpdate.interrupt();
            Log.i("balanceThread", "stopped");
        }

        // if not, start it
        else {
            BalanceUpdate.start();
            Log.i("balanceThread", "started");
        }
    }

}