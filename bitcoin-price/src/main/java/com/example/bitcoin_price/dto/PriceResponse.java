package com.example.bitcoin_price.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceResponse {
    private String date;
    private double price;
    private String marker;
    private String currency; // "high", "low", or ""
}
