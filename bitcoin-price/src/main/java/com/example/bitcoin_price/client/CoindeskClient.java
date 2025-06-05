package com.example.bitcoin_price.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class CoindeskClient {
    private final RestTemplate restTemplate;

    public CoindeskClient() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Double> getHistoricalPricesSync(String start, String end) {
        String url = "https://api.coindesk.com/v1/bpi/historical/close.json?start=" + start + "&end=" + end;
        try {
            HistoricalResponse response = restTemplate.getForObject(url, HistoricalResponse.class);
            return response != null ? response.getBpi() : Map.of();
        } catch (RestClientException ex) {
            
            throw ex; 
        }
    }

    private static class HistoricalResponse {
        private Map<String, Double> bpi;
        public Map<String, Double> getBpi() { return bpi; }
        public void setBpi(Map<String, Double> bpi) { this.bpi = bpi; }
    }
}