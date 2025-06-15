package com.example.bitcoin_price.client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;





@Slf4j
@Component

public class CoindeskClient {

    @Autowired
    private RestTemplate restTemplate;
    public Map<String, Double> getBitcoinPrices(LocalDate start, LocalDate end) {
        long startEpoch = start.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long endEpoch = end.atStartOfDay().toEpochSecond(ZoneOffset.UTC);

        String url = String.format(
            "https://api.coingecko.com/api/v3/coins/bitcoin/market_chart/range" +
            "?vs_currency=usd&from=%d&to=%d", startEpoch, endEpoch);

        log.info("Calling CoinGecko API: {}", url);

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<List<Object>> prices = (List<List<Object>>) response.get("prices");
        Map<String, Double> result = new TreeMap<>();

        for (List<Object> entry : prices) {
            long timestamp = ((Number) entry.get(0)).longValue();
            double price = ((Number) entry.get(1)).doubleValue();
            String date = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).toLocalDate().toString();
            result.put(date, price);
        }
        return result;
    }
}
