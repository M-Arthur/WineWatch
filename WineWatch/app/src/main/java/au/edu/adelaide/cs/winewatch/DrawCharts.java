package au.edu.adelaide.cs.winewatch;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BaseEasingMethod;
import com.db.chart.view.animation.easing.quint.QuintEaseOut;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import au.edu.adelaide.cs.winewatch.JsonObject.Temperature;
import au.edu.adelaide.cs.winewatch.Tools.Constants;
import au.edu.adelaide.cs.winewatch.Tools.DummyData;


public class DrawCharts extends ActionBarActivity {

	private static boolean TESTING = Constants.TESTING;

	private final TimeInterpolator enterInterpolator = new DecelerateInterpolator(1.5f);
	private final TimeInterpolator exitInterpolator = new AccelerateInterpolator();

	private static final String DAY = "day";
	private static final String MONTH = "month";
	private static final String YEAR = "year";

	private List<Temperature> historyTemperatures;
	private int[] colors = new int[7];

	private void initialColor() {
		colors[0] = this.getResources().getColor(R.color.Cantaloupe);
		colors[1] = this.getResources().getColor(R.color.Honeydew);
		colors[2] = this.getResources().getColor(R.color.Lavender);
		colors[3] = this.getResources().getColor(R.color.Snow);
		colors[4] = this.getResources().getColor(R.color.Salmon);
		colors[5] = this.getResources().getColor(R.color.Banana);
		colors[6] = this.getResources().getColor(R.color.Sky);
	}

	private Spinner dateTypeSpinner;
	private Spinner dateRangeSpinner;
	private Spinner tempSpinner;
	private ImageButton mPlayBtn;
	private static TextView dateTextView;

	private boolean mNewInstance;

	/**
	 * Order
	 */
	private static ImageButton mOrderBtn;
	private final static int[] beginOrder = {0, 1, 2, 3, 4, 5, 6};
	private final static int[] middleOrder = {3, 2, 4, 1, 5, 0, 6};
	private final static int[] endOrder = {6, 5, 4, 3, 2, 1, 0};
	private static float mCurrOverlapFactor;
	private static int[] mCurrOverlapOrder;
	private static float mOldOverlapFactor;
	private static int[] mOldOverlapOrder;


	/**
	 * Ease
	 */
	private static ImageButton mEaseBtn;
	private static BaseEasingMethod mCurrEasing;
	private static BaseEasingMethod mOldEasing;


	/**
	 * Enter
	 */
	private static ImageButton mEnterBtn;
	private static float mCurrStartX;
	private static float mCurrStartY;
	private static float mOldStartX;
	private static float mOldStartY;


	/**
	 * Alpha
	 */
	private static ImageButton mAlphaBtn;
	private static int mCurrAlpha;
	private static int mOldAlpha;

	private final Runnable mEnterEndAction = new Runnable() {
		@Override
		public void run() {
			mPlayBtn.setEnabled(true);
		}
	};
	private Handler mHandler;
	private final Runnable mExitEndAction = new Runnable() {
		@Override
		public void run() {
			mHandler.postDelayed(new Runnable() {
				public void run() {
					mOldOverlapFactor = mCurrOverlapFactor;
					mOldOverlapOrder = mCurrOverlapOrder;
					mOldEasing = mCurrEasing;
					mOldStartX = mCurrStartX;
					mOldStartY = mCurrStartY;
					mOldAlpha = mCurrAlpha;
					updateLineChart();
				}
			}, 500);
		}
	};


	/**
	 * Line
	 */
	private int LINE_MAX = 0;
	private int LINE_MIN = 40;
	// TODO: change it to the right value
	private String[] lineLabelsMonth;
	private String[] lineLabelsDay;
	private String[] lineLabelsYear;
	private String[] lineLabel;
	private float[][] lineValues;
	private int[][] times;
	private static LineChartView mLineChart;
	private Paint mLineGridPaint;
	private TextView mLineTooltip;
	private boolean flag = false;
	private String time;
	private int lineIndex = 0;


	private final OnEntryClickListener lineEntryListener = new OnEntryClickListener() {
		@Override
		public void onClick(int setIndex, int entryIndex, Rect rect) {
			setIndex = 0;
			if (mLineTooltip == null)
				showLineTooltip(setIndex, entryIndex, rect);
			else
				dismissLineTooltip(setIndex, entryIndex, rect);
		}
	};

	private final View.OnClickListener lineClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mLineTooltip != null)
				dismissLineTooltip(-1, -1, null);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draw_charts);

		dateTextView = (TextView) findViewById(R.id.charts_date);

		if (TESTING) {
//			historyTemperatures = DummyData.getHistoryTemperatures();
			historyTemperatures = DummyData.getSampleHT();
		} else {
			MyApplication myApp = (MyApplication) getApplicationContext();
			historyTemperatures = myApp.getHistoryTemperatures();
			Log.e("size", historyTemperatures.size() + "");
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		time = sdf.format(new Date());
		dateTextView.setText(time);

		mNewInstance = false;
		mHandler = new Handler();


		dateTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				time = s.toString();
				lineIndex=0;
				getValuesForDay(time, historyTemperatures, lineIndex);
				updateLineChart();
				// TODO: get values,update lineValues.
			}
		});
		dateTypeSpinner = (Spinner) findViewById(R.id.charts_type_select);
		dateTypeSpinner.setAdapter(new ArrayAdapter<String>(
				this,
				R.layout.my_spinner_item,
				new String[]{"hour", "day", "month"}
		));
		tempSpinner = (Spinner) findViewById(R.id.charts_temp_select);

		mPlayBtn = (ImageButton) findViewById(R.id.play);
		mPlayBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPlayBtn.setImageResource(R.drawable.play);
				mPlayBtn.setBackgroundResource(R.drawable.button);
				mPlayBtn.setEnabled(false);

				mLineChart.dismissAllTooltips();
				mLineTooltip = null;

				getValuesForDay(time, historyTemperatures, lineIndex);
				mLineChart.dismiss(getAnimation(false).setEndAction(mExitEndAction));
				mNewInstance = !mNewInstance;
			}
		});

		mCurrOverlapFactor = 1;
		mCurrEasing = new QuintEaseOut();
		mCurrStartX = -1;
		mCurrStartY = 0;
		mCurrAlpha = -1;

		mOldOverlapFactor = 1;
		mOldEasing = new QuintEaseOut();
		mOldStartX = -1;
		mOldStartY = 0;
		mOldAlpha = -1;

		initialColor();
		initialTemperature();
		initLineChart();
		int number = historyTemperatures.get(0).getTemperatures().length;
		final String[] ts = new String[number];
		for (int i = 0; i < number; i++) {
			ts[i] = "Temperature " + (i+1);
		}
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.my_spinner_item, ts) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null) {
					Context mContext = this.getContext();
					LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					v = vi.inflate(R.layout.my_spinner_item, null);
				}

				TextView tv = (TextView) v.findViewById(R.id.charts_spinner);
				tv.setText(ts[position]);
				tv.setGravity(Gravity.CENTER);
				if (position >= 0) {
					tv.setTextColor(colors[position]);
				}
				return v;
			}

			@Override
			public boolean isEnabled(int position) {
				if (position == -1) {
					return false;
				} else {
					return true;
				}
			}

			@Override
			public boolean areAllItemsEnabled() {
				return false;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null) {
					Context mContext = this.getContext();
					LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					v = vi.inflate(R.layout.my_spinner_item, null);
				}

				TextView tv = (TextView) v.findViewById(R.id.charts_spinner);
				tv.setText(ts[position]);
				if (position >= 0) {
					tv.setTextColor(colors[position]);
				}
				return v;
			}
		};
		tempSpinner.setAdapter(spinnerAdapter);
		tempSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				lineIndex = position;
				Log.e("poistion",position+"");
				getValuesForDay(time, historyTemperatures, lineIndex);
//				updateValues(mLineChart, position);
				mLineChart.dismiss(getAnimation(false).setEndAction(mExitEndAction));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		updateLineChart();
	}

	private void initialTemperature() {
		lineLabelsDay = new String[]{"", "0:00", "", "", "", "4:00", "", "", "", "8:00", "",
				"", "", "12:00", "", "", "", "16:00", "", "", "", "20:00", "", "", "23:00", ""};
		lineLabelsMonth = new String[]{"", "1st", "", "", "", "5th.", "", "", "", "", "10th.", "", "",
				"", "", "15th.", "", "", "", "", "20th", "", "", "", "", "25th.", "", "", "", "", "30th.", "", ""};
		lineLabelsYear = new String[]{"", "Jan.", "Feb.", "Mar.", "Apr.", "May", "Jun.", "Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec.", ""};

		getValuesForDay(time, historyTemperatures, lineIndex);
	}

	private boolean compareTime(String time1, String time2) {
		String[] t1 = time1.split("-");
		String[] t2 = time2.split("-");
		if (Integer.parseInt(t1[0]) != Integer.parseInt(t2[2]) ||
				Integer.parseInt(t1[1]) != Integer.parseInt(t2[1]) ||
				Integer.parseInt(t1[2]) != Integer.parseInt(t2[0])) {
			return false;
		}
		return true;
	}

	static int count = 1;

	private void getValuesForDay(String time, List<Temperature> historyTemperatures, int theOne) {
		lineLabel = lineLabelsDay;
		LINE_MAX = Integer.MIN_VALUE;
		LINE_MIN = Integer.MAX_VALUE;
		double[] t;
		String[] date;
		int n = 1;
		int length = lineLabel.length;
		lineValues = new float[n][length];
		times = new int[n][length];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < length; j++) {
				lineValues[i][j] = 0;
				times[i][j] = 0;
			}
		}
		int index;
		Log.e("count",count+"");
		count++;
		for (Temperature temp : historyTemperatures) {
			date = temp.getUpdatingTime().split(" ");
			if (compareTime(date[0], time)) {
				index = Integer.parseInt(date[1].split(":")[0]);
				t = temp.getTemperatures();
				lineValues[0][index + 1] += t[theOne];
				times[0][index + 1]++;
			}
		}
		float tp;
		for (int j = 0; j < lineValues[0].length; j++) {
			if (times[0][j] != 0) {
				tp = lineValues[0][j] / times[0][j];
				lineValues[0][j] = tp;
				if (LINE_MIN > tp && tp != 0) {
					LINE_MIN = (int) tp;
				}
				if (LINE_MAX < tp) {
					LINE_MAX = (int) tp;
				}
			}
		}
		if (LINE_MAX < LINE_MIN) {
			LINE_MAX = 20;
			LINE_MIN = 0;
		} else {
			LINE_MAX += 2;
			LINE_MIN -= 1;
		}
	}

	private void getValuesForDay(String time, List<Temperature> historyTemperatures) {
		lineLabel = lineLabelsDay;
		LINE_MAX = Integer.MIN_VALUE;
		LINE_MIN = Integer.MAX_VALUE;
		double[] t;
		String[] date;
		int n = historyTemperatures.get(0).getTemperatures().length;
		int length = lineLabel.length;
		lineValues = new float[n][length];
		times = new int[n][length];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < length; j++) {
				lineValues[i][j] = 0;
				times[i][j] = 0;
			}
		}
		int index;
		for (Temperature temp : historyTemperatures) {
			date = temp.getUpdatingTime().split(" ");
			if (compareTime(date[0], time)) {
				index = Integer.parseInt(date[1].split(":")[0]);
				t = temp.getTemperatures();
				for (int i = 0; i < t.length; i++) {
					lineValues[i][index + 1] += t[i];
					times[i][index + 1]++;
				}
			}
		}
		float tp;
		for (int i = 0; i < lineValues.length; i++) {
			for (int j = 0; j < lineValues[i].length; j++) {
				if (times[i][j] != 0) {
					tp = lineValues[i][j] / times[i][j];
					lineValues[i][j] = tp;
					if (LINE_MIN > tp && tp != 0) {
						LINE_MIN = (int) tp;
					}
					if (LINE_MAX < tp) {
						LINE_MAX = (int) tp;
					}
				}
			}
		}
		if (LINE_MAX < LINE_MIN) {
			LINE_MAX = 20;
			LINE_MIN = 0;
		} else {
			LINE_MAX += 2;
		}
	}

	private void getValues(String range, String type, List<Temperature> historyTemperatures, int ind) {
		LINE_MAX = 0;
		LINE_MIN = 40;
		flag = false;
		int typePosition;
		int typeDate;
		String sign;
		switch (type) {
			case DAY:
				lineLabel = lineLabelsDay;
				typePosition = 0;
				typeDate = 1;
				sign = ":";
				break;
			case MONTH:
				typePosition = 2;
				typeDate = 0;
				sign = "-";
				lineLabel = lineLabelsMonth;
				break;
			case YEAR:
				typePosition = 1;
				typeDate = 0;
				sign = "-";
				lineLabel = lineLabelsYear;
				break;
			default:
				typePosition = 2;
				typeDate = 0;
				sign = "-";
				lineLabel = lineLabelsMonth;
				break;
		}
		int n;
		int length = lineLabel.length;
		List<Temperature> tl = historyTemperatures;
		if (ind == 0) {
			n = tl.get(0).getTemperatures().length;
		} else {
			n = 1;
		}
		double[] t;
		lineValues = new float[n][length];
		String updateTime;
		String[] time;
		Temperature temp;
		int index;
		String[] timeD;
		String[] rangeD;
		float init = 0;
		boolean right = false;
		times = new int[n][length];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < length; j++) {
				lineValues[i][j] = init;
				times[i][j] = 1;
			}
		}
		for (int i = 0; i < tl.size(); i++) {
			temp = tl.get(i);
			t = temp.getTemperatures();
			updateTime = temp.getUpdatingTime();
			time = updateTime.split(" ");
			timeD = time[0].split("-");
			rangeD = range.split("-");
			right = true;
			for (int j = 0; j < rangeD.length; j++) {
				if (!rangeD[j].equals(timeD[j])) {
					right = false;
				}
			}
			if (right) {
				flag = true;
				index = Integer.parseInt(time[typeDate].split(sign)[typePosition]);
				if (n == 1) {
					lineValues[0][index] += t[ind - 1];
				} else {
					for (int j = 0; j < n; j++) {
						if (lineValues[j][index] == init) {
							lineValues[j][index] = (float) t[j];
						} else {
							lineValues[j][index] += t[j];
						}
						times[j][index]++;
					}
				}
			}
		}

		float tp;
		for (int i = 0; i < n; i++) {
			for (int j = 1; j < length - 1; j++) {
				tp = lineValues[i][j] / times[i][j];
				if (LINE_MAX < tp) {
					LINE_MAX = (int) tp;
				}
				if (LINE_MIN > tp) {
					LINE_MIN = (int) tp;
				}
				lineValues[i][j] = tp;
			}
		}
		LINE_MAX += 2;
		if (LINE_MIN > LINE_MAX) {
			LINE_MIN = LINE_MAX - 1;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_draw_charts, menu);
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

	private Animation getAnimation(boolean newAnim) {
		if (newAnim)
			return new Animation()
					.setAlpha(mCurrAlpha)
					.setEasing(mCurrEasing)
					.setOverlap(mCurrOverlapFactor, mCurrOverlapOrder)
					.setStartPoint(mCurrStartX, mCurrStartY);
		else
			return new Animation()
					.setAlpha(mOldAlpha)
					.setEasing(mOldEasing)
					.setOverlap(mOldOverlapFactor, mOldOverlapOrder)
					.setStartPoint(mOldStartX, mOldStartY);
	}

	/*------------------------------------*
	 *              LINECHART             *
	 *------------------------------------*/

	private void initLineChart() {

		mLineChart = (LineChartView) findViewById(R.id.linechart);
		mLineChart.setOnEntryClickListener(lineEntryListener);
		mLineChart.setOnClickListener(lineClickListener);

		mLineGridPaint = new Paint();
		mLineGridPaint.setColor(this.getResources().getColor(R.color.line_grid));
		mLineGridPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
		mLineGridPaint.setStyle(Paint.Style.STROKE);
		mLineGridPaint.setAntiAlias(true);
		mLineGridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));
	}

	private void showRight(int index, List<Integer> show) {
		show.clear();
		boolean initial = true;
		for (int i = 1; i < lineValues[index].length - 1; i++) {
			if (initial && times[index][i] != 0) {
				show.add(i);
				initial = false;
			}
			if (!initial && times[index][i] == 0) {
				show.add(i);
				initial = true;
			}
		}
		if (!initial) {
			show.add(lineValues[index].length - 1);
		}
	}

	private void updateLineChart() {

		mLineChart.reset();
		LineSet dataSet;
		List<Integer> show = new ArrayList<>();
		for (int i = 0; i < lineValues.length; i++) {
			showRight(i, show);
			for (int j = 0; j < show.size() / 2; j++) {
				dataSet = new LineSet();
				dataSet.addPoints(lineLabel, lineValues[i]);
				dataSet.setDots(true)
						.setDotsColor(this.getResources().getColor(R.color.line_bg))
						.setDotsRadius(Tools.fromDpToPx(5))
						.setDotsStrokeThickness(Tools.fromDpToPx(2))
						.setDotsStrokeColor(colors[lineIndex])
						.setSmooth(true).setLineColor(colors[lineIndex])
						.setLineThickness(Tools.fromDpToPx(3))
						.beginAt(show.get(j * 2)).endAt(show.get(j * 2 + 1));
				mLineChart.addData(dataSet);
			}
		}
		if (mLineChart.getData().size() == 0) {
			dataSet = new LineSet();
			float[] defaultValue = new float[lineLabel.length];
			dataSet.addPoints(lineLabel, defaultValue);
			dataSet.setSmooth(true).setLineColor(getResources().getColor(R.color.line))
					.setLineThickness(Tools.fromDpToPx(3))
					.beginAt(1).endAt(2);
			mLineChart.addData(dataSet);
		}
		mLineChart.setBorderSpacing(Tools.fromDpToPx(4))
				.setGrid(LineChartView.GridType.HORIZONTAL, mLineGridPaint)
				.setXAxis(false)
				.setXLabels(XController.LabelPosition.OUTSIDE)
				.setYAxis(false)
				.setYLabels(YController.LabelPosition.OUTSIDE)
				.setAxisBorderValues(LINE_MIN, LINE_MAX, 1)
				.setLabelsFormat(new DecimalFormat("##'C'"))
				.show(getAnimation(true).setEndAction(mEnterEndAction))
		//.show()
		;

//		mLineChart.animateSet(0, new DashAnimation());
	}


	@SuppressLint("NewApi")
	private void showLineTooltip(int setIndex, int entryIndex, Rect rect) {

		DecimalFormat df = new DecimalFormat("0.0");

		mLineTooltip = (TextView) getLayoutInflater().inflate(R.layout.circular_tooltip, null);
		mLineTooltip.setText(df.format(lineValues[setIndex][entryIndex]));

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) Tools.fromDpToPx(35), (int) Tools.fromDpToPx(35));
		layoutParams.leftMargin = rect.centerX() - layoutParams.width / 2;
		layoutParams.topMargin = rect.centerY() - layoutParams.height / 2;
		mLineTooltip.setLayoutParams(layoutParams);

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			mLineTooltip.setPivotX(layoutParams.width / 2);
			mLineTooltip.setPivotY(layoutParams.height / 2);
			mLineTooltip.setAlpha(0);
			mLineTooltip.setScaleX(0);
			mLineTooltip.setScaleY(0);
			mLineTooltip.animate()
					.setDuration(150)
					.alpha(1)
					.scaleX(1).scaleY(1)
					.rotation(360)
					.setInterpolator(enterInterpolator);
		}

		mLineChart.showTooltip(mLineTooltip);
	}


	@SuppressLint("NewApi")
	private void dismissLineTooltip(final int setIndex, final int entryIndex, final Rect rect) {

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mLineTooltip.animate()
					.setDuration(100)
					.scaleX(0).scaleY(0)
					.alpha(0)
					.setInterpolator(exitInterpolator).withEndAction(new Runnable() {
				@Override
				public void run() {
					mLineChart.removeView(mLineTooltip);
					mLineTooltip = null;
					if (entryIndex != -1)
						showLineTooltip(setIndex, entryIndex, rect);
				}
			});
		} else {
			mLineChart.dismissTooltip(mLineTooltip);
			mLineTooltip = null;
			if (entryIndex != -1)
				showLineTooltip(setIndex, entryIndex, rect);
		}
	}


	private void updateValues(LineChartView chartView, final int position) {
		if (position == 0) {
			for (int i = 0; i < lineValues.length; i++) {
				chartView.updateValues(i, lineValues[i]);
			}
		} else {
			float[] temp = new float[lineLabel.length];
			for (int i = 0; i < chartView.getData().size(); i++) {
				if (i != position - 1) {
					chartView.updateValues(i, temp);
				} else {
					chartView.updateValues(i, lineValues[i]);
				}
			}
		}
		chartView.notifyDataUpdate();
	}

	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}

	public static class DatePickerFragment extends DialogFragment
			implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			dateTextView.setText(day + "-" + (month + 1) + "-" + year);
		}
	}

	private static void getValue() {

	}

}
