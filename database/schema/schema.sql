-- ===================================================
-- Dental Lab Application Database (MySQL 8+)
-- Full schema (Client / Worker architecture)
-- ===================================================

DROP DATABASE IF EXISTS dental_lab_app;
CREATE DATABASE dental_lab_app
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE dental_lab_app;

-- ===================================================
-- 0) Security base (users, roles)
-- ===================================================

CREATE TABLE role (
  role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name    VARCHAR(50) UNIQUE NOT NULL
) ENGINE=InnoDB;

CREATE TABLE user_account (
  user_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(100) UNIQUE NOT NULL,
  email         VARCHAR(255) UNIQUE NOT NULL,
  phone         VARCHAR(50) DEFAULT NULL,
  password_hash VARCHAR(255) NOT NULL,
  enabled       BOOLEAN DEFAULT TRUE,
  locked        BOOLEAN DEFAULT FALSE,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;
CREATE INDEX idx_user_email ON user_account(email);
CREATE INDEX idx_user_username ON user_account(username);


CREATE TABLE user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) 
    REFERENCES user_account(user_id)
    ON DELETE CASCADE,
  FOREIGN KEY (role_id)
    REFERENCES role(role_id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- ===================================================
-- 1) Core Personas: CLIENT / WORKER
-- ===================================================

CREATE TABLE client (
  client_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  display_name    VARCHAR(255) NOT NULL,
  first_name       VARCHAR(255) NOT NULL,
  second_name      VARCHAR(255),
  last_name        VARCHAR(255) NOT NULL,
  second_last_name VARCHAR(255),
  primary_email   VARCHAR(255) DEFAULT NULL COMMENT 'Mirrored from client_email.is_primary=TRUE',
  primary_phone   VARCHAR(50)  DEFAULT NULL COMMENT 'Mirrored from client_phone.is_primary=TRUE',
  primary_address VARCHAR(255) DEFAULT NULL COMMENT 'Mirrored from client_address.is_primary=TRUE',
  is_active       BOOLEAN DEFAULT TRUE,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Client entity: contact tables are the source of truth; always update via client_email/phone/address tables.';

CREATE TABLE worker (
  worker_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id   BIGINT,
  display_name  VARCHAR(255) NOT NULL,
  first_name       VARCHAR(255) NOT NULL,
  second_name      VARCHAR(255),
  last_name        VARCHAR(255) NOT NULL,
  second_last_name VARCHAR(255),
  address    VARCHAR(255),
  phone      VARCHAR(50),
  email      VARCHAR(255),
  is_active     BOOLEAN DEFAULT TRUE,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id)
    REFERENCES user_account(user_id)
    ON DELETE CASCADE,
  UNIQUE (user_id),
  UNIQUE (email)
) ENGINE=InnoDB;

-- ===================================================
-- 2) CLIENT Profiles (Dentist, Student, Technician)
-- ===================================================

CREATE TABLE dentist_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_id BIGINT NOT NULL UNIQUE,
  clinic_name      VARCHAR(255),
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE student_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_id BIGINT NOT NULL UNIQUE,
  university_name VARCHAR(255),
  semester        INT,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ===================================================
-- 3) WORKER Profiles (Technician, Delivery Person)
-- ===================================================

CREATE TABLE technician_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  worker_id BIGINT NULL,
  client_id BIGINT NULL,
  lab_name   VARCHAR(255),
  specialization VARCHAR(100),
  is_active  BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (worker_id) REFERENCES worker(worker_id) ON DELETE CASCADE,
  FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE,
  CONSTRAINT chk_technician_has_one_parent CHECK (
    (worker_id IS NOT NULL AND client_id IS NULL)
    OR (worker_id IS NULL AND client_id IS NOT NULL)
  )
) ENGINE=InnoDB;

CREATE TABLE delivery_profile (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  worker_id BIGINT NOT NULL UNIQUE,
  vehicle_info VARCHAR(255),
  license_number VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (worker_id) REFERENCES worker(worker_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ===================================================
-- 4) Patients (not a persona)
-- ===================================================

CREATE TABLE patient (
  patient_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  identifier      VARCHAR(100) NOT NULL,
  date_of_birth   DATE,
  dentist_client_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (dentist_client_id) REFERENCES client(client_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ===================================================
-- 5.0) Lookup Table relevant for Order - Work workflow
-- ===================================================

CREATE TABLE work_family_ref (
  code VARCHAR(50) PRIMARY KEY,
  label VARCHAR(100) NOT NULL
);

CREATE TABLE work_type_ref (
  code VARCHAR(50) PRIMARY KEY,
  label VARCHAR(100) NOT NULL,
  family_code VARCHAR(50) NOT NULL,
  FOREIGN KEY (family_code) REFERENCES work_family_ref(code)
);

CREATE TABLE work_status_ref (
  code VARCHAR(50) PRIMARY KEY,
  label VARCHAR(100) NOT NULL,
  sequence_order INT NOT NULL,
  
  CONSTRAINT uq_status_seq UNIQUE (sequence_order)
);

-- LOOKUP FOR BUILDING STATUS

-- 1) UNIVERSAL CATALOG OF TECHNICAL (BUILD) STATUSES
CREATE TABLE IF NOT EXISTS building_status_ref (
  status_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  code        VARCHAR(50)  NOT NULL UNIQUE,     -- e.g. 'DESIGN_READY', 'MILLING'
  label       VARCHAR(100) NOT NULL,            -- e.g. 'Design Ready'
  description VARCHAR(255) NULL
) ENGINE=InnoDB;
CREATE INDEX idx_building_status_code ON building_status_ref(code);

-- 2) ORDERED RULES FOR EACH WORKFLOW PROFILE
CREATE TABLE IF NOT EXISTS building_status_rule (
  rule_id            BIGINT PRIMARY KEY AUTO_INCREMENT,

  work_family        VARCHAR(50)  NOT NULL,     -- e.g. 'FIXED_PROSTHESIS'
  type               VARCHAR(50)  NOT NULL,     -- e.g. 'CROWN', 'BRIDGE'
  constitution       VARCHAR(50)  NULL,         -- e.g. 'MONOLITHIC' (nullable = applies to all)
  building_technique VARCHAR(50)  NULL,         -- e.g. 'DIGITAL' / 'MANUAL' (nullable = applies to all)

  status_id          BIGINT       NOT NULL,     -- FK to catalog
  sequence_order     INT          NOT NULL,     -- 1..N linear chain
  is_terminal        BOOLEAN      NOT NULL DEFAULT FALSE,

  CONSTRAINT fk_rule_status
    FOREIGN KEY (status_id) REFERENCES building_status_ref(status_id)
      ON DELETE RESTRICT ON UPDATE CASCADE,

  -- Prevent duplicate steps in the same position for the same profile
  CONSTRAINT uq_rule_profile_order
    UNIQUE (work_family, type, constitution, building_technique, sequence_order),

  -- Prevent the same status appearing twice in the same profile
  CONSTRAINT uq_rule_profile_status
    UNIQUE (work_family, type, constitution, building_technique, status_id)
) ENGINE=InnoDB;
CREATE INDEX idx_rule_profile
  ON building_status_rule(work_family, type, constitution, building_technique, sequence_order);

-- ===================================================
-- 5) Order - Work workflow
-- ===================================================

CREATE TABLE material (
  material_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  name            VARCHAR(100) NOT NULL,
  category        VARCHAR(50),
  unit            VARCHAR(20),
  price_per_unit  DECIMAL(12,2) NOT NULL,
  status          VARCHAR(50) DEFAULT 'ACTIVE',
  notes           TEXT
) ENGINE=InnoDB;

CREATE TABLE work_order (
  order_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_id     BIGINT NOT NULL,
  date_received TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  due_date      TIMESTAMP, -- Date it should be delivered to the client
  delivered_at TIMESTAMP NULL, -- Date the order actualy was delivered to the client
  status        VARCHAR(50) DEFAULT 'RECEIVED',
  notes         TEXT,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Date the order is created in the system
  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update in the system
  FOREIGN KEY (status) REFERENCES work_status_ref(code) ON DELETE RESTRICT,
  FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='Groups one or more works sent by a client in a single submission or pickup.';
CREATE INDEX idx_order_client ON work_order(client_id);
CREATE INDEX idx_order_due_date ON work_order(due_date);

CREATE TABLE work (
  work_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id    BIGINT NOT NULL,
  client_id   BIGINT NOT NULL,
  
  -- Represents broad prosthetic category (e.g., FIXED_PROSTHESIS, REMOVABLE_PROSTHESIS, FULL_DENTURE)
  work_family VARCHAR(50) NOT NULL,

  -- Represents general type or shape of work within that family (e.g., CROWN, BRIDGE, INLAY)
  type        VARCHAR(50) NOT NULL,

  -- Human-readable or cataloged specific description (e.g., "Stratified disilicate crown")
  description VARCHAR(255),
  
  shade       VARCHAR(50),
  
  -- RECEIVED | ASIGNED | IN_PROGRES | FINISED | DELIVERING | DELIVERED 
  status      VARCHAR(50),
  notes       TEXT,
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  -- fields for a human-readalbe code (Id) that can be used in fisical works in the lab
  profile_prefix CHAR(1) NULL, -- Informative to easily know whther the code belogs to Dentist, Student, etc
  client_profile_id BIGINT NULL, -- Here used only for internal_code, no FK (no relationship with profile table)
  internal_seq INT NULL, -- Each year starts from zero (0). Increases by 1 for each new work of the corresponding client (counts client's work in a year)
  internal_year INT NULL, -- Year the work is registered. Used in interna_code. internal_seq is restarted when internal_year increases.
  internal_code VARCHAR(50) NULL, -- Human-readable code for each work. Form by the formula {PREFIX}{clientProfileId}-{seq}-{year}
  
  FOREIGN KEY (order_id) REFERENCES work_order(order_id) ON DELETE CASCADE,
  FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE,
  FOREIGN KEY (work_family) REFERENCES work_family_ref(code) ON DELETE RESTRICT,
  FOREIGN KEY (type) REFERENCES work_type_ref(code) ON DELETE RESTRICT,
  FOREIGN KEY (status) REFERENCES work_status_ref(code) ON DELETE RESTRICT
) ENGINE=InnoDB
COMMENT='Base work table: family = prosthetic domain, type = general category, description = detailed variant. Has 1 to 1 related extension tables for type (Crown, bridge)';
CREATE INDEX idx_work_order ON work(order_id);
CREATE INDEX idx_work_client ON work(client_id);
CREATE INDEX idx_work_family ON work(work_family);
CREATE INDEX idx_work_type ON work(type);
CREATE INDEX idx_work_profile_year_seq
  ON work(client_profile_id, internal_year, internal_seq);

CREATE TABLE work_step_template (
  template_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  work_type   VARCHAR(50) NOT NULL,        -- e.g. 'CROWN', 'BRIDGE', 'INLAY'
  step_code   VARCHAR(50) NOT NULL,        -- e.g. 'SCAN', 'DESIGN_CAD', 'MILLING'
  step_label  VARCHAR(100) NOT NULL,       -- human-readable, e.g. 'Design (CAD)'
  step_order  INT NOT NULL,
  is_digital  BOOLEAN DEFAULT TRUE,
  UNIQUE (work_type, step_code)
) ENGINE=InnoDB;
CREATE INDEX idx_work_type_step_order ON work_step_template(work_type, step_order);

-- 3) HISTORY OF TECHNICAL STATUS CHANGES
-- Usage: every time you update crown_work.building_status_id (or bridge), insert a row here with work_id, extension_type and from → to
CREATE TABLE IF NOT EXISTS building_status_history (
  history_id         BIGINT PRIMARY KEY AUTO_INCREMENT,

  work_id            BIGINT      NOT NULL,      -- FK to work (not to extension)
  extension_type     VARCHAR(30) NOT NULL,      -- 'CROWN' | 'BRIDGE' | etc.
  from_status_id     BIGINT      NULL,
  to_status_id       BIGINT      NOT NULL,

  changed_by_worker_id BIGINT    NULL,          -- optional FK to worker/ user
  changed_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  note               VARCHAR(255) NULL,

  FOREIGN KEY (work_id)        REFERENCES work(work_id) ON DELETE CASCADE,
  FOREIGN KEY (from_status_id) REFERENCES building_status_ref(status_id) ON DELETE SET NULL,
  FOREIGN KEY (changed_by_worker_id) REFERENCES worker(worker_id) ON DELETE SET NULL,
  FOREIGN KEY (to_status_id)   REFERENCES building_status_ref(status_id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE INDEX idx_bld_hist_work ON building_status_history(work_id, changed_at);

CREATE TABLE work_step (
  step_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
  work_id   BIGINT NOT NULL,
  worker_id      BIGINT,
  template_id   BIGINT NOT NULL,
  date_started   TIMESTAMP,
  date_completed TIMESTAMP,
  notes          TEXT,
  FOREIGN KEY (template_id) REFERENCES work_step_template(template_id) 	ON DELETE CASCADE,
  FOREIGN KEY (work_id) REFERENCES work(work_id) ON DELETE CASCADE,
  FOREIGN KEY (worker_id) REFERENCES worker(worker_id)
) ENGINE=InnoDB;

CREATE TABLE work_file (
  file_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  work_id BIGINT NOT NULL,
  file_type    VARCHAR(50),
  file_path    VARCHAR(500) NOT NULL,
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  description  TEXT,
  FOREIGN KEY (work_id) REFERENCES work(work_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_work_file_work_id ON work_file(work_id);

CREATE TABLE work_category (
  category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL UNIQUE,
  description TEXT
) ENGINE=InnoDB;

-- Join table Work - Work_category
CREATE TABLE work_work_category (
  work_id     BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  PRIMARY KEY (work_id, category_id),
  FOREIGN KEY (work_id) REFERENCES work(work_id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES work_category(category_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ===================================================
-- Work-type extension tables
-- ===================================================

CREATE TABLE crown_work (
  work_id BIGINT PRIMARY KEY,
  tooth_number VARCHAR(10),
  
  -- Composition & manufacturing
  constitution ENUM('MONOLITHIC','STRATIFIED','METAL','TEMPORARY') NOT NULL
    COMMENT 'Structural composition of the restoration',
  building_technique ENUM('DIGITAL','MANUAL','HYBRID') DEFAULT 'DIGITAL'
    COMMENT 'Fabrication workflow (CAD/CAM, manual, or hybrid)',
  
  building_status_id BIGINT NULL,
  core_material_id BIGINT NOT NULL,   -- Always required. If monolithic reprsents the matirial, if stratified represent the core's material
  veneering_material_id BIGINT NULL,  -- Required if stratified/metal
  notes TEXT,
  
  FOREIGN KEY (work_id) REFERENCES work(work_id) ON DELETE CASCADE,
  FOREIGN KEY (core_material_id) REFERENCES material(material_id),
  FOREIGN KEY (veneering_material_id) REFERENCES material(material_id),
  FOREIGN KEY (building_status_id) REFERENCES building_status_ref(status_id)
      ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT chk_crown_material_logic CHECK (
    (constitution = 'MONOLITHIC' AND veneering_material_id IS NULL)
    OR (constitution IN ('STRATIFIED','METAL') AND veneering_material_id IS NOT NULL)
    OR (constitution = 'TEMPORARY')
  )
) ENGINE=InnoDB
COMMENT='Extension table for crowns (fixed prosthesis). Includes structure and manufacturing attributes.';
CREATE INDEX idx_crown_build_status  ON crown_work(building_status_id);

CREATE TABLE bridge_work (
  work_id BIGINT PRIMARY KEY,
  
  -- Composition & manufacturing
  constitution ENUM('MONOLITHIC','STRATIFIED','METAL','TEMPORARY') NOT NULL
    COMMENT 'Structural composition of the restoration',
  building_technique ENUM('DIGITAL','MANUAL','HYBRID') DEFAULT 'DIGITAL'
    COMMENT 'Fabrication workflow (CAD/CAM, manual, or hybrid)',
  
  building_status_id BIGINT NULL,
  core_material_id BIGINT NOT NULL,     -- Always required. If monolithic reprsents the matirial, if stratified represent the core's material
  veneering_material_id BIGINT NULL,    -- Required if stratified/metal
  connector_type VARCHAR(50),
  pontic_design VARCHAR(50),
  notes TEXT,
  
  FOREIGN KEY (work_id) REFERENCES work(work_id) ON DELETE CASCADE,
  FOREIGN KEY (core_material_id) REFERENCES material(material_id),
  FOREIGN KEY (veneering_material_id) REFERENCES material(material_id),
  FOREIGN KEY (building_status_id) REFERENCES building_status_ref(status_id)
      ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT chk_bridge_material_logic CHECK (
    (constitution = 'MONOLITHIC' AND veneering_material_id IS NULL)
    OR (constitution IN ('STRATIFIED','METAL') AND veneering_material_id IS NOT NULL)
    OR (constitution = 'TEMPORARY')
  )
) ENGINE=InnoDB;
CREATE INDEX idx_bridge_build_status ON bridge_work(building_status_id);

-- Reprensents a single unit in a crown_work.
CREATE TABLE tooth_ref (
  tooth_id BIGINT PRIMARY KEY,
  tooth_number VARCHAR(5) NOT NULL UNIQUE
    COMMENT 'Tooth identifier (FDI or Universal notation, e.g. 11, 21, 36)'
) ENGINE=InnoDB;

-- Join table between crown_work and tooth_ref. Contain atributes that describe the
-- relationship between those two tables.
CREATE TABLE bridge_tooth (
  bridge_work_id BIGINT NOT NULL,
  tooth_id BIGINT NOT NULL,

  role ENUM('ABUTMENT','PONTIC') NOT NULL
    COMMENT 'ABUTMENT = supporting crowned tooth, PONTIC = suspended unit',

  position_index INT NULL
    COMMENT 'Relative order of the tooth within the bridge span',

  PRIMARY KEY (bridge_work_id, tooth_id),

  FOREIGN KEY (bridge_work_id)
    REFERENCES bridge_work(work_id)
    ON DELETE CASCADE,

  FOREIGN KEY (tooth_id)
    REFERENCES tooth_ref(tooth_id)
    ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE INDEX idx_bridge_tooth_role
  ON bridge_tooth(role);
CREATE INDEX idx_bridge_tooth_bridge
  ON bridge_tooth(bridge_work_id);

CREATE TABLE inlay_work (
  work_id BIGINT PRIMARY KEY,
  cavity_type VARCHAR(50),
  preparation_depth DECIMAL(5,2),
  FOREIGN KEY (work_id) REFERENCES work(work_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ===================================================
-- 6) Materials: inventory, items, usage
-- ===================================================

CREATE TABLE material_inventory (
  inventory_id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  material_id        BIGINT NOT NULL,
  quantity_available DECIMAL(12,2) CHECK (quantity_available >= 0) NOT NULL,
  minimum_stock      DECIMAL(12,2),
  last_updated       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (material_id) REFERENCES material(material_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE material_item (
  material_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  material_id      BIGINT NOT NULL,
  batch_number     VARCHAR(50),
  barcode          VARCHAR(100) UNIQUE,
  status           VARCHAR(50) DEFAULT 'IN_STORE',
  quantity         DECIMAL(12,2) NOT NULL,
  unit             VARCHAR(20) DEFAULT 'g',
  date_received    DATE,
  date_used        DATE,
  expiry_date      DATE,
  supplier_name    VARCHAR(100),
  FOREIGN KEY (material_id) REFERENCES material(material_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE material_usage (
  usage_id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  work_id          BIGINT NOT NULL,
  material_item_id BIGINT NOT NULL,
  quantity_used    DECIMAL(12,2) NULL,
  unit             VARCHAR(20),
  notes            VARCHAR(255),
  step_id          BIGINT NULL,
  FOREIGN KEY (work_id) REFERENCES work(work_id) ON DELETE CASCADE,
  FOREIGN KEY (material_item_id) REFERENCES material_item(material_item_id) ON DELETE CASCADE,
  FOREIGN KEY (step_id) REFERENCES work_step(step_id),
  CHECK (quantity_used > 0)
) ENGINE=InnoDB;

-- ===================================================
-- 7) Pricing
-- ===================================================

-- Contiene un precio base para un trabajo basado en algunos de sus parametros (work_family, work_type, price_group, constitution, etc)
-- al crear un trabajo nuevo se muestra este precio base pero el usuario puede decidir modificarlo, la fuente de verdad de precio para
-- un trabajo ya creado debe ser work_price.price
-- Si se quiere modificar el precio de un trabajo especifico se deberia hacer agregado una o mas entradas a work_item_price_override.ajustment 
-- asociadas al trabajo en cuestion. 
CREATE TABLE work_type_price (
  price_id BIGINT PRIMARY KEY AUTO_INCREMENT,

  -- Identidad del tipo de trabajo
  work_family VARCHAR(50) NOT NULL,
  work_type   VARCHAR(50) NOT NULL,

  price_group VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',  -- Permite crear precios especiales como precio para combenio con una universidad o por urgencia de trabajo.
  
  -- -------------------------------------------------------------------------
  -- Atributos tarifarios opcionales (solo se llenan si influyen en el precio)
  -- Si un campo es NULL → el precio aplica a cualquier valor ("comodín")
  -- -------------------------------------------------------------------------

  constitution       VARCHAR(50) NULL,        -- MONOLITHIC / STRATIFIED / METAL / TEMPORARY...
  building_technique VARCHAR(50) NULL,        -- DIGITAL / MANUAL / HYBRID
  denture_variant    VARCHAR(50) NULL,        -- STANDARD / PREMIUM / IMMEDIATE...
  resin_type         VARCHAR(50) NULL,        -- para placas (inyectada, termo-curado, etc.)
  teeth_type         VARCHAR(50) NULL,        -- dientes premium/estándar, etc.

  -- -------------------------------------------------------------------------
  -- Precios: uno de estos dos debe estar presente
  -- -------------------------------------------------------------------------

  base_price     DECIMAL(12,2) NULL,          -- coronas, inlays, trabajos unitarios
  price_per_unit DECIMAL(12,2) NULL,          -- puentes (precio por unidad protésica)

  CHECK (base_price IS NOT NULL OR price_per_unit IS NOT NULL),

  currency CHAR(3) DEFAULT 'MXN',

  -- -------------------------------------------------------------------------
  -- Vigencia del precio (solo fecha de inicio)
  -- El final se deduce del siguiente registro con mismos atributos
  -- -------------------------------------------------------------------------

  valid_from DATE NOT NULL,

  notes VARCHAR(255),

  -- -------------------------------------------------------------------------
  -- Evitar duplicados exactos:
  -- combinación de family + type + atributos + fecha de inicio
  -- -------------------------------------------------------------------------

  CONSTRAINT uq_work_type_price UNIQUE (
      work_family,
      work_type,
      price_group,
      constitution,
      building_technique,
      denture_variant,
      resin_type,
      teeth_type,
      valid_from
  ),

  FOREIGN KEY (work_family)
      REFERENCES work_family_ref(code)
      ON DELETE RESTRICT,

  FOREIGN KEY (work_type)
      REFERENCES work_type_ref(code)
      ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE INDEX idx_work_type_price_lookup
	ON work_type_price (
	  work_family,
	  work_type,
	  constitution,
	  building_technique,
	  denture_variant,
	  resin_type,
	  teeth_type,
	  valid_from
	);

-- Es le precio base de un trabajo ya creado. Es la fuente de verdad para el precio de un trabajo ya creado.
-- recordar que un renglon en work representa un trabajo fisico creado en el laboratorio y no una categoria abstracta. 
-- en principio se decide usando la tabla work_type_price, si se quiere modificar se agregan entradas en
-- work_item_price_override.ajustment. Al calcular el precio final de un trabajo siempre se usa la formula
-- work_price.price + Sum(work_item_price_override.ajustmets). Aun asi se considera la fuente de verdad porque
-- work_type_price.base_price puede cambiar con el tiempo.
CREATE TABLE work_price (
    price_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    work_id BIGINT NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    price_group VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',
    currency CHAR(3) DEFAULT 'MXN',
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    UNIQUE(work_id),
    CHECK (price >= 0),
    FOREIGN KEY (work_id) REFERENCES work(work_id),
    FOREIGN KEY (created_by) REFERENCES user_account(user_id)
);

-- Permite modificar precio de trabajos ya creados, Se peude usar para hacer descuentos a clientes especificos
-- o por promociones, etc. 
CREATE TABLE work_item_price_override (
  override_id  BIGINT PRIMARY KEY AUTO_INCREMENT,

  work_price_id BIGINT NOT NULL,

  -- Monto del ajuste. Puede ser positivo (recargo) o negativo (descuento).
  adjustment DECIMAL(12,2) NOT NULL,

  currency CHAR(3) DEFAULT 'MXN',

  -- Para registrar motivo del ajuste
  reason VARCHAR(255),

  -- Momento en que se aplicó el override
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  created_by BIGINT NULL,
  
  CHECK (adjustment <> 0),
  CHECK (adjustment IS NOT NULL),

  FOREIGN KEY (work_price_id) REFERENCES work_price(price_id),
  FOREIGN KEY (created_by) REFERENCES user_account(user_id)
) ENGINE=InnoDB;
CREATE INDEX idx_override_work ON work_item_price_override(work_price_id);

-- ===================================================
-- 8) Invoicing
-- ===================================================

CREATE TABLE invoice (
  invoice_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id     BIGINT NOT NULL,
  client_id    BIGINT NOT NULL,
  issue_date   DATE,
  total_amount DECIMAL(12,2) DEFAULT 0,
  last_updated TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6)
                 ON UPDATE CURRENT_TIMESTAMP(6),
  status       VARCHAR(50),
  FOREIGN KEY (order_id) REFERENCES work_order(order_id),
  FOREIGN KEY (client_id) REFERENCES client(client_id)
) ENGINE=InnoDB;

CREATE TABLE invoice_item (
  item_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  invoice_id  BIGINT NOT NULL,
  work_id     BIGINT,
  description VARCHAR(255),
  amount      DECIMAL(12,2),
  unit_price  DECIMAL(12,2),
  currency    CHAR(3) DEFAULT 'MXN',
  FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id) ON DELETE CASCADE,
  FOREIGN KEY (work_id)    REFERENCES work(work_id)
) ENGINE=InnoDB;

-- ===================================================
-- 9) Payments
-- ===================================================

-- Para saber si un trabajo esta pagado hay que sumar los payment_allocation.amount_applied (cliente da pago)
-- y los client_balance_movement.amount_change (se usa credito que el cliente tiene) asociados a dicho trabajo.
-- pagado(work) =
--   SUM(payment_allocation.amount_applied)
-- + SUM(ABS(client_balance_movement.amount_change WHERE type='APPLY_WORK'))

CREATE TABLE payment (
  payment_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_id    BIGINT NOT NULL,
  received_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  method       VARCHAR(40),
  amount_total DECIMAL(12,2) NOT NULL,
  currency     CHAR(3) DEFAULT 'MXN',
  status ENUM('RECEIVED', 'CANCELLED') DEFAULT 'RECEIVED',
  last_updated TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6)
                 ON UPDATE CURRENT_TIMESTAMP(6),
  reference    VARCHAR(100),
  notes        VARCHAR(255),
  FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE payment_allocation (
  allocation_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  
  payment_id      BIGINT NOT NULL,
  work_id         BIGINT NOT NULL,      -- vínculo directo con trabajo
  
  amount_applied  DECIMAL(12,2) NOT NULL
                  CHECK (amount_applied >= 0),
  
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  FOREIGN KEY (payment_id) REFERENCES payment(payment_id) ON DELETE CASCADE,
  FOREIGN KEY (work_id)    REFERENCES work(work_id)    ON DELETE CASCADE
) ENGINE=InnoDB;

-- Registra el saldo favorable que un cliente pueda tener, ya sea porque pago de mas
-- Se cancelo un trabajo ya pagado, se asigno saldo por promocion o recompenza, etc.
-- Un renglon por cliente. El historico se mantiene en client_balance_movement, en esta
-- tabla solo se mantiene un reglon con el saldo (balance) actual.
CREATE TABLE client_balance (
  balance_id BIGINT PRIMARY KEY AUTO_INCREMENT,

  client_id  BIGINT NOT NULL UNIQUE,
  
  -- Se usa como cache del saldo acutal del cliente. La fuente de verdad es Sum(client_balance_movement.amount_change)
  -- Se suma todo el hitorial de client_balance_movement.amount_change para un cliente dado. Habra valores negativos
  -- y positivos y la suma total es (la fuente de verdad) el saldo actual del cliente.
  amount     DECIMAL(12,2) NOT NULL CHECK (amount >= 0), 
  currency   CHAR(3) DEFAULT 'MXN',

  active     BOOLEAN DEFAULT TRUE, -- Si el cliente puede o no tener un balance. (saldo favorable)

  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                 ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (client_id)
      REFERENCES client(client_id)
      ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_pa_work ON payment_allocation(work_id);

-- Registro historico de client_balance.
CREATE TABLE client_balance_movement (
  movement_id   BIGINT PRIMARY KEY AUTO_INCREMENT,

  client_id     BIGINT NOT NULL,

  -- Positivo = entra saldo
  -- Negativo = se usa saldo (por ejemplo, para pagar un trabajo)
  -- Es la fuente de verdad para el saldo de un cliente. client_balance.amount es solo un cache cuya
  -- fuente de verdad es Sum(amount_change) para cada cliente.
  amount_change DECIMAL(12,2) NOT NULL,

  currency      CHAR(3) DEFAULT 'MXN',

  -- 
  -- Tipos comunes:
  -- 'PAY_EXCESS'      → sobrante de un pago real (se va al balance)
  -- 'APPLY_WORK'      → se usa saldo para pagar un trabajo
  -- 'PROMO'           → bono/promoción
  -- 'MANUAL_ADJUST'   → corrección administrativa
  --
  type          VARCHAR(40) NOT NULL,

  -- Si el movimiento proviene de un pago real
  payment_id    BIGINT NULL,

  -- Si el movimiento corresponde a pagar un trabajo
  work_id       BIGINT NULL,

  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  note          VARCHAR(255),
  
  CHECK (amount_change <> 0),

  FOREIGN KEY (client_id)
      REFERENCES client(client_id)
      ON DELETE CASCADE,

  FOREIGN KEY (payment_id)
      REFERENCES payment(payment_id)
      ON DELETE SET NULL,

  FOREIGN KEY (work_id)
      REFERENCES work(work_id)
      ON DELETE SET NULL
) ENGINE=InnoDB;
CREATE INDEX idx_cbm_client ON client_balance_movement(client_id, created_at);
CREATE INDEX idx_cbm_payment ON client_balance_movement(payment_id);
CREATE INDEX idx_cbm_work ON client_balance_movement(work_id);

-- ===================================================
-- 10) Security (Refresh Tokens)
--
-- Stores refresh JWTs for users.
-- Each token is uniquely identified by its JTI claim (token ID)
-- and securely linked to a user.
--
-- Security properties:
--   - Each refresh token is unique per issue (via JTI)
--   - Tokens are revocable and expire independently
--   - Revoked and expired tokens can be cleaned up periodically
-- ================================================================

CREATE TABLE refresh_token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(500) NOT NULL,
  jti VARCHAR(255) NOT NULL,
  user_id BIGINT NOT NULL,
  expiry_date TIMESTAMP NOT NULL,
  revoked BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_used_at TIMESTAMP NULL DEFAULT NULL,
  CONSTRAINT uk_refresh_token_token UNIQUE (token),
  CONSTRAINT uk_refresh_token_jti UNIQUE (jti),
  FOREIGN KEY (user_id) REFERENCES user_account(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;
-- Indexes for performance
CREATE INDEX idx_refresh_user_id ON refresh_token(user_id);
CREATE INDEX idx_refresh_user_revoked ON refresh_token(user_id, revoked);
CREATE INDEX idx_refresh_revoked_expiry ON refresh_token(revoked, expiry_date);
CREATE INDEX idx_refresh_expiry_date ON refresh_token(expiry_date);

-- ===================================================
-- 11) Contact tables (source of truth)
-- ===================================================

CREATE TABLE client_phone (
  phone_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_id    BIGINT NOT NULL,
  phone        VARCHAR(50) NOT NULL,
  type ENUM('MOBILE','OFFICE','HOME','FAX','OTHER') DEFAULT 'MOBILE',
  is_primary   BOOLEAN DEFAULT FALSE,
  is_active    BOOLEAN DEFAULT TRUE,
  created_at   TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  last_updated TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6)
                 ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE (client_id, phone),
  FOREIGN KEY (client_id)
    REFERENCES client(client_id)
    ON DELETE CASCADE
) ENGINE=InnoDB
  COMMENT='Source of truth for phones; mirrors client.primary_phone.';
CREATE INDEX idx_client_phone_value ON client_phone(phone);

CREATE TABLE client_email (
  email_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_id    BIGINT NOT NULL,
  email        VARCHAR(255) NOT NULL,
  type ENUM('WORK','PERSONAL','OTHER') DEFAULT 'WORK',
  is_primary   BOOLEAN DEFAULT FALSE,
  is_active    BOOLEAN DEFAULT TRUE,
  created_at   TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
  last_updated TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6)
                 ON UPDATE CURRENT_TIMESTAMP(6),

  UNIQUE (client_id, email),
  FOREIGN KEY (client_id)
    REFERENCES client(client_id)
    ON DELETE CASCADE
) ENGINE=InnoDB
  COMMENT='Source of truth for emails; mirrors client.primary_email.';

CREATE INDEX idx_client_email_value ON client_email(email);

CREATE TABLE client_address (
  address_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  client_id    BIGINT NOT NULL,
  street       VARCHAR(255) NOT NULL,
  street_num   VARCHAR(255) NOT NULL,
  interior_num VARCHAR(255),
  zip_code     VARCHAR(255) NOT NULL,
  neighborhood VARCHAR(255) NOT NULL,
  city         VARCHAR(255) NOT NULL,
  state        VARCHAR(255) NOT NULL,
  type ENUM('HOME','CLINIC','OTHER') DEFAULT 'CLINIC',
  name_label   VARCHAR(255),
  is_primary   BOOLEAN DEFAULT FALSE,
  is_active    BOOLEAN DEFAULT TRUE,
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                 ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (client_id)
    REFERENCES client(client_id)
    ON DELETE CASCADE,

  UNIQUE KEY uniq_client_address
    (client_id, street(100), street_num(50), city(100), state(100))
) ENGINE=InnoDB
  COMMENT='Source of truth for addresses; mirrors client.primary_address.';

CREATE INDEX idx_client_address_city ON client_address(city);
CREATE INDEX idx_client_address_zip  ON client_address(zip_code);

-- ===================================================
-- 12) Synchronization triggers
-- ===================================================

DELIMITER //
-- ========================================================
-- prevent manual edits to invoice.total_amount
-- ========================================================
CREATE TRIGGER trg_invoice_prevent_manual_total_update
BEFORE UPDATE ON invoice
FOR EACH ROW
BEGIN
  IF NEW.total_amount <> OLD.total_amount THEN
    SET NEW.total_amount = OLD.total_amount;
  END IF;
END//

-- ---------- EMAIL ----------
CREATE TRIGGER trg_sync_email_after_insert
AFTER INSERT ON client_email
FOR EACH ROW
BEGIN
  IF NEW.is_primary THEN
    -- Update only the client record, no join to user_account
    -- If client latter is related to user_account, update mail in user_account too.
    UPDATE client
      SET primary_email = NEW.email
      WHERE client_id = NEW.client_id;
  END IF;
END//

CREATE TRIGGER trg_sync_email_after_update
AFTER UPDATE ON client_email
FOR EACH ROW
BEGIN
  IF NEW.is_primary
     AND (OLD.email <> NEW.email OR OLD.is_primary <> NEW.is_primary) THEN
    -- Mirror updated primary email to client record.
    -- If client is later linked to a user_account, extend logic here.
    UPDATE client
      SET primary_email = NEW.email
      WHERE client_id = NEW.client_id;
  END IF;
END//


CREATE TRIGGER trg_sync_email_after_delete
AFTER DELETE ON client_email
FOR EACH ROW
BEGIN
  IF OLD.is_primary THEN
    UPDATE client SET primary_email = NULL WHERE client_id = OLD.client_id;
  END IF;
END//

-- ---------- PHONE ----------
CREATE TRIGGER trg_sync_phone_after_insert
AFTER INSERT ON client_phone
FOR EACH ROW
BEGIN
  IF NEW.is_primary THEN
    -- Mirror primary phone to client record.
    -- If client is later linked to a user_account, extend logic here.
    UPDATE client
      SET primary_phone = NEW.phone
      WHERE client_id = NEW.client_id;
  END IF;
END//

CREATE TRIGGER trg_sync_phone_after_update
AFTER UPDATE ON client_phone
FOR EACH ROW
BEGIN
  IF NEW.is_primary
     AND (OLD.phone <> NEW.phone OR OLD.is_primary <> NEW.is_primary) THEN
    -- Mirror updated primary phone to client record.
    -- If client is later linked to a user_account, extend logic here.
    UPDATE client
      SET primary_phone = NEW.phone
      WHERE client_id = NEW.client_id;
  END IF;
END//

CREATE TRIGGER trg_sync_phone_after_delete
AFTER DELETE ON client_phone
FOR EACH ROW
BEGIN
  IF OLD.is_primary THEN
    UPDATE client SET primary_phone = NULL WHERE client_id = OLD.client_id;
  END IF;
END//

-- ---------- ADDRESS ----------
CREATE TRIGGER trg_sync_address_after_insert
AFTER INSERT ON client_address
FOR EACH ROW
BEGIN
  IF NEW.is_primary THEN
    UPDATE client
      SET primary_address = CONCAT(NEW.street, ' ', NEW.street_num, ', ', NEW.city, ', ', NEW.state)
      WHERE client_id = NEW.client_id;
  END IF;
END//

CREATE TRIGGER trg_sync_address_after_update
AFTER UPDATE ON client_address
FOR EACH ROW
BEGIN
  IF NEW.is_primary AND (OLD.is_primary <> NEW.is_primary OR OLD.street <> NEW.street OR OLD.city <> NEW.city) THEN
    UPDATE client
      SET primary_address = CONCAT(NEW.street, ' ', NEW.street_num, ', ', NEW.city, ', ', NEW.state)
      WHERE client_id = NEW.client_id;
  END IF;
END//

CREATE TRIGGER trg_sync_address_after_delete
AFTER DELETE ON client_address
FOR EACH ROW
BEGIN
  IF OLD.is_primary THEN
    UPDATE client SET primary_address = NULL WHERE client_id = OLD.client_id;
  END IF;
END//

CREATE TRIGGER trg_invoice_item_after_insert
AFTER INSERT ON invoice_item
FOR EACH ROW
BEGIN
  UPDATE invoice
  SET total_amount = (
    SELECT COALESCE(SUM(amount), 0)
    FROM invoice_item
    WHERE invoice_id = NEW.invoice_id
  )
  WHERE invoice_id = NEW.invoice_id;
END//

CREATE TRIGGER trg_invoice_item_after_update
AFTER UPDATE ON invoice_item
FOR EACH ROW
BEGIN
  UPDATE invoice
  SET total_amount = (
    SELECT COALESCE(SUM(amount), 0)
    FROM invoice_item
    WHERE invoice_id = NEW.invoice_id
  )
  WHERE invoice_id = NEW.invoice_id;
END//

-- ========================
-- Keep integrity of invoice_total_amount. It will always be
-- computed from invoice_item in the child table. Never touch
-- invoice_total_amount directly.
-- ========================

CREATE TRIGGER trg_invoice_item_after_delete
AFTER DELETE ON invoice_item
FOR EACH ROW
BEGIN
  UPDATE invoice
  SET total_amount = (
    SELECT COALESCE(SUM(amount), 0)
    FROM invoice_item
    WHERE invoice_id = OLD.invoice_id
  )
  WHERE invoice_id = OLD.invoice_id;
END//

-- ========================
-- For Client - display_name
-- Keep display_name syncrhonized with first_name etc.
-- ========================

CREATE TRIGGER trg_client_display_name
BEFORE INSERT ON client
FOR EACH ROW
BEGIN
  IF NEW.display_name IS NULL OR NEW.display_name = '' THEN
    SET NEW.display_name =
      CONCAT_WS(' ',
        NEW.first_name,
        NEW.second_name,
        NEW.last_name,
        NEW.second_last_name
      );
  END IF;
END//

CREATE TRIGGER trg_client_display_name_update
BEFORE UPDATE ON client
FOR EACH ROW
BEGIN
  IF (NEW.first_name <> OLD.first_name
      OR NEW.second_name <> OLD.second_name
      OR NEW.last_name <> OLD.last_name
      OR NEW.second_last_name <> OLD.second_last_name) THEN
    SET NEW.display_name =
      CONCAT_WS(' ',
        NEW.first_name,
        NEW.second_name,
        NEW.last_name,
        NEW.second_last_name
      );
  END IF;
END//

-- ========================
-- For Worker - display_name
-- Keep display_name syncrhonized with first_name etc.
-- ========================

CREATE TRIGGER trg_worker_display_name
BEFORE INSERT ON worker
FOR EACH ROW
BEGIN
  IF NEW.display_name IS NULL OR NEW.display_name = '' THEN
    SET NEW.display_name =
      CONCAT_WS(' ',
        NEW.first_name,
        NEW.second_name,
        NEW.last_name,
        NEW.second_last_name
      );
  END IF;
END//

CREATE TRIGGER trg_worker_display_name_update
BEFORE UPDATE ON worker
FOR EACH ROW
BEGIN
  IF (NEW.first_name <> OLD.first_name
      OR NEW.second_name <> OLD.second_name
      OR NEW.last_name <> OLD.last_name
      OR NEW.second_last_name <> OLD.second_last_name) THEN
    SET NEW.display_name =
      CONCAT_WS(' ',
        NEW.first_name,
        NEW.second_name,
        NEW.last_name,
        NEW.second_last_name
      );
  END IF;
END//

-- =====================================================
-- WORK FAMILY consistency. work.work_family should 
-- correspond with the associated extencion table.
-- =====================================================

-- =====================================================
-- CROWN WORK consistency: work.type must be 'CROWN'
-- =====================================================
CREATE TRIGGER trg_crown_work_before_insert_type
BEFORE INSERT ON crown_work
FOR EACH ROW
BEGIN
  DECLARE wt VARCHAR(50);
  SELECT type INTO wt FROM work WHERE work_id = NEW.work_id;

  IF wt IS NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid work_id in crown_work (no matching work found)';
  ELSEIF wt <> 'CROWN' THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid type for crown_work (must be CROWN)';
  END IF;
END//

CREATE TRIGGER trg_crown_work_before_update_type
BEFORE UPDATE ON crown_work
FOR EACH ROW
BEGIN
  DECLARE wt VARCHAR(50);
  SELECT type INTO wt FROM work WHERE work_id = NEW.work_id;

  IF wt <> 'CROWN' THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid type for crown_work (must be CROWN)';
  END IF;
END//

-- =====================================================
-- BRIDGE WORK consistency: work.type must be 'BRIDGE'
-- =====================================================
CREATE TRIGGER trg_bridge_work_before_insert_type
BEFORE INSERT ON bridge_work
FOR EACH ROW
BEGIN
  DECLARE wt VARCHAR(50);
  SELECT type INTO wt FROM work WHERE work_id = NEW.work_id;

  IF wt IS NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid type in bridge_work (no matching work found)';
  ELSEIF wt <> 'BRIDGE' THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid type for bridge_work (must be BRIDGE)';
  END IF;
END//

CREATE TRIGGER trg_bridge_work_before_update_type
BEFORE UPDATE ON bridge_work
FOR EACH ROW
BEGIN
  DECLARE wt VARCHAR(50);
  SELECT type INTO wt FROM work WHERE work_id = NEW.work_id;

  IF wt <> 'BRIDGE' THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid type for bridge_work (must be BRIDGE)';
  END IF;
END//

-- =====================================================
-- Optional safeguard: prevent changing type in work
-- when a subtype row (relationship with extension table) already exists
-- =====================================================

CREATE TRIGGER trg_work_prevent_type_change
BEFORE UPDATE ON work
FOR EACH ROW
BEGIN
  DECLARE has_crown INT DEFAULT 0;
  DECLARE has_bridge INT DEFAULT 0;
  DECLARE has_inlay INT DEFAULT 0;

  SELECT COUNT(*) INTO has_crown FROM crown_work WHERE work_id = OLD.work_id;
  SELECT COUNT(*) INTO has_bridge FROM bridge_work WHERE work_id = OLD.work_id;
  SELECT COUNT(*) INTO has_inlay FROM inlay_work WHERE work_id = OLD.work_id;

  IF NEW.type <> OLD.type THEN
    IF has_crown > 0 THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot change type: associated crown_work exists';
    ELSEIF has_bridge > 0 THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot change type: associated bridge_work exists';
    ELSEIF has_inlay > 0 THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot change type: associated inlay_work exists';
    END IF;
  END IF;
END//

-- =====================================================
-- FAMILY WORK CONSISTENCY
-- =====================================================

CREATE TRIGGER trg_work_family_type_consistency
BEFORE INSERT ON work
FOR EACH ROW
BEGIN
  DECLARE expected_family VARCHAR(50);
  DECLARE msg TEXT;

  SELECT family_code INTO expected_family
  FROM work_type_ref
  WHERE code = NEW.type;

  IF expected_family IS NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid type: not found in work_type_ref';
  ELSEIF expected_family <> NEW.work_family THEN
    SET msg = CONCAT(
      'Family mismatch: type ', NEW.type,
      ' belongs to ', expected_family,
      ', not ', NEW.work_family
    );
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = msg;
  END IF;
END//
  
CREATE TRIGGER trg_work_family_type_consistency_update
BEFORE UPDATE ON work
FOR EACH ROW
BEGIN
  DECLARE expected_family VARCHAR(50);
  DECLARE msg TEXT;

  SELECT family_code INTO expected_family
  FROM work_type_ref
  WHERE code = NEW.type;

  IF expected_family IS NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid type: not found in work_type_ref';
  ELSEIF expected_family <> NEW.work_family THEN
    SET msg = CONCAT(
      'Family mismatch: type ', NEW.type,
      ' belongs to ', expected_family,
      ', not ', NEW.work_family
    );
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = msg;
  END IF;
END//

-- =====================================================
-- Trigger for technical building status history
-- =====================================================

CREATE TRIGGER trg_crown_work_build_status_history
BEFORE UPDATE ON crown_work
FOR EACH ROW
BEGIN
  IF NEW.building_status_id <> OLD.building_status_id THEN
    INSERT INTO building_status_history(
      work_id,
      extension_type,
      from_status_id,
      to_status_id,
      changed_by_worker_id,
      note
    ) VALUES (
      NEW.work_id,
      'CROWN',
      OLD.building_status_id,
      NEW.building_status_id,
      NULL,
      'Auto logged by trigger'
    );
  END IF;
END//

CREATE TRIGGER trg_bridge_work_build_status_history
BEFORE UPDATE ON bridge_work
FOR EACH ROW
BEGIN
  IF NEW.building_status_id <> OLD.building_status_id THEN
    INSERT INTO building_status_history(
      work_id,
      extension_type,
      from_status_id,
      to_status_id,
      changed_by_worker_id,
      note
    ) VALUES (
      NEW.work_id,
      'BRIDGE',
      OLD.building_status_id,
      NEW.building_status_id,
      NULL,
      'Auto logged by trigger'
    );
  END IF;
END//

DELIMITER ;

-- ===================================================
-- INSERT LOOKUP TABLE VALUES FOR STARTING
-- ===================================================

INSERT INTO work_status_ref (code, label, sequence_order) VALUES
('RECEIVED', 'Recibido', 1),
('ASSIGNED', 'Asignado', 2),
('IN_PROGRESS', 'En proceso', 3),
('FINISHED', 'Terminado', 4),
('DELIVERING', 'Listo para entregar', 5),
('DELIVERED', 'Entregado', 6);

INSERT INTO work_family_ref (code, label) VALUES
('FIXED_PROSTHESIS', 'Protesis fija'),
('REMOVABLE_PROSTHESIS', 'Protesis Removible'),
('IMPLANT_PROSTHESIS', 'Implant Prosthesis'),
('ORTHODONTICS', 'Orthodontics'),
('OTHER', 'Other');

INSERT INTO work_type_ref (code, label, family_code) VALUES
-- Fixed prosthesis
('CROWN', 'Corona', 'FIXED_PROSTHESIS'),
('BRIDGE', 'Puente', 'FIXED_PROSTHESIS'),
('INLAY', 'Incrustación', 'FIXED_PROSTHESIS'),

-- Removable
('DENTURE_PARTIAL', 'Partial Denture', 'REMOVABLE_PROSTHESIS'),
('DENTURE_COMPLETE', 'Complete Denture', 'REMOVABLE_PROSTHESIS'),

-- Implant prosthesis
('IMPLANT_CROWN', 'Implant Crown', 'IMPLANT_PROSTHESIS'),
('IMPLANT_BRIDGE', 'Implant Bridge', 'IMPLANT_PROSTHESIS'),
('IMPLANT_BAR', 'Implant Bar', 'IMPLANT_PROSTHESIS'),
('IMPLANT_OVERDENTURE', 'Implant Overdenture', 'IMPLANT_PROSTHESIS'),

-- Other
('REPAIR', 'Repair', 'OTHER'),
('ADJUSTMENT', 'Adjustment', 'OTHER');

INSERT INTO building_status_ref (code, label, description) VALUES
('RECEIVED', 'Received', 'Work received by the lab'),
('PRE_SCAN', 'Pre-Scan Check', 'Model or STL preparation'),
('SCAN', 'Scanning', 'Intraoral or model scanning'),
('DESIGN', 'CAD Design', 'Digital design in CAD software'),
('READY_FOR_MILL', 'Ready for Milling', 'Design approved for CAM'),
('MILLING', 'Milling', 'CAM milling process'),
('SINTERING', 'Sintering / Crystallization', 'Furnace cycles'),
('POST_PROCESS', 'Post-Process', 'Finishing, corrections'),
('STRATIFICATION', 'Layering / Characterization', 'Ceramic characterization'),
('GLAZE', 'Glaze / Stain', 'Final surface glaze'),
('QA', 'Quality Control', 'Final quality inspection'),
('READY_FOR_DELIVERY', 'Ready for Delivery', 'Work completed'),
('DELIVERED', 'Delivered', 'Delivered to client/patient');

INSERT INTO building_status_rule
(work_family, type, constitution, building_technique, status_id, sequence_order, is_terminal)
SELECT 'FIXED_PROSTHESIS', 'CROWN', 'MONOLITHIC', 'DIGITAL',
       r.status_id, t.seq, t.term
FROM (
  SELECT 'RECEIVED'        AS s, 1 AS seq, FALSE AS term UNION ALL
  SELECT 'SCAN'            AS s, 2 AS seq, FALSE AS term UNION ALL
  SELECT 'DESIGN'          AS s, 3 AS seq, FALSE AS term UNION ALL
  SELECT 'MILLING'         AS s, 4 AS seq, FALSE AS term UNION ALL
  SELECT 'SINTERING'       AS s, 5 AS seq, FALSE AS term UNION ALL
  SELECT 'POST_PROCESS'    AS s, 6 AS seq, FALSE AS term UNION ALL
  SELECT 'GLAZE'           AS s, 7 AS seq, FALSE AS term UNION ALL
  SELECT 'QA'              AS s, 8 AS seq, FALSE AS term UNION ALL
  SELECT 'READY_FOR_DELIVERY' AS s, 9 AS seq, TRUE AS term
) t
JOIN building_status_ref r ON r.code = t.s;

INSERT INTO building_status_rule
(work_family, type, constitution, building_technique, status_id, sequence_order, is_terminal)
SELECT 'FIXED_PROSTHESIS', 'CROWN', 'STRATIFIED', 'DIGITAL',
       r.status_id, t.seq, t.term
FROM (
  SELECT 'RECEIVED'        AS s, 1 AS seq, FALSE AS term UNION ALL
  SELECT 'SCAN'            AS s, 2 AS seq, FALSE AS term UNION ALL
  SELECT 'DESIGN'          AS s, 3 AS seq, FALSE AS term UNION ALL
  SELECT 'MILLING'         AS s, 4 AS seq, FALSE AS term UNION ALL
  SELECT 'SINTERING'       AS s, 5 AS seq, FALSE AS term UNION ALL
  SELECT 'STRATIFICATION'  AS s, 6 AS seq, FALSE AS term UNION ALL
  SELECT 'GLAZE'           AS s, 7 AS seq, FALSE AS term UNION ALL
  SELECT 'QA'              AS s, 8 AS seq, FALSE AS term UNION ALL
  SELECT 'READY_FOR_DELIVERY' AS s, 9 AS seq, TRUE AS term
) t
JOIN building_status_ref r ON r.code = t.s;

INSERT INTO building_status_rule
(work_family, type, constitution, building_technique, status_id, sequence_order, is_terminal)
SELECT 'FIXED_PROSTHESIS', 'BRIDGE', NULL, 'DIGITAL',
       r.status_id, t.seq, t.term
FROM (
  SELECT 'RECEIVED'        AS s, 1 AS seq, FALSE AS term UNION ALL
  SELECT 'SCAN'            AS s, 2 AS seq, FALSE AS term UNION ALL
  SELECT 'DESIGN'          AS s, 3 AS seq, FALSE AS term UNION ALL
  SELECT 'MILLING'         AS s, 4 AS seq, FALSE AS term UNION ALL
  SELECT 'SINTERING'       AS s, 5 AS seq, FALSE AS term UNION ALL
  SELECT 'POST_PROCESS'    AS s, 6 AS seq, FALSE AS term UNION ALL
  SELECT 'STRATIFICATION'  AS s, 7 AS seq, FALSE AS term UNION ALL
  SELECT 'GLAZE'           AS s, 8 AS seq, FALSE AS term UNION ALL
  SELECT 'QA'              AS s, 9 AS seq, FALSE AS term UNION ALL
  SELECT 'READY_FOR_DELIVERY' AS s, 10 AS seq, TRUE AS term
) t
JOIN building_status_ref r ON r.code = t.s;


-- ===================================================
-- END OF SCHEMA
-- Version: 2025.11 (Client/Worker unified architecture)
-- ===================================================
