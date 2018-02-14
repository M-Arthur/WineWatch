package au.edu.adelaide.cs.winewatch.Tools;

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
		double[] t;
		Temperature temperature;
		for (int i = 0; i < 100; i++) {
			ferment = new Ferment();
			ferment.setFermentId(i);
			ferment.setMoteId(i);
			ferment.setTankNumber("Tank " + i);
			t = new double[7];
			for (int j = 0; j < 7; j++) {
				t[j] = Math.random() * 30 + 10;
			}
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

	public static User getUser(){
		User user = new User();
		user.setUid(1001);
		user.setUsername("test@test.com");
		user.setPassword("test");
		user.setName("Dummy user 1001");
		user.setToken("Dummy Token");
		user.setType("Tester");
		return user;
	}

	public static Winery getExampleWinery(){
		Winery winery = new Winery();
		winery.setWid(1001);
		winery.setName("Dummy Winery");
		winery.setDescription("This is the description of this dummy winery.");
		winery.setFerments(getFermentList());
		return winery;
	}

	public static Ferment getFerment(int index){
		return getFermentList().get(index);
	}
}
