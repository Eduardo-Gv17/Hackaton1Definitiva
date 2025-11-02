package org.example.hackaton1_.service;

import org.example.hackaton1_.dto.SaleRequest;
import org.example.hackaton1_.exception.ForbiddenException;
import org.example.hackaton1_.exception.ResourceNotFoundException;
import org.example.hackaton1_.model.Role;
import org.example.hackaton1_.model.Sale;
import org.example.hackaton1_.model.User;
import org.example.hackaton1_.repository.SalesRepository;
import org.example.hackaton1_.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SalesService {

    private final SalesRepository salesRepository;

    public SalesService(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    // (Corregido: Usar ForbiddenException y createdBy)
    public Sale createSale(SaleRequest req) {
        User currentUser = SecurityUtils.getCurrentUser();

        if (currentUser.getRole() == Role.BRANCH && !currentUser.getBranch().equals(req.getBranch())) {
            throw new ForbiddenException("No tiene permiso para crear ventas en la sucursal: " + req.getBranch());
        }

        Sale sale = new Sale();
        sale.setSku(req.getSku());
        sale.setUnits(req.getUnits());
        sale.setPrice(req.getPrice());
        sale.setBranch(req.getBranch());
        sale.setSoldAt(req.getSoldAt());
        sale.setCreatedBy(currentUser.getUsername()); // <-- Corregido

        return salesRepository.save(sale);
    }

    // (Nuevo: Método de filtrado implementado)
    public List<Sale> getFilteredSales(String fromStr, String toStr, String branchFilter) {
        User currentUser = SecurityUtils.getCurrentUser();

        LocalDateTime start = (fromStr != null) ? LocalDate.parse(fromStr).atStartOfDay() : LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime end = (toStr != null) ? LocalDate.parse(toStr).atTime(LocalTime.MAX) : LocalDateTime.now();

        String effectiveBranch = null;

        if (currentUser.getRole() == Role.BRANCH) {
            effectiveBranch = currentUser.getBranch();
            if (branchFilter != null && !branchFilter.equals(effectiveBranch)) {
                throw new ForbiddenException("No puede consultar ventas de otra sucursal.");
            }
        } else { // ROLE_CENTRAL
            effectiveBranch = branchFilter;
        }

        if (effectiveBranch != null) {
            return salesRepository.findByBranchAndDateRange(effectiveBranch, start, end);
        } else {
            return salesRepository.findByDateRange(start, end);
        }
    }

    // (Corregido: Aplicar permisos)
    public Sale getSaleById(String id) {
        User currentUser = SecurityUtils.getCurrentUser();
        Sale sale = salesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        if (currentUser.getRole() == Role.BRANCH && !currentUser.getBranch().equals(sale.getBranch())) {
            throw new ForbiddenException("No tiene permiso para ver esta venta.");
        }

        return sale;
    }

    // (Nuevo: Método Update implementado)
    public Sale updateSale(String id, SaleRequest req) {
        User currentUser = SecurityUtils.getCurrentUser();
        Sale sale = getSaleById(id); // Reutiliza el método que ya valida permisos de lectura

        // Validar permisos de escritura (solo CENTRAL o BRANCH dueño)
        if (currentUser.getRole() == Role.BRANCH && !currentUser.getBranch().equals(sale.getBranch())) {
            throw new ForbiddenException("No tiene permiso para actualizar esta venta.");
        }
        // Un BRANCH no puede mover una venta a otra sucursal
        if (currentUser.getRole() == Role.BRANCH && !req.getBranch().equals(currentUser.getBranch())) {
            throw new ForbiddenException("No puede mover una venta a otra sucursal.");
        }
        // Un CENTRAL sí puede moverla

        sale.setSku(req.getSku());
        sale.setUnits(req.getUnits());
        sale.setPrice(req.getPrice());
        sale.setBranch(req.getBranch());
        sale.setSoldAt(req.getSoldAt());

        return salesRepository.save(sale);
    }

    // (Nuevo: Método Delete implementado)
    // La seguridad @PreAuthorize("hasRole('CENTRAL')") ya está en el Controller
    public void deleteSale(String id) {
        if (!salesRepository.existsById(id)) {
            // Línea corregida (se eliminó la "D" extra)
            throw new ResourceNotFoundException("Venta no encontrada con id: " + id);
        }
        salesRepository.deleteById(id);
    }
}