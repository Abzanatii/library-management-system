CREATE INDEX idx_borrowing_overdue
ON borrowings((CAST(due_date AS date) - CAST(borrowed_At AS date)))
WHERE status = 'ACTIVE';