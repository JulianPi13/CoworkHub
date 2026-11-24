-- =====================================================================================
-- CoWorkHub - migration.sql
-- Script independiente (estructura + datos de prueba) para crear la base a mano:
--   mysql -u coworkhub -p coworkhub < migration.sql
-- La aplicación NO necesita este archivo: con ddl-auto=create Hibernate crea las tablas y ejecuta import.sql.
-- =====================================================================================

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `additional_services`;

CREATE TABLE `additional_services` (
  `unit_price` decimal(10,2) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKf7cdh07s4bs1qavhcdtiftbv4` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `booking_items`;

CREATE TABLE `booking_items` (
  `applied_unit_price` decimal(10,2) NOT NULL,
  `quantity` int NOT NULL,
  `booking_id` bigint NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_booking_item_service` (`booking_id`,`service_id`),
  KEY `FKq2m1rsv5kb7464rmpliamu490` (`service_id`),
  CONSTRAINT `FKq2m1rsv5kb7464rmpliamu490` FOREIGN KEY (`service_id`) REFERENCES `additional_services` (`id`),
  CONSTRAINT `FKrw74irmyat5c39cnjkn02u99m` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `bookings`;

CREATE TABLE `bookings` (
  `attendees` int NOT NULL,
  `cancellation_fee` decimal(12,2) DEFAULT NULL,
  `hours_refunded` bit(1) NOT NULL,
  `room_cost` decimal(12,2) NOT NULL,
  `services_cost` decimal(12,2) NOT NULL,
  `total_cost` decimal(12,2) NOT NULL,
  `end_time` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `start_time` datetime(6) NOT NULL,
  `status` enum('CANCELLED','COMPLETED','CONFIRMED','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_booking_room_time` (`room_id`,`start_time`,`end_time`),
  KEY `idx_booking_member` (`member_id`),
  CONSTRAINT `FK2hu43b2w26um1gql30wbf0173` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`),
  CONSTRAINT `FKrgoycol97o21kpjodw1qox4nc` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `equipment`;

CREATE TABLE `equipment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKm6whtgwe6ya84f59coio1j1wh` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `locations`;

CREATE TABLE `locations` (
  `closing_time` time NOT NULL,
  `opening_time` time NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `city` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `members`;

CREATE TABLE `members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `user_account_id` bigint DEFAULT NULL,
  `document_number` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `full_name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','SUSPENDED') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKd5c97lgxv17hn0p5c4ttbxxqk` (`document_number`),
  UNIQUE KEY `UK9d30a9u1qpg8eou0otgkwrp5d` (`email`),
  UNIQUE KEY `UK3v63j60dtktxj4q0darlbppg1` (`user_account_id`),
  KEY `FKjuwy40aicmdogitnkjj38s78k` (`plan_id`),
  CONSTRAINT `FKdkq38de7wa91vr0tkssv9m8d2` FOREIGN KEY (`user_account_id`) REFERENCES `user_accounts` (`id`),
  CONSTRAINT `FKjuwy40aicmdogitnkjj38s78k` FOREIGN KEY (`plan_id`) REFERENCES `membership_plans` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `membership_plans`;

CREATE TABLE `membership_plans` (
  `included_hours_per_month` int NOT NULL,
  `max_active_bookings` int NOT NULL,
  `overage_discount` decimal(5,2) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKnconflq2x1uvxvs7sc0mnmh4` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `room_equipment`;

CREATE TABLE `room_equipment` (
  `equipment_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`equipment_id`,`room_id`),
  KEY `FKiyhaivc7it7c8go6ursatnvte` (`room_id`),
  CONSTRAINT `FKiyhaivc7it7c8go6ursatnvte` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`),
  CONSTRAINT `FKrmdm2ihfq0rjieivqjmnty6hq` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `rooms`;

CREATE TABLE `rooms` (
  `active` bit(1) NOT NULL,
  `capacity` int NOT NULL,
  `hourly_rate` decimal(10,2) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `location_id` bigint NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('DESK','MEETING_ROOM','PRIVATE_OFFICE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_name_location` (`location_id`,`name`),
  CONSTRAINT `FKn44nnd0bt5f0gwkdtw5ub1esg` FOREIGN KEY (`location_id`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `user_accounts`;

CREATE TABLE `user_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ADMIN','MEMBER','RECEPTIONIST') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlxwlgwuy2yrbye2vgs9w9x7mr` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================== Datos de prueba ==============================

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
