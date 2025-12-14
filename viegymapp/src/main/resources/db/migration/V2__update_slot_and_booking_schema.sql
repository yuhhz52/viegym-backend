-- Migration to update slot and booking schema for new booking flow
-- Add capacity and booked_count to coach_time_slots
-- Add expired_at to booking_sessions
-- Update slot status enum values

-- Add capacity and booked_count to coach_time_slots
ALTER TABLE coach_time_slots 
ADD COLUMN IF NOT EXISTS capacity INTEGER NOT NULL DEFAULT 1,
ADD COLUMN IF NOT EXISTS booked_count INTEGER NOT NULL DEFAULT 0;

-- Update existing slots to have capacity = 1
UPDATE coach_time_slots SET capacity = 1 WHERE capacity IS NULL;
UPDATE coach_time_slots SET booked_count = 0 WHERE booked_count IS NULL;

-- Add expired_at to booking_sessions
ALTER TABLE booking_sessions 
ADD COLUMN IF NOT EXISTS expired_at TIMESTAMP;

-- Update slot status: change 'BOOKED' to 'FULL' where applicable
-- Note: We'll handle status migration in application code
-- For now, just ensure the enum values are correct

-- Create index for expired bookings query
CREATE INDEX IF NOT EXISTS idx_booking_status_expired_at 
ON booking_sessions(status, expired_at) 
WHERE status = 'PENDING' AND deleted = false;

