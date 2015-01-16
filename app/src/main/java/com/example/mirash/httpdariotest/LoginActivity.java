package com.example.mirash.httpdariotest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import static com.example.mirash.httpdariotest.LogUtils.log;

public class LoginActivity extends Activity {
    private EditText mLoginView;
    private EditText mPasswordView;

    private View mProgressView;
    private View mLoginFormView;

    private MyWebView mWebView;

    private UserLoginTask mAuthTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mLoginView = (EditText) findViewById(R.id.login);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.email_sign_in_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        findViewById(R.id.apply_default_credentials_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoginView.setText(HttpClientHelper.CREDENTIALS.LOGIN);
                mPasswordView.setText(HttpClientHelper.CREDENTIALS.PASSWORD);
            }
        });
        mWebView = (MyWebView) findViewById(R.id.web_view);
        mWebView.init();
    }

    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        mPasswordView.setError(null);

        String login = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();

        mAuthTask = new UserLoginTask(login, password);
        mAuthTask.execute();

        //
        mWebView.loadUrl(HttpClientHelper.getPostRequestUriString(login, password));
    }

    public void showProgress(boolean isShow) {
        mLoginFormView.setVisibility(isShow ? View.INVISIBLE : View.VISIBLE);
        mProgressView.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String mLogin;
        private final String mPassword;

        UserLoginTask(String login, String password) {
            mLogin = login;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpClientHelper.login(mLogin, mPassword);
            } catch (Exception e) {
                log("doInBackground fail " + e.toString());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            log("onPostExecute " + success);
            if (success) {
                log("SUCCESS!");
            } else {
                mPasswordView.setError(getString(R.string.error));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}



