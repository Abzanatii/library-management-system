CREATE INDEX idx_reservation_expires_at
ON reservations(expires_at)
WHERE status = 'READY';