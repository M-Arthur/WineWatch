package au.edu.adelaide.cs.winewatch.Tools;

import java.util.ArrayList;
import java.util.List;

import au.edu.adelaide.cs.winewatch.JsonObject.Ferment;
import au.edu.adelaide.cs.winewatch.JsonObject.Temperature;

/**
 * Created by Arthur on 25/05/15.
 */
public class InitialDummy {

	private List<Ferment> fermentList;

	public InitialDummy(){
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
			ferment.setTemperatures(temperature);
			ferments.add(ferment);
		}
		fermentList=ferments;
	}
}
