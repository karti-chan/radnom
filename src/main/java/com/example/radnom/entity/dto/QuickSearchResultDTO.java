// src/main/java/com/example/radnom/dto/QuickSearchResultDTO.java
package com.example.radnom.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuickSearchResultDTO {
    private Integer id;
    private String name;
    private Integer price;
    private String imageUrl;
    private String category;
}