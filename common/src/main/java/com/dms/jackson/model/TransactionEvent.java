
package com.dms.jackson.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.ValidationException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "payment",
        "extraData"
})
public class TransactionEvent implements Serializable, Schema {

    private final static long serialVersionUID = -1L;

    @JsonProperty(value = "id", required = true)
    private String id;
    @JsonProperty(value = "payment", required = true)
    private Payment payment;
    @JsonProperty(value = "extraData", required = false)
    private ExtraData extraData;

    @JsonIgnore
    private String originalId;

    public TransactionEvent() {
    }

    public void overrideId(String pseudonym) {
        originalId = id;
        id = pseudonym;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public ExtraData getExtraData() {
        return extraData;
    }

    public void setExtraData(ExtraData extraData) {
        this.extraData = extraData;
    }

    public String getOriginalId() {
        return originalId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("payment", payment)
                .append("extraData", extraData).toString();
    }

    @Override
    public boolean validate() throws ValidationException {
        if (StringUtils.isBlank(id)) {
            throw new ValidationException("TransactionEvent | Id should be not blank");
        }

        if (payment == null) {
            throw new ValidationException("TransactionEvent | Payment object should exists");
        }

        return payment.validate();
    }
}
