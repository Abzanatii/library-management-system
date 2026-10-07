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

    public boolean canTransitionTo(CopyStatus next){
        return ALLOWED.get(this).contains(next);
    }

    public Set<CopyStatus> allowedTransition(){
        return Collections.unmodifiableSet(ALLOWED.get(this));
    }
}
