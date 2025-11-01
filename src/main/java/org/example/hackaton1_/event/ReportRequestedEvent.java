package org.example.hackaton1_.event;

import org.example.hackaton1_.dto.SalesSummaryRequest;


public class ReportRequestedEvent {

    private final SalesSummaryRequest request;

    public ReportRequestedEvent(SalesSummaryRequest request) {
        this.request = request;
    }

    public SalesSummaryRequest getRequest() {
        return request;
    }
}