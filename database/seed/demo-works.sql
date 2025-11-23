SET @client := 1;
SET @order := 1;
SET @coreMat := 1;
SET @veneerMat := 2;

-- ==============================================================
-- 15 CROWNS
-- ==============================================================

INSERT INTO work (order_id, client_id, work_family, type, description, shade, status, notes, created_at, updated_at)
VALUES
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 1',  'A1', 'PENDING',      NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 2',  'A2', 'IN_PROGRESS', NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 3',  'A3', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 4',  'B1', 'DELIVERED',   NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 5',  'A1', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 6',  'A2', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 7',  'A3', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 8',  'B2', 'IN_PROGRESS', NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 9',  'A1', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 10', 'A2', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 11', 'A3', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 12', 'B1', 'DELIVERED',   NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 13', 'A1', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 14', 'A2', 'IN_PROGRESS', NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'CROWN', 'Corona prueba 15', 'A3', 'PENDING',     NULL, NOW(), NOW());


INSERT INTO crown_work (work_id, tooth_number, constitution, building_technique, core_material_id, veneering_material_id)
SELECT w.work_id, '11', 'MONOLITHIC', 'DIGITAL', @coreMat, NULL
FROM work w
WHERE w.type = 'CROWN'
ORDER BY w.work_id DESC
LIMIT 15;


-- ==============================================================
-- 15 BRIDGES
-- ==============================================================

INSERT INTO work (order_id, client_id, work_family, type, description, shade, status, notes, created_at, updated_at)
VALUES
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 1',  'A2', 'PENDING',      NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 2',  'A1', 'IN_PROGRESS', NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 3',  'A3', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 4',  'B1', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 5',  'B2', 'DELIVERED',   NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 6',  'A1', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 7',  'A2', 'IN_PROGRESS', NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 8',  'A3', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 9',  'B1', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 10', 'B2', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 11', 'A2', 'DELIVERED',   NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 12', 'A1', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 13', 'A3', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 14', 'B1', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'BRIDGE', 'Puente prueba 15', 'A1', 'IN_PROGRESS', NULL, NOW(), NOW());


INSERT INTO bridge_work (work_id, abutment_teeth, pontic_teeth, constitution, building_technique, core_material_id, veneering_material_id)
SELECT w.work_id,
       JSON_ARRAY('13', '15'),
       JSON_ARRAY('14'),
       'STRATIFIED',
       'DIGITAL',
       @coreMat,
       @veneerMat
FROM work w
WHERE w.type = 'BRIDGE'
ORDER BY w.work_id DESC
LIMIT 15;


-- ==============================================================
-- 15 INLAYS
-- ==============================================================

INSERT INTO work (order_id, client_id, work_family, type, description, shade, status, notes, created_at, updated_at)
VALUES
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 1',  'A2', 'PENDING',      NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 2',  'A1', 'IN_PROGRESS', NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 3',  'A3', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 4',  'B1', 'DELIVERED',   NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 5',  'B2', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 6',  'A1', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 7',  'A2', 'IN_PROGRESS', NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 8',  'A3', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 9',  'B1', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 10', 'B2', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 11', 'A1', 'DELIVERED',   NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 12', 'A2', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 13', 'A3', 'IN_PROGRESS', NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 14', 'B1', 'PENDING',     NULL, NOW(), NOW()),
  (@order, @client, 'FIXED_PROSTHESIS', 'INLAY', 'Inlay prueba 15', 'A1', 'PENDING',     NULL, NOW(), NOW());


INSERT INTO inlay_work (work_id, cavity_type, preparation_depth)
SELECT w.work_id, 'MO', 1.50
FROM work w
WHERE w.type = 'INLAY'
ORDER BY w.work_id DESC
LIMIT 15;
