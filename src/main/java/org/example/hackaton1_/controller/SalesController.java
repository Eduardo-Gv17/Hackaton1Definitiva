package org.example.hackaton1_.controller;

import org.example.hackaton1_.dto.SaleRequest;
import org.example.hackaton1_.event.ReportRequestedEvent;
import org.example.hackaton1_.model.Sale;
import org.example.hackaton1_.service.SalesService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/sales")
public class SalesController {

    private final SalesService salesService;
    private final ApplicationEventPublisher eventPublisher;

    public SalesController(SalesService salesService, ApplicationEventPublisher eventPublisher) {
        this.salesService = salesService;
        this.eventPublisher = eventPublisher;
    }


    @PostMapping
    public ResponseEntity<Sale> createSale(@RequestBody SaleRequest request) {
        Sale created = salesService.createSale(request);
        return ResponseEntity.status(201).body(created);
    }


    @GetMapping
    public ResponseEntity<List<Sale>> getAllSales(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String branch) {

        // El SalesService debe manejar la lógica de filtrado por usuario/rol
        List<Sale> sales = salesService.getFilteredSales(from, to, branch);
        return ResponseEntity.ok(sales);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sale> updateSale(@PathVariable String id, @RequestBody SaleRequest request) {
        Sale updated = salesService.updateSale(id, request);
        return ResponseEntity.ok(updated);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Sale> getSale(@PathVariable String id) {
        Sale sale = salesService.getSaleById(id);
        return ResponseEntity.ok(sale);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CENTRAL')")
    public ResponseEntity<Void> deleteSale(@PathVariable String id) {
        salesService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/summary/weekly")
    public ResponseEntity<Map<String, Object>> requestWeeklySummary(@RequestBody SalesSummaryRequest req) {
        User currentUser = SecurityUtils.getCurrentUser();

        // 1. Validaciones y Fechas por defecto
        if (req.getEmailTo() == null || req.getEmailTo().isBlank()) {
            throw new RuntimeException("El campo emailTo es obligatorio."); // 400
        }
        if (currentUser.getRole() == Role.BRANCH && !currentUser.getBranch().equals(req.getBranch())) {
            throw new RuntimeException("No puede generar resúmenes de otra sucursal."); // 403
        }

        if (req.getFrom() == null || req.getTo() == null) {
            req.setTo(LocalDate.now());
            req.setFrom(req.getTo().minusDays(7)); // Última semana
        }

        // 2. Publicar evento (dispara el procesamiento asíncrono)
        eventPublisher.publishEvent(new ReportRequestedEvent(req));

        // 3. Respuesta 202 Accepted
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "requestId", "req_" + UUID.randomUUID(),
                "status", "PROCESSING",
                "message", "Su solicitud de reporte está siendo procesada. Recibirá el resumen en " + req.getEmailTo() + " en unos momentos.",
                "estimatedTime", "30-60 segundos",
                "requestedAt", LocalDateTime.now()
        ));
    }
}
