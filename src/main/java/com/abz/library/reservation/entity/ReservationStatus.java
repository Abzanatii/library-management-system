package com.abz.library.reservation.entity;

import java.util.EnumSet;
import java.util.Set;

public enum ReservationStatus {

    WAITING,
    READY,
    COMPLETED,
    CANCELLED,
    EXPIRED;

    public static final Set<ReservationStatus> IS_RESERVED = EnumSet.of(READY, WAITING);
}
