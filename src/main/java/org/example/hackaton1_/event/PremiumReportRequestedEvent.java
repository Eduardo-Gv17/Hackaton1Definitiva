package org.example.hackaton1_.event;

import org.example.hackaton1_.dto.PremiumSalesSummaryRequest;

public class PremiumReportRequestedEvent {

    private final PremiumSalesSummaryRequest request;

    public PremiumReportRequestedEvent(PremiumSalesSummaryRequest request) {
        this.request = request;
    }

    public PremiumSalesSummaryRequest getRequest() {
        return request;
    }
}