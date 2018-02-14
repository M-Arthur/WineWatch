package au.edu.adelaide.cs.winewatch;

import android.app.Application;
import android.content.Context;

import au.edu.adelaide.cs.winewatch.JsonObject.Ferment;
import au.edu.adelaide.cs.winewatch.JsonObject.User;
import au.edu.adelaide.cs.winewatch.JsonObject.Winery;
import au.edu.adelaide.cs.winewatch.Tools.DummyData;

/**
 * Created by Arthur on 13/04/15.
 */
public class MyApplication extends Application {

	private static Context context;
	private String menuTitle;
	private Ferment ferment;

	private DummyData dummyData;

	public void onCreate() {
		super.onCreate();
		MyApplication.context = getApplicationContext();
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
