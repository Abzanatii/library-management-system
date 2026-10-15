package com.abz.library.reservation.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.abz.library.reservation.dto.ReservationResponse;
import com.abz.library.reservation.entity.Reservation;
import com.abz.library.user.entity.User;

@Mapper (componentModel = "Spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {

    @Mapping (source = "customer.id", target = "customerId")
    @Mapping (source = "book.id", target = "bookId")
    @Mapping (source = "book.title", target = "bookTitle")
    @Mapping (source = "customer", target = "customerName", qualifiedByName = "fullName")
    ReservationResponse toDetailedResponse(Reservation reservation);

    @Mapping (source = "book.id", target = "bookId")
    @Mapping (source = "book.title", target = "bookTitle")
    @Mapping (target = "customerId", ignore = true)
    @Mapping (target = "customerName", ignore = true)
    ReservationResponse toCustomerResponse(Reservation reservation);

    @Named ("fullName")
    default String fullName(User customer){
        return customer.getFirstName() + " " + customer.getLastName();
    }
}
