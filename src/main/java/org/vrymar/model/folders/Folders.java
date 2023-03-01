package org.vrymar.model.folders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Folders {

    @JsonProperty("next")
    private String next;

    @JsonProperty("startAt")
    private Integer startAt;

    @JsonProperty("maxResults")
    private Integer maxResults;

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("isLast")
    private Boolean isLast;

    @JsonProperty("values")
    private List<Values> values;


    public static List<Integer> getZephyrFoldersIdsByName(Folders folders, String folderName) {
        return folders.getValues().stream()
                .filter(p -> p.getName().equalsIgnoreCase(folderName))
                .map(Values::getId)
                .collect(Collectors.toList());
    }
}
