
--  Motor: MySQL 8.0+


CREATE DATABASE IF NOT EXISTS mundial_ticketing
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE mundial_ticketing;


--   USUARIO

CREATE TABLE IF NOT EXISTS Usuario (
    mail                VARCHAR(150)    NOT NULL,
    password            VARCHAR(255)    NOT NULL,
    rol                 ENUM('ADMIN','FUNCIONARIO','USUARIO') NOT NULL,
    doc_pais            VARCHAR(100)    NOT NULL,
    doc_tipo            VARCHAR(50)     NOT NULL,
    doc_numero          VARCHAR(50)     NOT NULL,
    dir_pais            VARCHAR(100)    NOT NULL,
    dir_localidad       VARCHAR(100)    NOT NULL,
    dir_calle           VARCHAR(150)    NOT NULL,
    dir_numero          VARCHAR(20)     NOT NULL,
    dir_cod_postal      VARCHAR(20)     NOT NULL,

    PRIMARY KEY (mail),
    UNIQUE KEY uq_documento (doc_pais, doc_tipo, doc_numero)
) ENGINE=InnoDB;

-- Teléfonos (multivaluado)
CREATE TABLE IF NOT EXISTS Usuario_Telefono (
    mail                VARCHAR(150)    NOT NULL,
    telefono            VARCHAR(30)     NOT NULL,

    PRIMARY KEY (mail, telefono),
    CONSTRAINT fk_tel_usuario FOREIGN KEY (mail)
        REFERENCES Usuario(mail) ON DELETE CASCADE
) ENGINE=InnoDB;




--  ESPECIALIZACIONES DE USUARIO
CREATE TABLE IF NOT EXISTS Usuario_General (
    mail                VARCHAR(150)    NOT NULL,
    fecha_registro      DATE            NOT NULL,
    estado_verificacion ENUM('PENDIENTE','VERIFICADO','RECHAZADO')
                                        NOT NULL DEFAULT 'PENDIENTE',

    PRIMARY KEY (mail),
    CONSTRAINT fk_ug_usuario FOREIGN KEY (mail)
        REFERENCES Usuario(mail) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Func_Validacion (
    mail                VARCHAR(150)    NOT NULL,
    nro_legajo          VARCHAR(50)     NOT NULL,

    PRIMARY KEY (mail),
    UNIQUE KEY uq_legajo (nro_legajo),
    CONSTRAINT fk_fv_usuario FOREIGN KEY (mail)
        REFERENCES Usuario(mail) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Adm_Pais_Sede (
    mail                VARCHAR(150)    NOT NULL,
    fecha_asignacion    DATE            NOT NULL,
    nombre_pais         VARCHAR(100)    NOT NULL,

    PRIMARY KEY (mail),
    CONSTRAINT fk_adm_usuario FOREIGN KEY (mail)
        REFERENCES Usuario(mail) ON DELETE CASCADE
) ENGINE=InnoDB;


--   PAÍS SEDE Y ESTADIO
CREATE TABLE IF NOT EXISTS Pais_Sede (
    nombre_pais         VARCHAR(100)    NOT NULL,
    PRIMARY KEY (nombre_pais)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Estadio (
    id_estadio          INT             NOT NULL AUTO_INCREMENT,
    nombre              VARCHAR(150)    NOT NULL,
    nombre_pais         VARCHAR(100)    NOT NULL,

    PRIMARY KEY (id_estadio),
    CONSTRAINT fk_est_pais FOREIGN KEY (nombre_pais)
        REFERENCES Pais_Sede(nombre_pais)
) ENGINE=InnoDB;

ALTER TABLE Adm_Pais_Sede
    ADD CONSTRAINT fk_adm_pais FOREIGN KEY (nombre_pais)
        REFERENCES Pais_Sede(nombre_pais);


--   SECTOR (entidad débil de Estadio)
CREATE TABLE IF NOT EXISTS Sector (
    id_estadio          INT             NOT NULL,
    letra               CHAR(1)         NOT NULL,
    capacidad           INT             NOT NULL,

    PRIMARY KEY (id_estadio, letra),
    CONSTRAINT fk_sec_estadio FOREIGN KEY (id_estadio)
        REFERENCES Estadio(id_estadio)
) ENGINE=InnoDB;

-- Asignación de Funcionario a Sectores
CREATE TABLE IF NOT EXISTS Func_Sector_Asignado (
    mail_funcionario    VARCHAR(150)    NOT NULL,
    id_estadio          INT             NOT NULL,
    letra_sector        CHAR(1)         NOT NULL,

    PRIMARY KEY (mail_funcionario, id_estadio, letra_sector),
    CONSTRAINT fk_fsa_func FOREIGN KEY (mail_funcionario)
        REFERENCES Func_Validacion(mail),
    CONSTRAINT fk_fsa_sector FOREIGN KEY (id_estadio, letra_sector)
        REFERENCES Sector(id_estadio, letra)
) ENGINE=InnoDB;


--   EQUIPO
CREATE TABLE IF NOT EXISTS Equipo (
    nombre              VARCHAR(100)    NOT NULL,
    PRIMARY KEY (nombre)
) ENGINE=InnoDB;


--   COMISIÓN
CREATE TABLE IF NOT EXISTS Comision (
    id_comision         INT             NOT NULL AUTO_INCREMENT,
    porcentaje          DECIMAL(5,2)    NOT NULL,
    fecha_inicio        DATE            NOT NULL,
    fecha_fin           DATE            NULL,

    PRIMARY KEY (id_comision)
) ENGINE=InnoDB;


--   EVENTO
CREATE TABLE IF NOT EXISTS Evento (
    id_evento           INT             NOT NULL AUTO_INCREMENT,
    id_estadio          INT             NOT NULL,
    mail_admin          VARCHAR(150)    NOT NULL,
    fecha_evento        DATE            NOT NULL,
    hora_evento         TIME            NOT NULL,

    PRIMARY KEY (id_evento),
    CONSTRAINT fk_ev_estadio FOREIGN KEY (id_estadio)
        REFERENCES Estadio(id_estadio),
    CONSTRAINT fk_ev_admin FOREIGN KEY (mail_admin)
        REFERENCES Adm_Pais_Sede(mail),
    UNIQUE KEY uq_evento_estadio_fechahora (id_estadio, fecha_evento, hora_evento)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Evento_Equipo (
    id_evento           INT             NOT NULL,
    nombre_equipo       VARCHAR(100)    NOT NULL,
    condicion           ENUM('LOCAL','VISITANTE') NOT NULL,

    PRIMARY KEY (id_evento, nombre_equipo),
    CONSTRAINT fk_ee_evento FOREIGN KEY (id_evento)
        REFERENCES Evento(id_evento),
    CONSTRAINT fk_ee_equipo FOREIGN KEY (nombre_equipo)
        REFERENCES Equipo(nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS Evento_Sector (
    id_evento           INT             NOT NULL,
    id_estadio          INT             NOT NULL,
    letra_sector        CHAR(1)         NOT NULL,
    costo_entrada       DECIMAL(10,2)   NOT NULL,

    PRIMARY KEY (id_evento, id_estadio, letra_sector),
    CONSTRAINT fk_es_evento FOREIGN KEY (id_evento)
        REFERENCES Evento(id_evento),
    CONSTRAINT fk_es_sector FOREIGN KEY (id_estadio, letra_sector)
        REFERENCES Sector(id_estadio, letra)
) ENGINE=InnoDB;


--   DISPOSITIVO
CREATE TABLE IF NOT EXISTS Dispositivo (
    id_dispositivo      VARCHAR(100)    NOT NULL,
    mail_funcionario    VARCHAR(150)    NOT NULL,

    PRIMARY KEY (id_dispositivo),
    UNIQUE KEY uq_disp_func (mail_funcionario),
    CONSTRAINT fk_disp_func FOREIGN KEY (mail_funcionario)
        REFERENCES Func_Validacion(mail)
) ENGINE=InnoDB;


--   VENTA
CREATE TABLE IF NOT EXISTS Venta (
    id_venta            INT             NOT NULL AUTO_INCREMENT,
    mail_comprador      VARCHAR(150)    NOT NULL,
    id_comision         INT             NOT NULL,
    fecha_venta         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado              ENUM('PENDIENTE','CONFIRMADA','PAGA') NOT NULL DEFAULT 'PENDIENTE',
    monto_total         DECIMAL(10,2)   NOT NULL,

    PRIMARY KEY (id_venta),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (mail_comprador)
        REFERENCES Usuario_General(mail),
    CONSTRAINT fk_venta_comision FOREIGN KEY (id_comision)
        REFERENCES Comision(id_comision)
) ENGINE=InnoDB;


--   ENTRADA
CREATE TABLE IF NOT EXISTS Entrada (
    id_entrada          INT             NOT NULL AUTO_INCREMENT,
    id_venta            INT             NOT NULL,
    id_evento           INT             NOT NULL,
    id_estadio          INT             NOT NULL,
    letra_sector        CHAR(1)         NOT NULL,
    mail_propietario    VARCHAR(150)    NOT NULL,
    estado              ENUM('ACTIVA','TRANSFERIDA','CONSUMIDA') NOT NULL DEFAULT 'ACTIVA',
    costo_entrada       DECIMAL(10,2)   NOT NULL,
    cant_transferencias INT             NOT NULL DEFAULT 0,

    PRIMARY KEY (id_entrada),
    CONSTRAINT fk_ent_venta FOREIGN KEY (id_venta)
        REFERENCES Venta(id_venta),
    CONSTRAINT fk_ent_evento_sector FOREIGN KEY (id_evento, id_estadio, letra_sector)
        REFERENCES Evento_Sector(id_evento, id_estadio, letra_sector),
    CONSTRAINT fk_ent_propietario FOREIGN KEY (mail_propietario)
        REFERENCES Usuario_General(mail)
) ENGINE=InnoDB;


--   TRANSFERENCIA
CREATE TABLE IF NOT EXISTS Transferencia (
    id_transferencia    INT             NOT NULL AUTO_INCREMENT,
    id_entrada          INT             NOT NULL,
    mail_origen         VARCHAR(150)    NOT NULL,
    mail_destino        VARCHAR(150)    NOT NULL,
    fecha_transferencia DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado              ENUM('PENDIENTE','ACEPTADA','RECHAZADA') NOT NULL DEFAULT 'PENDIENTE',

    PRIMARY KEY (id_transferencia),
    CONSTRAINT fk_tr_entrada FOREIGN KEY (id_entrada)
        REFERENCES Entrada(id_entrada),
    CONSTRAINT fk_tr_origen FOREIGN KEY (mail_origen)
        REFERENCES Usuario_General(mail),
    CONSTRAINT fk_tr_destino FOREIGN KEY (mail_destino)
        REFERENCES Usuario_General(mail)
) ENGINE=InnoDB;


--   QR
CREATE TABLE IF NOT EXISTS QR (
    id_qr               INT             NOT NULL AUTO_INCREMENT,
    id_entrada          INT             NOT NULL,
    token               VARCHAR(500)    NOT NULL,
    fecha_generacion    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion    DATETIME        NOT NULL,
    activo              BOOLEAN         NOT NULL DEFAULT TRUE,

    PRIMARY KEY (id_qr),
    CONSTRAINT fk_qr_entrada FOREIGN KEY (id_entrada)
        REFERENCES Entrada(id_entrada)
) ENGINE=InnoDB;


--   INGRESO (registro de validación en puerta)
CREATE TABLE IF NOT EXISTS Ingreso (
    id_ingreso          INT             NOT NULL AUTO_INCREMENT,
    id_entrada          INT             NOT NULL,
    id_qr               INT             NOT NULL,
    id_dispositivo      VARCHAR(100)    NOT NULL,
    hora_ingreso        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    puerta_ingreso      VARCHAR(50)     NULL,

    PRIMARY KEY (id_ingreso),
    CONSTRAINT fk_ing_entrada FOREIGN KEY (id_entrada)
        REFERENCES Entrada(id_entrada),
    CONSTRAINT fk_ing_qr FOREIGN KEY (id_qr)
        REFERENCES QR(id_qr),
    CONSTRAINT fk_ing_dispositivo FOREIGN KEY (id_dispositivo)
        REFERENCES Dispositivo(id_dispositivo)
) ENGINE=InnoDB;


--  DATOS INICIALES
INSERT IGNORE INTO Pais_Sede (nombre_pais) VALUES ('USA'), ('Canada'), ('Mexico');

INSERT IGNORE INTO Comision (porcentaje, fecha_inicio, fecha_fin)
    VALUES (5.00, '2026-01-01', NULL);

INSERT IGNORE INTO Estadio (nombre, nombre_pais) VALUES
    ('MetLife Stadium',  'USA'),
    ('SoFi Stadium',     'USA'),
    ('Estadio Azteca',   'Mexico'),
    ('BC Place',         'Canada');

INSERT IGNORE INTO Sector (id_estadio, letra, capacidad) VALUES
    (1,'A',15000),(1,'B',15000),(1,'C',10000),(1,'D',10000),
    (2,'A',12000),(2,'B',12000),(2,'C',8000), (2,'D',8000),
    (3,'A',20000),(3,'B',20000),(3,'C',15000),(3,'D',15000),
    (4,'A',10000),(4,'B',10000),(4,'C',8000), (4,'D',8000);

INSERT IGNORE INTO Equipo (nombre) VALUES
    ('Uruguay'),('Argentina'),('Brasil'),('Francia'),
    ('España'),('Alemania'),('Portugal'),('Inglaterra');

-- Admin de prueba  |  password: admin123
INSERT IGNORE INTO Usuario (mail, password, rol,
    doc_pais, doc_tipo, doc_numero,
    dir_pais, dir_localidad, dir_calle, dir_numero, dir_cod_postal)
VALUES ('admin@ucu.edu.uy',
        '$2a$10$zL1LTXMFjMaggfa6fL6MLe0Ghbky3TbiWPdzoPuxtqwmdP097Hkiy',
        'ADMIN',
        'Uruguay','CI','12345678',
        'Uruguay','Montevideo','8 de Octubre','2738','11600');

INSERT IGNORE INTO Adm_Pais_Sede (mail, fecha_asignacion, nombre_pais)
    VALUES ('admin@ucu.edu.uy', '2026-01-01', 'USA');

-- Usuario general de prueba  |  password: user123
INSERT IGNORE INTO Usuario (mail, password, rol,
    doc_pais, doc_tipo, doc_numero,
    dir_pais, dir_localidad, dir_calle, dir_numero, dir_cod_postal)
VALUES ('usuario@test.com',
        '$2a$10$OKQZJEydAgGbICunSWA8w.guNYu1H4rB4Mz62dhktgSVOzLp/MTSu',
        'USUARIO',
        'Uruguay','CI','87654321',
        'Uruguay','Montevideo','Rivera','1234','11200');

INSERT IGNORE INTO Usuario_General (mail, fecha_registro, estado_verificacion)
    VALUES ('usuario@test.com', CURDATE(), 'VERIFICADO');

-- Funcionario de prueba  |  password: func123
INSERT IGNORE INTO Usuario (mail, password, rol,
    doc_pais, doc_tipo, doc_numero,
    dir_pais, dir_localidad, dir_calle, dir_numero, dir_cod_postal)
VALUES ('func@test.com',
        '$2a$10$H9nwr.qOhBufdnA/lYqWiOYCklM8OGxmA6vgqbnsvPJPCAv.M31.C',
        'FUNCIONARIO',
        'Uruguay','CI','11112222',
        'Uruguay','Montevideo','Bulevar','500','11300');

INSERT IGNORE INTO Func_Validacion (mail, nro_legajo)
    VALUES ('func@test.com', 'LEG-001');
