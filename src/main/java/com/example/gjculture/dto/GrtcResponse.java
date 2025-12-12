package com.example.gjculture.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GrtcResponse {

    public Body body;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        public Items items;
        public int pageNo;
        public int numOfRows;
        public int totalCount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        public List<Item> item;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        public String stationName;
        public String placeName;
        public String category;
        public String locplc;
        public String operTime;
        public String siteTel;
        public String hmpg;
        public String latitude;
        public String longitude;
    }
}
