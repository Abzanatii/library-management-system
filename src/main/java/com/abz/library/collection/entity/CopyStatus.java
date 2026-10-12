package com.abz.library.collection.entity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum CopyStatus {

    AVAILABLE,
    BORROWED,
    RESERVED,
    DAMAGED,
    LOST,
    MAINTENANCE;

    private static final Map<CopyStatus, Set<CopyStatus>> ALLOWED = Map.of(
        AVAILABLE, EnumSet.of(BORROWED, RESERVED, DAMAGED, LOST, MAINTENANCE),
        BORROWED, EnumSet.of(AVAILABLE, DAMAGED, LOST),
        RESERVED, EnumSet.of(AVAILABLE, BORROWED),
        DAMAGED, EnumSet.of(MAINTENANCE),
        LOST, EnumSet.noneOf(CopyStatus.class),
        MAINTENANCE, EnumSet.of(AVAILABLE, DAMAGED)
    );

    public boolean changeTo(CopyStatus next){
        return ALLOWED.get(this).contains(next);
    }

    public Set<CopyStatus> allowed(){
        return Collections.unmodifiableSet(ALLOWED.get(this));
    }

    public boolean isServiceManaged(){
        return this == BORROWED || this == RESERVED;
    }

    private static final Map<CopyStatus, Set<CopyStatus>> STAFF_ALLOWED = Map.of(
        AVAILABLE, EnumSet.of(DAMAGED, LOST, MAINTENANCE),
        BORROWED, EnumSet.noneOf(CopyStatus.class),
        RESERVED, EnumSet.noneOf(CopyStatus.class),
        DAMAGED, EnumSet.of(MAINTENANCE),
        LOST, EnumSet.noneOf(CopyStatus.class),
        MAINTENANCE, EnumSet.of(AVAILABLE, DAMAGED)
    );

    public Set<CopyStatus> staffAllowed(){
        return Collections.unmodifiableSet(STAFF_ALLOWED.get(this));
    }

    public boolean staffChangeTo(CopyStatus next){
        return STAFF_ALLOWED.get(this).contains(next);
    }
}
