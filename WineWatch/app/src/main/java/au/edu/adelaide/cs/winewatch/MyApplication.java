package au.edu.adelaide.cs.winewatch;

import android.app.Application;
import android.content.Context;

import java.util.List;

import au.edu.adelaide.cs.winewatch.JsonObject.Ferment;
import au.edu.adelaide.cs.winewatch.JsonObject.Temperature;
import au.edu.adelaide.cs.winewatch.JsonObject.User;
import au.edu.adelaide.cs.winewatch.JsonObject.Winery;
import au.edu.adelaide.cs.winewatch.Tools.DummyData;

/**
 * Created by Arthur on 13/04/15.
 */
public class MyApplication extends Application {

	private static Context context;
	private int fermentIndex;
	private String menuTitle;
	private Ferment ferment;
	private List<Temperature> historyTemperatures;
	private DummyData dummyData;

	public List<Temperature> getHistoryTemperatures() {
		return historyTemperatures;
	}

	public void setHistoryTemperatures(List<Temperature> historyTemperatures) {
		this.historyTemperatures = historyTemperatures;
	}

	public void onCreate() {
		super.onCreate();
		MyApplication.context = getApplicationContext();
	}

	public int getFermentIndex() {
		return fermentIndex;
	}

	public void setFermentIndex(int fermentIndex) {
		this.fermentIndex = fermentIndex;
	}

	public DummyData getDummyData() {
		return dummyData;
	}

	public void setDummyData(DummyData dummyData) {
		this.dummyData = dummyData;
	}

	public Ferment getFerment() {
		return ferment;
	}

	public void setFerment(Ferment ferment) {
		this.ferment = ferment;
	}

	public static Context getAppContext() {
		return MyApplication.context;
	}

	public String getMenuTitle() {
		return menuTitle;
	}

	public void setMenuTitle(String menuTitle) {
		this.menuTitle = menuTitle;
	}

	private User user;
	private Winery winery;

	public Winery getWinery() {
		return winery;
	}

	public void setWinery(Winery winery) {
		this.winery = winery;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
