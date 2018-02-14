package au.edu.adelaide.cs.winewatch;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.edu.adelaide.cs.winewatch.JsonObject.Ferment;
import au.edu.adelaide.cs.winewatch.JsonObject.Temperature;
import au.edu.adelaide.cs.winewatch.JsonObject.User;
import au.edu.adelaide.cs.winewatch.JsonObject.Winery;
import au.edu.adelaide.cs.winewatch.Tools.Constants;
import au.edu.adelaide.cs.winewatch.Tools.DummyData;
import au.edu.adelaide.cs.winewatch.Tools.ServerManager;
import au.edu.adelaide.cs.winewatch.Tools.Tool;


public class TankListActivity extends ActionBarActivity {

	private static boolean TESTING = Constants.TESTING;

	private static final int verticalRange = 200;
	private static final int horizontalRange = 250;
	private static final String TITLE_LIST = "title_list";
	private static final String LIST_NAME = "Tank";
	private static final int NUMBER_OF_CIRCLE_IN_ONE_LINE = 4;
	private static int interval = 24;

	private String menuTitle;
	private static int scWidth;
	private static int scHeight;
	private static Bitmap tankListBitmap;
	private static int page = 1;

	private static int wineryId;
	private static int userId;
	private static String token;
	private static Toast toast;

	private static List<String> ts = new ArrayList<>();
	private static List<Ferment> fl = new ArrayList<>();

	private static Context mContext;

	private ViewPager mPager;
	private static PagerAdapter mPagerAdapter;
	private static int numPage = 1;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tank_list);

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setPageTransformer(true, new ZoomOutPageTransformer());
		mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());

		mPager.setAdapter(mPagerAdapter);
		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// When changing pages, reset the action bar actions since they are dependent
				// on which page is currently active. An alternative approach is to have each
				// fragment expose actions itself (rather than the activity exposing actions),
				// but for simplicity, the activity provides the actions in this sample.
				invalidateOptionsMenu();
			}
		});

		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		scWidth = size.x;
		scHeight = size.y;

		mContext = getApplicationContext();

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (TESTING) {
			initDummyParams();
		} else {
			initPreviousParams();
		}
	}

	private void initDummyParams() {
		User user = DummyData.getUser();
		Winery winery = DummyData.getExampleWinery();
		userId = user.getUid();
		token = user.getToken();
		wineryId = winery.getWid();
		menuTitle = winery.getName();
	}

	private void initPreviousParams() {
		MyApplication myApp = (MyApplication) getApplicationContext();
		User user = myApp.getUser();
		userId = user.getUid();
		token = user.getToken();
		Winery winery = myApp.getWinery();
		wineryId = winery.getWid();
		menuTitle = winery.getName();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_tank_list, menu);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(menuTitle);

//		menu.findItem(R.id.action_previous).setEnabled(mPager.getCurrentItem() > 0);
//
//		// Add either a "next" or "finish" button to the action bar, depending on which page
//		// is currently selected.
//		MenuItem item = menu.add(Menu.NONE, R.id.action_next, Menu.NONE,
//				(mPager.getCurrentItem() == mPagerAdapter.getCount() - 1)
//						? R.string.action_finish
//						: R.string.action_next);
//		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add:
				Intent intent = new Intent();
				intent.setClass(this, AddFermentationActivity.class);
				startActivity(intent);

				return true;
			case R.id.action_search:
				// TODO: implement search action
				Toast.makeText(this, "Search action.", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.action_logout:
				// TODO: implement logout action
				Toast.makeText(this, "Logout action.", Toast.LENGTH_SHORT).show();
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

	private static Map<String, Object> getFermentList() {
		Map<String, Object> map = new HashMap<>();
		ArrayList<Ferment> fermentList;
		ArrayList<String> contentList = new ArrayList<>();
		ServerManager sm = new ServerManager();

		Map result = sm.getFermentList(userId, token, wineryId);
		if ((boolean) result.get(Constants.RESULT)) {
			map.put("boolean", true);
			fermentList = (ArrayList<Ferment>) result.get(Constants.RESULT_OBJECT);
			map.put("ferment_list", fermentList);
			for (int i = 0; i < fermentList.size(); i++) {
				contentList.add(LIST_NAME + fermentList.get(i).getTankNumber());
			}
			map.put("ferment_title", contentList);
		} else {
			map.put("boolean", false);
		}
		return map;
	}

	private static float xd = 0;
	private static float yd = -1;
	private static float xu = -1;
	private static float yu = -1;
	private static float xm = 0;
	private static float ym = -2;
	private static boolean flag = false;
	private static boolean back = false;

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class TankListFragment extends android.app.Fragment {

		public static final String ARG_PAGE = "page";
		ListView tankList;
		private int mPageNumber;

		public static TankListFragment create(int pageNumber) {
			TankListFragment fragment = new TankListFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_PAGE, pageNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public TankListFragment() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mPageNumber = getArguments().getInt(ARG_PAGE);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tank_list, container, false);
			ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.tank_list_progress);
			final ImageView imageView = (ImageView) rootView.findViewById(R.id.tank_list_circle);

			final MyHandler handler = new MyHandler(getActivity(), imageView, progressBar);//TODO: delete this line

			if (mPageNumber + 1 == 1) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Message msg = new Message();
						if (TESTING) {
							try {
								new Thread().sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							msg.arg1 = 1;
							List<Ferment> fermentList = DummyData.getFermentList();
							fl = fermentList;
							ArrayList<String> titles = new ArrayList<String>();
							for (int i = 0; i < fl.size(); i++) {
								titles.add(fl.get(i).getTankNumber());
							}
							ts = titles;
							tankListBitmap = drawList(ts, fl, mPageNumber + 1, -1);
						} else {
							Map<String, Object> map = getFermentList();
							if ((boolean) map.get("boolean")) {
								msg.arg1 = 1;
								ArrayList<String> titles = (ArrayList<String>) map.get("ferment_title");
								ArrayList<Ferment> fermentL = (ArrayList<Ferment>) map.get("ferment_list");
								ts = titles;
								fl = fermentL;
//								fl.addAll(DummyData.getFermentList());
								Ferment tempF;
								Map<String, Object> result;

								ServerManager sm = new ServerManager();
								for (int i = 0; i < fermentL.size(); i++) {
									tempF = fermentL.get(i);
									result = sm.getTemperatures(userId, token, wineryId, tempF.getFermentId());
									if ((boolean) result.get(Constants.RESULT)) {
										Temperature temp = (Temperature) result.get(Constants.RESULT_OBJECT);
										fl.get(i).setTemperatures(temp);
									} else {
										fl.get(i).setTemperatures(null);
									}
								}
								tankListBitmap = drawList(ts, fl, mPageNumber + 1, -1);
							} else {
								msg.arg1 = 0;
							}
						}
						handler.sendMessage(msg);
					}
				}).start();
			} else {
				imageView.setImageBitmap(drawList(ts, fl, mPageNumber + 1, -1));
				progressBar.setVisibility(View.GONE);
			}
			imageView.setOnTouchListener(
					new View.OnTouchListener() {
						@Override
						public boolean onTouch(View v, MotionEvent event) {

							switch (event.getAction()) {
								case (MotionEvent.ACTION_DOWN):
									xd = event.getX();
									yd = event.getY();
									flag = true;
									int indexD = findFermentByLocation(xd, yd, mPageNumber + 1, ts.size());
									if (indexD > 0) {
										imageView.setImageBitmap(drawList(ts, fl, mPageNumber + 1, indexD - 1));
										back = true;
									}
									return true;
								case (MotionEvent.ACTION_MOVE):
									imageView.setImageBitmap(drawList(ts, fl, mPageNumber + 1, -1));
									return true;
								case (MotionEvent.ACTION_UP):
									xu = event.getX();
									yu = event.getY();
									if (xd == xu && yd == yu) {
//										Log.e("action", "redirect");
										int index = findFermentByLocation(xu, yu, mPageNumber + 1, ts.size());
										if (index > 0) {
											index -= 1;
											Intent intent = new Intent();
											intent.setClass(mContext, DetailsActivity.class);
											MyApplication myApp = (MyApplication) mContext.getApplicationContext();
											String fermentName = "default";
											if (!ts.isEmpty()) {
												fermentName = ts.get(index);
												myApp.setMenuTitle(fermentName);
											}
											Ferment ferment = new Ferment();
											if (!fl.isEmpty()) {
												ferment = fl.get(index);
												myApp.setFerment(ferment);
											}
											if (TESTING) {
												startActivity(intent);
//												AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//												builder.setTitle("Congratulation");
//												builder.setMessage("fermentName: " + fermentName + "\n" + "moteID: " + ferment.getMoteId());
//												builder.create().show();
											} else {
												startActivity(intent);
											}
										}

									}
//									Log.e("action", "Action was UP");
									return true;
								case (MotionEvent.ACTION_CANCEL):
//									Log.e("action", "Action was CANCEL");
									return true;
								case (MotionEvent.ACTION_OUTSIDE):
//									Log.e("action", "Movement occurred outside bounds " +
//											"of current screen element");
									return true;
								default:
									return false;
							}
						}
					}

			);
			return rootView;
		}

		public int getPageNumber() {
			return mPageNumber;
		}
	}

	private static Bitmap drawList(List<String> titles, List<Ferment> ferments, int pageNumber, int indexD) {

		float r = (float) scWidth / (3 * NUMBER_OF_CIRCLE_IN_ONE_LINE + 1);
		float width = 2 * r;
		float r1 = r * (float) 0.7;
		int nC = (int) ( (scHeight - 2 * width) / (3 * r));
		float height = 3 * r;
		float tempW = width;
		float tempH = height;
		Shader mShader;
		interval = nC * NUMBER_OF_CIRCLE_IN_ONE_LINE;
		int limitation = pageNumber * interval;
		int index = limitation - interval;

		Bitmap bitmap = Bitmap.createBitmap(scWidth, scHeight, Bitmap.Config.ARGB_8888);

		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setStyle(Paint.Style.FILL);


		Paint p2 = new Paint();
		p2.setAntiAlias(true);
		p2.setStyle(Paint.Style.FILL);
		p2.setColor(Color.WHITE);
		int textSize = 30;
		p2.setTextSize(textSize);


		Paint p3 = new Paint();
		p3.setAntiAlias(true);
		p3.setStyle(Paint.Style.STROKE);
		p3.setColor(Color.WHITE);

		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.TRANSPARENT);

		p.setColor(Color.parseColor("#FFAFAFAF"));
		double[] temp;
		int[] color;
		int size = limitation;
		Ferment ferment;
		while (index < size) {
			if (index >= fl.size()) {
				break;
			}
			for (int j = 1; j <= NUMBER_OF_CIRCLE_IN_ONE_LINE; j++) {
				if (index >= fl.size()) {
					break;
				}
				ferment = ferments.get(index);
				try {
					temp = tempMaxAndMin(ferments.get(index).getTemperatures().getTemperatures());
					color = Tool.getColor(temp);
					mShader = new LinearGradient(tempW, tempH - r, tempW, tempH + r, color, null, Shader.TileMode.REPEAT);
					p.setShader(mShader);
				} catch (Exception e) {
					p.setColor(Color.WHITE);
				}

				if (indexD == index) {
					canvas.drawCircle(tempW, tempH, r * (float) 0.9, p);
					canvas.drawCircle(tempW, tempH, r * (float) 0.9, p3);
					canvas.drawCircle(tempW, tempH, r1 * (float) 0.9, p2);
				} else {
					canvas.drawCircle(tempW, tempH, r, p);
					canvas.drawCircle(tempW, tempH, r, p3);
					canvas.drawCircle(tempW, tempH, r1, p2);
				}
				canvas.drawRect(tempW - r,
						tempH + r + (float) 0.6 * r - (float) 1.2 * textSize,
						tempW + r,
						tempH + r + (float) 0.6 * r + (float) 0.4 * textSize,
						p3);
				canvas.drawText(ferment.getTankNumber(), tempW - (float) 0.6 * r, tempH + r + (float) 0.6 * r, p2);
				tempW += 3 * r;

				index++;

			}
			tempW = width;
			tempH += 3 * r;
		}

		return bitmap;
	}

	private static double[] tempMaxAndMin(double[] temperatures) {
		double[] result = new double[2];
		double min = 100;
		double max = 0;
		for (int i = 0; i < temperatures.length; i++) {
			if (min > temperatures[i]) {
				min = temperatures[i];
			}
			if (max < temperatures[i]) {
				max = temperatures[i];
			}
		}
		result[0] = min;
		result[1] = max;
		return result;
	}

	private static int findFermentByLocation(float x, float y, int pageNumber, int n) {
		Ferment ferment = new Ferment();
		float r = (float) scWidth / (3 * NUMBER_OF_CIRCLE_IN_ONE_LINE + 1);
		float r1 = r * (float) 0.7;
		float width = 2 * r;
		int nC = (int) ( (scHeight - 2 * width) / (3 * r));
		float height = 3 * r;
		float tempW = width;
		float tempH = height;
		int col = 0;
		int row = 0;
		for (int j = 1; j <= NUMBER_OF_CIRCLE_IN_ONE_LINE; j++) {
			if (n < j) {
				return -1;
			}
			if (Math.abs(x - tempW) <= r) {
				col = j;
				break;
			}
			tempW += 3 * r;
		}
		if (col == 0) {
			return -1;
		}
		while (Math.abs(y - tempH + 2*r-r1) > r) {
			if (n < (row * 5 + col)) {
				return -1;
			}
			row++;
			tempH += 3 * r;
			if(row>=nC){
				return -1;
			}
		}
		int index = row * NUMBER_OF_CIRCLE_IN_ONE_LINE + col + (pageNumber - 1) * interval;
		return index;
	}

	private static class MyHandler extends Handler {

		private final Activity mActivity;
		private ListView mTitleList;
		private View mProgressView;
		private ImageView mImageView;
		private Bitmap mBitmap;

		private MyHandler(Activity activity) {
			mActivity = activity;
		}

		private MyHandler(Activity activity, ListView titleList, View progressView) {
			mActivity = activity;
			mTitleList = titleList;
			mProgressView = progressView;
		}

		// TODO: delete this method
		private MyHandler(Activity activity, ImageView imageView, View progressView) {
			mActivity = activity;
			mImageView = imageView;
			mProgressView = progressView;
		}

		private MyHandler(Activity activity, Bitmap bitmap, ImageView imageView, View progressView) {
			mActivity = activity;
			mImageView = imageView;
			mProgressView = progressView;
			mBitmap = bitmap;
		}


		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
				case 0:
					Toast.makeText(mContext, "There is no data.", Toast.LENGTH_LONG).show();
					break;
				case 1:
					int n = fl.size();
					int temp = n / interval;
					if (n % interval == 0) {
						numPage = temp;
					} else {
						numPage = temp + 1;
					}
					mPagerAdapter.notifyDataSetChanged();
					mImageView.setImageBitmap(tankListBitmap);
					break;
				default:
					Toast.makeText(mContext, "System ERROR!", Toast.LENGTH_LONG).show();
					break;
			}
			mProgressView.setVisibility(View.GONE);
		}
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public android.app.Fragment getItem(int position) {
			return TankListFragment.create(position);
		}

		@Override
		public int getCount() {
			return numPage;
		}
	}

	public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
		private static final float MIN_SCALE = 0.85f;
		private static final float MIN_ALPHA = 0.5f;

		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();
			int pageHeight = view.getHeight();

			if (position < -1) { // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0);

			} else if (position <= 1) { // [-1,1]
				// Modify the default slide transition to shrink the page as well
				float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
				float vertMargin = pageHeight * (1 - scaleFactor) / 2;
				float horzMargin = pageWidth * (1 - scaleFactor) / 2;
				if (position < 0) {
					view.setTranslationX(horzMargin - vertMargin / 2);
				} else {
					view.setTranslationX(-horzMargin + vertMargin / 2);
				}

				// Scale the page down (between MIN_SCALE and 1)
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

				// Fade the page relative to its size.
				view.setAlpha(MIN_ALPHA +
						(scaleFactor - MIN_SCALE) /
								(1 - MIN_SCALE) * (1 - MIN_ALPHA));

			} else { // (1,+Infinity]
				// This page is way off-screen to the right.
				view.setAlpha(0);
			}
		}
	}
}

