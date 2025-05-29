package com.example.bitcoin_price.client;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
public class CoindeskClient {
     private final WebClient webClient;
    public CoindeskClient(WebClient coindeskWebClient) {
        this.webClient = coindeskWebClient;
    }
    public Mono<Map<String, Double>> getHistoricalPrices(String start, String end) {
        return webClient.get()
            .uri(uri -> uri.path("/historical/close.json")
                           .queryParam("start", start)
                           .queryParam("end", end)
                           .build())
            .retrieve()
            .bodyToMono(HistoricalResponse.class)
            .map(HistoricalResponse::getBpi);
    }
    private static class HistoricalResponse {
        private Map<String, Double> bpi;
        public Map<String, Double> getBpi() { return bpi; }
        public void setBpi(Map<String, Double> bpi) { this.bpi = bpi; }
    }
}