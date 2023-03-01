package org.vrymar.model.jiraIssue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssue {

    @JsonProperty("id")
    private String id;

    @JsonProperty("self")
    private String self;

    @JsonProperty("key")
    private String key;

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }

        if(o.getClass() != this.getClass()){
            return false;
        }

        final JiraIssue other = (JiraIssue) o;

        if (!Objects.equals(this.self, other.self)) {
            return false;
        }


        if (!Objects.equals(this.id, other.id)) {
            return false;
        }

        return Objects.equals(this.key, other.key);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.key != null ? this.key.hashCode() : 0);
        hash = 53 * hash + (this.self != null ? this.self.hashCode() : 0);

        return hash;
    }
}
