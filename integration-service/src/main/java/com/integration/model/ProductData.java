package com.integration.model;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductData {

    private int id;
    private String pid;
    private String name;
    private String price_per_unit;
}
