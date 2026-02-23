INSERT INTO product (id, name) VALUES
(1, 'WMS'), (2, 'TMS'), (3, 'OMS')
ON CONFLICT (id) DO NOTHING;

INSERT INTO feature (id, name) VALUES
(1, 'Invoicing'), (2, 'Picking')
ON CONFLICT (id) DO NOTHING;

INSERT INTO customer (id, name) VALUES
(1, 'Liddle'), (2, 'MaksiMatt'), (3, 'Whalemart'), (4, 'Coup')
ON CONFLICT (id) DO NOTHING;

INSERT INTO scope (id, name) VALUES
(1, 'Patch'), (2, 'Minor'), (3, 'Major')
ON CONFLICT (id) DO NOTHING;

-- Update auto-increment counter to avoid conflicts
SELECT setval('product_id_seq', (SELECT MAX(id) FROM product));
SELECT setval('feature_id_seq', (SELECT MAX(id) FROM feature));
SELECT setval('customer_id_seq', (SELECT MAX(id) FROM customer));
SELECT setval('scope_id_seq', (SELECT MAX(id) FROM scope));
