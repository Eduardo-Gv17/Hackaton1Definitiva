package org.example.hackaton1_.service;

import org.example.hackaton1_.model.Sale;
import org.example.hackaton1_.repository.SalesRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalesAggregationService {

    private final SalesRepository salesRepository;

    public SalesAggregationService(SalesRepository salesRepository) {
        this.salesRepository = salesRepository;
    }

    public Map<String, Object> calculateSummary(LocalDate from, LocalDate to, String branch) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);

        List<Sale> sales = (branch == null)
                ? salesRepository.findByDateRange(start, end)
                : salesRepository.findByBranchAndDateRange(branch, start, end);

        if (sales.isEmpty()) {
            return Map.of("totalUnits", 0, "totalRevenue", 0.0, "topSku", "N/A", "topBranch", branch == null ? "N/A" : branch);
        }

        int totalUnits = sales.stream().mapToInt(Sale::getUnits).sum();
        double totalRevenue = sales.stream().mapToDouble(s -> s.getUnits() * s.getPrice()).sum();

        String topSku = sales.stream()
                .collect(Collectors.groupingBy(Sale::getSku, Collectors.summingInt(Sale::getUnits)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();

        String topBranch = sales.stream()
                .collect(Collectors.groupingBy(Sale::getBranch, Collectors.summingInt(Sale::getUnits)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();

        return Map.of(
                "totalUnits", totalUnits,
                "totalRevenue", totalRevenue,
                "topSku", topSku,
                "topBranch", topBranch
        );
    }
}
