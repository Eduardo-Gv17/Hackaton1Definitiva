package org.example.hackaton1_.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PremiumSalesSummaryRequest extends SalesSummaryRequest {

    @NotBlank
    private String format; // Debe ser "PREMIUM"

    private boolean includeCharts;
    private boolean attachPdf;
}