package au.edu.adelaide.cs.winewatch.Tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import au.edu.adelaide.cs.winewatch.R;

/**
 * Created by Arthur on 14/04/15.
 */
public class RefreshableView extends LinearLayout implements OnTouchListener {

	/**
	 * Pulling status
	 */
	public static final int STATUS_PULL_TO_REFRESH = 0;

	/**
	 * Release to refresh
	 */
	public static final int STATUS_RELEASE_TO_REFRESH = 1;

	/**
	 * Refreshing status
	 */
	public static final int STATUS_REFRESHING = 2;

	/**
	 * Refreshing did not finish
	 */
	public static final int STATUS_REFRESH_FINISHED = 3;

	/**
	 * The speed of textView scrolling back
	 */
	public static final int SCROLL_SPEED = -20;

	/**
	 * one minute in millisecond
	 */
	public static final long ONE_MINUTE = 60 * 1000;

	/**
	 * one hour in millisecond
	 */
	public static final long ONE_HOUR = 60 * ONE_MINUTE;

	/**
	 * one day in millisecond
	 */
	public static final long ONE_DAY = 24 * ONE_HOUR;

	/**
	 * one month in millisecond
	 */
	public static final long ONE_MONTH = 30 * ONE_DAY;

	/**
	 * one year in millisecond
	 */
	public static final long ONE_YEAR = 12 * ONE_MONTH;

	/**
	 * The key of last time update
	 */
	private static final String UPDATED_AT = "updated_at";

	/**
	 * 下拉刷新的回调接口
	 */
	private PullToRefreshListener mListener;

	/**
	 * Using to save the time last update happened
	 */
	private SharedPreferences preferences;

	/**
	 * The header of pulling
	 */
	private View header;

	/**
	 * pulling view
	 */
	private ListView pullView;

	/**
	 * progressBar
	 */
	private ProgressBar progressBar;

	/**
	 * the arrow imply that it is pulling down or up
	 */
	private ImageView arrow;

	/**
	 * The description when the header showed up
	 */
	private TextView description;

	/**
	 * The description of last update time
	 */
	private TextView updateAt;

	/**
	 * The layoutParams of header
	 */
	private MarginLayoutParams headerLayoutParams;

	/**
	 * the time passed from last update in millisecond
	 */
	private long lastUpdateTime;

	/**
	 * Using to distinguish different RefreshableView in different view
	 */
	private int mId = -1;

	/**
	 * the header height
	 */
	private int hideHeaderHeight;

	/**
	 * Current status: STATUS_PULL_TO_REFRESH, STATUS_RELEASE_TO_REFRESH,
	 * STATUS_REFRESHING 和 STATUS_REFRESH_FINISHED
	 */
	private int currentStatus = STATUS_REFRESH_FINISHED;
	;

	/**
	 * record the last status to avoid repeating operation
	 */
	private int lastStatus = currentStatus;

	/**
	 * the y coordinate of finger
	 */
	private float yDown;

	/**
	 * before the finger move this length, it won`t be regarded as refreshing
	 */
	private int touchSlop;

	/**
	 * whether layout have been loaded，the onLayout only need to be loaded once
	 */
	private boolean loadOnce;

	/**
	 * judge whether it can be pulled down, only when pullView in the top it can be pulled down
	 */
	private boolean ableToPull;

	public RefreshableView(Context context){
		super(context);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh, null, true);
		progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
		arrow = (ImageView) header.findViewById(R.id.arrow);
		description = (TextView) header.findViewById(R.id.description);
		updateAt = (TextView) header.findViewById(R.id.updated_at);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		refreshUpdatedAtValue();
		setOrientation(VERTICAL);
		addView(header, 0);
	}

	/**
	 * 下拉刷新控件的构造函数，会在运行时动态添加一个下拉头的布局。
	 *
	 * @param context
	 * @param attrs
	 */
	public RefreshableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh, null, true);
		progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
		arrow = (ImageView) header.findViewById(R.id.arrow);
		description = (TextView) header.findViewById(R.id.description);
		updateAt = (TextView) header.findViewById(R.id.updated_at);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		refreshUpdatedAtValue();
		setOrientation(VERTICAL);
		addView(header, 0);
	}

	/**
	 * 进行一些关键性的初始化操作，比如：将下拉头向上偏移进行隐藏，给ListView注册touch事件。
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && !loadOnce) {
			hideHeaderHeight = -header.getHeight();
			headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
			headerLayoutParams.topMargin = hideHeaderHeight;
			pullView =  (ListView)getChildAt(1);
			pullView.setOnTouchListener(this);
			loadOnce = true;
		}
	}

	/**
	 * 当ListView被触摸时调用，其中处理了各种下拉刷新的具体逻辑。
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		setIsAbleToPull(event);
		if (ableToPull) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					yDown = event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					float yMove = event.getRawY();
					int distance = (int) (yMove - yDown);
					// 如果手指是下滑状态，并且下拉头是完全隐藏的，就屏蔽下拉事件
					if (distance <= 0 && headerLayoutParams.topMargin <= hideHeaderHeight) {
						return false;
					}
					if (distance < touchSlop) {
						return false;
					}
					if (currentStatus != STATUS_REFRESHING) {
						if (headerLayoutParams.topMargin > 0) {
							currentStatus = STATUS_RELEASE_TO_REFRESH;
						} else {
							currentStatus = STATUS_PULL_TO_REFRESH;
						}
						// 通过偏移下拉头的topMargin值，来实现下拉效果
						headerLayoutParams.topMargin = (distance / 2) + hideHeaderHeight;
						header.setLayoutParams(headerLayoutParams);
					}
					break;
				case MotionEvent.ACTION_UP:
				default:
					if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
						// 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
						new RefreshingTask().execute();
					} else if (currentStatus == STATUS_PULL_TO_REFRESH) {
						// 松手时如果是下拉状态，就去调用隐藏下拉头的任务
						new HideHeaderTask().execute();
					}
					break;
			}
			// 时刻记得更新下拉头中的信息
			if (currentStatus == STATUS_PULL_TO_REFRESH
					|| currentStatus == STATUS_RELEASE_TO_REFRESH) {
				updateHeaderView();
				// 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
				pullView.setPressed(false);
				pullView.setFocusable(false);
				pullView.setFocusableInTouchMode(false);
				lastStatus = currentStatus;
				// 当前正处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
				return true;
			}
		}
		return false;
	}

	/**
	 * 给下拉刷新控件注册一个监听器。
	 *
	 * @param listener 监听器的实现。
	 * @param id       为了防止不同界面的下拉刷新在上次更新时间上互相有冲突， 请不同界面在注册下拉刷新监听器时一定要传入不同的id。
	 */
	public void setOnRefreshListener(PullToRefreshListener listener, int id) {
		mListener = listener;
		mId = id;
	}

	/**
	 * 当所有的刷新逻辑完成后，记录调用一下，否则你的ListView将一直处于正在刷新状态。
	 */
	public void finishRefreshing() {
		currentStatus = STATUS_REFRESH_FINISHED;
		preferences.edit().putLong(UPDATED_AT + mId, System.currentTimeMillis()).commit();
		new HideHeaderTask().execute();
	}

	/**
	 * 根据当前ListView的滚动状态来设定 {@link #ableToPull}
	 * 的值，每次都需要在onTouch中第一个执行，这样可以判断出当前应该是滚动ListView，还是应该进行下拉。
	 *
	 * @param event
	 */
	private void setIsAbleToPull(MotionEvent event) {
		View firstChild = pullView.getChildAt(0);
		if (firstChild != null) {
			int firstVisiblePos = pullView.getFirstVisiblePosition();
			if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
				if (!ableToPull) {
					yDown = event.getRawY();
				}
				// 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
				ableToPull = true;
			} else {
				if (headerLayoutParams.topMargin != hideHeaderHeight) {
					headerLayoutParams.topMargin = hideHeaderHeight;
					header.setLayoutParams(headerLayoutParams);
				}
				ableToPull = false;
			}
		} else {
			// 如果ListView中没有元素，也应该允许下拉刷新
			ableToPull = true;
		}
	}

	/**
	 * 更新下拉头中的信息。
	 */
	private void updateHeaderView() {
		if (lastStatus != currentStatus) {
			if (currentStatus == STATUS_PULL_TO_REFRESH) {
				description.setText(getResources().getString(R.string.pull_to_refresh));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
				description.setText(getResources().getString(R.string.release_to_refresh));
				arrow.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				rotateArrow();
			} else if (currentStatus == STATUS_REFRESHING) {
				description.setText(getResources().getString(R.string.refreshing));
				progressBar.setVisibility(View.VISIBLE);
				arrow.clearAnimation();
				arrow.setVisibility(View.GONE);
			}
			refreshUpdatedAtValue();
		}
	}

	/**
	 * 根据当前的状态来旋转箭头。
	 */
	private void rotateArrow() {
		float pivotX = arrow.getWidth() / 2f;
		float pivotY = arrow.getHeight() / 2f;
		float fromDegrees = 0f;
		float toDegrees = 0f;
		if (currentStatus == STATUS_PULL_TO_REFRESH) {
			fromDegrees = 180f;
			toDegrees = 360f;
		} else if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
			fromDegrees = 0f;
			toDegrees = 180f;
		}
		RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
		animation.setDuration(100);
		animation.setFillAfter(true);
		arrow.startAnimation(animation);
	}

	/**
	 * 刷新下拉头中上次更新时间的文字描述。
	 */
	private void refreshUpdatedAtValue() {
		lastUpdateTime = preferences.getLong(UPDATED_AT + mId, -1);
		long currentTime = System.currentTimeMillis();
		long timePassed = currentTime - lastUpdateTime;
		long timeIntoFormat;
		String updateAtValue;
		if (lastUpdateTime == -1) {
			updateAtValue = getResources().getString(R.string.not_updated_yet);
		} else if (timePassed < 0) {
			updateAtValue = getResources().getString(R.string.time_error);
		} else if (timePassed < ONE_MINUTE) {
			updateAtValue = getResources().getString(R.string.updated_just_now);
		} else if (timePassed < ONE_HOUR) {
			timeIntoFormat = timePassed / ONE_MINUTE;
			String value = timeIntoFormat + "minutes";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_DAY) {
			timeIntoFormat = timePassed / ONE_HOUR;
			String value = timeIntoFormat + "hours";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_MONTH) {
			timeIntoFormat = timePassed / ONE_DAY;
			String value = timeIntoFormat + "days";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else if (timePassed < ONE_YEAR) {
			timeIntoFormat = timePassed / ONE_MONTH;
			String value = timeIntoFormat + "months";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		} else {
			timeIntoFormat = timePassed / ONE_YEAR;
			String value = timeIntoFormat + "years";
			updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
		}
		updateAt.setText(updateAtValue);
	}

	/**
	 * 正在刷新的任务，在此任务中会去回调注册进来的下拉刷新监听器。
	 *
	 * @author guolin
	 */
	class RefreshingTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= 0) {
					topMargin = 0;
					break;
				}
				publishProgress(topMargin);
				sleep(10);
			}
			currentStatus = STATUS_REFRESHING;
			publishProgress(0);
			if (mListener != null) {
				mListener.onRefresh();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... topMargin) {
			updateHeaderView();
			headerLayoutParams.topMargin = topMargin[0];
			header.setLayoutParams(headerLayoutParams);
		}

	}

	/**
	 * 隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏。
	 *
	 * @author guolin
	 */
	class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			int topMargin = headerLayoutParams.topMargin;
			while (true) {
				topMargin = topMargin + SCROLL_SPEED;
				if (topMargin <= hideHeaderHeight) {
					topMargin = hideHeaderHeight;
					break;
				}
				publishProgress(topMargin);
				sleep(10);
			}
			return topMargin;
		}

		@Override
		protected void onProgressUpdate(Integer... topMargin) {
			headerLayoutParams.topMargin = topMargin[0];
			header.setLayoutParams(headerLayoutParams);
		}

		@Override
		protected void onPostExecute(Integer topMargin) {
			headerLayoutParams.topMargin = topMargin;
			header.setLayoutParams(headerLayoutParams);
			currentStatus = STATUS_REFRESH_FINISHED;
		}
	}

	/**
	 * 使当前线程睡眠指定的毫秒数。
	 *
	 * @param time 指定当前线程睡眠多久，以毫秒为单位
	 */
	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
	 *
	 * @author guolin
	 */
	public interface PullToRefreshListener {

		/**
		 * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。注意此方法是在子线程中调用的， 你可以不必另开线程来进行耗时操作。
		 */
		void onRefresh();

	}

}
