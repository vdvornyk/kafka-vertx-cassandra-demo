package com.dms.jackson.model.example;

import java.io.Serializable;
/**
 * TODO: This is only for example purposes; should replace with our implementation
 */
public class CommandResponse implements Serializable {

	private static final long serialVersionUID = -1L;

	private long transactionId;
	private long errorCode;
	private String balanceVersion = "v1";
	private double balanceChange;
	private double balanceAfterChange;

	public CommandResponse() {
	}

	public CommandResponse(long transactionId, long errorCode, String balanceVersion, double balanceChange, double balanceAfterChange) {
		this.transactionId = transactionId;
		this.errorCode = 0;
		this.balanceVersion = balanceVersion;
		this.balanceChange = balanceChange;
		this.balanceAfterChange = balanceAfterChange;
	}

	public CommandResponse(Command command) {
		this.transactionId = command.getTransactionId();
		this.balanceChange = command.getBalanceChange();
	}

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public long getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(long errorCode) {
		this.errorCode = errorCode;
	}

	public String getBalanceVersion() {
		return balanceVersion;
	}

	public void setBalanceVersion(String balanceVersion) {
		this.balanceVersion = balanceVersion;
	}

	public double getBalanceChange() {
		return balanceChange;
	}

	public void setBalanceChange(double balanceChange) {
		this.balanceChange = balanceChange;
	}

	public double getBalanceAfterChange() {
		return balanceAfterChange;
	}

	public void setBalanceAfterChange(double balanceAfterChange) {
		this.balanceAfterChange = balanceAfterChange;
	}

	@Override public String toString() {
		return "CommandResponse{" +
				"transactionId=" + transactionId +
				", errorCode=" + errorCode +
				", balanceVersion='" + balanceVersion + '\'' +
				", balanceChange=" + balanceChange +
				", balanceAfterChange=" + balanceAfterChange +
				'}';
	}
}
