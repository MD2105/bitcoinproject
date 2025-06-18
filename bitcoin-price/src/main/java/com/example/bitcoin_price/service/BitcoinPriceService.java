package com.example.bitcoin_price.service;

import com.example.bitcoin_price.client.CoindeskClient;
import com.example.bitcoin_price.dto.PriceResponse;
import com.example.bitcoin_price.util.SimpleCircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class BitcoinPriceService {

    private final SimpleCircuitBreaker circuitBreaker = new SimpleCircuitBreaker(3, 60000);

    @Autowired
    private CoindeskClient coindeskClient;

    private final Map<String, Map<String, Double>> priceCache = new HashMap<>();

    public List<PriceResponse> getPrices(LocalDate start, LocalDate end, boolean offline, String currency) {
        currency = currency.toUpperCase();
        Map<String, Double> currencyCache = priceCache.computeIfAbsent(currency, k -> new HashMap<>());
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            dates.add(d);
        }

        List<LocalDate> missingDates = new ArrayList<>();
        for (LocalDate d : dates) {
            if (!currencyCache.containsKey(d.toString())) {
                missingDates.add(d);
            }
        }

        // If not offline, fetch missing dates and update cache
        if (!offline && !missingDates.isEmpty()) {
            if (!circuitBreaker.allowRequest()) {
                return fallbackPrices(start, end, offline, currency, new RuntimeException("Circuit is OPEN"));
            }
            try {
              
                Map<String, Double> fetched = coindeskClient.getBitcoinPrices(currency, missingDates.get(0), missingDates.get(missingDates.size() - 1));
                for (Map.Entry<String, Double> entry : fetched.entrySet()) {
                    currencyCache.put(entry.getKey(), entry.getValue());
                }
                circuitBreaker.recordSuccess(); 
            } catch (Exception e) {
                circuitBreaker.recordFailure(); 
                return fallbackPrices(start, end, offline, currency, e);
            }
        }

        
        List<PriceResponse> responseList = new ArrayList<>();
        double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
        for (LocalDate d : dates) {
            Double price = currencyCache.get(d.toString());
            if (price != null) {
                max = Math.max(max, price);
                min = Math.min(min, price);
            }
        }
        for (LocalDate d : dates) {
            Double price = currencyCache.get(d.toString());
            if (price != null) {
                String marker = price == max ? "high" : price == min ? "low" : "";
                responseList.add(new PriceResponse(d.toString(), price, marker, currency));
            }
        }
        return responseList;
    }

    // Fallback method for circuit breaker
    public List<PriceResponse> fallbackPrices(LocalDate start, LocalDate end, boolean offline, String currency, Throwable t) {
        currency = currency.toUpperCase();
        Map<String, Double> currencyCache = priceCache.getOrDefault(currency, Collections.emptyMap());
        List<PriceResponse> responseList = new ArrayList<>();
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            dates.add(d);
        }
        double max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
        for (LocalDate d : dates) {
            Double price = currencyCache.get(d.toString());
            if (price != null) {
                max = Math.max(max, price);
                min = Math.min(min, price);
            }
        }
        for (LocalDate d : dates) {
            Double price = currencyCache.get(d.toString());
            if (price != null) {
                String marker = price == max ? "high" : price == min ? "low" : "";
                responseList.add(new PriceResponse(d.toString(), price, marker, currency));
            }
        }
        return responseList;
    }
}
