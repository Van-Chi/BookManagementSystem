package com.library.repository;

import com.library.entity.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    private BookSpecification() {}

    public static Specification<Book> withFilters(String keyword, String category, Boolean available) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("author")), pattern)
                ));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }

            if (Boolean.TRUE.equals(available)) {
                predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
