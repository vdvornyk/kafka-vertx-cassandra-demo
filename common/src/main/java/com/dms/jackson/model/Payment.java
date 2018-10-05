
package com.dms.jackson.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.ValidationException;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "from",
        "to",
        "amount",
        "currency",
        "createdAt"
})
public class Payment implements Serializable,Schema {

    private final static long serialVersionUID = -1L;

    @JsonProperty(value = "from")
    private String from;
    @JsonProperty(value = "to")
    private String to;
    @JsonProperty(value = "amount")
    private Double amount;
    @JsonProperty(value = "currency")
    private String currency;
    @JsonProperty(value = "createdAt")
    private String createdAt;


    public Payment() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("from", from)
                .append("to", to)
                .append("amount", amount)
                .append("currency", currency)
                .append("createdAt", createdAt).toString();
    }

    @Override
    public boolean validate() throws ValidationException {
        if (StringUtils.isBlank(from)) {
            throw new ValidationException("Payment | \"from\' can not be blank");
        }
        if (StringUtils.isBlank(to)) {
            throw new ValidationException("Payment | \"to\" can not be blank");
        }
        if (amount == null) {
            throw new ValidationException("Payment | \"amount\" can not be blank");
        }
        if (StringUtils.isBlank(currency)) {
            throw new ValidationException("Payment | \"currency\" can not be blank");

        }
        if (StringUtils.isBlank(createdAt)) {
            throw new ValidationException("Payment | \"createdAt\" can not be blank");
        }

        return true;
    }
}
