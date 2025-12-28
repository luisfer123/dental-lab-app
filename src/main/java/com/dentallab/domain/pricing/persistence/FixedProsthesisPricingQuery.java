package com.dentallab.domain.pricing.persistence;

import com.dentallab.domain.pricing.model.WorkPricingView;
import com.dentallab.persistence.entity.WorkEntity;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * <h2>FixedProsthesisPricingQuery</h2>
 *
 * <hr/>
 *
 * <p>
 * Repository-level query used to extract <strong>PRICING PARAMETERS</strong>
 * for works belonging to the <strong>FIXED_PROSTHESIS</strong> family.
 * </p>
 *
 * <h3>IMPORTANT: THIS QUERY DOES NOT RESOLVE PRICES</h3>
 *
 * <p>This repository does <strong>NOT</strong>:</p>
 * <ul>
 *   <li>calculate prices</li>
 *   <li>consult {@code work_type_price}</li>
 *   <li>apply overrides</li>
 *   <li>persist anything</li>
 * </ul>
 *
 * <p>Its sole responsibility is to:</p>
 * <ul>
 *   <li>take a {@code workId}</li>
 *   <li>inspect the correct extension table
 *       ({@code crown_work} or {@code bridge_work})</li>
 *   <li>return a normalized view of pricing-relevant attributes</li>
 * </ul>
 *
 * <p>
 * In other words:
 * </p>
 * <blockquote>
 *   Given a fixed prosthesis work, what attributes are needed to
 *   <em>look up</em> the correct pricing rule?
 * </blockquote>
 *
 * <h3>Why this exists</h3>
 *
 * <p>Works are polymorphic:</p>
 * <ul>
 *   <li>Crowns and bridges live in different extension tables</li>
 *   <li>They have different clinical structures</li>
 *   <li>They have different notions of prosthetic units</li>
 * </ul>
 *
 * <p>
 * Pricing rules, however, require a <strong>unified input</strong>.
 * </p>
 *
 * <p>
 * This query flattens:
 * </p>
 * <ul>
 *   <li>{@code crown_work}</li>
 *   <li>{@code bridge_work} (with teeth normalized in {@code bridge_tooth})</li>
 * </ul>
 *
 * <p>
 * into a single projection: {@link WorkPricingView}.
 * </p>
 *
 * <p>
 * This allows higher layers (pricing resolvers) to remain completely
 * unaware of work subtype details.
 * </p>
 *
 * <h3>Where this is used in the architecture</h3>
 *
 * <p>FixedProsthesisPricingQuery is invoked by:</p>
 * <ul>
 *   <li>{@code WorkPricingQueryDispatcher}</li>
 * </ul>
 *
 * <p>Which in turn is used by:</p>
 * <ul>
 *   <li>{@code WorkTypePriceResolver}</li>
 * </ul>
 *
 * <p>The data flow is:</p>
 *
 * <pre>
 *   workId
 *     ↓
 *   FixedProsthesisPricingQuery
 *     ↓
 *   WorkPricingView   (pricing identity)
 *     ↓
 *   work_type_price  (pricing rules)
 * </pre>
 *
 * <h3>Hard requirements / invariants</h3>
 *
 * <ul>
 *   <li>The given {@code workId} <strong>MUST</strong> belong to a
 *       {@code FIXED_PROSTHESIS} work</li>
 *   <li>Exactly <strong>ONE</strong> row must be returned for a valid
 *       {@code workId}</li>
 *   <li>Column aliases <strong>MUST</strong> match
 *       {@link WorkPricingView} getter names</li>
 * </ul>
 *
 * <p>
 * Violating any of these invariants will cause pricing resolution to fail.
 * </p>
 */

@Repository
public interface FixedProsthesisPricingQuery
        extends org.springframework.data.repository.Repository<WorkEntity, Long> {

	/**
	 * Native pricing query for fixed prosthesis works (crowns and bridges).
	 *
	 * <p>
	 * This query produces a flattened {@link WorkPricingView} used by the pricing engine.
	 * It intentionally abstracts away clinical structure and exposes only the data
	 * required for pricing resolution.
	 * </p>
	 *
	 * <h3>Crown pricing</h3>
	 * <ul>
	 *   <li>Each crown represents exactly one prosthetic unit.</li>
	 *   <li>{@code prostheticUnits = 1}</li>
	 * </ul>
	 *
	 * <h3>Bridge pricing</h3>
	 * <ul>
	 *   <li>Bridge teeth are normalized and stored in the {@code bridge_tooth} table.</li>
	 *   <li>The number of prosthetic units is calculated as:
	 *       <pre>COUNT(bridge_tooth.tooth_id)</pre>
	 *   </li>
	 *   <li>Each bridge tooth is priced using crown pricing rules.</li>
	 *   <li>For pricing purposes, the {@code workType} is forced to {@code CROWN}.</li>
	 * </ul>
	 *
	 * <p>
	 * Important architectural notes:
	 * </p>
	 * <ul>
	 *   <li>This query does <strong>not</strong> inspect pontic vs abutment roles.
	 *       Role-based adjustments, if any, are applied later by the pricing engine.</li>
	 *   <li>This query does <strong>not</strong> compute pricing logic.
	 *       It only supplies pricing inputs.</li>
	 *   <li>Clinical structure (bridge vs crown) is intentionally flattened here
	 *       to keep pricing deterministic and uniform.</li>
	 * </ul>
	 *
	 * <p>
	 * Any future pricing rules that depend on pontic/abutment roles must be implemented
	 * in the pricing resolution layer, not in this query.
	 * </p>
	 */
	@Query(value = """
		    SELECT
		        w.work_id             AS workId,
		        w.work_family         AS workFamily,
		        w.type                AS workType,
		        cw.constitution       AS constitution,
		        cw.building_technique AS buildingTechnique,
		        cw.core_material_id   AS coreMaterialId,
		        1                     AS prostheticUnits
		    FROM work w
		    JOIN crown_work cw ON cw.work_id = w.work_id
		    WHERE w.work_id = :workId

		    UNION ALL

		    SELECT
		        w.work_id             AS workId,
		        w.work_family         AS workFamily,
		        'CROWN'               AS workType, -- 👈 pricing identity
		        bw.constitution       AS constitution,
		        bw.building_technique AS buildingTechnique,
		        bw.core_material_id   AS coreMaterialId,
		        COUNT(bt.tooth_id)    AS prostheticUnits
		    FROM work w
		    JOIN bridge_work bw      ON bw.work_id = w.work_id
		    JOIN bridge_tooth bt     ON bt.bridge_work_id = bw.work_id
		    WHERE w.work_id = :workId
		    GROUP BY
		        w.work_id,
		        w.work_family,
		        bw.constitution,
		        bw.building_technique,
		        bw.core_material_id
		""", nativeQuery = true)
		WorkPricingView findFixedPricing(@Param("workId") Long workId);

}
