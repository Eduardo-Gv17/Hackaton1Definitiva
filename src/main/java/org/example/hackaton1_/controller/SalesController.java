package org.example.hackaton1_.controller;

import jakarta.validation.Valid;
import org.example.hackaton1_.dto.SaleRequest;
import org.example.hackaton1_.dto.SalesSummaryRequest;
import org.example.hackaton1_.event.ReportRequestedEvent;
import org.example.hackaton1_.exception.BadRequestException;
import org.example.hackaton1_.exception.ForbiddenException;
import org.example.hackaton1_.model.Role;
import org.example.hackaton1_.model.Sale;
import org.example.hackaton1_.model.User;
import org.example.hackaton1_.security.SecurityUtils;
import org.example.hackaton1_.service.SalesService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.example.hackaton1_.dto.PremiumSalesSummaryRequest;
import org.example.hackaton1_.event.PremiumReportRequestedEvent;
import java.util.ArrayList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/sales")
@Validated
public class SalesController {

    private final SalesService salesService;
    private final ApplicationEventPublisher eventPublisher;

    public SalesController(SalesService salesService, ApplicationEventPublisher eventPublisher) {
        this.salesService = salesService;
        this.eventPublisher = eventPublisher;
    }


    @PostMapping
    public ResponseEntity<Sale> createSale(@Valid @RequestBody SaleRequest request) { // Añadir @Valid
        Sale created = salesService.createSale(request);
        return ResponseEntity.status(201).body(created);
    }


    @GetMapping
    public ResponseEntity<List<Sale>> getAllSales(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String branch) {

        List<Sale> sales = salesService.getFilteredSales(from, to, branch);
        return ResponseEntity.ok(sales);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sale> updateSale(@PathVariable String id, @Valid @RequestBody SaleRequest request) { // Añadir @Valid
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
    public ResponseEntity<Map<String, Object>> requestWeeklySummary(@RequestBody SalesSummaryRequest req) { // @Valid si añades validaciones
        User currentUser = SecurityUtils.getCurrentUser();

        // 1. Validaciones
        if (req.getEmailTo() == null || req.getEmailTo().isBlank()) {
            throw new BadRequestException("El campo emailTo es obligatorio."); // 400
        }

        // Asignar sucursal si es BRANCH
        if (currentUser.getRole() == Role.BRANCH) {
            if (req.getBranch() != null && !req.getBranch().equals(currentUser.getBranch())) {
                throw new ForbiddenException("No puede generar resúmenes de otra sucursal."); // 403
            }
            req.setBranch(currentUser.getBranch()); // Forzar la sucursal del usuario
        }

        if (req.getFrom() == null || req.getTo() == null) {
            req.setTo(LocalDate.now());
            req.setFrom(req.getTo().minusDays(7)); // Última semana
        }

        // 2. Publicar evento
        eventPublisher.publishEvent(new ReportRequestedEvent(req));

        // 3. Respuesta 202 Accepted
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "requestId", "req_" + UUID.randomUUID().toString().substring(0, 8),
                "status", "PROCESSING",
                "message", "Su solicitud de reporte está siendo procesada. Recibirá el resumen en " + req.getEmailTo() + " en unos momentos.",
                "estimatedTime", "30-60 segundos",
                "requestedAt", LocalDateTime.now()
        ));
    }

    @PostMapping("/summary/weekly/premium")
    public ResponseEntity<Map<String, Object>> requestPremiumSummary(@Valid @RequestBody PremiumSalesSummaryRequest req) {
        User currentUser = SecurityUtils.getCurrentUser();

        // 1. Validaciones
        if (req.getEmailTo() == null || req.getEmailTo().isBlank()) {
            throw new BadRequestException("El campo emailTo es obligatorio."); // 400
        }
        if (req.getFormat() == null || !req.getFormat().equalsIgnoreCase("PREMIUM")) {
            throw new BadRequestException("El formato debe ser 'PREMIUM'.");
        }

        // Asignar sucursal si es BRANCH
        if (currentUser.getRole() == Role.BRANCH) {
            if (req.getBranch() != null && !req.getBranch().equals(currentUser.getBranch())) {
                throw new ForbiddenException("No puede generar resúmenes de otra sucursal."); // 403
            }
            req.setBranch(currentUser.getBranch()); // Forzar la sucursal del usuario
        }

        if (req.getFrom() == null || req.getTo() == null) {
            req.setTo(LocalDate.now());
            req.setFrom(req.getTo().minusDays(7)); // Última semana
        }

        // 2. Publicar evento PREMIUM
        eventPublisher.publishEvent(new PremiumReportRequestedEvent(req));

        // 3. Respuesta 202 Accepted (como pide el README)
        List<String> features = new ArrayList<>();
        features.add("HTML_FORMAT");
        if (req.isIncludeCharts()) features.add("CHARTS");
        if (req.isAttachPdf()) features.add("PDF_ATTACHMENT");

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "requestId", "req_premium_" + UUID.randomUUID().toString().substring(0, 8),
                "status", "PROCESSING",
                "message", "Su reporte premium está siendo generado.",
                "estimatedTime", "60-90 segundos",
                "features", features,
                "requestedAt", LocalDateTime.now()
        ));
    }
}