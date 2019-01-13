package com.evaluacion.callcenter.model;

/**
 * Class representing a basic call to the callcenter.
 * 
 * @author Alexis Sessarego
 *
 */
public class Call {
	
	private int id;
	private CallResult result = CallResult.PENDANT;
	private Reason reason;

	@Override
	public String toString() {
		return "Call [id=" + id + ", result=" + result + ", reason=" + reason + "]";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public CallResult getResult() {
		return result;
	}

	public void setResult(CallResult result) {
		this.result = result;
	}

	/**
	 * @return the reason
	 */
	public Reason getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(Reason reason) {
		this.reason = reason;
	}

}
