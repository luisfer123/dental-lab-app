package com.dentallab.domain.pricing.persistence;

import com.dentallab.domain.pricing.query.WorkFamilyLookupQuery;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.entity.WorkFamilyRefEntity;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaWorkFamilyLookupQuery
        extends org.springframework.data.repository.Repository<WorkEntity, Long>,
                WorkFamilyLookupQuery {

	@Query("""
			  select wt.family
			  from WorkTypeRefEntity wt
			  join WorkEntity w on w.type = wt
			  where w.id = :workId
			""")
    @Override
    WorkFamilyRefEntity findFamilyByWorkId(@Param("workId") Long workId);
}
