package com.example.bitcoin_price.controller;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.example.bitcoin_price.service.BitcoinPriceService;
import com.example.bitcoin_price.service.BitcoinPriceService.PricePoint;
import reactor.core.publisher.Mono;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;


@RestController
@RequestMapping("/api/v1/prices")
@CrossOrigin(origins = "http://localhost:5173")
public class PriceController {
    private final BitcoinPriceService service;
    public PriceController(BitcoinPriceService service) {
        this.service = service;
        System.out.println("PriceController initialized");
        logger.info("PriceController initialized");
    }

private static final Logger logger = LoggerFactory.getLogger(PriceController.class);

    @GetMapping
    public Mono<List<PricePoint>> getPrices(
        @RequestParam String start,
        @RequestParam String end,
        @RequestParam(defaultValue="false") boolean offline
    ) {
        // Validate date format
        try {
            LocalDate.parse(start);
            LocalDate.parse(end);
        } catch (DateTimeParseException ex) {
            logger.error("Invalid date format for start or end: start={}, end={}", start, end);
            return Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid date format. Expected yyyy-MM-dd."
            ));
        }

        return service.fetchPrices(start, end, offline)
        .onErrorResume(ex -> {
            logger.error("Error fetching prices", ex);
            return Mono.error(new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Public API not available"
            ));
        });
    }

}