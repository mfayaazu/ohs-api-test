package com.integration.model;

import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProcessedOrder {

    private String userPid;
    private String orderPid;
    private String supplierPid;
}
