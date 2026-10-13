package com.abz.library.user.dto;

import com.abz.library.user.entity.UserStatus;

public record CustomerSearchCriteria(
    UserStatus status,
    String search
) {
}
