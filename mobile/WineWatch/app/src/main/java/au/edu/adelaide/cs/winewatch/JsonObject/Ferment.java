package au.edu.adelaide.cs.winewatch.JsonObject;

/**
 * Created by Arthur on 26/04/15.
 */
public class Ferment {

	private int fermentId;
	private String tankNumber;
	private int moteId; 	// Mote id
	private String startTime; // start time
	private String updatingTime;
	private Temperature temperatures;

	public Ferment() {
	}

	public Ferment(int fermentId, String tankNumber, int moteId) {
		this.fermentId = fermentId;
		this.tankNumber = tankNumber;
		this.moteId = moteId;
	}

	public Temperature getTemperatures() {
		return temperatures;
	}

	public boolean hasTemperatures(){
		if(temperatures==null){
			return false;
		}
		else{
			return true;
		}
	}

	public void setTemperatures(Temperature temperatures) {
		this.temperatures = temperatures;
	}

	public String getUpdatingTime() {
		return updatingTime;
	}

	public void setUpdatingTime(String updatingTime) {
		this.updatingTime = updatingTime;
	}

	public int getFermentId() {
		return fermentId;
	}

	public void setFermentId(int fermentId) {
		this.fermentId = fermentId;
	}

	public String getTankNumber() {
		return tankNumber;
	}

	public void setTankNumber(String tankNumber) {
		this.tankNumber = tankNumber;
	}

	public int getMoteId() {
		return moteId;
	}

	public void setMoteId(int moteId) {
		this.moteId = moteId;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
}
