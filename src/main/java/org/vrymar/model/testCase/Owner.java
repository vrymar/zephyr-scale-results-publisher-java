package org.vrymar.model.testCase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Owner {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("accountId")
    private String accountId;

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }

        if(o.getClass() != this.getClass()){
            return false;
        }

        final Owner other = (Owner) o;
        if(!Objects.equals(this.accountId, other.accountId)){
            return false;
        }

        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.accountId != null ? this.accountId.hashCode() : 0);
        hash = 53 * hash + this.id;

        return hash;
    }
}
