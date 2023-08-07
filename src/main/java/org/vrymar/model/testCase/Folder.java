package org.vrymar.model.testCase;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Folder model object
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Folder {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("self")
    private String self;

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }

        if(o.getClass() != this.getClass()){
            return false;
        }

        final Folder other = (Folder) o;
        if(!Objects.equals(this.self, other.self)){
            return false;
        }

        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.self != null ? this.self.hashCode() : 0);
        hash = 53 * hash + this.id;

        return hash;
    }
}
