package org.example.hackaton1_.service;

import org.example.hackaton1_.dto.SaleRequest;
import org.example.hackaton1_.model.Role;
import org.example.hackaton1_.model.Sale;
import org.example.hackaton1_.model.User;
import org.example.hackaton1_.repository.SalesRepository;
import org.example.hackaton1_.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalesService {

    private final SalesRepository salesRepository;

    public SalesService(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    public Sale createSale(SaleRequest req) {
        User currentUser = SecurityUtils.getCurrentUser();

        // 1. Validar permiso de sucursal para usuarios BRANCH
        if (currentUser.getRole() == Role.BRANCH && !currentUser.getBranch().equals(req.getBranch())) {
            // Lanza una excepción que GlobalExceptionHandler mapeará a 403 Forbidden
            throw new RuntimeException("No tiene permiso para crear ventas en la sucursal: " + req.getBranch());
        }
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
