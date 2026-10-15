package com.abz.library.borrowing.entity;

import java.util.EnumSet;
import java.util.Set;

public enum BorrowingStatus {

    ACTIVE,
    RETURNED,
    OVERDUE,
    LOST;

    public static final Set<BorrowingStatus> IS_BORROWED = EnumSet.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE);
}
