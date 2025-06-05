package com.example.bitcoin_price.service;
import com.example.bitcoin_price.client.CoindeskClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BitcoinPriceService {
    private final CoindeskClient client;
    private final Map<String, Map<String, Double>> cache = new ConcurrentHashMap<>();

    public BitcoinPriceService(CoindeskClient client) {
        this.client = client;
    }

    public List<PricePoint> fetchPrices(String start, String end, boolean offline) {
        String key = start + "_" + end;
        if (offline) {
            return toPoints(cache.getOrDefault(key, Collections.emptyMap()));
        }
        CircuitBreaker cb = CircuitBreaker.ofDefaults("coindeskCB");
        try {
            Map<String, Double> data = cb.executeSupplier(() -> client.getHistoricalPricesSync(start, end));
            cache.put(key, data);
            return toPoints(data);
        } catch (Exception ex) {
  
            return toPoints(cache.getOrDefault(key, Collections.emptyMap()));
        }
    }

    private List<PricePoint> toPoints(Map<String, Double> bpiMap) {
        if (bpiMap.isEmpty()) return Collections.emptyList();
        double min = bpiMap.values().stream().min(Double::compareTo).orElse(0d);
        double max = bpiMap.values().stream().max(Double::compareTo).orElse(0d);
        return bpiMap.entrySet().stream()
            .map(e -> new PricePoint(
                e.getKey(), e.getValue(),
                e.getValue() == max ? "high" : e.getValue() == min ? "low" : ""))
            .sorted(Comparator.comparing(PricePoint::getDate))
            .collect(Collectors.toList());
    }

    public static class PricePoint {
        private String date;
        private double price;
        private String marker;

        public PricePoint() {}
        public PricePoint(String date, double price, String marker) {
            this.date = date; this.price = price; this.marker = marker;
        }
        public String getDate() { return date; }
        public double getPrice() { return price; }
        public String getMarker() { return marker; }
    }
}