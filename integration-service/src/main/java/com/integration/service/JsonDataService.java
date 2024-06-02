package com.integration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integration.model.ProductData;
import org.springframework.stereotype.Service;
import product.Product;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class JsonDataService {

    private static final String PRODUCT_DATA_FILE = "bootstrap/product/product-data.json";

    public List<ProductData> getProducts() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return List.of(mapper.readValue(new File(PRODUCT_DATA_FILE), ProductData[].class));
    }
}