package au.edu.adelaide.cs.winewatch.Tools;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import au.edu.adelaide.cs.winewatch.JsonObject.Ferment;
import au.edu.adelaide.cs.winewatch.JsonObject.Temperature;
import au.edu.adelaide.cs.winewatch.JsonObject.User;
import au.edu.adelaide.cs.winewatch.JsonObject.Winery;

/**
 * Created by Arthur on 25/05/15.
 */
public class DummyData {

	public static List<Ferment> getFermentList() {
		List<Ferment> ferments = new ArrayList<>();
		Ferment ferment;
		double[] origin = InitialDummy.originTemperature();
		int index = 0;
		double[] t;
		int n = 0;
		Temperature temperature;
		for (int i = 0; i < 200; i++) {
			ferment = new Ferment();
			ferment.setFermentId(i);
			ferment.setMoteId(i);
			ferment.setTankNumber("Tank " + i);
			if (index < 100) {
				n = 7;
			} else {
				n = 3;
			}
			t = new double[n];
			for (int j = index; j < index + n; j++) {
				t[j - index] = origin[j];
			}
			index += n;
			temperature = new Temperature();
			temperature.setTemperatures(t);
			temperature.setUpdatingTime("25-May-2015 time" + i);
			temperature.setBattery(20);
			temperature.setInterval(15);
			ferment.setTemperatures(temperature);
			ferments.add(ferment);
		}
		return ferments;
	}

	public static User getUser() {
		User user = new User();
		user.setUid(1001);
		user.setUsername("test@test.com");
		user.setPassword("test");
		user.setName("Dummy user 1001");
		user.setToken("Dummy Token");
		user.setType("Tester");
		return user;
	}

	public static Winery getExampleWinery() {
		Winery winery = new Winery();
		winery.setWid(1001);
		winery.setName("Dummy Winery");
		winery.setDescription("This is the description of this dummy winery.");
		winery.setFerments(getFermentList());
		return winery;
	}

	public static List<Winery> getWineryList() {
		List<Winery> wineries = new ArrayList<>();
		Winery winery;
		for (int i = 0; i < 100; i++) {
			winery = new Winery();
			winery.setWid(i);
			winery.setName("Winery " + i);
			winery.setDescription("This is a dummy winery.");
			wineries.add(winery);
		}
		return wineries;
	}

	public static Ferment getFerment(int index) {
		return getFermentList().get(index);
	}

	public static List<Temperature> getSampleHT() {
		Temperature temperature;
		List<Temperature> tl = new ArrayList<>();
		int n = 7;
		double[] t;
		for (int k = 0; k < 24; k++) {
			if (k != 5 && k != 6 && k != 13 && k != 14) {
				temperature = new Temperature();
				t = new double[n];
				for (int i = 0; i < n; i++) {
					t[i] = Math.random() * 30 + 10;
				}
				temperature.setTemperatures(t);
				temperature.setUpdatingTime("2015-06-11 " + k + ":00:00");
				tl.add(temperature);
			}
		}
		return tl;
	}

	public static List<Temperature> getHistoryTemperatures() {
		Temperature temperature;
		List<Temperature> tl = new ArrayList<>();
		double[] t;
		int n = 7;
		int index = 0;
		String time;
		int temp;
		String month;
		String day;
		String hour;

		for (int j = 0; j < 100000; j++) {
			temp = index % 12 + 1;
			if (temp > 9) {
				month = temp + "";
			} else {
				month = "0" + temp;
			}
			temp = index % 31 + 1;
			if (temp > 9) {
				day = temp + "";
			} else {
				day = "0" + temp;
			}
			temp = index % 24 + 1;
			if (temp > 9) {
				hour = temp + "";
			} else {
				hour = "0" + temp;
			}
			time = day + "-" + month + "-" + "2015" + " " + hour + ":00:00";
			temperature = new Temperature();
			t = new double[n];
			for (int i = 0; i < n; i++) {
				t[i] = Math.random() * 30 + 10;
			}
			temperature.setTemperatures(t);
			temperature.setUpdatingTime(time);
			index++;
			tl.add(temperature);
		}
		return tl;
	}
}
