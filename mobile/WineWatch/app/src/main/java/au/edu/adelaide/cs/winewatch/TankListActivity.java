package au.edu.adelaide.cs.winewatch;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

	private static final String TITLE_LIST = "title_list";
	private static final String LIST_NAME = "Tank";
	private static final int NUMBER_OF_CIRCLE_IN_ONE_LINE = 6;
	private static final int interval = 35;

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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tank_list);
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_tank_list, menu);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(menuTitle);
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
	private static float yd = 0;
	private static float xu = 1;
	private static float yu = 1;
	private static float xm = 1;
	private static float ym = 1;
	private static boolean flag = false;

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class TankListFragment extends Fragment {
		ListView tankList;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//			final ScrollView rootView = new ScrollView(getActivity());
			final ProgressBar mProgressView = new ProgressBar(getActivity());
			LinearLayout linearLayout = new LinearLayout(getActivity());

//			rootView.addView(linearLayout);
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			linearLayout.addView(mProgressView);
			int paddingV = (int) getResources().getDimension(R.dimen.components_vertical_margin);
			ImageView imageView = new ImageView(getActivity());
			imageView.setPadding(0, paddingV, 0, 0);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			linearLayout.addView(imageView);
//			final RefreshableView refreshableView = new RefreshableView(getActivity());
//			rootView.addView(refreshableView);
//			tankList = new ListView(getActivity());
//			final MyHandler handler = new MyHandler(getActivity(),tankList,mProgressView);
//			refreshableView.addView(tankList);

			final MyHandler handler = new MyHandler(getActivity(), imageView, mProgressView);//TODO: delete this line

			mProgressView.setVisibility(View.VISIBLE);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						new Thread().sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					Message msg = new Message();
					if (TESTING) {
						msg.arg1 = 1;
						List<Ferment> fermentList = DummyData.getFermentList();
						fl = fermentList;
						ArrayList<String> titles = new ArrayList<String>();
						for (int i = 0; i < fl.size(); i++) {
							titles.add(fl.get(i).getTankNumber());
						}
						ts = titles;
						tankListBitmap = drawList(ts, fl);
					} else {
						Map<String, Object> map = getFermentList();
						if ((boolean) map.get("boolean")) {
							msg.arg1 = 1;
							ArrayList<String> titles = (ArrayList<String>) map.get("ferment_title");
							ArrayList<Ferment> fermentL = (ArrayList<Ferment>) map.get("ferment_list");
							ts = titles;
							fl = fermentL;
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
							tankListBitmap = drawList(ts, fl);
						} else {
							msg.arg1 = 0;
						}
					}
					handler.sendMessage(msg);
				}
			}).start();

			imageView.setOnTouchListener(
					new View.OnTouchListener() {
						@Override
						public boolean onTouch(View v, MotionEvent event) {

							switch (event.getAction()) {
								case (MotionEvent.ACTION_DOWN):
									xd = event.getX();
									yd = event.getY();
									flag = true;
//									Log.e("action", "Action was DOWN");
									return true;
								case (MotionEvent.ACTION_MOVE):
									xm = event.getX();
									ym = event.getY();
									if (xd - xm > 300 && Math.abs(ym - yd) < 50 && flag) {
										page++;
										flag = false;
										if (page * interval - interval < fl.size() && page * interval - interval >= 0) {
											new Thread(new Runnable() {
												@Override
												public void run() {
													Message msg = new Message();
													msg.arg1 = 1;
													tankListBitmap = drawList(ts, fl);
													handler.sendMessage(msg);
												}
											}).start();
										} else {
											page--;
											Toast.makeText(getActivity(), "There is no more tanks", Toast.LENGTH_SHORT).show();
										}
									} else if (xm - xd > 300 && Math.abs(ym - yd) < 50 && flag) {
										page--;
										flag = false;
										if (page * interval - interval >= 0 && page * interval - interval < fl.size()) {
											new Thread(new Runnable() {
												@Override
												public void run() {
													Message msg = new Message();
													msg.arg1 = 1;
													tankListBitmap = drawList(ts, fl);
													handler.sendMessage(msg);
												}
											}).start();
										} else {
											page++;
											Toast.makeText(getActivity(), "There is no more tanks", Toast.LENGTH_SHORT).show();
										}
									}
									return true;
								case (MotionEvent.ACTION_UP):
									xu = event.getX();
									yu = event.getY();
									if (xd == xu && yd == yu) {
//										Log.e("action", "redirect");
										int index = findFermentByLocation(xu, yu, ts.size());
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
												intent.putExtra("index_of_ferment", index);
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

//			refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
//				@Override
//				public void onRefresh() {
//					new Thread(new Runnable() {
//						@Override
//						public void run() {
//							Message msg = new Message();
//							msg.arg1 = 1;
//							Bundle bundle = new Bundle();
//							bundle.putStringArrayList(TITLE_LIST, getTitleList());
//							msg.setData(bundle);
//							handler.sendMessage(msg);
//						}
//					}).start();
//					refreshableView.finishRefreshing();
//				}
//			}, 0);
//			tankList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//				@Override
//				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//					Intent intent = new Intent();
//					intent.setClass(getActivity(), DetailsActivity.class);
//					Ferment ferment = fermentList.get(position);
//					String fermentName = LIST_NAME+ferment.getTankNumber();
//					MyApplication myApp = (MyApplication) getActivity().getApplicationContext();
//					myApp.setMenuTitle(fermentName);
//					myApp.setFerment(ferment);
//					startActivity(intent);
//				}
//			});
			return linearLayout;
		}
	}

	private static Bitmap drawList(List<String> titles, List<Ferment> ferments) {


		int limitation = page * interval;
		int index = limitation - interval;

		float width = (float) scWidth / NUMBER_OF_CIRCLE_IN_ONE_LINE;
		float r = width * (float) 0.4;
		float r1 = r * (float) 0.7;
		int nH = (int) ((float) scHeight / width);
		float height = (float) scHeight / nH;
		float tempW = width;
		float tempH = height;
		Shader mShader;

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
		float hOffset = 0;
		float vOffset = 0;
		while (index < size) {
			if (index >= fl.size()) {
				break;
			}
			for (int j = 1; j < NUMBER_OF_CIRCLE_IN_ONE_LINE; j++) {
				if (index >= fl.size()) {
					break;
				}
				ferment = ferments.get(index);
				if (ferment.hasTemperatures() && ferment.getTemperatures().hasTemperatures()) {
					temp = tempMaxAndMin(ferments.get(index).getTemperatures().getTemperatures());
					color = Tool.getColor(temp);
					mShader = new LinearGradient(tempW, tempH - r, tempW, tempH + r, color, null, Shader.TileMode.REPEAT);
					p.setShader(mShader);
				} else {
					p.setColor(Color.WHITE);
				}

				canvas.drawCircle(tempW, tempH, r, p);
				canvas.drawCircle(tempW, tempH, r, p3);
				canvas.drawCircle(tempW, tempH, r1, p2);

				canvas.drawText(ferment.getTankNumber(), tempW - r, tempH + height * (float) 0.55, p2);
				tempW += width;

				index++;

			}
			tempW = width;
			tempH += height;
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

	private static int findFermentByLocation(float x, float y, int n) {
		Ferment ferment = new Ferment();
		float width = (float) scWidth / NUMBER_OF_CIRCLE_IN_ONE_LINE;
		int nH = (int) ((float) scHeight / width);
		float height = (float) scHeight / nH;
		float r = width * (float) 0.4;
		float tempW = width;
		float tempH = height;
		int col = 0;
		int row = 0;
		for (int j = 1; j < NUMBER_OF_CIRCLE_IN_ONE_LINE; j++) {
			if (n < j) {
				return -1;
			}
			if (Math.abs(x - tempW) <= r) {
				col = j;
				break;
			}
			tempW += width;
		}
		if (col == 0) {
			return -1;
		}
		while (Math.abs(y - tempH + r) > r) {
			if (n < (row * 5 + col)) {
				return -1;
			}
			row++;
			tempH += height;
		}
		int index = row * 5 + col + (page - 1) * interval;
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
					mImageView.setImageBitmap(tankListBitmap);
					break;
				default:
					Toast.makeText(mContext, "System ERROR!", Toast.LENGTH_LONG).show();
					break;
			}
			mProgressView.setVisibility(View.GONE);
		}
	}
}

