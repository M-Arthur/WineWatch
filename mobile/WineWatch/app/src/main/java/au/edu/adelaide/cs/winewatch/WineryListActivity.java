package au.edu.adelaide.cs.winewatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import au.edu.adelaide.cs.winewatch.JsonObject.User;
import au.edu.adelaide.cs.winewatch.JsonObject.Winery;
import au.edu.adelaide.cs.winewatch.Tools.Constants;
import au.edu.adelaide.cs.winewatch.Tools.RefreshableView;
import au.edu.adelaide.cs.winewatch.Tools.ServerManager;


public class WineryListActivity extends ActionBarActivity
		implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	private static final String TITLE_LIST = "title_list";
	private static User user;
	private static Toast toast;
	private static ArrayList<Winery> wineryList;

	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	private static ListView titleListView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_winery_list);

		MyApplication myApp = (MyApplication) getApplicationContext();
		user = myApp.getUser();

		mNavigationDrawerFragment = (NavigationDrawerFragment)
				getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// TODO:delete
		Log.e(">>>>>>>>>>>","restart");
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {

		// update the container content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.container_main, PlaceholderFragment.newInstance(position + 1))
				.commit();

	}

	public void onSectionAttached(int number) {
		// TODO: Fetching data from server.
		switch (number) {
			case 1:
				mTitle = getString(R.string.title_section1);
				break;
			case 2:
				mTitle = getString(R.string.title_section2);
				break;
			case 3:
				mTitle = getString(R.string.title_section3);
				break;
		}
//		ListView test = (ListView) findViewById(R.id.list_title);
//		System.out.println(test.getId());
	}

	private static ArrayList<String> getTitleList() {

		ArrayList<String> contentList = new ArrayList<>();

		ServerManager sm = new ServerManager();
		Map result = sm.getWineryList(user);
		wineryList = (ArrayList<Winery>) result.get(Constants.WINERY_LIST);
		if ((boolean) result.get(Constants.GET_WINERY_LIST_RESULT)) {
			for (int i = 0; i < wineryList.size(); i++) {
				contentList.add(wineryList.get(i).getName());
			}
		}

//      else {
//			switch ((int) result.get(Constants.GET_WINERY_LIST_ERROR_TYPE)) {
//				case Constants.LOGIN_PASSWORD_ERROR:
//				case Constants.LOGIN_INTERNET_ERROR:
//				case Constants.LOGIN_SYSTEM_ERROR:
//					new Thread(new Runnable() {
//						@Override
//						public void run() {
//							Message msg = new Message();
//							msg.arg1 = 1;
//							Bundle bundle = new Bundle();
//							bundle.putStringArrayList(TITLE_LIST, getTitleList());
//							msg.setData(bundle);
//							msg.arg1 = 1;
//							handler.sendMessage(msg);
//						}
//					}).start();
//					break;
//				default:
//					toast = Toast.makeText(MyApplication.getAppContext(), "Unknown error", Toast.LENGTH_SHORT);
//					toast.setGravity(Gravity.CENTER, 10, 10);
//					toast.show();
//					break;
//			}
//	}

	return contentList;
}

	// Create new activity according to the item selected.
	private static void details(int index, int position) {
		// TODO: Connect to server
		// send index and position into server


	}

	public void restoreActionBar() {

		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.menu_list, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings_global) {
			// TODO: Implement the drawer settings action.
			Toast.makeText(this, "Settings action WineryListActivity.", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

/**
 * A placeholder fragment containing a simple view.
 */
public static class PlaceholderFragment extends Fragment {

	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";

	/**
	 * Returns a new instance of this fragment for the given section
	 * number.
	 */
	public static PlaceholderFragment newInstance(int sectionNumber) {

		PlaceholderFragment fragment = new PlaceholderFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public PlaceholderFragment() {
	}

	public int showIndex() {
		return getArguments().getInt(ARG_SECTION_NUMBER, 1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		final LinearLayout rootView = new LinearLayout(getActivity());
		int paddingV = (int) getResources().getDimension(R.dimen.activity_vertical_margin);
		int paddingH = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
		rootView.setPadding(paddingH, paddingV, paddingH, paddingV);
		rootView.setOrientation(LinearLayout.VERTICAL);
		final ProgressBar mProgressView = new ProgressBar(getActivity());
		final ListView titleList = new ListView(getActivity());
		final LinearLayout refreshableView = new RefreshableView(getActivity());
		final int index = showIndex();
		final MyHandler handler = new MyHandler(getActivity(), titleList, mProgressView);
		rootView.addView(refreshableView);
		refreshableView.addView(titleList);
		rootView.addView(mProgressView);
//			rootView.addView(titleList);
		mProgressView.setVisibility(View.VISIBLE);
		titleList.setVisibility(View.GONE);
		((RefreshableView) refreshableView).setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
			@Override
			public void onRefresh() {
//					try {
//						Thread.sleep(3000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
				new Thread(new Runnable() {
					@Override
					public void run() {
						Message msg = new Message();
						msg.arg1 = 1;
						Bundle bundle = new Bundle();
						bundle.putStringArrayList(TITLE_LIST, getTitleList());
						msg.setData(bundle);
						msg.arg1 = 1;
						handler.sendMessage(msg);
					}
				}).start();
				((RefreshableView) refreshableView).finishRefreshing();
			}
		}, 0);
		titleList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), TankListActivity.class);
				Winery winery = wineryList.get(position);
				MyApplication myApp = (MyApplication) MyApplication.getAppContext();
				myApp.setWinery(winery);
				startActivity(intent);
			}
		});
		new Thread(new Runnable() {
			@Override
			public void run() {
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putStringArrayList(TITLE_LIST, getTitleList());
				msg.setData(bundle);
				msg.arg1 = 1;
				handler.sendMessage(msg);
			}
		}).start();
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((WineryListActivity) activity).onSectionAttached(
				getArguments().getInt(ARG_SECTION_NUMBER));
	}
}

private static class MyHandler extends Handler {

	private final Activity mActivity;
	private ListView mTitleList;
	private View mProgressView;

	private MyHandler(Activity activity) {
		mActivity = activity;
	}

	private MyHandler(Activity activity, ListView titleList, View progressView) {
		mActivity = activity;
		mTitleList = titleList;
		mProgressView = progressView;
	}

	@Override
	public void handleMessage(Message msg) {
		Bundle bundle = msg.getData();
		switch (msg.arg1) {
			case 1:
				mTitleList.setAdapter(new ArrayAdapter<String>(
						mActivity,
						android.R.layout.simple_list_item_1,
						android.R.id.text1,
						bundle.getStringArrayList(TITLE_LIST)));
				mProgressView.setVisibility(View.GONE);
				mTitleList.setVisibility(View.VISIBLE);
				break;
			case 2:
				String error = bundle.getString("error");
				toast = Toast.makeText(MyApplication.getAppContext(), error, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 10, 10);
				toast.show();
				break;
		}
	}

}
}

