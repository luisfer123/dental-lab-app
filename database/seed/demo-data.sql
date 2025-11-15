USE dental_lab_app;

-- ===================================================
-- 0) Security base
-- ===================================================

INSERT INTO role (name)
VALUES 
  ('ROLE_ADMIN'),
  ('ROLE_WORKER'),
  ('ROLE_DENTIST'),
  ('ROLE_TECHNICIAN');

INSERT INTO user_account (username, email, phone, password_hash, enabled)
VALUES
  ('admin',       'admin@dentallab.com',         '555-1000',
     '$2a$10$WS16Gi/.fW5eWiLDCc9PE.hmR0WvuVW553At/P/0ziTERp3VBCb9m', TRUE),
  ('tech_julio',  'julio@dentallab.com',         '555-2000',
     '$2a$10$WS16Gi/.fW5eWiLDCc9PE.hmR0WvuVW553At/P/0ziTERp3VBCb9m', TRUE),
  ('dentist_mr',  'dr.martinez@smileclinic.com', '555-3000',
     '$2a$10$WS16Gi/.fW5eWiLDCc9PE.hmR0WvuVW553At/P/0ziTERp3VBCb9m', TRUE);

INSERT INTO user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM user_account u
JOIN role r
ON (
     (u.username='admin'       AND r.name='ROLE_ADMIN')
  OR (u.username='tech_julio'  AND r.name='ROLE_TECHNICIAN')
  OR (u.username='dentist_mr'  AND r.name='ROLE_DENTIST')
);

-- ===================================================
-- 1) Clients + Profiles
-- ===================================================

INSERT INTO client (first_name, last_name)
VALUES
  ('Marcos', 'Ríos'),
  ('Ana', 'Pérez');

INSERT INTO dentist_profile (client_id, clinic_name)
VALUES (1, 'Clínica Sonrisa Perfecta');

INSERT INTO technician_profile (client_id, lab_name, specialization)
VALUES (2, 'AnaTech Dental', 'External Lab Technician');

-- ===================================================
-- 2) Workers + Profiles
-- ===================================================

INSERT INTO worker (first_name, last_name, address, phone, email)
VALUES
  ('Julio', 'Ramírez', 'Calle del Sol 12, Puebla', '555-2000', 'julio@dentallab.com'),
  ('Sofía', 'López',   'Calle del Sol 15, Querétaro', '555-3000', 'sofia@dentallab.com');

UPDATE worker
SET user_id = (SELECT user_id FROM user_account WHERE username='tech_julio')
WHERE email = 'julio@dentallab.com';

INSERT INTO technician_profile(worker_id, lab_name, specialization)
SELECT worker_id, 'Main Lab', 'Ceramist'
FROM worker WHERE email='julio@dentallab.com';

INSERT INTO delivery_profile (worker_id, vehicle_info, license_number)
SELECT worker_id, 'Moto Honda 125cc', 'LIC12345MX'
FROM worker WHERE email='sofia@dentallab.com';

-- ===================================================
-- 3) Contact info
-- ===================================================

INSERT INTO client_email (client_id, email, type, is_primary)
VALUES 
  (1, 'dr.marcos.rios@example.com', 'WORK', TRUE),
  (2, 'laboratorio.anatech@example.com', 'WORK', TRUE);

INSERT INTO client_phone (client_id, phone, type, is_primary)
VALUES
  (1, '555-1001', 'MOBILE', TRUE),
  (2, '555-1003', 'MOBILE', TRUE);

INSERT INTO client_address (
  client_id, street, street_num, zip_code, neighborhood,
  city, state, type, name_label, is_primary
)
VALUES
  (1, 'Av. Reforma', '345', '06000', 'Centro', 'CDMX', 'CDMX',
      'CLINIC', 'Clínica Sonrisa Perfecta', TRUE),
  (2, 'Av. Hidalgo', '890', '64000', 'Centro', 'Monterrey', 'NL',
      'CLINIC', 'AnaTech Dental', TRUE);

-- ===================================================
-- 4) Materials + Inventory
-- ===================================================

INSERT INTO material (name, category, unit, price_per_unit)
VALUES
  ('Zirconia Disk', 'Ceramic', 'unit', 120.00),
  ('Lithium Disilicate Ingot', 'Glass Ceramic', 'unit', 90.00),
  ('Resin Cement', 'Cement', 'tube', 35.00);

INSERT INTO material_inventory(material_id, quantity_available, minimum_stock)
VALUES
  (1, 10, 3),
  (2, 15, 5),
  (3, 50, 10);

-- ===================================================
-- 5) Patient
-- ===================================================

INSERT INTO patient(identifier, date_of_birth, dentist_client_id)
VALUES ('PAT001', '1990-04-12', 1);

-- ===================================================
-- 6) Work Order + Work
-- ===================================================

INSERT INTO work_order (client_id, date_received, due_date, status, notes)
VALUES (1, '2025-10-01', '2025-10-10', 'IN_PROGRESS', 'Three-unit bridge upper left.');

-- Base work record
INSERT INTO work(order_id, client_id, type, work_family, shade, status, notes)
VALUES (1, 1, 'BRIDGE', 'FIXED_PROSTHESIS', 'A2', 'IN_PROGRESS',
        'CAD/CAM design with zirconia framework.');

-- ===================================================
-- 7) Bridge extension (with building_status)
-- ===================================================

INSERT INTO bridge_work (
  work_id,
  abutment_teeth,
  pontic_teeth,
  constitution,
  building_technique,
  building_status_id,
  core_material_id,
  veneering_material_id,
  connector_type,
  pontic_design,
  notes
)
VALUES (
  1,
  JSON_ARRAY('24','26'),
  JSON_ARRAY('25'),
  'MONOLITHIC',
  'DIGITAL',
  (SELECT status_id FROM building_status_ref WHERE code='SCAN'),
  1,
  NULL,
  'Standard',
  'Modified Ridge Lap',
  'Upper left 3-unit zirconia bridge'
);

-- ===================================================
-- 8) Step Templates + Steps
-- ===================================================

INSERT INTO work_step_template (work_type, step_code, step_label, step_order, is_digital)
VALUES
  ('BRIDGE', 'DESIGN_CAD', 'CAD Design', 1, TRUE),
  ('BRIDGE', 'MILLING',    'Milling',    2, TRUE),
  ('BRIDGE', 'SINTERING',  'Sintering',  3, TRUE);

INSERT INTO work_step (work_id, worker_id, template_id, date_started, notes)
VALUES
  (1, 1, 1, '2025-10-02 09:00:00', 'Completed CAD design.'),
  (1, 1, 2, '2025-10-03 11:00:00', 'Milling started.');

-- ===================================================
-- 9) Files
-- ===================================================

INSERT INTO work_file(work_id, file_type, file_path, description)
VALUES
  (1, 'STL', '/data/files/bridge_case_1/design.stl', 'CAD design file'),
  (1, 'IMG', '/data/files/bridge_case_1/photo.jpg', 'Reference photo');

-- ===================================================
-- 10) Pricing / Invoice / Payment
-- ===================================================

INSERT INTO work_price(work_id, price, currency, valid_from, client_id, notes)
VALUES (1, 300.00, 'MXN', '2025-09-01', 1, 'Base price for 3-unit bridge');

INSERT INTO invoice(order_id, client_id, issue_date, total_amount, status)
VALUES (1, 1, '2025-10-05', 300.00, 'ISSUED');

INSERT INTO invoice_item(invoice_id, work_id, description, amount, unit_price)
VALUES (1, 1, '3-unit zirconia bridge', 300.00, 300.00);

INSERT INTO payment(client_id, received_at, method, amount_total, currency, reference, notes)
VALUES (1, '2025-10-07 10:00:00', 'BANK_TRANSFER',
        300.00, 'MXN', 'TRX12345', 'Full payment received.');

INSERT INTO payment_allocation(payment_id, invoice_item_id, amount_applied)
VALUES (1, 1, 300.00);

-- ===================================================
-- 11) Material usage
-- ===================================================

INSERT INTO material_item(material_id, batch_number, status, quantity, date_received)
VALUES (1, 'ZIRKON-B2025', 'USED', 1, '2025-09-20');

INSERT INTO material_usage(work_id, material_item_id, quantity_used, unit, notes)
VALUES (1, 1, 1.00, 'unit', 'One zirconia disk used for the bridge.');

-- ===================================================
-- 12) Refresh token
-- ===================================================

INSERT INTO refresh_token(token, jti, user_id, expiry_date, revoked)
VALUES ('sample_refresh_token', 'sample-jti-123456', 1,
        DATE_ADD(NOW(), INTERVAL 30 DAY), FALSE);

-- ===================================================
-- DONE
-- ===================================================

SELECT 'Demo data inserted successfully (schema 2025.11).' AS status;
