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

INSERT INTO prompt (id, name, prompt) VALUES
(1, 'Translation Prompt', ''),
(2, 'Change Notes Summary', 'You are an assistant for a release notes portal. Summarize the provided git diff(s) into a concise free text summary for end users. Include only user-facing changes (features, fixes, UI/UX changes, behavior changes). Exclude internal/refactoring/dev tooling/test/build/formatting changes unless they affect user behavior. Do not invent details. If information is unclear, omit it. Output only the summary as free text. No introduction, no conclusion, no headings, no extra commentary. Make no mistakes.')
ON CONFLICT (id) DO NOTHING;

-- Update auto-increment counter to avoid conflicts
SELECT setval('product_id_seq', (SELECT MAX(id) FROM product));
SELECT setval('feature_id_seq', (SELECT MAX(id) FROM feature));
SELECT setval('customer_id_seq', (SELECT MAX(id) FROM customer));
SELECT setval('scope_id_seq', (SELECT MAX(id) FROM scope));
