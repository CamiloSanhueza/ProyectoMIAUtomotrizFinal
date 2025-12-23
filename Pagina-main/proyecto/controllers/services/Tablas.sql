CREATE TABLE Direccion (
    codigoPostal VARCHAR(10) PRIMARY KEY,
    calle VARCHAR(100) NOT NULL,
    numero INT NOT NULL,
    pasaje VARCHAR(100) NOT NULL,
    poblacion VARCHAR(100) NOT NULL,
    ciudad VARCHAR(50) NOT NULL,
    region VARCHAR(50) NOT NULL
);
CREATE TABLE Marca (
    IDMarca SERIAL PRIMARY KEY,
    nombreMarca VARCHAR(50) UNIQUE NOT NULL
);
CREATE TABLE ModeloVehiculo (
    IDModelo SERIAL PRIMARY KEY,
    modelo VARCHAR(50) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    transmision VARCHAR(50) NOT NULL,
    IDMarca INT NOT NULL,
    FOREIGN KEY (IDMarca) REFERENCES Marca(IDMarca) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE usuarios (
    IDUsuario SERIAL PRIMARY KEY,
    correo VARCHAR(100) UNIQUE NOT NULL,  
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL CHECK (rol IN ('Administrador', 'Mecanico')),
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    telefono VARCHAR(20),
    rut VARCHAR(12),
    fcm_token TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE cliente (
    RUNCliente VARCHAR(12) PRIMARY KEY,
    nombreCliente VARCHAR(50) NOT NULL,
    apellidoCliente VARCHAR(50) NOT NULL,
    fechaNacimientoCliente DATE,
    telefonoCliente VARCHAR(15),
    giro VARCHAR(100),
    correo VARCHAR(100) UNIQUE NOT NULL,
    codigoPostal VARCHAR(10) NOT NULL,
    FOREIGN KEY (codigoPostal) REFERENCES Direccion(codigoPostal) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE TABLE Vehiculo (
    patente VARCHAR(6) PRIMARY KEY,
    anioVehiculo INT NOT NULL CHECK (anioVehiculo BETWEEN 1995 AND EXTRACT(YEAR FROM CURRENT_DATE) + 1), 
    RUNCliente VARCHAR(12) NOT NULL,
    IDModelo INT NOT NULL,
    color VARCHAR(15) NOT NULL, 
    FOREIGN KEY (RUNCliente) REFERENCES cliente(RUNCliente) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (IDModelo) REFERENCES ModeloVehiculo(IDModelo) ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE TABLE Seguro (
    IDSeguro SERIAL PRIMARY KEY,
    EmpresaSeguro VARCHAR(100) NOT NULL,
    nombreSeguro VARCHAR(100) NOT NULL,
    numeroPoliza VARCHAR(50) UNIQUE NOT NULL CHECK (numeroPoliza ~ '^[A-Z0-9]{10,20}$'),
    correoSeguro VARCHAR(100) NOT NULL CHECK (correoSeguro ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'),
    TelefonoSeguro VARCHAR(20) NOT NULL CHECK (TelefonoSeguro ~ '^\+?[0-9]{9,15}$'),
    nombreEjecutivo VARCHAR(100) NOT NULL,
    TelefonoEjecutivo VARCHAR(20) NOT NULL CHECK (TelefonoEjecutivo ~ '^\+?[0-9]{9,15}$'),
    correoEjecutivo VARCHAR(100) NOT NULL CHECK (correoEjecutivo ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'),
    RUNCliente VARCHAR(12) NOT NULL,
    FOREIGN KEY (RUNCliente) REFERENCES cliente(RUNCliente) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE OrdenDeTrabajo (
    numeroOrdenDeTrabajo SERIAL PRIMARY KEY,
    fechaOrdenDeTrabajo DATE NOT NULL DEFAULT CURRENT_DATE CHECK (fechaOrdenDeTrabajo >= DATE '1995-01-01'),
    estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente' CHECK (estado IN ('Pendiente', 'En Proceso', 'Completada', 'Cancelada')),
    ObservacionesOrdenDeTrabajo TEXT,
    patente VARCHAR(6) NOT NULL,
    IDSeguro INT,
    IDMecanico INT,
    FOREIGN KEY (patente) REFERENCES Vehiculo(patente) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (IDSeguro) REFERENCES Seguro(IDSeguro) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (IDMecanico) REFERENCES usuarios(IDUsuario) ON DELETE SET NULL
);
CREATE TABLE MarcaPieza (
    IDMarcaPieza SERIAL PRIMARY KEY,
    nombreMarcaPieza VARCHAR(50) NOT NULL UNIQUE,
    anioFundacion SMALLINT NOT NULL CHECK (anioFundacion > 1800)
);
CREATE TABLE CategoriaPieza (
    IDCategoriaPieza SERIAL PRIMARY KEY,
    nombreCategoria VARCHAR(100) NOT NULL UNIQUE
);
CREATE TABLE PiezaExterior (
    IDPiezaExterior SERIAL PRIMARY KEY,
    NombrePiezaExterior VARCHAR(100) NOT NULL,
    descripcionPiezaExterior TEXT,
    precioCompraPiezaExterior DECIMAL(10,2) NOT NULL CHECK (precioCompraPiezaExterior >= 0),
    stock_actual INTEGER DEFAULT 0,
    stock_minimo INTEGER DEFAULT 5,
    precio_compra INTEGER DEFAULT 0,
    precio_venta INTEGER DEFAULT 0,
    IDMarcaPieza INT NOT NULL,
    IDCategoriaPieza INT NOT NULL,
    FOREIGN KEY (IDMarcaPieza) REFERENCES MarcaPieza(IDMarcaPieza) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (IDCategoriaPieza) REFERENCES CategoriaPieza(IDCategoriaPieza) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE Proveedores (
    id_proveedor SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    contacto_nombre VARCHAR(100),
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE OrdenesCompra (
    id_orden_compra SERIAL PRIMARY KEY,
    id_proveedor INTEGER REFERENCES Proveedores(id_proveedor),
    fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'Pendiente', 
    total INTEGER DEFAULT 0
);
CREATE TABLE DetalleOrdenCompra (
    id_detalle SERIAL PRIMARY KEY,
    id_orden_compra INTEGER REFERENCES OrdenesCompra(id_orden_compra) ON DELETE CASCADE,
    id_repuesto INTEGER REFERENCES PiezaExterior(IDPiezaExterior),
    cantidad INTEGER NOT NULL,
    precio_unitario INTEGER NOT NULL,
    subtotal INTEGER GENERATED ALWAYS AS (cantidad * precio_unitario) STORED
);

CREATE TABLE ManoDeObra (
    IDManoDeObra SERIAL PRIMARY KEY,
    nombreTarea VARCHAR(100) NOT NULL,
    precioManoDeObra INTEGER NOT NULL
);

CREATE TABLE TipoDano (
    TipoDano SERIAL PRIMARY KEY,
    DescripcionDano VARCHAR(100) NOT NULL
);

CREATE TABLE UbicacionDano (
    IDUbicacionDano SERIAL PRIMARY KEY,
    DescripcionUbicacion VARCHAR(100) NOT NULL
);