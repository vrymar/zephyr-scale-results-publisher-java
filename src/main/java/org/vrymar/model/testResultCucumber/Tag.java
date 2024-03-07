package org.vrymar.model.testResultCucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Tags model object
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag {

    @JsonProperty("name")
    private String name;

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }

        if(o.getClass() != this.getClass()){
            return false;
        }

        final Tag other = (Tag) o;
        if(!Objects.equals(this.name, other.name)){
            return false;
        }

        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

}
