package com.integration.service;

import com.integration.model.OrderCsv;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;

@Service
public class CsvReaderService {

    public List<OrderCsv> readOrders(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            CsvToBean<OrderCsv> csvToBean = new CsvToBeanBuilder<OrderCsv>(reader)
                    .withType(OrderCsv.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            return csvToBean.parse();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}