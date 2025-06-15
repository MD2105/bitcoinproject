package com.example.bitcoin_price.service;

import com.example.bitcoin_price.client.CoindeskClient;
import com.example.bitcoin_price.dto.PriceResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class BitcoinPriceService {

    private static final String CB_NAME = "coindeskCB";

    @Autowired
    private CoindeskClient coindeskClient;

    // currency -> (date -> price)
    private final Map<String, Map<String, Double>> priceCache = new HashMap<>();
    private final Map<String, Double> rateCache = new HashMap<>();

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "fallbackPrices")
    public List<PriceResponse> getPrices(LocalDate start, LocalDate end, boolean offline, String currency) {
        currency = currency.toUpperCase();
        Map<String, Double> currencyCache = priceCache.computeIfAbsent(currency, k -> new HashMap<>());
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            dates.add(d);
        }

        // Collect missing dates
        List<LocalDate> missingDates = new ArrayList<>();
        for (LocalDate d : dates) {
            if (!currencyCache.containsKey(d.toString())) {
                missingDates.add(d);
            }
        }

        // If not offline, fetch missing dates and update cache
        if (!offline && !missingDates.isEmpty()) {
            Map<String, Double> fetched = coindeskClient.getBitcoinPrices(
                missingDates.get(0), missingDates.get(missingDates.size() - 1)
            );
            double rate = "USD".equals(currency) ? 1.0 : getExchangeRate(currency);
            for (Map.Entry<String, Double> entry : fetched.entrySet()) {
                currencyCache.put(entry.getKey(), entry.getValue() * rate);
            }
        }

        // Gather all prices for the requested range
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

    // Example exchange rate fetcher (simple, not production-ready)
    private double getExchangeRate(String currency) {
        currency = currency.toUpperCase();
        if ("USD".equals(currency)) return 1.0;
        if (rateCache.containsKey(currency)) {
            return rateCache.get(currency);
        }
        try {
            var restTemplate = new org.springframework.web.client.RestTemplate();
            String url = "https://api.exchangerate.host/latest?base=USD&symbols=" + currency;
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
            Map<String, Double> rates = (Map<String, Double>) resp.get("rates");
            double rate = rates.getOrDefault(currency, 1.0);
            rateCache.put(currency, rate);
            return rate;
        } catch (Exception e) {
            return 1.0;
        }
    }
}