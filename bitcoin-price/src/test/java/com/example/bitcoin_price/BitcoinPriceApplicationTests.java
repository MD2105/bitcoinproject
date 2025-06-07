package com.example.bitcoin_price;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.bitcoin_price.client.CoindeskClient;
import com.example.bitcoin_price.service.BitcoinPriceService;
import com.example.bitcoin_price.service.BitcoinPriceService.PricePoint;



@SpringBootTest
class BitcoinPriceApplicationTests {

	@Test
  void fetchPricesAssignsMarkers() {
    var client = mock(CoindeskClient.class);
    var svc = new BitcoinPriceService(client);
    Map<String,Double> data = Map.of(
      "2025-01-01", 10.0,
      "2025-01-02", 20.0,
      "2025-01-03", 5.0
    );
   when(client.getHistoricalPricesSync("2025-01-01","2025-01-03"))
  .thenReturn(data);

List<PricePoint> list = svc.fetchPrices("2025-01-01","2025-01-03",false);
assertTrue(list.stream().anyMatch(p -> p.getMarker().equals("high")));
assertTrue(list.stream().anyMatch(p -> p.getMarker().equals("low")));
  }

  @Test
  void fetchPricesUsesCacheWhenOffline() {
    var client = mock(CoindeskClient.class);
    var svc = new BitcoinPriceService(client);
    Map<String,Double> data = Map.of(
      "2025-01-01", 10.0,
      "2025-01-02", 20.0
    );
    when(client.getHistoricalPricesSync("2025-01-01","2025-01-02"))
      .thenReturn(data)
      .thenThrow(new RuntimeException("fail"));

    List<PricePoint> first = svc.fetchPrices("2025-01-01","2025-01-02", false);
    List<PricePoint> second = svc.fetchPrices("2025-01-01","2025-01-02", true);

    assertEquals(first.size(), second.size());
    assertEquals(first.get(0).getPrice(), second.get(0).getPrice());
    verify(client, times(1)).getHistoricalPricesSync("2025-01-01","2025-01-02");
  }

}




