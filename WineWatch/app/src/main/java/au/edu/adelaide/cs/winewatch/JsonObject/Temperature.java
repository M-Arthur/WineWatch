package au.edu.adelaide.cs.winewatch.JsonObject;

/**
 * Created by Arthur on 27/04/15.
 */
public class Temperature {
	private double[] temperatures;
	private String updatingTime;
	private double battery;
	private double interval;

	public Temperature(double[] temperatures, String updatingTime) {
		this.temperatures = temperatures;
		this.updatingTime = updatingTime;
	}

	public double getBattery() {
		return battery;
	}

	public boolean hasTemperatures(){
		if(temperatures==null){
			return false;
		}
		else{
			return true;
		}
	}

	public void setBattery(double battery) {
		this.battery = battery;
	}

	public double getInterval() {
		return interval;
	}

	public void setInterval(double interval) {
		this.interval = interval;
	}

	public double[] getTemperatures() {
		return temperatures;
	}

	public void setTemperatures(double[] temperatures) {
		this.temperatures = temperatures;
	}

	public String getUpdatingTime() {
		return updatingTime;
	}

	public void setUpdatingTime(String updatingTime) {
		this.updatingTime = updatingTime;
	}

	public Temperature() {

	}
}
