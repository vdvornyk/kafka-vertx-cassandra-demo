package com.dms.jackson.model.example;

import java.io.Serializable;
/**
 * TODO: This is only for example purposes; should replace with our implementation
 */
public class Command implements Serializable{

	private static final long serialVersionUID = -1L;

	private long transactionId;
	private String username;
	private double balanceChange;

	public Command() {
	}

	public Command(long transactionId, String username, double balanceChange) {
		this.transactionId = transactionId;
		this.username = username;
		this.balanceChange = balanceChange;
	}

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public double getBalanceChange() {
		return balanceChange;
	}

	public void setBalanceChange(double balanceChange) {
		this.balanceChange = balanceChange;
	}

    @Override
    public String toString() {
        return "Command{" +
                "transactionId=" + transactionId +
                ", username='" + username + '\'' +
                ", balanceChange=" + balanceChange +
                '}';
    }
}
