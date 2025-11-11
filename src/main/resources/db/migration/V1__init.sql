CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          sku VARCHAR(1) NOT NULL UNIQUE,
                          unit_price NUMERIC(12,2) NOT NULL
);

CREATE TABLE pricing_rules (
                               id BIGSERIAL PRIMARY KEY,
                               sku VARCHAR(1) NOT NULL,
                               rule_type VARCHAR(32) NOT NULL,
                               x_qty INT,
                               y_price NUMERIC(12,2),
                               CONSTRAINT uk_rule_sku_type UNIQUE (sku, rule_type)
);

INSERT INTO products (sku, unit_price) VALUES
                                           ('A', 50.00), ('B', 30.00), ('C', 20.00), ('D', 15.00)
    ON CONFLICT (sku) DO NOTHING;

INSERT INTO pricing_rules (sku, rule_type, x_qty, y_price) VALUES
                                                               ('A', 'BULK_X_FOR_Y', 3, 130.00),
                                                               ('B', 'BULK_X_FOR_Y', 2, 45.00)
    ON CONFLICT DO NOTHING;
