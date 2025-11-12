UPDATE pricing_rules
SET starts_at = now() AT TIME ZONE 'UTC',
    ends_at = NULL
WHERE starts_at IS NULL AND ends_at IS NULL;
