package com.legalai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Wrapper for paginated results from CourtListener Search API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourtListenerSearchResponse {

    private int count;
    private String next;
    private String previous;
    private List<CourtListenerDTO> results;

    public CourtListenerSearchResponse() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public List<CourtListenerDTO> getResults() {
        return results;
    }

    public void setResults(List<CourtListenerDTO> results) {
        this.results = results;
    }
}
