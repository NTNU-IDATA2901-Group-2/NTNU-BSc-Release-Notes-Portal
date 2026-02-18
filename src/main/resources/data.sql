INSERT INTO product (name) VALUES ('WMS'), ('TMS'), ('OMS');
INSERT INTO feature (name) VALUES ('Invoicing'), ('Picking');
INSERT INTO customer (name) VALUES ('Liddle'),('MaksiMatt'), ('Whalemart'), ('Coup');
INSERT INTO scope (name) VALUES ('Patch'), ('Minor'), ('Major');

INSERT INTO change_note (archived, published, reference, description, developer_notes, upgrade_notes, change_source, product_id, customer_id, feature_id, scope_id, timestamp) VALUES
(false, false, 'REF-001', 'Initial release', 'Dev notes 1', 'Upgrade notes 1', 'JIRA-11', 1, 1, 1, 1, 1706745600000),
(false, false, 'REF-002', 'Added new feature X', 'Dev notes 2', 'Upgrade notes 2', 'JIRA-12', 2, 2, 2, 2, 1708300200000),
(false, false, 'REF-003', 'Fixed bug Y', 'Dev notes 3', 'Upgrade notes 3', 'JIRA-13', 3, 3, 2, 3, 1710063900000),
(false, false, 'REF-004', 'Added new feature Z', 'Dev notes 4', 'Upgrade notes 4', 'JIRA-14', 1, 1, 1, 1, 1711728500000),
(false, false, 'REF-005', 'Performance improvements', 'Dev notes 5', 'Upgrade notes 5', 'JIRA-15', 2, 2, 2, 2, 1713393100000),
(false, false, 'REF-006', 'Security patch', 'Dev notes 6', 'Upgrade notes 6', 'JIRA-16', 3, 3, 1, 3, 1715057700000),
(false, false, 'REF-007', 'Added support for new platform', 'Dev notes 7', 'Upgrade notes 7', 'JIRA-17', 1, 1, 1, 1, 1716722300000),
(false, false, 'REF-008', 'Deprecated old API', 'Dev notes 8', 'Upgrade notes 8', 'JIRA-18', 2, 2, 2, 2, 1718386900000),
(false, false, 'REF-009', 'Added new feature A', 'Dev notes 9', 'Upgrade notes 9', 'JIRA-19', 3, 3, 2, 3, 1720051500000),
(false, false, 'REF-010', 'Fixed critical bug B', 'Dev notes 10', 'Upgrade notes 10', 'JIRA-20', 1, 1, 1, 1, 1721716100000); 

INSERT INTO release_note (archived, published, created_at, summary, tag) VALUES
(false, false, 1706745600000, 'Initial release', 'v1.0.0'),
(false, false, 1708300200000, 'Added new feature X', 'v1.1.0'),
(false, false, 1710063900000, 'Fixed bug Y', 'v1.1.1'),
(false, false, 1711728500000, 'Added new feature Z', 'v1.2.0'),
(false, false, 1713393100000, 'Performance improvements', 'v1.2.1'),
(false, false, 1715057700000, 'Security patch', 'v1.2.2'),
(false, false, 1716722300000, 'Added support for new platform', 'v1.3.0'),
(false, false, 1718386900000, 'Deprecated old API', 'v1.3.1'),
(false, false, 1720051500000, 'Added new feature A', 'v1.4.0'),
(false, false, 1721716100000, 'Fixed critical bug B', 'v1.4.1')