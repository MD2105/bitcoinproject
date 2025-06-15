package com.example.bitcoin_price.controller;
import com.example.bitcoin_price.dto.ErrorResponse;
import com.example.bitcoin_price.dto.PriceResponse;

import org.springframework.web.bind.annotation.RestController;

import com.example.bitcoin_price.service.BitcoinPriceService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = {"http://localhost:5173", "https://localhost:3000"})
@RestController
public class PriceController {

    private final BitcoinPriceService service;
    private static final Logger logger = LoggerFactory.getLogger(PriceController.class);

    public PriceController(BitcoinPriceService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/prices")
public ResponseEntity<?> getPrices(
    @RequestParam(name = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
    @RequestParam(name = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
    @RequestParam(name = "offline", defaultValue="false") boolean offline,
    @RequestParam(name = "currency", defaultValue="USD") String currency){
        try {
            if (start.isAfter(end)) {
                logger.error("Start date {} is after end date {}", start, end);
                return ResponseEntity.badRequest().body(new ErrorResponse("Start date cannot be after end date."));
            }
            if (start.isBefore(LocalDate.of(2013, 1, 1))) {
                logger.error("Start date {} is before 2013-01-01", start);
                return ResponseEntity.badRequest().body(new ErrorResponse("Start date cannot be before 2013-01-01."));
            }
            if (end.isAfter(LocalDate.now())) {
                logger.error("End date {} is in the future", end);
                return ResponseEntity.badRequest().body(new ErrorResponse("End date cannot be in the future."));
            }
            if (start.isEqual(end)) {
                logger.error("Start date {} is the same as end date {}", start, end);
                return ResponseEntity.badRequest().body(new ErrorResponse("Start date cannot be the same as end date."));
            }
        } catch (DateTimeParseException ex) {
            logger.error("Invalid date format for start or end: start={}, end={}", start, end);
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid date format. Expected yyyy-MM-dd."));
        }

        try {
            List<PriceResponse> prices = service.getPrices(start,end, offline,currency);
            if (prices.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("No price data found for the given range."));
            }
            return ResponseEntity.ok(prices);
        } catch (Exception ex) {
            logger.error("Error fetching prices", ex);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Public API not available"));
        }
    }
}