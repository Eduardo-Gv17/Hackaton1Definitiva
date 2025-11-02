package org.example.hackaton1_.model;

import jakarta.persistence.*;
import lombok.Data; // Importar Lombok
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String sku;
    private int units;
    private double price;
    private String branch;
    private LocalDateTime soldAt;
    private String createdBy;
    private LocalDateTime createdAt = LocalDateTime.now();

}