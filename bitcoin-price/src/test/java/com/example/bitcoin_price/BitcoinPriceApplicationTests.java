package com.example.bitcoin_price;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.bitcoin_price.client.CoindeskClient;
import com.example.bitcoin_price.dto.PriceResponse;
import com.example.bitcoin_price.service.BitcoinPriceService;




@SpringBootTest
class BitcoinPriceApplicationTests {

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
    }

    @Test
    void testGetPrices() {
        CoindeskClient mockClient = mock(CoindeskClient.class);
        BitcoinPriceService service = new BitcoinPriceService();
        service.setCoindeskClient(mockClient);

        // Mocking the response from the client
        java.time.LocalDate startDate = java.time.LocalDate.parse("2023-01-01");
        java.time.LocalDate endDate = java.time.LocalDate.parse("2023-01-31");
        when(mockClient.getBitcoinPrices(startDate, endDate))
            .thenReturn(Map.of("2023-01-01", 30000.0, "2023-01-02", 31000.0));

        java.time.LocalDate start = java.time.LocalDate.parse("2023-01-01");
        java.time.LocalDate end = java.time.LocalDate.parse("2023-01-31");
        List<PriceResponse> prices = service.getPrices(start, end, false);
        assertTrue(prices.size() > 0);
    }

}




