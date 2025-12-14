-- Migration to make booking_session_id nullable in payments table
-- This allows slot payments to be created without a booking session initially
-- Booking session will be created after successful payment

ALTER TABLE payments 
ALTER COLUMN booking_session_id DROP NOT NULL;
