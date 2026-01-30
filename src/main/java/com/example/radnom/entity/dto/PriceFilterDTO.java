package com.example.radnom.entity.dto;

import lombok.Data;

@Data
public class PriceFilterDTO {
    private Integer minPrice;
    private Integer maxPrice;

    // Dodaj walidację jeśli chcesz
    public boolean isValid() {
        if (minPrice != null && minPrice < 0) return false;
        if (maxPrice != null && maxPrice < 0) return false;
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) return false;
        return true;
    }
}