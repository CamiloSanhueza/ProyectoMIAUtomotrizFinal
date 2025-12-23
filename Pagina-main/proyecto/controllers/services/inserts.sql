-- MarcaPieza
INSERT INTO MarcaPieza (nombreMarcaPieza, anioFundacion) VALUES ('Bosch', 1970);
INSERT INTO MarcaPieza (nombreMarcaPieza, anioFundacion) VALUES ('Denso', 1925);
INSERT INTO MarcaPieza (nombreMarcaPieza, anioFundacion) VALUES ('Valeo', 1922);
INSERT INTO MarcaPieza (nombreMarcaPieza, anioFundacion) VALUES ('Delphi', 1906);
INSERT INTO MarcaPieza (nombreMarcaPieza, anioFundacion) VALUES ('Mann', 1919);
INSERT INTO MarcaPieza (nombreMarcaPieza, anioFundacion) VALUES ('Mahle', 1989);
INSERT INTO MarcaPieza (nombreMarcaPieza, anioFundacion) VALUES ('NGK', 1915);
INSERT INTO MarcaPieza (nombreMarcaPieza, anioFundacion) VALUES ('Brembo', 1971);
INSERT INTO MarcaPieza (nombreMarcaPieza, anioFundacion) VALUES ('SKF', 1919);
INSERT INTO MarcaPieza (nombreMarcaPieza, anioFundacion) VALUES ('Monroe', 1925);

-- CategoriaPieza
INSERT INTO CategoriaPieza (nombreCategoria) VALUES ('Motor');
INSERT INTO CategoriaPieza (nombreCategoria) VALUES ('Frenos');
INSERT INTO CategoriaPieza (nombreCategoria) VALUES ('Suspensión');
INSERT INTO CategoriaPieza (nombreCategoria) VALUES ('Eléctrico');
INSERT INTO CategoriaPieza (nombreCategoria) VALUES ('Carrocería');
INSERT INTO CategoriaPieza (nombreCategoria) VALUES ('Interior');
INSERT INTO CategoriaPieza (nombreCategoria) VALUES ('Transmisión');
INSERT INTO CategoriaPieza (nombreCategoria) VALUES ('Dirección');

-- PiezaExterior (Repuestos)
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Bandeja Suspensión Modelo A', 'Repuesto para Bandeja Suspensión Modelo A', 77855, 68, 5, 77855, 108997, 3, 3);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Disco Freno Ventilado Genérico', 'Repuesto para Disco Freno Ventilado Genérico', 47397, 100, 5, 47397, 66355, 1, 2);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Filtro Aire Genérico', 'Repuesto para Filtro Aire Genérico', 124747, 61, 5, 124747, 174645, 8, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Amortiguador Delantero Modelo A', 'Repuesto para Amortiguador Delantero Modelo A', 120286, 68, 5, 120286, 168400, 10, 3);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Terminal Dirección Genérico', 'Repuesto para Terminal Dirección Genérico', 96716, 13, 5, 96716, 135402, 7, 8);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Bomba Agua Modelo A', 'Repuesto para Bomba Agua Modelo A', 11894, 53, 5, 11894, 16651, 8, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Radiador Modelo A', 'Repuesto para Radiador Modelo A', 36628, 89, 5, 36628, 51279, 2, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Amortiguador Delantero Genérico', 'Repuesto para Amortiguador Delantero Genérico', 138426, 63, 5, 138426, 193796, 9, 3);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Alternador Modelo B', 'Repuesto para Alternador Modelo B', 113405, 34, 5, 113405, 158767, 8, 4);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Amortiguador Delantero Pro', 'Repuesto para Amortiguador Delantero Pro', 24009, 31, 5, 24009, 33612, 2, 3);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Batería 55Ah X', 'Repuesto para Batería 55Ah X', 59272, 36, 5, 59272, 82980, 9, 4);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Bujía Iridium Genérico', 'Repuesto para Bujía Iridium Genérico', 76496, 93, 5, 76496, 107094, 8, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Batería 55Ah Modelo A', 'Repuesto para Batería 55Ah Modelo A', 95338, 23, 5, 95338, 133473, 4, 4);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Disco Freno Ventilado X', 'Repuesto para Disco Freno Ventilado X', 114096, 14, 5, 114096, 159734, 5, 2);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Alternador X', 'Repuesto para Alternador X', 29620, 13, 5, 29620, 41468, 6, 4);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Compresor A/C Modelo A', 'Repuesto para Compresor A/C Modelo A', 124687, 52, 5, 124687, 174561, 5, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Terminal Dirección Pro', 'Repuesto para Terminal Dirección Pro', 92177, 30, 5, 92177, 129047, 7, 8);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Faro Delantero Genérico', 'Repuesto para Faro Delantero Genérico', 103749, 31, 5, 103749, 145248, 4, 5);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Bandeja Suspensión Modelo B', 'Repuesto para Bandeja Suspensión Modelo B', 87529, 62, 5, 87529, 122540, 9, 3);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Faro Delantero X', 'Repuesto para Faro Delantero X', 13533, 76, 5, 13533, 18946, 7, 5);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Amortiguador Delantero Pro', 'Repuesto para Amortiguador Delantero Pro', 122564, 45, 5, 122564, 171589, 7, 3);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Filtro Aire Pro', 'Repuesto para Filtro Aire Pro', 34996, 28, 5, 34996, 48994, 2, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Filtro de Aceite Modelo B', 'Repuesto para Filtro de Aceite Modelo B', 13972, 90, 5, 13972, 19560, 8, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Correa Distribución Modelo A', 'Repuesto para Correa Distribución Modelo A', 48639, 40, 5, 48639, 68094, 5, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Disco Freno Ventilado Genérico', 'Repuesto para Disco Freno Ventilado Genérico', 8683, 66, 5, 8683, 12156, 10, 2);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Filtro Aire Modelo B', 'Repuesto para Filtro Aire Modelo B', 89622, 48, 5, 89622, 125470, 10, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Filtro Aire Modelo B', 'Repuesto para Filtro Aire Modelo B', 66046, 25, 5, 66046, 92464, 1, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Sensor Oxígeno X', 'Repuesto para Sensor Oxígeno X', 13850, 60, 5, 13850, 19390, 9, 4);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Bomba Agua Genérico', 'Repuesto para Bomba Agua Genérico', 62985, 36, 5, 62985, 88179, 9, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Bandeja Suspensión Pro', 'Repuesto para Bandeja Suspensión Pro', 15432, 56, 5, 15432, 21604, 2, 3);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Espejo Retrovisor Genérico', 'Repuesto para Espejo Retrovisor Genérico', 144796, 14, 5, 144796, 202714, 4, 5);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Compresor A/C Modelo B', 'Repuesto para Compresor A/C Modelo B', 107060, 8, 5, 107060, 149884, 1, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Correa Distribución Modelo A', 'Repuesto para Correa Distribución Modelo A', 38770, 87, 5, 38770, 54278, 4, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Pastillas de Freno Modelo B', 'Repuesto para Pastillas de Freno Modelo B', 146014, 52, 5, 146014, 204419, 9, 2);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Espejo Retrovisor Modelo A', 'Repuesto para Espejo Retrovisor Modelo A', 94213, 19, 5, 94213, 131898, 3, 5);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Compresor A/C X', 'Repuesto para Compresor A/C X', 49060, 85, 5, 49060, 68684, 2, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Bomba Agua Genérico', 'Repuesto para Bomba Agua Genérico', 22584, 24, 5, 22584, 31617, 6, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Correa Distribución Pro', 'Repuesto para Correa Distribución Pro', 38908, 7, 5, 38908, 54471, 9, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Bobina Encendido Modelo A', 'Repuesto para Bobina Encendido Modelo A', 83904, 92, 5, 83904, 117465, 9, 4);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Correa Distribución Pro', 'Repuesto para Correa Distribución Pro', 147073, 38, 5, 147073, 205902, 2, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Espejo Retrovisor Modelo A', 'Repuesto para Espejo Retrovisor Modelo A', 27175, 5, 5, 27175, 38045, 6, 5);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Radiador Modelo A', 'Repuesto para Radiador Modelo A', 19236, 10, 5, 19236, 26930, 6, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Bomba Agua Modelo B', 'Repuesto para Bomba Agua Modelo B', 141404, 39, 5, 141404, 197965, 9, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Bobina Encendido Genérico', 'Repuesto para Bobina Encendido Genérico', 127070, 37, 5, 127070, 177898, 4, 4);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Parachoques X', 'Repuesto para Parachoques X', 139113, 80, 5, 139113, 194758, 9, 5);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Filtro de Aceite X', 'Repuesto para Filtro de Aceite X', 84798, 47, 5, 84798, 118717, 10, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Espejo Retrovisor X', 'Repuesto para Espejo Retrovisor X', 28222, 25, 5, 28222, 39510, 3, 5);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Compresor A/C Pro', 'Repuesto para Compresor A/C Pro', 58660, 31, 5, 58660, 82124, 4, 1);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Faro Delantero Modelo A', 'Repuesto para Faro Delantero Modelo A', 149712, 39, 5, 149712, 209596, 1, 5);
INSERT INTO PiezaExterior (NombrePiezaExterior, descripcionPiezaExterior, precioCompraPiezaExterior, stock_actual, stock_minimo, precio_compra, precio_venta, IDMarcaPieza, IDCategoriaPieza) VALUES ('Espejo Retrovisor X', 'Repuesto para Espejo Retrovisor X', 82572, 12, 5, 82572, 115600, 9, 5);

-- ManoDeObra
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Cambio de Aceite', 25000);
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Afinamiento Motor', 80000);
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Cambio Pastillas Freno', 35000);
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Alineación y Balanceo', 20000);
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Diagnóstico Scanner', 15000);
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Cambio Neumáticos', 10000);
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Revisión Frenos', 15000);
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Cambio Batería', 10000);
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Limpieza Inyectores', 45000);
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Cambio Distribución', 120000);
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Cambio Embrague', 150000);
INSERT INTO ManoDeObra (nombreTarea, precioManoDeObra) VALUES ('Reparación Culata', 250000);

-- TipoDano
INSERT INTO TipoDano (DescripcionDano) VALUES ('Rayón Profundo');
INSERT INTO TipoDano (DescripcionDano) VALUES ('Abolladura');
INSERT INTO TipoDano (DescripcionDano) VALUES ('Rotura de Cristal');
INSERT INTO TipoDano (DescripcionDano) VALUES ('Despintado');
INSERT INTO TipoDano (DescripcionDano) VALUES ('Golpe Carrocería');
INSERT INTO TipoDano (DescripcionDano) VALUES ('Faro Roto');
INSERT INTO TipoDano (DescripcionDano) VALUES ('Espejo Roto');
INSERT INTO TipoDano (DescripcionDano) VALUES ('Parachoques Desencajado');
INSERT INTO TipoDano (DescripcionDano) VALUES ('Fuga de Líquido');
INSERT INTO TipoDano (DescripcionDano) VALUES ('Ruido Anormal');

-- UbicacionDano
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Puerta Delantera Izq');
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Puerta Delantera Der');
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Puerta Trasera Izq');
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Puerta Trasera Der');
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Capó');
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Techo');
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Maletero');
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Parachoques Delantero');
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Parachoques Trasero');
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Aleta Delantera Izq');
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Aleta Delantera Der');
INSERT INTO UbicacionDano (DescripcionUbicacion) VALUES ('Llantas');
