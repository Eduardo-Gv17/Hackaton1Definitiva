package org.example.hackaton1_.dto;

import java.time.LocalDate;

public class SalesSummaryRequest {
    private LocalDate from;
    private LocalDate to;
    private String branch;
    private String emailTo;


    public LocalDate getFrom() { return from; }
    public void setFrom(LocalDate from) { this.from = from; }
    public LocalDate getTo() { return to; }
    public void setTo(LocalDate to) { this.to = to; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getEmailTo() { return emailTo; }
    public void setEmailTo(String emailTo) { this.emailTo = emailTo; }
}
