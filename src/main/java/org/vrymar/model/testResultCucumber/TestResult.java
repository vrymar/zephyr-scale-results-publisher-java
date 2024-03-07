package org.vrymar.model.testResultCucumber;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * TestResult model object
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestResult {
    @JsonProperty("elements")
    private List<Element> elements;

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

        final TestResult other = (TestResult) o;
        if(!Objects.equals(this.name, other.name)){
            return false;
        }

        return this.elements.equals(other.elements);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
