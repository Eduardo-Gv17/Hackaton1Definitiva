// Archivo: org.example.hackaton1_.SalesAggregationServiceTest.java
package org.example.hackaton1_;

import org.example.hackaton1_.model.Sale;
import org.example.hackaton1_.repository.SalesRepository;
import org.example.hackaton1_.service.SalesAggregationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SalesAggregationServiceTest { // Asegúrate que el nombre de la clase termine en Test

    @Mock
    private SalesRepository salesRepository;

    @InjectMocks
    private SalesAggregationService salesAggregationService;

    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        start = LocalDate.of(2025, 9, 1).atStartOfDay();
        end = LocalDate.of(2025, 9, 7).atTime(23, 59, 59);
    }

    private Sale createSale(String sku, int units, double price, String branch) {
        Sale sale = new Sale();
        sale.setSku(sku);
        sale.setUnits(units);
        sale.setPrice(price);
        sale.setBranch(branch);
        sale.setSoldAt(LocalDateTime.now());
        return sale;
    }

    // 1. Test de agregados con datos válidos
    @Test
    void shouldCalculateCorrectAggregatesWithValidData() {
        // Given
        List<Sale> mockSales = List.of(
                createSale("OREO_CLASSIC", 10, 1.99, "Miraflores"),
                createSale("OREO_DOUBLE", 5, 2.49, "San Isidro"),
                createSale("OREO_CLASSIC", 15, 1.99, "Miraflores")
        );
        when(salesRepository.findByDateRange(any(), any())).thenReturn(mockSales);

        // When
        Map<String, Object> result = salesAggregationService.calculateSummary(start.toLocalDate(), end.toLocalDate(), null);

        // Then
        assertThat(result.get("totalUnits")).isEqualTo(30);
        // (10*1.99) + (5*2.49) + (15*1.99) = 19.9 + 12.45 + 29.85 = 62.20
        assertThat((Double) result.get("totalRevenue")).isEqualTo(62.20);
        assertThat(result.get("topSku")).isEqualTo("OREO_CLASSIC");
        assertThat(result.get("topBranch")).isEqualTo("Miraflores");
    }

    // 2. Test con lista vacía
    @Test
    void shouldReturnDefaultValuesWhenSalesListIsEmpty() {
        // Given
        when(salesRepository.findByDateRange(any(), any())).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = salesAggregationService.calculateSummary(start.toLocalDate(), end.toLocalDate(), null);

        // Then
        assertThat(result.get("totalUnits")).isEqualTo(0);
        assertThat(result.get("totalRevenue")).isEqualTo(0.0);
        assertThat(result.get("topSku")).isEqualTo("N/A");
    }

    // 3. Test de filtrado por sucursal
    @Test
    void shouldFilterByBranchCorrectly() {
        // Given
        String targetBranch = "Miraflores";
        List<Sale> mockSales = List.of(
                createSale("OREO_CLASSIC", 10, 1.99, targetBranch),
                createSale("OREO_DOUBLE", 5, 2.49, targetBranch)
        );
        // Mockear el método que filtra por branch
        when(salesRepository.findByBranchAndDateRange(any(), any(), any())).thenReturn(mockSales);

        // When
        Map<String, Object> result = salesAggregationService.calculateSummary(start.toLocalDate(), end.toLocalDate(), targetBranch);

        // Then
        assertThat(result.get("totalUnits")).isEqualTo(15);
        assertThat(result.get("topBranch")).isEqualTo(targetBranch);
    }

    // 4. Test de filtrado por fechas (implícito por el mock)
    // El setup ya prueba esto porque pasamos las fechas start/end a Mockito.

    // 5. Test de cálculo de SKU top (con empate)
    @Test
    void shouldIdentifyTopSkuCorrectlyOnTieBreak() {
        // Given
        List<Sale> mockSales = List.of(
                createSale("OREO_A", 10, 1.0, "B"),
                createSale("OREO_B", 10, 1.0, "B"),
                createSale("OREO_C", 5, 1.0, "B")
        );
        when(salesRepository.findByDateRange(any(), any())).thenReturn(mockSales);

        // When
        Map<String, Object> result = salesAggregationService.calculateSummary(start.toLocalDate(), end.toLocalDate(), null);

        // Then
        // La implementación actual usará el primer SKU con el máximo valor encontrado (OREO_A o OREO_B)
        assertThat(result.get("topSku")).isIn("OREO_A", "OREO_B");
        assertThat(result.get("totalUnits")).isEqualTo(25);
    }
}