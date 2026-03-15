package com.railway.train_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trains", indexes = {
        @Index(name = "idx_train_number", columnList = "trainNumber"),
        @Index(name = "idx_source_destination", columnList = "source, destination")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer trainNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private Integer totalDistanceInKm;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private Integer availableSeats;

    @PrePersist
    public void availableSeatsInitialization() {
        this.availableSeats = this.totalSeats;
    }

}
