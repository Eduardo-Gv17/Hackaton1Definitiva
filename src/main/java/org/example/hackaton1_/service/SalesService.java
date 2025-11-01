package org.example.hackaton1_.service;

import org.example.hackaton1_.dto.SaleRequest;
import org.example.hackaton1_.model.Sale;
import org.example.hackaton1_.repository.SalesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalesService {

    private final SalesRepository salesRepository;

    public SalesService(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    public Sale createSale(SaleRequest req) {
        Sale sale = new Sale();
        sale.setSku(req.getSku());
        sale.setUnits(req.getUnits());
        sale.setPrice(req.getPrice());
        sale.setBranch(req.getBranch());
        sale.setSoldAt(req.getSoldAt());
        sale.setCreatedBy("system"); 

        return salesRepository.save(sale);
    }

    public List<Sale> getAllSales() {
        return salesRepository.findAll();
    }

    public Sale getSaleById(String id) {
        return salesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
    }
}
