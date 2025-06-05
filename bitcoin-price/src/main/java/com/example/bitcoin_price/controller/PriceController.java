package com.example.bitcoin_price.controller;

import com.example.bitcoin_price.dto.ErrorResponse;
import org.springframework.web.bind.annotation.RestController;

import com.example.bitcoin_price.service.BitcoinPriceService;
import com.example.bitcoin_price.service.BitcoinPriceService.PricePoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
public class PriceController {

    private final BitcoinPriceService service;
    private static final Logger logger = LoggerFactory.getLogger(PriceController.class);

    public PriceController(BitcoinPriceService service) {
        this.service = service;
    }

    @GetMapping("/api/v1/prices")
    public ResponseEntity<?> getPrices(
        @RequestParam String start,
        @RequestParam String end,
        @RequestParam(defaultValue="false") boolean offline) {
        try {
            LocalDate.parse(start);
            LocalDate.parse(end);
        } catch (DateTimeParseException ex) {
            logger.error("Invalid date format for start or end: start={}, end={}", start, end);
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid date format. Expected yyyy-MM-dd."));
        }

        try {
            List<PricePoint> prices = service.fetchPrices(start, end, offline);
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