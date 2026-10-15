package com.abz.library.reservation.entity;

import java.time.LocalDateTime;

import com.abz.library.collection.entity.Book;
import com.abz.library.collection.entity.BookCopy;
import com.abz.library.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "reservations")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class Reservation {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "book_id", nullable = false) 
    private Book book;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "book_copy_id") 
    private BookCopy bookCopy;

    @Column (name = "reserved_at", nullable = false, updatable = false)
    private LocalDateTime reservedAt;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.WAITING;

    @Column (name = "queue_position")
    private Integer queuePosition;

    @Column (name = "expires_at")
    private LocalDateTime expiresAt;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((reservedAt == null) ? 0 : reservedAt.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((queuePosition == null) ? 0 : queuePosition.hashCode());
        result = prime * result + ((expiresAt == null) ? 0 : expiresAt.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Reservation other = (Reservation) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (reservedAt == null) {
            if (other.reservedAt != null)
                return false;
        } else if (!reservedAt.equals(other.reservedAt))
            return false;
        if (status != other.status)
            return false;
        if (queuePosition == null) {
            if (other.queuePosition != null)
                return false;
        } else if (!queuePosition.equals(other.queuePosition))
            return false;
        if (expiresAt == null) {
            if (other.expiresAt != null)
                return false;
        } else if (!expiresAt.equals(other.expiresAt))
            return false;
        return true;
    }
}
