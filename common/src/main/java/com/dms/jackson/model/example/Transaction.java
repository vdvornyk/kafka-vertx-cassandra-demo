package com.dms.jackson.model.example;

import java.io.Serializable;
/**
 * TODO: This is only for example purposes; should replace with our implementation
 */
public class Transaction implements Serializable {

	private static final long serialVersionUID = -1L;

	private long transactionId;

	public Transaction() {
	}

	public Transaction(long transactionId) {
		this.transactionId = transactionId;
	}

	public static Transaction create(long txId){
		return new Transaction(txId);
	}

    public long getTransactionId() {
        return transactionId;
    }
}
