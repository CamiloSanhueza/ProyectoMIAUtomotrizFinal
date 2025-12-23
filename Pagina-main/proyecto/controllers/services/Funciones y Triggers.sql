-- FUNCIONES Y TRIGGERS (ADAPTADOS PARA TABLAS LIMPIAS)
-- 1. VALIDAR RUT (Para Cliente y Usuarios)
CREATE OR REPLACE FUNCTION validar_rut_chileno()
RETURNS TRIGGER AS $$
DECLARE
    cuerpo TEXT;
    dv CHAR(1);
    suma INT := 0;
    multiplo INT := 2;
    i INT;
    resto INT;
    dv_calc CHAR(1);
    rut_limpio TEXT;
BEGIN
    -- Limpiar puntos y guiones por si acaso, aunque el input deberia venir limpio
    -- Asumimos formato XXXXXXXX-Y
    -- Si la tabla es 'cliente', el campo es RUNCliente
    -- Si la tabla es 'usuarios' o 'personal', el campo podría variar. 
    
    -- Detectar tabla y campo
    IF TG_TABLE_NAME = 'cliente' THEN
        rut_limpio := NEW.RUNCliente;
    ELSE
        -- En tabla usuarios el campo es 'rut'
        rut_limpio := NEW.rut;
    END IF;
    cuerpo := split_part(rut_limpio, '-', 1);
    dv := upper(split_part(rut_limpio, '-', 2));
    -- Validación básica de longitud
    IF length(cuerpo) < 7 THEN
        RAISE EXCEPTION 'RUT inválido (muy corto)';
    END IF;
    FOR i IN reverse length(cuerpo)..1 LOOP
        suma := suma + (cast(substring(cuerpo, i, 1) AS INT) * multiplo);
        multiplo := multiplo + 1;
        IF multiplo > 7 THEN multiplo := 2; END IF;
    END LOOP;
    resto := 11 - (suma % 11);
    IF resto = 11 THEN
        dv_calc := '0';
    ELSIF resto = 10 THEN
        dv_calc := 'K';
    ELSE
        dv_calc := resto::TEXT;
    END IF;
    IF dv <> dv_calc THEN
        RAISE EXCEPTION 'RUT inválido: Dígito verificador incorrecto. Calculado: %', dv_calc;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
-- Trigger para Cliente
CREATE TRIGGER trg_validar_rut_cliente
BEFORE INSERT OR UPDATE ON cliente
FOR EACH ROW EXECUTE FUNCTION validar_rut_chileno();
-- Trigger para Usuarios (Personal)
CREATE TRIGGER trg_validar_rut_usuario
BEFORE INSERT OR UPDATE ON usuarios
FOR EACH ROW EXECUTE FUNCTION validar_rut_chileno();
-- 2. EVITAR PATENTE DUPLICADA POR CLIENTE
-- (Aunque la patente es PK en Vehiculo, esto asegura que la lógica de negocio se cumpla y da un mensaje claro)
CREATE OR REPLACE FUNCTION evitar_patente_duplicada()
RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM Vehiculo
        WHERE patente = NEW.patente
          AND patente <> NEW.patente -- Ignorarse a si mismo en updates (aunque patente es PK, por si acaso)
    ) THEN
        RAISE EXCEPTION 'El vehículo con patente % ya está registrado.', NEW.patente;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
-- Ya existe constraint PK, pero si quisieramos un mensaje personalizado:
-- CREATE TRIGGER trg_evitar_patente_duplicada
-- BEFORE INSERT ON Vehiculo
-- FOR EACH ROW EXECUTE FUNCTION evitar_patente_duplicada();
-- 3. ACTUALIZAR PRECIOS HISTORICOS (AUDITORIA)
CREATE TABLE IF NOT EXISTS Historial_Precios (
    id SERIAL PRIMARY KEY,
    id_repuesto INT,
    precio_anterior DECIMAL(10,2),
    precio_nuevo DECIMAL(10,2),
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE OR REPLACE FUNCTION log_cambio_precio()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.precio_venta IS DISTINCT FROM NEW.precio_venta THEN
        INSERT INTO Historial_Precios(id_repuesto, precio_anterior, precio_nuevo)
        VALUES (OLD.IDPiezaExterior, OLD.precio_venta, NEW.precio_venta);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_log_precio_piezaexterior
AFTER UPDATE ON PiezaExterior
FOR EACH ROW EXECUTE FUNCTION log_cambio_precio();
-- 4. REPORTES UTILES (Adaptados a Tablas Limpias)
-- Vehiculos por año (Simple, utiliza tablas validas)
CREATE OR REPLACE FUNCTION vehiculos_por_anio_fabricacion(p_anio INT)
RETURNS TABLE(
    patente VARCHAR,
    marca VARCHAR,
    modelo VARCHAR,
    anio_fabricacion INT
)
LANGUAGE sql
AS $$
    SELECT
        v.patente,
        m.nombreMarca,
        mv.modelo,
        v.anioVehiculo
    FROM Vehiculo v
    JOIN ModeloVehiculo mv ON v.IDModelo = mv.IDModelo
    JOIN Marca m ON mv.IDMarca = m.IDMarca
    WHERE v.anioVehiculo = p_anio;
$$;
-- Stock Bajo (Adaptado para usar PiezaExterior que es donde está el stock real)
CREATE OR REPLACE FUNCTION obtener_stock_bajo()
RETURNS TABLE (
    id_pieza INT,
    nombre_pieza VARCHAR,
    stock_actual INT,
    stock_minimo INT
)
AS $$
    SELECT 
        IDPiezaExterior,
        NombrePiezaExterior,
        stock_actual,
        stock_minimo
    FROM PiezaExterior
    WHERE stock_actual <= stock_minimo
    ORDER BY stock_actual ASC;
$$ LANGUAGE sql;
-- 5. AUDITORIA CLIENTES (Útil para seguridad)
CREATE TABLE IF NOT EXISTS AuditoriaCliente (
    operacion CHAR(1),
    fechaupd TIMESTAMP,
    usuario TEXT,
    runcliente VARCHAR,
    nombre TEXT,
    correo TEXT
);
CREATE OR REPLACE FUNCTION auditoria_cliente_func()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        INSERT INTO AuditoriaCliente
        VALUES('D', CURRENT_TIMESTAMP, current_user, OLD.RUNCliente,
               OLD.nombreCliente || ' ' || OLD.apellidoCliente, OLD.correo);
        RETURN OLD;
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO AuditoriaCliente
        VALUES('U', CURRENT_TIMESTAMP, current_user, NEW.RUNCliente,
               NEW.nombreCliente || ' ' || NEW.apellidoCliente, NEW.correo);
        RETURN NEW;
    ELSIF TG_OP = 'INSERT' THEN
        INSERT INTO AuditoriaCliente
        VALUES('I', CURRENT_TIMESTAMP, current_user, NEW.RUNCliente,
               NEW.nombreCliente || ' ' || NEW.apellidoCliente, NEW.correo);
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_auditoria_cliente
AFTER INSERT OR UPDATE OR DELETE ON cliente
FOR EACH ROW EXECUTE FUNCTION auditoria_cliente_func();