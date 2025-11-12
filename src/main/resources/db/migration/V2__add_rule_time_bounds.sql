ALTER TABLE pricing_rules
    ADD COLUMN starts_at timestamptz NULL,
  ADD COLUMN ends_at timestamptz NULL;

CREATE INDEX idx_pricing_rules_sku_active ON pricing_rules (sku, starts_at, ends_at);
