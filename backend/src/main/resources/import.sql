-- =====================================================================================
-- CoWorkHub - datos de prueba. Hibernate ejecuta este archivo al arrancar con ddl-auto=create.
-- Cada sentencia en una sola línea. Las fechas de las reservas se calculan a partir de hoy.
-- Contraseñas (BCrypt): admin/Admin123, recepcion/Recep123, ana/Ana12345, luis/Luis12345
-- =====================================================================================

-- Planes de membresía
INSERT INTO membership_plans (id, name, included_hours_per_month, overage_discount, max_active_bookings) VALUES (1, 'Plan Básico', 10, 10.00, 3);
INSERT INTO membership_plans (id, name, included_hours_per_month, overage_discount, max_active_bookings) VALUES (2, 'Plan Profesional', 30, 20.00, 5);
INSERT INTO membership_plans (id, name, included_hours_per_month, overage_discount, max_active_bookings) VALUES (3, 'Plan Corporativo', 80, 30.00, 50);
INSERT INTO membership_plans (id, name, included_hours_per_month, overage_discount, max_active_bookings) VALUES (4, 'Plan Por Horas', 0, 0.00, 100);

-- Cuentas de usuario: 1 ADMIN, 1 RECEPTIONIST y 2 MEMBER
INSERT INTO user_accounts (id, username, password, role) VALUES (1, 'admin', '$2a$10$iN2w3wLBEyx8XbaKsZUwNO.pP0DPf0m2ogdyUGPMOTWtHoBEW5mla', 'ADMIN');
INSERT INTO user_accounts (id, username, password, role) VALUES (2, 'recepcion', '$2a$10$JUNFWwsaHoNp/UAbYtu9S.MM2cJkWQLbLFtQmJbGXz7P21JCIOpaO', 'RECEPTIONIST');
INSERT INTO user_accounts (id, username, password, role) VALUES (3, 'ana', '$2a$10$gJK5V1dfrMk4H5AVq25X5.WZPkL8lpOigKEXhWYPCz3jsptG1QdM.', 'MEMBER');
INSERT INTO user_accounts (id, username, password, role) VALUES (4, 'luis', '$2a$10$FjOW3JvXn5xQCYRuaxxRFehqwTPCvY3biv7isKaH2zg/0BD4r7fKm', 'MEMBER');

-- Miembros (Ana y Luis tienen cuenta; los demás no inician sesión). María está suspendida.
INSERT INTO members (id, document_number, full_name, email, status, plan_id, user_account_id) VALUES (1, '1098765432', 'Ana Gómez', 'ana.gomez@correo.com', 'ACTIVE', 1, 3);
INSERT INTO members (id, document_number, full_name, email, status, plan_id, user_account_id) VALUES (2, '1095123456', 'Luis Pérez', 'luis.perez@correo.com', 'ACTIVE', 2, 4);
INSERT INTO members (id, document_number, full_name, email, status, plan_id, user_account_id) VALUES (3, '91234567', 'Carlos Ruiz', 'carlos.ruiz@correo.com', 'ACTIVE', 3, NULL);
INSERT INTO members (id, document_number, full_name, email, status, plan_id, user_account_id) VALUES (4, '63456789', 'María Torres', 'maria.torres@correo.com', 'SUSPENDED', 1, NULL);
INSERT INTO members (id, document_number, full_name, email, status, plan_id, user_account_id) VALUES (5, '1102345678', 'Jorge Díaz', 'jorge.diaz@correo.com', 'ACTIVE', 2, NULL);
INSERT INTO members (id, document_number, full_name, email, status, plan_id, user_account_id) VALUES (6, '900123456', 'Startup Demo S.A.S.', 'demo@startup.com', 'ACTIVE', 4, NULL);

-- Sedes
INSERT INTO locations (id, name, city, address, opening_time, closing_time) VALUES (1, 'CoWork Centro', 'Bucaramanga', 'Calle 36 # 15-20', '07:00:00', '20:00:00');
INSERT INTO locations (id, name, city, address, opening_time, closing_time) VALUES (2, 'CoWork Cabecera 24h', 'Bucaramanga', 'Carrera 33 # 48-10', '00:00:00', '23:59:00');

-- Equipamiento
INSERT INTO equipment (id, name) VALUES (1, 'Proyector');
INSERT INTO equipment (id, name) VALUES (2, 'Pizarra');
INSERT INTO equipment (id, name) VALUES (3, 'Videoconferencia');
INSERT INTO equipment (id, name) VALUES (4, 'Televisor');

-- Salas (la 4 está inactiva). La sala 5 queda libre para las pruebas de Postman.
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (1, 'Sala Andes', 'MEETING_ROOM', 8, 40000.00, true, 1);
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (2, 'Oficina Chicamocha', 'PRIVATE_OFFICE', 4, 60000.00, true, 1);
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (3, 'Puesto Flex 1', 'DESK', 1, 10000.00, true, 1);
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (4, 'Sala Antigua', 'MEETING_ROOM', 6, 30000.00, false, 1);
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (5, 'Sala Santurbán', 'MEETING_ROOM', 12, 50000.00, true, 2);
INSERT INTO rooms (id, name, type, capacity, hourly_rate, active, location_id) VALUES (6, 'Oficina Guane', 'PRIVATE_OFFICE', 3, 45000.00, true, 2);

-- Equipamiento de cada sala (tabla intermedia N ↔ N)
INSERT INTO room_equipment (room_id, equipment_id) VALUES (1, 1);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (1, 2);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (1, 3);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (2, 2);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (2, 4);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (4, 2);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (5, 1);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (5, 3);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (5, 4);
INSERT INTO room_equipment (room_id, equipment_id) VALUES (6, 2);

-- Servicios adicionales
INSERT INTO additional_services (id, name, unit_price) VALUES (1, 'Café ilimitado', 8000.00);
INSERT INTO additional_services (id, name, unit_price) VALUES (2, 'Refrigerio', 15000.00);
INSERT INTO additional_services (id, name, unit_price) VALUES (3, 'Paquete de impresiones', 5000.00);

-- Reservas (15) en distintos estados. Futuras: CURDATE() + INTERVAL n DAY; pasadas: CURDATE() - INTERVAL n DAY.
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (1, TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '09:00:00'), TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '11:00:00'), 4, 'PENDING', 0.00, 32000.00, 32000.00, NULL, false, 1, 1);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (2, TIMESTAMP(CURDATE() + INTERVAL 3 DAY, '14:00:00'), TIMESTAMP(CURDATE() + INTERVAL 3 DAY, '16:00:00'), 2, 'CONFIRMED', 0.00, 0.00, 0.00, NULL, false, 2, 1);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (3, TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '14:00:00'), TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '17:00:00'), 6, 'CONFIRMED', 0.00, 90000.00, 90000.00, NULL, false, 1, 2);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (4, TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '08:00:00'), TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '12:00:00'), 1, 'PENDING', 0.00, 0.00, 0.00, NULL, false, 3, 2);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (5, TIMESTAMP(CURDATE() + INTERVAL 4 DAY, '10:00:00'), TIMESTAMP(CURDATE() + INTERVAL 4 DAY, '12:00:00'), 3, 'PENDING', 0.00, 0.00, 0.00, NULL, false, 6, 2);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (6, TIMESTAMP(CURDATE() + INTERVAL 5 DAY, '10:00:00'), TIMESTAMP(CURDATE() + INTERVAL 5 DAY, '12:00:00'), 8, 'CONFIRMED', 0.00, 74000.00, 74000.00, NULL, false, 1, 3);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (7, TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '09:00:00'), TIMESTAMP(CURDATE() + INTERVAL 2 DAY, '10:30:00'), 2, 'PENDING', 0.00, 0.00, 0.00, NULL, false, 2, 5);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (8, TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '09:00:00'), TIMESTAMP(CURDATE() - INTERVAL 2 DAY, '11:00:00'), 3, 'COMPLETED', 0.00, 0.00, 0.00, NULL, false, 1, 1);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (9, TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '13:00:00'), TIMESTAMP(CURDATE() - INTERVAL 3 DAY, '15:00:00'), 2, 'COMPLETED', 0.00, 0.00, 0.00, NULL, false, 2, 2);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (10, TIMESTAMP(CURDATE() - INTERVAL 5 DAY, '08:00:00'), TIMESTAMP(CURDATE() - INTERVAL 5 DAY, '16:00:00'), 1, 'COMPLETED', 0.00, 8000.00, 8000.00, NULL, false, 3, 3);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (11, TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '15:00:00'), TIMESTAMP(CURDATE() - INTERVAL 1 DAY, '17:00:00'), 3, 'COMPLETED', 0.00, 0.00, 0.00, NULL, false, 6, 5);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (12, TIMESTAMP(CURDATE() - INTERVAL 7 DAY, '10:00:00'), TIMESTAMP(CURDATE() - INTERVAL 7 DAY, '11:00:00'), 2, 'CANCELLED', 0.00, 0.00, 0.00, 0.00, true, 1, 4);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (13, TIMESTAMP(CURDATE() + INTERVAL 6 DAY, '10:00:00'), TIMESTAMP(CURDATE() + INTERVAL 6 DAY, '11:00:00'), 2, 'CANCELLED', 0.00, 0.00, 0.00, 0.00, true, 2, 3);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (14, TIMESTAMP(CURDATE() - INTERVAL 4 DAY, '16:00:00'), TIMESTAMP(CURDATE() - INTERVAL 4 DAY, '18:00:00'), 4, 'CANCELLED', 0.00, 0.00, 0.00, 0.00, false, 1, 5);
INSERT INTO bookings (id, start_time, end_time, attendees, status, room_cost, services_cost, total_cost, cancellation_fee, hours_refunded, room_id, member_id) VALUES (15, TIMESTAMP(CURDATE() + INTERVAL 7 DAY, '08:00:00'), TIMESTAMP(CURDATE() + INTERVAL 7 DAY, '09:00:00'), 2, 'CONFIRMED', 0.00, 0.00, 0.00, NULL, false, 6, 3);

-- Servicios de algunas reservas (applied_unit_price = precio vigente al agregarlo)
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (1, 4, 8000.00, 1, 1);
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (2, 6, 15000.00, 3, 2);
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (3, 8, 8000.00, 6, 1);
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (4, 2, 5000.00, 6, 3);
INSERT INTO booking_items (id, quantity, applied_unit_price, booking_id, service_id) VALUES (5, 1, 8000.00, 10, 1);
