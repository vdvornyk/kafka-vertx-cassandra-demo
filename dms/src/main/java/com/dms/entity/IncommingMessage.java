package com.dms.entity;

import com.datastax.driver.core.Row;

import java.util.UUID;

/**
 * Created by volodymyr on 15.12.17.
 */
public class IncommingMessage implements AbstractEntity {

    public static final String NAME = "incomming_message";

    public static final String INIT_QUERY = "CREATE TABLE IF NOT EXISTS " + IncommingMessage.NAME + " (id uuid, message varchar, PRIMARY KEY(id))";

    public static final String GET_ALL_QUERY = "SELECT * FROM " + NAME;

    //ID
    private UUID id;

    //Value
    private String message;

    public IncommingMessage() {
    }

    public IncommingMessage(UUID id, String message) {
        this.id = id;
        this.message = message;
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

    //TODO: refactor this later using Entity annotations
    @Override
    public String saveQuery() {
        return "INSERT INTO " + NAME + " (id, message) VALUES (" + id + ",'" + message + "')";
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
        return this;
    }

    @Override
    public String toString() {
        return "IncommingMessage{" +
                "id=" + id +
                ", message='" + message + '\'' +
                '}';
    }
}
