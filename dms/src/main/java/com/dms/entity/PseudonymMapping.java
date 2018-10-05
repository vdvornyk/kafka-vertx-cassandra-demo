package com.dms.entity;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;

import java.util.Objects;
import java.util.UUID;

public class PseudonymMapping implements AbstractEntity {

    public static final String NAME = "pseudonym_mapping";

    public static final String INIT_QUERY = "CREATE TABLE IF NOT EXISTS " + PseudonymMapping.NAME + " (id uuid, transaction_id varchar, pseudonym varchar, PRIMARY KEY(id))";

    public static final String GET_ALL_QUERY = "SELECT * FROM " + NAME;

    private UUID id;

    private String transactionId;

    private String pseudonym;

    public PseudonymMapping() {
    }

    public PseudonymMapping(UUID id, String transactionId, String pseudonym) {
        this.id = id;
        this.transactionId = transactionId;
        this.pseudonym = pseudonym;
    }

    @Override
    public String saveQuery() {
        return QueryBuilder.insertInto(NAME).value("id", id).value("transaction_id", transactionId)
                .value("pseudonym", pseudonym).toString();
    }

    // TODO implement if needed
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
        this.transactionId = row.get("transaction_id", String.class);
        this.pseudonym = row.get("pseudonym", String.class);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PseudonymMapping that = (PseudonymMapping) o;
        return Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(pseudonym, that.pseudonym);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, pseudonym);
    }

    @Override
    public String toString() {
        return "PseudonymMapping{" +
                "id=" + id +
                ", transactionId='" + transactionId + '\'' +
                ", pseudonym='" + pseudonym + '\'' +
                '}';
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPseudonym() {
        return pseudonym;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
    }
}
