package au.edu.adelaide.cs.winewatch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import au.edu.adelaide.cs.winewatch.JsonObject.Ferment;
import au.edu.adelaide.cs.winewatch.JsonObject.User;
import au.edu.adelaide.cs.winewatch.JsonObject.Winery;
import au.edu.adelaide.cs.winewatch.Tools.Constants;
import au.edu.adelaide.cs.winewatch.Tools.ServerManager;


public class AddFermentationActivity extends ActionBarActivity {

	private Winery winery;
	private User user;
	private Ferment ferment;
	private AddFermentation mAddFTask = null;
	private int baseId;
	private int moteId;
	private String tankId;

	private TextView wineryNameTextView;
	private TextView wineryIdTextView;
	private TextView userNameTextView;
	private EditText baseIdEditTextView;
	private EditText moteIdEditTextView;
	private EditText tankIdEditTextView;
	private Spinner fermentationTypeSpinner;
	private ScrollView mScrollView;
	private RelativeLayout mRelativeLayout;
	private TableLayout mTableLayout;
	private Button okay;
	private Button cancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_fermentation);
		initWidgets();
		initListener();
		initParameters();

	}

	// TODO: delete it
	private void dummy() {
		// Dummy data
		winery = new Winery();
		user = new User();
		winery.setName("fake winery name");
		winery.setWid(000);
		user.setName("fake user name");
	}

	private void initParameters() {
		MyApplication myApp = (MyApplication) getApplicationContext();
		winery = myApp.getWinery();
		user = myApp.getUser();
		ferment = new Ferment();

		wineryNameTextView.setText(winery.getName());
		wineryIdTextView.setText(winery.getWid() + "");
		userNameTextView.setText(user.getName());
	}

	private void initWidgets() {
		mScrollView = (ScrollView) findViewById(R.id.add_fermentation_layout);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.add_fermentation_relative_layout);
		mTableLayout = (TableLayout) findViewById(R.id.add_fermentation_table_layout);
		wineryNameTextView = (TextView) findViewById(R.id.add_fermentation_winery_name_id);
		wineryIdTextView = (TextView) findViewById(R.id.add_fermentation_winery_id_id);
		userNameTextView = (TextView) findViewById(R.id.add_fermentation_user_name_id);
		baseIdEditTextView = (EditText) findViewById(R.id.add_fermentation_base_id_id);
		moteIdEditTextView = (EditText) findViewById(R.id.add_fermentation_mote_id_id);
		tankIdEditTextView = (EditText) findViewById(R.id.add_fermentation_tank_id_id);
		okay = (Button) findViewById(R.id.add_fermentation_fermentation_Button_okay);
		cancel = (Button) findViewById(R.id.add_fermentation_fermentation_Button_cancel);
		fermentationTypeSpinner = (Spinner) findViewById(R.id.add_fermentation_fermentation_type_id);
		fermentationTypeSpinner.setAdapter(new ArrayAdapter<String>(
				this,
				android.R.layout.simple_spinner_item,
				getFermentationType()
		));
	}

	private void initListener() {
		mScrollView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard();
			}
		});

		mRelativeLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard();
			}
		});

		mTableLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyboard();
			}
		});

		okay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog("confirm");
			}
		});

		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog("cancel");
			}
		});

		fermentationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Log.e("testing", position + "");
				hideKeyboard();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void addFerment(){
		if (mAddFTask != null) {
			return;
		}

		// Reset errors.
		wineryNameTextView.setError(null);
		wineryIdTextView.setError(null);
		userNameTextView.setError(null);
		baseIdEditTextView.setError(null);
		moteIdEditTextView.setError(null);
		tankIdEditTextView.setError(null);

		// Store values at the time of the login attempt.
		String sWineryName = wineryNameTextView.getText().toString();
		String sWineryId = wineryIdTextView.getText().toString();
		String sUserName = userNameTextView.getText().toString();
		String sBaseId = baseIdEditTextView.getText().toString();
		String sMoteId = moteIdEditTextView.getText().toString();
		String sTankId = tankIdEditTextView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(sWineryName) || TextUtils.isEmpty(sWineryId)||TextUtils.isEmpty(sUserName)) {
			Toast.makeText(this,"The basic information is not correct, " +
					"please exit this page and re-enter to solve this problem.",Toast.LENGTH_LONG).show();
			cancel = true;
		}

		if (TextUtils.isEmpty(sBaseId)) {
			baseIdEditTextView.setError(getString(R.string.error_field_required));
			focusView = baseIdEditTextView;
			cancel = true;
		}
		if (TextUtils.isEmpty(sMoteId)) {
			moteIdEditTextView.setError(getString(R.string.error_field_required));
			focusView = moteIdEditTextView;
			cancel = true;
		}
		if (TextUtils.isEmpty(sTankId)) {
			tankIdEditTextView.setError(getString(R.string.error_field_required));
			focusView = tankIdEditTextView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don`t attempt to login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			moteId = Integer.parseInt(moteIdEditTextView.getText().toString());
			tankId = tankIdEditTextView.getText().toString();
			baseId = Integer.parseInt(baseIdEditTextView.getText().toString());
			ferment.setMoteId(moteId);
			ferment.setTankNumber(tankId);
			mAddFTask = new AddFermentation(user,ferment,winery.getWid(),baseId);
			mAddFTask.execute((Void) null);
		}
	}

	private void dialog(String s){
		String message = "";
		String title = "";
		if(s.equals("cancel")){
			message = "Are you sure want to cancel this?";
			title = "Cancel";
		}
		else{
			message="Are you sure want to save this to the winery list?";
			title ="Confirm";
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(AddFermentationActivity.this);
		builder.setMessage(message);
		builder.setTitle(title);
		if(s.equals("cancel")) {
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					AddFermentationActivity.this.finish();
				}
			});
		}
		else
		{
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					addFerment();
				}
			});
		}

		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	private ArrayList<String> getFermentationType() {
		ArrayList<String> list = new ArrayList<>();
		list.add("type 1");
		list.add("type 2");
		list.add("type 3");
		return list;
	}

	private void hideKeyboard() {
		mRelativeLayout.requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(AddFermentationActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_add_fermentation, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private class AddFermentation extends AsyncTask<Void, Void, Boolean> {

		User aUser;
		Ferment aFerment;
		int aWineryId;
		int aBaseId;
		Map<String,Object> result;

		AddFermentation(User aUser, Ferment aFerment, int aWineryId, int aBaseId) {
			this.aUser = aUser;
			this.aFerment = aFerment;
			this.aWineryId = aWineryId;
			this.aBaseId = aBaseId;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ServerManager sm = new ServerManager();
			result = sm.addFermentation(aUser,aFerment,aWineryId,aBaseId);
			if((boolean)result.get(Constants.RESULT))
			{
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {

			mAddFTask = null;
			if (success) {
				AlertDialog.Builder builder = new AlertDialog.Builder(AddFermentationActivity.this);
				builder.setTitle("Congratulation");
				builder.setMessage("Adding fermentation successfully!");
				builder.setNegativeButton("Okay",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AddFermentationActivity.this.finish();
					}
				});
				builder.create().show();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(AddFermentationActivity.this);
				builder.setTitle("Error");
				builder.setMessage((String) result.get(Constants.INFO));
				builder.setNegativeButton("Okay",null);
				builder.create().show();
			}
		}

		@Override
		protected void onCancelled() {
			mAddFTask = null;
		}
	}
}
