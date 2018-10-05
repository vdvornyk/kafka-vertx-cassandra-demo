package com.dms.entity;

import com.datastax.driver.core.Row;

import java.util.UUID;

/**
 * Created by volodymyr on 15.12.17.
 */
public class InvalidTransaction implements AbstractEntity {

    public static final String NAME = "invalid_transaction";

    public static final String INIT_QUERY = "CREATE TABLE IF NOT EXISTS " + InvalidTransaction.NAME + " (id uuid, message text,failReason text, PRIMARY KEY(id))";

    public static final String GET_ALL_QUERY = "SELECT * FROM " + NAME;

    //ID
    private UUID id;

    //Value
    private String message;

    //Reason
    private String failReason;

    public InvalidTransaction() {
    }

    public InvalidTransaction(UUID id, String message, String failReason) {
        this.id = id;
        this.message = message;
        this.failReason = failReason;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    //TODO: refactor this later using Entity annotations
    @Override
    public String saveQuery() {
        return "INSERT INTO " + NAME + " (id, message, failReason) VALUES (" + id + ",'" + message + "'" + ",'" + failReason + "')";
    }

    //TODO: refactor this later
    @Override
    public String readByIdQuery() {
        return null;
    }

    @Override
    public String selectQuery() {
        return null;
    }

    @Override
    public AbstractEntity withRow(Row row) {
        this.id = row.get("id", UUID.class);
        this.message = row.get("message", String.class);
        this.failReason = row.get("failReason", String.class);
        return this;
    }

    @Override
    public String toString() {
        return "InvalidTransaction{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", failReason='" + failReason + '\'' +
                '}';
    }
}
