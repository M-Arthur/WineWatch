package au.edu.adelaide.cs.winewatch;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;

import au.edu.adelaide.cs.winewatch.JsonObject.Ferment;
import au.edu.adelaide.cs.winewatch.JsonObject.Temperature;
import au.edu.adelaide.cs.winewatch.JsonObject.User;
import au.edu.adelaide.cs.winewatch.JsonObject.Winery;
import au.edu.adelaide.cs.winewatch.Tools.Constants;
import au.edu.adelaide.cs.winewatch.Tools.DummyData;
import au.edu.adelaide.cs.winewatch.Tools.ServerManager;
import au.edu.adelaide.cs.winewatch.Tools.Tool;


public class DetailsActivity extends ActionBarActivity {

	private static boolean TESTING = Constants.TESTING;

	private static final int SUCCESS = 1;
	private static final int FAILURE = 2;
	private static final int REFRESH = 3;

	private String menuTitle;
	private static int wineryId;
	private static int fid;
	private static int userId;
	private static String token;

	private static MyHandler handler = new MyHandler();

	private static ImageView mImageView;
	private static Toast toast;
	private static View mProgressView;
	private static Context context;
	private TextView wineryNameTV;
	private TextView fermentationTV;
	private TextView tankIdTV;

	private static TextView updateTimeTV;
	private static TextView batteryTV;
	private static EditText intervalTV;
	private static Button updateIntervalButton;
	private static TableLayout mTableLayoutD;
	private static TableLayout mTableLayoutR;
	private static double[] temperatures;
	private static double battery;
	private static double interval;
	private static String updateTime;
	private View layoutView;

	private static List<Temperature> historyTemperatures;

	// TODO: delete testing arguments
	private static Ferment dummyFerment;

	@Override

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		mProgressView = findViewById(R.id.detail_process);
		mProgressView.setVisibility(View.VISIBLE);
		layoutView = findViewById(R.id.details_layout);
		mTableLayoutD = (TableLayout) findViewById(R.id.tank_detail_text_description);
		mTableLayoutR = (TableLayout) findViewById(R.id.tank_detail_text_recommendation);
		wineryNameTV = (TextView) findViewById(R.id.detail_winery_name);
		fermentationTV = (TextView) findViewById(R.id.detail_fermentation_name);
		tankIdTV = (TextView) findViewById(R.id.detail_tank_id);
		batteryTV = (TextView) findViewById(R.id.detail_battery);
		intervalTV = (EditText) findViewById(R.id.detail_interval);
		updateIntervalButton = (Button) findViewById(R.id.details_update_interval);
		updateTimeTV = (TextView) findViewById(R.id.detail_update_time);
		mTableLayoutD.setVisibility(View.GONE);
		mTableLayoutR.setVisibility(View.GONE);
		mImageView = (ImageView) findViewById(R.id.temperature_tank);
		mImageView.setVisibility(View.GONE);

		updateIntervalButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						ServerManager sm = new ServerManager();
						MyApplication myApp = (MyApplication) getApplicationContext();
						sm.changeInterval(myApp.getUser(),myApp.getFerment().getBaseId(),
								myApp.getFerment().getMoteId(),"sample",intervalTV.getText().toString().isEmpty()?"10":intervalTV.getText().toString());
						Message msg = new Message();
						msg.arg1 = REFRESH;
						hideKeyboard();
						handler.sendMessage(msg);
					}
				}).start();
			}
		});
		if (TESTING) {
			initDummyParams();
		} else {
			initPreviousParams();
		}
		new Thread(new RefreshThread()).start();
		context = this;
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private void initDummyParams() {
		Intent intent = new Intent();
		MyApplication myApp = (MyApplication) MyApplication.getAppContext();
		Ferment ferment = myApp.getFerment();
		if(ferment == null){
			ferment = DummyData.getFerment(10);
		}
		dummyFerment = ferment;
		menuTitle = ferment.getTankNumber();

		fid = ferment.getFermentId();
		User user = DummyData.getUser();
		userId = user.getUid();
		token = user.getToken();
		Winery winery = DummyData.getExampleWinery();
		wineryId = winery.getWid();
		wineryNameTV.setText(winery.getName());
		fermentationTV.setText(fid + "");
		tankIdTV.setText(ferment.getTankNumber());
	}

	private void initPreviousParams() {
		Intent intent = getIntent();
		// TODO: check the data is not equal to -1


		MyApplication myApp = (MyApplication) getApplicationContext();
		Ferment ferment = myApp.getFerment();
		menuTitle = myApp.getMenuTitle();
		fid = ferment.getFermentId();

		User user = myApp.getUser();
		userId = user.getUid();
		token = user.getToken();
		Winery winery = myApp.getWinery();
		wineryId = winery.getWid();
		wineryNameTV.setText(winery.getName());
		fermentationTV.setText(fid + "");
		tankIdTV.setText(ferment.getTankNumber());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_details, menu);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(menuTitle);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_more:
				// TODO: redirect to other view
				Intent intent = new Intent();
				MyApplication myApp = (MyApplication) getApplicationContext();
				myApp.setHistoryTemperatures(historyTemperatures);
				intent.setClass(this, DrawCharts.class);
				startActivity(intent);
				return true;
			case R.id.action_refresh:
				mProgressView.setVisibility(View.VISIBLE);
				mImageView.setVisibility(View.GONE);
				mTableLayoutD.setVisibility(View.GONE);
				mTableLayoutR.setVisibility(View.GONE);
				new Thread(new RefreshThread()).start();
				return true;
			case R.id.action_settings_main:
				// TODO: implement settings action
				Toast.makeText(this, "Settings action.", Toast.LENGTH_SHORT).show();
				return true;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	private static Bitmap drawTank(double[] temps) {

		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.temperature);
		int height = bmp.getHeight();
		int width = bmp.getWidth();
		float x = 0;
		float y = 0;
		float r = 50;
		int textSize = 30;
		int cap = 20;
		int[] temp = new int[2];
		Shader mShader;

		Bitmap bDraw = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		if (temps != null) {
			int[] color = Tool.getColor(temps);
			int columnHeight = height / (color.length + 1);
			Paint p = new Paint();
			p.setAntiAlias(true);
			p.setStyle(Paint.Style.FILL);

			Canvas canvas = new Canvas(bDraw);
			canvas.drawColor(Color.TRANSPARENT);

			p.setColor(Color.parseColor("#FFAFAFAF"));
			canvas.drawRect(width / 2 - 2 * r, y, width / 2 + 2 * r, y + cap, p);

			y += cap;
			for (int i = 0; i < color.length; i++) {

				temp[0] = color[i];
				if (i + 1 < color.length) {
					temp[1] = color[i + 1];
				} else {
					temp[1] = color[i];
				}
				mShader = new LinearGradient(width / 2, y, width / 2, y + columnHeight, temp, null, Shader.TileMode.REPEAT);
				p.setShader(mShader);
//			p.setColor(color[i]);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					canvas.drawRoundRect(x, y, width, y + columnHeight, 20, 20, p);
				} else {
					canvas.drawRect(x, y, width, y + columnHeight, p);
				}
				y += columnHeight;
			}
			y = (float) (0.5 * columnHeight) + cap;
			p.reset();
			p.setAntiAlias(true);
			p.setStyle(Paint.Style.FILL);
			for (int i = 0; i < color.length; i++) {
				p.setColor(Color.WHITE);
				canvas.drawCircle(width / 2, y, r, p);
				p.setColor(Color.BLACK);
				p.setTextSize(textSize);
				canvas.drawText((int) temps[i] + "", width / 2 - textSize / 2, y + textSize / 2, p);
				y += columnHeight;
			}
		}
		return bDraw;
	}

	private static double[] getTemperature() {
		// TODO: get temperature from the server

		DecimalFormat df = new DecimalFormat(".00");
		Random random = new Random();
		double[] ts = new double[7];
		for (int j = 0; j < ts.length; j++) {
			ts[j] = Double.parseDouble(df.format(random.nextDouble() * 40 + 5));
		}
		return ts;
	}

	private static int calculateBitmapSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest BitmapSize that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	private static Bitmap decodeSampleBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateBitmapSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	// TODO: Re-write it with fuzzy logic
	private static int temperatureState(double t) {
		if (t < 10 || t > 40) {
			return R.drawable.ic_temperature_status_error;
		} else if (t < 15 || t > 35) {
			return R.drawable.ic_temperature_status_unusual;
		} else {
			return R.drawable.ic_temperature_status_normal;
		}
	}


	// TODO: re-write it to use it
	private static class RefreshThread implements Runnable {
		public void run() {
			Message message = new Message();
			Bundle bundle = new Bundle();
			if (TESTING) {
				message.arg1 = SUCCESS;
				Temperature temp = dummyFerment.getTemperatures();
				temperatures = temp.getTemperatures();
				updateTime = temp.getUpdatingTime();
				battery = temp.getBattery();
				interval = temp.getInterval();
			} else {
				ServerManager sm = new ServerManager();
				Map<String, Object> result = sm.getTemperatures(userId, token, wineryId, fid);
				Log.e("detail_tank", (boolean) result.get(Constants.RESULT) + "");
				if ((boolean) result.get(Constants.RESULT)) {
					message.arg1 = SUCCESS;
					Temperature temp = (Temperature) result.get(Constants.RESULT_OBJECT);
					historyTemperatures = (List<Temperature>)result.get(Constants.RESULT_JSON_OBJECT_HISTORY);
					temperatures = temp.getTemperatures();
					updateTime = temp.getUpdatingTime();
					battery = temp.getBattery();
					interval = temp.getInterval();
				} else {
					message.arg1 = FAILURE;
					bundle.putString("error_message", (String) result.get(Constants.INFO));
				}
				message.setData(bundle);
			}
			handler.sendMessage(message);
		}
	}
	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(layoutView.getWindowToken(), 0);
	}
	// TODO: modify it
	private static class MyHandler extends Handler {

		private MyHandler() {
		}

		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			switch (msg.arg1) {
				case SUCCESS:
					updateIntervalButton.setClickable(true);
					updateTimeTV.setText(updateTime);
					DecimalFormat format = new DecimalFormat("0.00");
					batteryTV.setText(format.format(battery)+" V");
					if(interval>1) {
						intervalTV.setText(interval+"");
					}
					else{
						intervalTV.setText(interval+"");
					}
					mImageView.setImageBitmap(drawTank(temperatures));
					mProgressView.setVisibility(View.GONE);
					mImageView.setVisibility(View.VISIBLE);
					mTableLayoutD.setVisibility(View.VISIBLE);
					mTableLayoutR.setVisibility(View.VISIBLE);
					break;
				case FAILURE:
					mProgressView.setVisibility(View.GONE);
					toast = Toast.makeText(MyApplication.getAppContext(), bundle.getString("error_message"), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 10, 10);
					toast.show();
					break;
				case REFRESH:
					updateIntervalButton.setClickable(false);
					Toast.makeText(MyApplication.getAppContext(), "Please wait for a minute",Toast.LENGTH_SHORT);
//					new Thread(new RefreshThread()).start();
					break;
				default:
					break;
			}
		}
	}
}
