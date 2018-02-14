package au.edu.adelaide.cs.winewatch.JsonObject;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arthur on 25/04/15.
 */
public class Tank{
	private int tid;
	private String name;
	private double[] temperatures;

	public Tank() {
	}

	public Tank(double[] temperatures, int tid) {
		this.temperatures = temperatures;
		this.tid = tid;
	}

	public long getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double[] getTemperatures() {
		return temperatures;
	}

	public void setTemperatures(double[] temperatures) {
		this.temperatures = temperatures;
	}

}
