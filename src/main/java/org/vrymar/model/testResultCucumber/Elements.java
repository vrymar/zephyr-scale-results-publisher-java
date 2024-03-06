package org.vrymar.model.testResultCucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * Elements model object
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Elements {
    @JsonProperty("name")
    private String name;

    @JsonProperty("tags")
    private List<Tags> tags;

    @JsonProperty("steps")
    private List<Steps> steps;


    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }

        if(o.getClass() != this.getClass()){
            return false;
        }

        final Elements other = (Elements) o;
        if(!Objects.equals(this.name, other.name)){
            return false;
        }

        return this.tags.equals(other.tags);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
