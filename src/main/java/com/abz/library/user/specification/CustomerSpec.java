package com.abz.library.user.specification;

import org.springframework.data.jpa.domain.Specification;

import com.abz.library.user.dto.CustomerSearchCriteria;
import com.abz.library.user.entity.Role;
import com.abz.library.user.entity.User;

public final class CustomerSpec {

    public static Specification<User> from(CustomerSearchCriteria criteria){
        Specification<User> spec = (root, query, cb) -> cb.equal(root.get("role"), Role.CUSTOMER);

        if (criteria.status() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), criteria.status()));
        }

        if (criteria.search() != null && !criteria.search().isBlank()) {
            String sub = contains(criteria.search());
            spec = spec.and((root, query, cb) -> cb.or(
                            cb.like(cb.lower(root.<String>get("firstName")), sub, '\\'),
                            cb.like(cb.lower(root.<String>get("lastName")), sub, '\\'),
                            cb.like(cb.lower(root.<String>get("email")), sub, '\\')));
        }

        return spec;
    }

    private static String contains(String value){
        return "%" + value.trim().toLowerCase()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_") + "%";
    }
}
