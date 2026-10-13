package com.abz.library.collection.specification;

import org.springframework.data.jpa.domain.Specification;

import com.abz.library.collection.dto.BookSearchCriteria;
import com.abz.library.collection.entity.Book;
import com.abz.library.collection.entity.BookCopy;
import com.abz.library.collection.entity.CopyStatus;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
public final class BookSpecs {

    public static Specification<Book> from(BookSearchCriteria criteria){
        Specification<Book> spec = (root, query, cb) -> cb.conjunction();
        
        if (notBlank(criteria.isbn())) {
            String normalize = criteria.isbn().replaceAll("[\\s-]", "").toUpperCase();
            spec =  spec.and((root, query, cb) -> cb.equal(
                    root.get("isbn"), 
                    normalize));
        }

        if (notBlank(criteria.title())) {
            spec =  spec.and((root, query, cb) -> cb.like(
                    cb.lower(root.<String>get("title")), 
                            contains(criteria.title()), 
                            '\\'));
        }

        if (notBlank(criteria.author())) {
            spec =  spec.and((root, query, cb) -> {
                    Subquery<Long> sub = query.subquery(Long.class);
                    Root<Book> book = sub.from(Book.class);
                    Join<Object, Object> authors = book.join("authors");
                    sub .select(book.<Long>get("id"))
                        .where( cb.equal(book.get("id"), root.get("id")),
                                cb.like(cb.lower(authors.<String>get("name")), 
                                        contains(criteria.author()), 
                                        '\\'));
                    return cb.exists(sub);
            });
        }

        if (notBlank(criteria.language())) {
            spec =  spec.and((root, query, cb) -> cb.equal(
                    cb.lower(root.<String>get("language")), 
                            criteria.language().trim().toLowerCase()));
        }

        if (notBlank(criteria.department())) {
            spec = spec.and((root, query, cb) -> cb.equal(
                cb.lower(root.get("department").<String>get("name")), 
                        criteria.department().trim().toLowerCase()));
        }

        if (notBlank(criteria.category())) {
            spec =  spec.and((root, query, cb) -> {
                    Subquery<Long> sub = query.subquery(Long.class);
                    Root<Book> book = sub.from(Book.class);
                    Join<Object, Object> categories = book.join("categories");
                    sub .select(book.<Long>get("id"))
                        .where( cb.equal(book.get("id"), root.get("id")),
                                cb.like(cb.lower(categories.<String>get("name")),
                                        criteria.category().trim().toLowerCase()));
                    return cb.exists(sub);
            });
        }

        if (criteria.available() != null) {
            spec =  spec.and((root, query, cb) -> {
                    Subquery<Long> sub = query.subquery(Long.class);
                    Root<BookCopy> copy = sub.from(BookCopy.class);
                    sub .select(copy.<Long>get("id"))
                        .where( cb.equal(
                                    copy.get("book").get("id"), 
                                    root.get("id")),
                                cb.equal(copy.get("status"), CopyStatus.AVAILABLE));
                    return criteria.available() ? cb.exists(sub) : cb.not(cb.exists(sub));
            });
        }
        
        return spec;
    }

    private static boolean notBlank(String value){
        return value != null && !value.isBlank();
    }

    private static String contains(String value){
        return "%" + value.trim().toLowerCase()
                    .replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_") + "%";
    }
}
