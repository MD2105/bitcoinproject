package com.example.bitcoin_price.service;
// Add the following import, or define the class if it doesn't exist
import com.example.bitcoin_price.client.CoindeskClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
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

  public Mono<List<PricePoint>> fetchPrices(String start, String end, boolean offline) {
    String key = start + "_" + end;
    if (offline) {
      return Mono.just(cache.getOrDefault(key, Collections.emptyMap()))
                 .map(this::toPoints);
    }
    CircuitBreaker cb = CircuitBreaker.ofDefaults("coindeskCB");
    return client.getHistoricalPrices(start, end)
      .transformDeferred(CircuitBreakerOperator.of(cb))
      .doOnNext(data -> cache.put(key, data))
      .onErrorResume(ex -> Mono.just(cache.getOrDefault(key, Collections.emptyMap())))
      .map(this::toPoints);
  }

  private List<PricePoint> toPoints(Map<String, Double> bpiMap) {
    double min = bpiMap.values().stream().min(Double::compareTo).orElse(0d);
    double max = bpiMap.values().stream().max(Double::compareTo).orElse(0d);
    return bpiMap.entrySet().stream()
      .map(e -> new PricePoint(
        e.getKey(), e.getValue(),
        e.getValue()==max?"high": e.getValue()==min?"low":""))
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
