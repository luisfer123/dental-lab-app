USE dental_lab_app;

-- ===================================================
-- DEMO DATA FOR CLIENT PROFILES (schema 2025.11)
-- Restart-safe and coexistence-safe version
-- ===================================================

-- ===========================================
-- Helper: record start offset
-- ===========================================
SET @start_id := COALESCE((SELECT MAX(client_id) FROM client), 0);

-- ---------- DENTISTS (30) ----------
INSERT INTO client (first_name, second_name, last_name, second_last_name, is_active)
SELECT 
  CONCAT('Dentista', LPAD(n, 2, '0')) AS first_name,
  NULL AS second_name,
  CONCAT('Apellido', LPAD(n, 2, '0')) AS last_name,
  NULL AS second_last_name,
  TRUE
FROM (
  SELECT @row := @row + 1 AS n
  FROM
    (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,
    (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b,
    (SELECT @row:=0) r
  LIMIT 30
) x;

SET @dentist_start := @start_id + 1;
SET @dentist_end := (SELECT MAX(client_id) FROM client);

DROP TEMPORARY TABLE IF EXISTS tmp_dentists;
CREATE TEMPORARY TABLE tmp_dentists
SELECT client_id FROM client WHERE client_id BETWEEN @dentist_start AND @dentist_end;

INSERT INTO client_email (client_id, email, type, is_primary)
SELECT client_id, CONCAT('dentista', client_id, '@correo.com'), 'WORK', TRUE
FROM tmp_dentists
ON DUPLICATE KEY UPDATE email=email;

INSERT INTO client_phone (client_id, phone, type, is_primary)
SELECT client_id, CONCAT('555-100', LPAD(client_id, 3, '0')), 'MOBILE', TRUE
FROM tmp_dentists
ON DUPLICATE KEY UPDATE phone=phone;

INSERT INTO client_address (client_id, street, street_num, zip_code, neighborhood, city, state, type, name_label, is_primary)
SELECT client_id,
       CONCAT('Calle D', client_id),
       '1',
       '06000',
       'Centro',
       'CDMX',
       'CDMX',
       'CLINIC',
       CONCAT('Clínica Dental ', client_id),
       TRUE
FROM tmp_dentists
ON DUPLICATE KEY UPDATE city=city;

INSERT INTO dentist_profile (client_id, clinic_name)
SELECT client_id, CONCAT('Clínica Sonrisa ', client_id)
FROM tmp_dentists
ON DUPLICATE KEY UPDATE clinic_name=VALUES(clinic_name);

DROP TEMPORARY TABLE IF EXISTS tmp_dentists;



-- ---------- STUDENTS (30) ----------
SET @start_id := (SELECT MAX(client_id) FROM client);

INSERT INTO client (first_name, second_name, last_name, second_last_name, is_active)
SELECT 
  CONCAT('Estudiante', LPAD(n, 2, '0')) AS first_name,
  NULL AS second_name,
  CONCAT('ApellidoE', LPAD(n, 2, '0')) AS last_name,
  NULL AS second_last_name,
  TRUE
FROM (
  SELECT @row2 := @row2 + 1 AS n
  FROM
    (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,
    (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b,
    (SELECT @row2:=0) r
  LIMIT 30
) x;

SET @student_start := @start_id + 1;
SET @student_end := (SELECT MAX(client_id) FROM client);

DROP TEMPORARY TABLE IF EXISTS tmp_students;
CREATE TEMPORARY TABLE tmp_students
SELECT client_id FROM client WHERE client_id BETWEEN @student_start AND @student_end;

INSERT INTO client_email (client_id, email, type, is_primary)
SELECT client_id, CONCAT('estudiante', client_id, '@correo.com'), 'WORK', TRUE
FROM tmp_students
ON DUPLICATE KEY UPDATE email=email;

INSERT INTO client_phone (client_id, phone, type, is_primary)
SELECT client_id, CONCAT('555-200', LPAD(client_id, 3, '0')), 'MOBILE', TRUE
FROM tmp_students
ON DUPLICATE KEY UPDATE phone=phone;

INSERT INTO client_address (client_id, street, street_num, zip_code, neighborhood, city, state, type, name_label, is_primary)
SELECT client_id,
       CONCAT('Calle E', client_id),
       '1',
       '58000',
       'Centro',
       'Morelia',
       'Michoacán',
       'HOME',
       CONCAT('Casa Estudiante ', client_id),
       TRUE
FROM tmp_students
ON DUPLICATE KEY UPDATE city=city;

INSERT INTO student_profile (client_id, university_name, semester)
SELECT client_id,
       CONCAT('Universidad Dental ', client_id),
       (client_id % 10) + 1
FROM tmp_students
ON DUPLICATE KEY UPDATE university_name=VALUES(university_name);

DROP TEMPORARY TABLE IF EXISTS tmp_students;



-- ---------- TECHNICIANS (9) ----------
SET @start_id := (SELECT MAX(client_id) FROM client);

INSERT INTO client (first_name, second_name, last_name, second_last_name, is_active)
SELECT 
  CONCAT('Tecnico', LPAD(n, 2, '0')) AS first_name,
  NULL AS second_name,
  CONCAT('ApellidoT', LPAD(n, 2, '0')) AS last_name,
  NULL AS second_last_name,
  TRUE
FROM (
  SELECT @row3 := @row3 + 1 AS n
  FROM
    (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a,
    (SELECT @row3:=0) r
  LIMIT 9
) x;

SET @tech_start := @start_id + 1;
SET @tech_end := (SELECT MAX(client_id) FROM client);

DROP TEMPORARY TABLE IF EXISTS tmp_techs;
CREATE TEMPORARY TABLE tmp_techs
SELECT client_id FROM client WHERE client_id BETWEEN @tech_start AND @tech_end;

INSERT INTO client_email (client_id, email, type, is_primary)
SELECT client_id, CONCAT('tecnico', client_id, '@correo.com'), 'WORK', TRUE
FROM tmp_techs
ON DUPLICATE KEY UPDATE email=email;

INSERT INTO client_phone (client_id, phone, type, is_primary)
SELECT client_id, CONCAT('555-300', LPAD(client_id, 3, '0')), 'MOBILE', TRUE
FROM tmp_techs
ON DUPLICATE KEY UPDATE phone=phone;

INSERT INTO client_address (client_id, street, street_num, zip_code, neighborhood, city, state, type, name_label, is_primary)
SELECT client_id,
       CONCAT('Calle T', client_id),
       '1',
       '64000',
       'Centro',
       'Monterrey',
       'NL',
       'CLINIC',
       CONCAT('Laboratorio ', client_id),
       TRUE
FROM tmp_techs
ON DUPLICATE KEY UPDATE city=city;

INSERT INTO technician_profile (client_id, lab_name, specialization)
SELECT client_id,
       CONCAT('Laboratorio ', client_id),
       CASE
         WHEN client_id % 3 = 0 THEN 'Cerámica'
         WHEN client_id % 3 = 1 THEN 'Zirconia'
         ELSE 'Resina'
       END
FROM tmp_techs
ON DUPLICATE KEY UPDATE lab_name=VALUES(lab_name);

DROP TEMPORARY TABLE IF EXISTS tmp_techs;



-- ===================================================
-- Verify results
-- ===================================================
SELECT 
  (SELECT COUNT(*) FROM dentist_profile) AS dentists,
  (SELECT COUNT(*) FROM student_profile) AS students,
  (SELECT COUNT(*) FROM technician_profile WHERE client_id IS NOT NULL) AS technicians;

SELECT client_id, display_name, primary_email, primary_phone
FROM client
ORDER BY client_id
LIMIT 20;
