package org.example.hackaton1_.dto;

import java.time.LocalDateTime;

public class SaleRequest {
    private String sku;
    private int units;
    private double price;
    private String branch;
    private LocalDateTime soldAt;


    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public LocalDateTime getSoldAt() { return soldAt; }
    public void setSoldAt(LocalDateTime soldAt) { this.soldAt = soldAt; }
}