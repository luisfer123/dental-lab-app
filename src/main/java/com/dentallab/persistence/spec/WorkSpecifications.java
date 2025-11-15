package com.dentallab.persistence.spec;

import com.dentallab.persistence.entity.WorkEntity;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.util.List;

/**
 * Dynamic filter specifications for WorkEntity.
 * Updated to support lookup-based family/type fields.
 */
public class WorkSpecifications {

    // -------------------------------------------------------------
    // Filter by TYPE CODE
    // -------------------------------------------------------------
    public static Specification<WorkEntity> hasType(String typeCode) {
        return (root, query, cb) ->
            (typeCode == null || typeCode.isBlank())
                ? null
                : cb.equal(root.get("type").get("code"), typeCode);
    }

    // -------------------------------------------------------------
    // Filter by STATUS (still a simple string column)
    // -------------------------------------------------------------
    public static Specification<WorkEntity> hasStatus(String status) {
        return (root, query, cb) ->
            (status == null || status.isBlank())
                ? null
                : cb.equal(root.get("status").get("code"), status);
    }

    // -------------------------------------------------------------
    // Filter by FAMILY CODE
    // -------------------------------------------------------------
    public static Specification<WorkEntity> hasWorkFamily(String familyCode) {
        return (root, query, cb) ->
            (familyCode == null || familyCode.isBlank())
                ? null
                : cb.equal(root.get("workFamily").get("code"), familyCode);
    }

    // -------------------------------------------------------------
    // Filter by CATEGORY NAMES
    // -------------------------------------------------------------
    public static Specification<WorkEntity> hasCategoryNames(List<String> categoryNames) {
        return (root, query, cb) -> {
            if (categoryNames == null || categoryNames.isEmpty()) return null;

            query.distinct(true);
            Join<Object, Object> catJoin = root.join("categories", JoinType.INNER);

            return catJoin.get("name").in(categoryNames);
        };
    }
}
