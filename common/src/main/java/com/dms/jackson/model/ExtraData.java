
package com.dms.jackson.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "payload"
})
public class ExtraData implements Serializable {

    private final static long serialVersionUID = -1L;

    @JsonProperty("payload")
    private String payload;

    public ExtraData() {
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("payload", payload).toString();
    }

}
