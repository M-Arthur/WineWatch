package au.edu.adelaide.cs.winewatch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import au.edu.adelaide.cs.winewatch.JsonObject.Ferment;
import au.edu.adelaide.cs.winewatch.JsonObject.User;
import au.edu.adelaide.cs.winewatch.Tools.Constants;
import au.edu.adelaide.cs.winewatch.Tools.ServerManager;

public class LoginActivity extends ActionBarActivity {

	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[]{
			"foo@example.com:hello", "bar@example.com:world", "a@b.com:aa123abbe"
	};

	/**
	 * Keep track of the login task to ensure we cancel it if required.
	 */
	private UserLoginTask mAuthTask = null;

	// UI references.
	private AutoCompleteTextView mEmailView;
	private EditText mPasswordView;
	private ImageButton usernameClear;
	private ImageButton passwordClear;
	private ProgressDialog mDialog;
	private View mProgressView;
	private View mLoginLayoutView;
	private View mLoginFormView;
	private SharedPreferences sp;
	private Toast toast;

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_login);


		sp = this.getSharedPreferences("WineWatchAccountFile01", MODE_PRIVATE);
		// Make the layout could get focus
		mLoginFormView = findViewById(R.id.login_form);

//		mDialog = new ProgressDialog(this);
//		mDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
//		mDialog.setCancelable(true);
//		mDialog.show();

		mLoginLayoutView = findViewById(R.id.login_layout);
		mLoginLayoutView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLoginFormView.requestFocus();
				hideKeyboard();
			}
		});

		// Set up the login form
		mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
		addEmailToAutoComplete();
		mEmailView.addTextChangedListener(usernameShowOrHide);
		mEmailView.setOnFocusChangeListener(clearOnFocusChangeListener);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.addTextChangedListener(passwordShowOrHide);
		mPasswordView.setOnFocusChangeListener(clearOnFocusChangeListener);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == R.id.login || actionId == EditorInfo.IME_NULL) {
					mLoginFormView.requestFocus();
					hideKeyboard();
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard();
				attemptLogin();
			}
		});

		usernameClear = (ImageButton) findViewById(R.id.ib_clear_username);
		usernameClear.setOnClickListener(clear);
		usernameClear.setOnTouchListener(imageChange);

		passwordClear = (ImageButton) findViewById(R.id.ib_clear_password);
		passwordClear.setOnClickListener(clear);
		passwordClear.setOnTouchListener(imageChange);

		mProgressView = findViewById(R.id.login_process);
		if (!sp.getString("username", "").isEmpty()) {
			mEmailView.setText(sp.getString("username", ""));
			mPasswordView.setText(sp.getString("password", ""));
		}
	}

	/**
	 * Attempt to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String email = mEmailView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password, if the user entered one.
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (!isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// check for a valid email address.
		if (TextUtils.isEmpty(email)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!isEmailValid(email)) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don`t attempt to login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			showProgress(true);
			mAuthTask = new UserLoginTask(email, password);
			mAuthTask.execute((Void) null);
		}
	}

	private boolean isEmailValid(String email) {
		// TODO: Replace this with more complicated logic in the future
		return email.contains("@");
	}

	private boolean isPasswordValid(String password) {
		// TODO: Replace this with some other requirements as required
		return password.length() > 4;
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewProperAnimator APIs, which allow
		// for very easy animations. if available, use these APIs to fade-in
		// the progress spinner.

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
				}
			});

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
				}
			});
		} else {
			// The ViewProperAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	private void addEmailToAutoComplete() {
		// TODO: add real List into AutoCompleteTextView
		// Create adapter to tell the AutoCompleteTextView what to show in its
		// dropdown list.
		List<String> emailAddressCollection = new ArrayList<>();
		emailAddressCollection.add("foo@example.com");
		ArrayAdapter<String> adapter = new ArrayAdapter<>(
				LoginActivity.this,
				android.R.layout.simple_dropdown_item_1line,
				emailAddressCollection);
		mEmailView.setAdapter(adapter);
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mLoginLayoutView.getWindowToken(), 0);
	}

	private OnClickListener clear = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.ib_clear_username) {
				mEmailView.setText("");
			}
			if (v.getId() == R.id.ib_clear_password) {
				mPasswordView.setText("");
			}
		}
	};

	private OnFocusChangeListener clearOnFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (v.getId() == R.id.email) {
				if (hasFocus && mEmailView.getText().length() > 0) {
					usernameClear.setVisibility(View.VISIBLE);
				}
				if (!hasFocus) {
					usernameClear.setVisibility(View.GONE);
				}
			}

			if (v.getId() == R.id.password) {
				if (hasFocus && mPasswordView.getText().length() > 0) {
					passwordClear.setVisibility(View.VISIBLE);
				}
				if (!hasFocus) {
					passwordClear.setVisibility(View.GONE);
				}
			}
		}
	};

	private OnTouchListener imageChange = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (v.getId() == R.id.ib_clear_username) {
				usernameClear.setImageDrawable(getResources().getDrawable(R.drawable.ib_clear_touched));
			}
			if (v.getId() == R.id.ib_clear_password) {
				passwordClear.setImageDrawable(getResources().getDrawable(R.drawable.ib_clear_touched));
			}
			return false;
		}
	};

	private TextWatcher usernameShowOrHide = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() > 0 && mEmailView.hasFocus()) {
				usernameClear.setImageDrawable(getResources().getDrawable(
						R.drawable.ib_clear_untouched));
				usernameClear.setVisibility(View.VISIBLE);
			} else {
				usernameClear.setVisibility(View.GONE);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	};

	private TextWatcher passwordShowOrHide = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() > 0 && mPasswordView.hasFocus()) {
				passwordClear.setImageDrawable(getResources().getDrawable(
						R.drawable.ib_clear_untouched));
				passwordClear.setVisibility(View.VISIBLE);
			} else {
				passwordClear.setVisibility(View.GONE);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	};

	/**
	 * Represents an asynchronous login/registration task used to authenticate the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

		private final String mEmail;
		private final String mPassword;
		private User user;
		private Map result;

		UserLoginTask(String email, String password) {
			mEmail = email;
			mPassword = password;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service

//			try {
			// Simulate network access
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				return false;
//			}

			user = new User();
			user.setUsername(mEmail);
			user.setPassword(mPassword);
			ServerManager sm = new ServerManager();
			result = sm.login(user);
			if ((boolean) result.get(Constants.LOGIN_RESULT)) {
				user = (User) result.get(Constants.LOGIN_USER);
				return true;
			}
//			for (String credential : DUMMY_CREDENTIALS) {
//				String[] pieces = credential.split(":");
//				if (pieces[0].equals(mEmail)) {
//				if (pieces[0].equals(mEmail)) {
//					// Account exists, return true if the password matches.
//					return pieces[1].equals(mPassword);
//				}
//			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {

			mAuthTask = null;
			showProgress(false);

			if (success) {
				Editor editor = sp.edit();
				editor.putString("username", mEmail);
				editor.putString("password", mPassword);
				editor.commit();
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, WineryListActivity.class);

				ServerManager sm = new ServerManager();

				// set global variables
				// TODO: replace with real name
				user.setName("fake name");
				MyApplication myApp = (MyApplication) getApplicationContext();
				myApp.setUser(user);

				startActivity(intent);
			} else {
				switch ((int) result.get(Constants.LOGIN_ERROR_TYPE)) {
					case Constants.LOGIN_PASSWORD_ERROR:
						mPasswordView.setError(getString(R.string.error_incorrect_password));
						mPasswordView.requestFocus();
						break;
					case Constants.LOGIN_INTERNET_ERROR:
					case Constants.LOGIN_SYSTEM_ERROR:
						toast = Toast.makeText(getApplicationContext(), (String) result.get(Constants.LOGIN_INFO), Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 10, 10);
						toast.show();
						break;
					default:
						toast = Toast.makeText(getApplicationContext(), "Unknown error", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 10, 10);
						toast.show();
						break;
				}

			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

}
