package com.abz.library.borrowing.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table (name = "borrowings")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class Borrowing {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "book_copy_id", nullable = false)
    private BookCopy bookCopy;

    @Column (name = "borrowed_at", nullable = false)
    private LocalDateTime borrowedAt;

    @Column (name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column (name = "returned_at")
    private LocalDateTime returnedAt;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    @Builder.Default
    private BorrowingStatus status = BorrowingStatus.ACTIVE;

    @Column (name = "fine_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((borrowedAt == null) ? 0 : borrowedAt.hashCode());
        result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
        result = prime * result + ((returnedAt == null) ? 0 : returnedAt.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((fineAmount == null) ? 0 : fineAmount.hashCode());
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
        Borrowing other = (Borrowing) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (borrowedAt == null) {
            if (other.borrowedAt != null)
                return false;
        } else if (!borrowedAt.equals(other.borrowedAt))
            return false;
        if (dueDate == null) {
            if (other.dueDate != null)
                return false;
        } else if (!dueDate.equals(other.dueDate))
            return false;
        if (returnedAt == null) {
            if (other.returnedAt != null)
                return false;
        } else if (!returnedAt.equals(other.returnedAt))
            return false;
        if (status != other.status)
            return false;
        if (fineAmount == null) {
            if (other.fineAmount != null)
                return false;
        } else if (!fineAmount.equals(other.fineAmount))
            return false;
        return true;
    }
}
