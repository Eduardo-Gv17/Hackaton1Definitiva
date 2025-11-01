package org.example.hackaton1_.controller;

import org.example.hackaton1_.dto.SaleRequest;
import org.example.hackaton1_.model.Sale;
import org.example.hackaton1_.service.SalesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }


    @PostMapping
    public ResponseEntity<Sale> createSale(@RequestBody SaleRequest request) {
        Sale created = salesService.createSale(request);
        return ResponseEntity.status(201).body(created);
    }


    @GetMapping
    public ResponseEntity<List<Sale>> getAllSales() {
        List<Sale> sales = salesService.getAllSales();
        return ResponseEntity.ok(sales);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSale(@PathVariable String id) {
        Sale sale = salesService.getSaleById(id);
        return ResponseEntity.ok(sale);
    }
}
