/*------------------------------------------------------------*/
-- CREACIÓN DE TABLAS --
/*------------------------------------------------------------*/

CREATE TABLE Pais (											-- Representa países en general y no solo los de sede
    Id_Pais BIGINT AUTO_INCREMENT PRIMARY KEY,
    Nombre_Pais VARCHAR(100) NOT NULL,

    CONSTRAINT uk_pais_nombre UNIQUE (Nombre_Pais)
);

CREATE TABLE Pais_Sede (
    Id_Pais_Sede BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Pais BIGINT NOT NULL,
    CONSTRAINT uk_pais_sede_pais 
		UNIQUE (Id_Pais),

    CONSTRAINT fk_pais_sede_pais
        FOREIGN KEY (Id_Pais)
        REFERENCES Pais(Id_Pais)
);

CREATE TABLE Direccion (								-- Atributo compuesto en el MER
    Id_Direccion BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Pais BIGINT NOT NULL,
    Localidad VARCHAR(120) NOT NULL,
    Calle VARCHAR(150) NOT NULL,
    Numero_Direccion VARCHAR(30) NOT NULL,
    Codigo_Postal VARCHAR(20),

    CONSTRAINT fk_direccion_pais			-- Se aplica FK para mantener identidad referencial
        FOREIGN KEY (Id_Pais)
        REFERENCES Pais(Id_Pais)
);

CREATE TABLE Documento (
    Id_Documento BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Pais BIGINT NOT NULL,
    Tipo_Documento VARCHAR(30) NOT NULL,
    Numero_Documento VARCHAR(50) NOT NULL,

    CONSTRAINT fk_documento_pais
        FOREIGN KEY (Id_Pais)
        REFERENCES Pais (Id_Pais),

    CONSTRAINT uk_documento
        UNIQUE (Id_Pais, Tipo_Documento, Numero_Documento)		-- La composición que lo identifica debe ser única de acuerdo al MER
);

CREATE TABLE Usuario (
    Id_Usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Documento BIGINT NOT NULL,
    Id_Direccion BIGINT NOT NULL,
    Contrasena VARCHAR(45) NOT NULL, 
    Mail VARCHAR(254) NOT NULL,
    Nombre VARCHAR(120),
    Apellido VARCHAR(120),
    Fecha_Creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,	-- Dato a mantener para registro y auditorías futuras
    CONSTRAINT uk_usuario_mail UNIQUE (Mail),
    CONSTRAINT uk_usuario_documento UNIQUE (Id_Documento),

    CONSTRAINT fk_usuario_documento
        FOREIGN KEY (Id_Documento)
        REFERENCES Documento(Id_Documento),

    CONSTRAINT fk_usuario_direccion
        FOREIGN KEY (Id_Direccion)
        REFERENCES Direccion(Id_Direccion)
);

CREATE TABLE Usuario_Telefono (							-- Atributo multivaluado en el MER
    Id_Telefono BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Usuario BIGINT NOT NULL,
    Telefono VARCHAR(40) NOT NULL,

    CONSTRAINT fk_usuario_telefono_usuario
        FOREIGN KEY (Id_Usuario)
        REFERENCES Usuario(Id_Usuario)
        ON DELETE CASCADE,								-- Nos permite eliminar sus teléfonos automáticamente si se elimina el usuario

    CONSTRAINT uk_usuario_telefono
        UNIQUE (Id_Usuario, Telefono)					-- Evitamos repetir el mismo teléfono para el mismo usuario
);

CREATE TABLE Adm_Pais_Sede (							-- Especialización de Usuario
    Id_Admin BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Usuario BIGINT NOT NULL,
    Id_Pais_Sede BIGINT NOT NULL,
    Fecha_Asignacion DATE NOT NULL,
    Activo BOOLEAN NOT NULL DEFAULT TRUE,				-- Nos permite la baja lógica

    CONSTRAINT uk_admin_usuario UNIQUE (Id_Usuario),

    CONSTRAINT fk_admin_usuario
        FOREIGN KEY (Id_Usuario)
        REFERENCES Usuario(Id_Usuario),

    CONSTRAINT fk_admin_pais_sede						-- Referencia a la sede que administra
        FOREIGN KEY (Id_Pais_Sede)
        REFERENCES Pais_Sede(Id_Pais_Sede)
);

CREATE TABLE Func_Validacion (							-- Especialización de Usuario
    Id_Funcionario BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Usuario BIGINT NOT NULL,
    Nro_Legajo VARCHAR(40) NOT NULL,
    Activo BOOLEAN NOT NULL DEFAULT TRUE,				-- Nos permite la baja lógica

    CONSTRAINT uk_funcionario_usuario UNIQUE (Id_Usuario),
    CONSTRAINT uk_funcionario_legajo UNIQUE (Nro_Legajo),

    CONSTRAINT fk_funcionario_usuario
        FOREIGN KEY (Id_Usuario)
        REFERENCES Usuario(Id_Usuario)
);

CREATE TABLE Usuario_General (											-- Especialización de Usuario
    Id_Usuario_General BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Usuario BIGINT NOT NULL,
    Fecha_Registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,		-- Dato a mantener para registro y auditorías futuras
    
    Estado_Verificacion ENUM('PENDIENTE', 'VERIFICADO', 'RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',

    CONSTRAINT uk_usuario_general_usuario UNIQUE (Id_Usuario),

    CONSTRAINT fk_usuario_general_usuario
        FOREIGN KEY (Id_Usuario)
        REFERENCES Usuario(Id_Usuario)
);

CREATE TABLE Estadio (
    Id_Estadio BIGINT AUTO_INCREMENT PRIMARY KEY,
    Nombre_Estadio VARCHAR(150) NOT NULL,
    Id_Pais_Sede BIGINT NOT NULL,
    Id_Direccion BIGINT NOT NULL,

	-- Evitamos repetir un estadio con un mismo nombre dentro de un país --
    CONSTRAINT uk_estadio_nombre_pais UNIQUE (Nombre_Estadio, Id_Pais_Sede),		

    CONSTRAINT fk_estadio_pais_sede
        FOREIGN KEY (Id_Pais_Sede)
        REFERENCES Pais_Sede(Id_Pais_Sede),

    CONSTRAINT fk_estadio_direccion
        FOREIGN KEY (Id_Direccion)
        REFERENCES Direccion(Id_Direccion)
);

CREATE TABLE Sector (
    Id_Sector BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Estadio BIGINT NOT NULL,
    Letra ENUM('A', 'B', 'C', 'D') NOT NULL,
    Capacidad INT NOT NULL,

    CONSTRAINT fk_sector_estadio
        FOREIGN KEY (Id_Estadio)
        REFERENCES Estadio(Id_Estadio),

    CONSTRAINT uk_sector_estadio_letra
        UNIQUE (Id_Estadio, Letra),

    CONSTRAINT ck_sector_capacidad
        CHECK (Capacidad > 0)						-- Validamos que la capacidad sea mayor a 0
);

CREATE TABLE Equipo (
    Id_Equipo BIGINT AUTO_INCREMENT PRIMARY KEY,
    Nombre VARCHAR(150) NOT NULL,

    CONSTRAINT uk_equipo_nombre
        UNIQUE (Nombre)
);

CREATE TABLE Evento (
    Id_Evento BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Estadio BIGINT NOT NULL,
    Id_Admin BIGINT NOT NULL,

    Id_Equipo_Local BIGINT NOT NULL,
    Id_Equipo_Visitante BIGINT NOT NULL,

    Fecha_Evento DATE NOT NULL,
    Hora_Evento TIME NOT NULL,

    CONSTRAINT fk_evento_estadio
        FOREIGN KEY (Id_Estadio)
        REFERENCES Estadio(Id_Estadio),

    CONSTRAINT fk_evento_admin
        FOREIGN KEY (Id_Admin)
        REFERENCES Adm_Pais_Sede(Id_Admin),

    CONSTRAINT fk_evento_equipo_local
        FOREIGN KEY (Id_Equipo_Local)
        REFERENCES Equipo(Id_Equipo),

    CONSTRAINT fk_evento_equipo_visitante
        FOREIGN KEY (Id_Equipo_Visitante)
        REFERENCES Equipo(Id_Equipo),

    -- No puede haber dos eventos en el mismo estadio, en la misma fecha y hora
    CONSTRAINT uk_evento_estadio_fecha_hora
        UNIQUE (Id_Estadio, Fecha_Evento, Hora_Evento),

    -- Un mismo equip no puede ser local y visitante en el mismo evento
    CONSTRAINT ck_evento_equipos_distintos
        CHECK (Id_Equipo_Local <> Id_Equipo_Visitante)
);

CREATE TABLE Evento_Sector (							-- Relación entre Evento y Sector (N:N)
    Id_Evento_Sector BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Evento BIGINT NOT NULL,
    Id_Sector BIGINT NOT NULL,
    Costo_Entrada DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_evento_sector_evento
        FOREIGN KEY (Id_Evento)
        REFERENCES Evento(Id_Evento),

    CONSTRAINT fk_evento_sector_sector
        FOREIGN KEY (Id_Sector)
        REFERENCES Sector(Id_Sector),

    CONSTRAINT uk_evento_sector
        UNIQUE (Id_Evento, Id_Sector),

    CONSTRAINT ck_evento_sector_costo
        CHECK (Costo_Entrada >= 0)
);

CREATE TABLE Comision (
    Id_Comision BIGINT AUTO_INCREMENT PRIMARY KEY,
    Fecha_Inicio DATE NOT NULL,
    Fecha_Fin DATE,							-- Puede ser null, no sabemos cuando finaliza
    Porcentaje DECIMAL(5,4) NOT NULL,		-- Ej: 0.0500 = 5%

    CONSTRAINT ck_comision_porcentaje
        CHECK (Porcentaje >= 0 AND Porcentaje < 1),		-- Rango de valores decimales para el porcentaje

    CONSTRAINT ck_comision_fechas								
        CHECK (Fecha_Fin IS NULL OR Fecha_Fin >= Fecha_Inicio)	-- La fecha fin puede ser nula, o mayir a la de inicio
);

CREATE TABLE Venta (
    Id_Venta BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Usuario_General BIGINT NOT NULL,
    Id_Comision BIGINT NOT NULL,

    Fecha_Venta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    Estado_Venta ENUM('PENDIENTE', 'CONFIRMADA', 'PAGA') 
        NOT NULL DEFAULT 'PENDIENTE',

    Monto_Total DECIMAL(12,2) NOT NULL DEFAULT 0,

    CONSTRAINT fk_venta_usuario_general							-- Vinculamos al usuario que compró la entrada
        FOREIGN KEY (Id_Usuario_General)
        REFERENCES Usuario_General(Id_Usuario_General),

    CONSTRAINT fk_venta_comision
        FOREIGN KEY (Id_Comision)
        REFERENCES Comision(Id_Comision),

    CONSTRAINT ck_venta_monto_total
        CHECK (Monto_Total >= 0)
);

CREATE TABLE Entrada (
    Id_Entrada BIGINT AUTO_INCREMENT PRIMARY KEY,

    Id_Evento_Sector BIGINT NOT NULL,
    Id_Venta BIGINT NOT NULL,
    Id_Propietario_Actual BIGINT NOT NULL,

    Costo_Entrada DECIMAL(10,2) NOT NULL,

    Estado_Entrada ENUM('NO_CONSUMIDA', 'CONSUMIDA') 
        NOT NULL DEFAULT 'NO_CONSUMIDA',

    CONSTRAINT fk_entrada_evento_sector
        FOREIGN KEY (Id_Evento_Sector)
        REFERENCES Evento_Sector(Id_Evento_Sector),

    CONSTRAINT fk_entrada_venta
        FOREIGN KEY (Id_Venta)
        REFERENCES Venta(Id_Venta),

    CONSTRAINT fk_entrada_propietario_actual				-- Es necesario conocer el propietario actual
        FOREIGN KEY (Id_Propietario_Actual)
        REFERENCES Usuario_General(Id_Usuario_General),

    CONSTRAINT ck_entrada_costo
        CHECK (Costo_Entrada >= 0)
);

CREATE TABLE Transferencia (
    Id_Transferencia BIGINT AUTO_INCREMENT PRIMARY KEY,

    Id_Entrada BIGINT NOT NULL,
    Id_Usuario_Origen BIGINT NOT NULL,
    Id_Usuario_Destino BIGINT NOT NULL,

    Fecha_Transferencia TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    Estado_Transferencia ENUM('PENDIENTE', 'ACEPTADA', 'RECHAZADA', 'CANCELADA') 
        NOT NULL DEFAULT 'PENDIENTE',

    CONSTRAINT fk_transferencia_entrada
        FOREIGN KEY (Id_Entrada)
        REFERENCES Entrada(Id_Entrada),

    CONSTRAINT fk_transferencia_usuario_origen
        FOREIGN KEY (Id_Usuario_Origen)
        REFERENCES Usuario_General(Id_Usuario_General),

    CONSTRAINT fk_transferencia_usuario_destino
        FOREIGN KEY (Id_Usuario_Destino)
        REFERENCES Usuario_General(Id_Usuario_General),

    CONSTRAINT ck_transferencia_usuarios_distintos			-- El usuario origen y usuario destino deben ser diferentes
        CHECK (Id_Usuario_Origen <> Id_Usuario_Destino)
);

CREATE TABLE Dispositivo (
    Id_Dispositivo BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Funcionario BIGINT NOT NULL,
    Codigo_Dispositivo VARCHAR(80) NOT NULL,
    Activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_dispositivo_codigo
        UNIQUE (Codigo_Dispositivo),

    CONSTRAINT uk_dispositivo_funcionario		    -- Un funcionario no puede tener más de un dispositivo
        UNIQUE (Id_Funcionario),

    CONSTRAINT fk_dispositivo_funcionario
        FOREIGN KEY (Id_Funcionario)
        REFERENCES Func_Validacion(Id_Funcionario)
);


-- Un funcionario debe haber validado entradas en todos los sectores a los que fue asignado durante un evento--
CREATE TABLE Funcionario_Evento_Sector (
    Id_Funcionario BIGINT NOT NULL,
    Id_Evento_Sector BIGINT NOT NULL,

    PRIMARY KEY (Id_Funcionario, Id_Evento_Sector),

    CONSTRAINT fk_func_evento_sector_funcionario
        FOREIGN KEY (Id_Funcionario)
        REFERENCES Func_Validacion(Id_Funcionario),

    CONSTRAINT fk_func_evento_sector_evento_sector
        FOREIGN KEY (Id_Evento_Sector)
        REFERENCES Evento_Sector(Id_Evento_Sector)
);

CREATE TABLE QR (
    Id_QR BIGINT AUTO_INCREMENT PRIMARY KEY,
    Id_Entrada BIGINT NOT NULL,

    Token VARCHAR(255) NOT NULL,
    Fecha_Generacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Fecha_Expiracion TIMESTAMP NOT NULL,

    CONSTRAINT fk_qr_entrada
        FOREIGN KEY (Id_Entrada)
        REFERENCES Entrada(Id_Entrada),

    CONSTRAINT uk_qr_token
        UNIQUE (Token),

    CONSTRAINT ck_qr_fechas
        CHECK (Fecha_Expiracion > Fecha_Generacion)
);

CREATE TABLE Validacion (								-- Relación entre Dispositivo y QR
    Id_Validacion BIGINT AUTO_INCREMENT PRIMARY KEY,

    Id_QR BIGINT NOT NULL,
    Id_Dispositivo BIGINT NOT NULL,
    Id_Funcionario BIGINT NOT NULL,

    Fecha_Hora_Ingreso TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Puerta_Ingreso VARCHAR(50) NOT NULL,

    CONSTRAINT fk_validacion_qr
        FOREIGN KEY (Id_QR)
        REFERENCES QR(Id_QR),

    CONSTRAINT fk_validacion_dispositivo
        FOREIGN KEY (Id_Dispositivo)
        REFERENCES Dispositivo(Id_Dispositivo),

    CONSTRAINT fk_validacion_funcionario
        FOREIGN KEY (Id_Funcionario)
        REFERENCES Func_Validacion(Id_Funcionario),

    CONSTRAINT uk_validacion_qr
        UNIQUE (Id_QR)
);
/*------------------------------------------------------------*/
-- CREACION DE TRIGGERS --
/*------------------------------------------------------------*/

-- Máximo 5 entradas por venta --
DELIMITER $$
CREATE TRIGGER trg_max_5_entradas
BEFORE INSERT ON Entrada
FOR EACH ROW
BEGIN
    DECLARE cantidad INT;

    -- Contamos cuántas entradas tiene la venta
    SELECT COUNT(*) INTO cantidad
    FROM Entrada
    WHERE Id_Venta = NEW.Id_Venta;

    -- Si ya tiene 5, no dejamos insertar otra
    IF cantidad >= 5 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Máximo 5 entradas por venta';
    END IF;
END $$

DELIMITER ;
/*------------------------------------------------------------*/

-- Máximo 3 transferencias por entrada
DELIMITER $$

CREATE TRIGGER trg_max_3_transferencias
BEFORE INSERT ON Transferencia
FOR EACH ROW
BEGIN
    DECLARE cantidad INT;

    -- Contamos cuántas transferencias aceptadas ya tiene la entrada
    SELECT COUNT(*) INTO cantidad
    FROM Transferencia
    WHERE Id_Entrada = NEW.Id_Entrada
      AND Estado_Transferencia = 'ACEPTADA';

    IF cantidad >= 3 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Máximo 3 transferencias por entrada';
    END IF;
END $$

DELIMITER ;
/*------------------------------------------------------------*/
DELIMITER $$
-- Es necesario validar la cantidad d etransferencias también antes de actualizar la tabla
CREATE TRIGGER trg_validar_aceptacion_transferencia
BEFORE UPDATE ON Transferencia
FOR EACH ROW
BEGIN
    DECLARE cantidad INT;
    DECLARE propietario_actual BIGINT;

    -- Solo validamos cuando la transferencia pasa a ACEPTADA
    IF NEW.Estado_Transferencia = 'ACEPTADA'
       AND OLD.Estado_Transferencia <> 'ACEPTADA' THEN

        -- Contamos transferencias aceptadas previas de esa entrada
        SELECT COUNT(*) INTO cantidad
        FROM Transferencia
        WHERE Id_Entrada = NEW.Id_Entrada
          AND Estado_Transferencia = 'ACEPTADA';

        IF cantidad >= 3 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Máximo 3 transferencias aceptadas por entrada';
        END IF;

        -- Validamos que el usuario origen siga siendo propietario actual
        SELECT Id_Propietario_Actual INTO propietario_actual
        FROM Entrada
        WHERE Id_Entrada = NEW.Id_Entrada;

        IF propietario_actual <> NEW.Id_Usuario_Origen THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El usuario origen no es el propietario actual al aceptar la transferencia';
        END IF;

    END IF;
END $$

DELIMITER ;
/*------------------------------------------------------------*/
-- Usuario origen debe ser dueño de la entrada
DELIMITER $$

CREATE TRIGGER trg_validar_origen_transferencia
BEFORE INSERT ON Transferencia
FOR EACH ROW
BEGIN
    DECLARE propietario_actual BIGINT;

    -- Obtenemos el propietario actual de la entrada
    SELECT Id_Propietario_Actual INTO propietario_actual
    FROM Entrada
    WHERE Id_Entrada = NEW.Id_Entrada;

    -- Validamos que quien transfiere sea el dueño
    IF propietario_actual <> NEW.Id_Usuario_Origen THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El usuario origen no es el propietario actual';
    END IF;
END $$

DELIMITER ;
/*------------------------------------------------------------*/
-- Actualizar propietario cuando la transferencia se acepta 
DELIMITER $$

CREATE TRIGGER trg_actualizar_propietario
AFTER UPDATE ON Transferencia
FOR EACH ROW
BEGIN

	-- Verifica que la transferencia haya sido aceptada
    IF NEW.Estado_Transferencia = 'ACEPTADA' 
       AND OLD.Estado_Transferencia <> 'ACEPTADA' THEN
		
	-- Actualiza el propietario
        UPDATE Entrada
        SET Id_Propietario_Actual = NEW.Id_Usuario_Destino
        WHERE Id_Entrada = NEW.Id_Entrada;

    END IF;
END $$

DELIMITER ;
/*------------------------------------------------------------*/
-- No vender más entradas que la capacidad del sector
DELIMITER $$

CREATE TRIGGER trg_control_capacidad
BEFORE INSERT ON Entrada
FOR EACH ROW
BEGIN
    DECLARE capacidad_total INT;
    DECLARE entradas_vendidas INT;

    -- Obtener capacidad del sector
    SELECT s.Capacidad INTO capacidad_total
    FROM Evento_Sector es
    JOIN Sector s ON es.Id_Sector = s.Id_Sector
    WHERE es.Id_Evento_Sector = NEW.Id_Evento_Sector;

    -- Contar entradas ya vendidas
    SELECT COUNT(*) INTO entradas_vendidas
    FROM Entrada
    WHERE Id_Evento_Sector = NEW.Id_Evento_Sector;

    -- Validar capacidad
    IF entradas_vendidas >= capacidad_total THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Capacidad del sector superada';
    END IF;
END $$

DELIMITER ;
/*------------------------------------------------------------*/
-- El administrador debe pertenecer al país del estadio
DELIMITER $$

CREATE TRIGGER trg_admin_mismo_pais
BEFORE INSERT ON Evento
FOR EACH ROW
BEGIN
    DECLARE pais_admin BIGINT;
    DECLARE pais_estadio BIGINT;

    -- País del admin
    SELECT Id_Pais_Sede INTO pais_admin
    FROM Adm_Pais_Sede
    WHERE Id_Admin = NEW.Id_Admin;

    -- País del estadio
    SELECT Id_Pais_Sede INTO pais_estadio
    FROM Estadio
    WHERE Id_Estadio = NEW.Id_Estadio;

    -- Comparación
    IF pais_admin <> pais_estadio THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El administrador debe pertenecer al país del estadio';
    END IF;
END $$

DELIMITER ;
/*------------------------------------------------------------*/

-- Sector del evento debe pertenecer al estadio del evento
DELIMITER $$

CREATE TRIGGER trg_sector_pertenece_estadio
BEFORE INSERT ON Evento_Sector
FOR EACH ROW
BEGIN
    DECLARE estadio_evento BIGINT;
    DECLARE estadio_sector BIGINT;

    -- Estadio del evento
    SELECT Id_Estadio INTO estadio_evento
    FROM Evento
    WHERE Id_Evento = NEW.Id_Evento;

    -- Estadio del sector
    SELECT Id_Estadio INTO estadio_sector
    FROM Sector
    WHERE Id_Sector = NEW.Id_Sector;

    IF estadio_evento <> estadio_sector THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El sector no pertenece al estadio del evento';
    END IF;
END $$

DELIMITER ;
/*------------------------------------------------------------*/
-- Marcar entrada como consumida al validar
DELIMITER $$

CREATE TRIGGER trg_consumir_entrada
AFTER INSERT ON Validacion
FOR EACH ROW
BEGIN
    DECLARE id_entrada BIGINT;

    -- Obtener entrada desde el QR
    SELECT Id_Entrada INTO id_entrada
    FROM QR
    WHERE Id_QR = NEW.Id_QR;

    -- Marcar como consumida
    UPDATE Entrada
    SET Estado_Entrada = 'CONSUMIDA'
    WHERE Id_Entrada = id_entrada;
END $$

DELIMITER ;
/*------------------------------------------------------------*/
-- No validar entrada ya consumida
DELIMITER $$

CREATE TRIGGER trg_no_validar_consumida
BEFORE INSERT ON Validacion
FOR EACH ROW
BEGIN
    DECLARE estado VARCHAR(20);

    SELECT e.Estado_Entrada INTO estado
    FROM QR q
    JOIN Entrada e ON q.Id_Entrada = e.Id_Entrada
    WHERE q.Id_QR = NEW.Id_QR;

    IF estado = 'CONSUMIDA' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Entrada ya utilizada';
    END IF;
END $$

DELIMITER ;
/*------------------------------------------------------------*/
-- CREACIÓN DE INDICES
/*------------------------------------------------------------*/
-- Busqueda por venta
CREATE INDEX idx_entrada_venta
ON Entrada(Id_Venta);

-- Busqueda por evento-sector
CREATE INDEX idx_entrada_evento_sector
ON Entrada(Id_Evento_Sector);

-- Transferencia por entrada
CREATE INDEX idx_transferencia_entrada
ON Transferencia(Id_Entrada, Estado_Transferencia); 	-- indice compuesto

-- Validacion QR
CREATE INDEX idx_qr_entrada
ON QR(Id_Entrada);

-- Evento sector
CREATE INDEX idx_evento_sector_evento
ON Evento_Sector(Id_Evento);

CREATE INDEX idx_evento_sector_sector
ON Evento_Sector(Id_Sector);

/*------------------------------------------------------------*/