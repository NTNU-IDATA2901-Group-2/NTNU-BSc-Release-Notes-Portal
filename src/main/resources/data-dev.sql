-- AI generated sample data for change_note and release_note tables
INSERT INTO change_note (id, archived, published, reference, description, developer_notes, upgrade_notes, change_source, product_id, customer_id, feature_id, scope_id, timestamp) VALUES
(1, false, false, 'REF-001', 'Initial release', 'Dev notes 1', 'Upgrade notes 1', 'JIRA-11', 1, 1, 1, 1, 1706745600000),
(2, false, false, 'REF-002', 'Added new feature X', 'Dev notes 2', 'Upgrade notes 2', 'JIRA-12', 2, 2, 2, 2, 1708300200000),
(3, false, false, 'REF-003', 'Fixed bug Y', 'Dev notes 3', 'Upgrade notes 3', 'JIRA-13', 3, 3, 2, 3, 1710063900000),
(4, false, false, 'REF-004', 'Added new feature Z', 'Dev notes 4', 'Upgrade notes 4', 'JIRA-14', 1, 1, 1, 1, 1711728500000),
(5, false, false, 'REF-005', 'Performance improvements', 'Dev notes 5', 'Upgrade notes 5', 'JIRA-15', 2, 2, 2, 2, 1713393100000),
(6, false, false, 'REF-006', 'Security patch', 'Dev notes 6', 'Upgrade notes 6', 'JIRA-16', 3, 3, 1, 3, 1715057700000),
(7, false, false, 'REF-007', 'Added support for new platform', 'Dev notes 7', 'Upgrade notes 7', 'JIRA-17', 1, 1, 1, 1, 1716722300000),
(8, false, false, 'REF-008', 'Deprecated old API', 'Dev notes 8', 'Upgrade notes 8', 'JIRA-18', 2, 2, 2, 2, 1718386900000),
(9, false, false, 'REF-009', 'Added new feature A', 'Dev notes 9', 'Upgrade notes 9', 'JIRA-19', 3, 3, 2, 3, 1720051500000),
(10, false, false, 'REF-010', 'Fixed critical bug B', 'Dev notes 10', 'Upgrade notes 10', 'JIRA-20', 1, 1, 1, 1, 1721716100000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO release_note (id, archived, published, created_at, summary, tag) VALUES
(1, false, false, 1706745600000, 'Initial release', 'v1.0.0'),
(2, false, false, 1708300200000, 'Added new feature X', 'v1.1.0'),
(3, false, false, 1710063900000, 'Fixed bug Y', 'v1.1.1'),
(4, false, false, 1711728500000, 'Added new feature Z', 'v1.2.0'),
(5, false, false, 1713393100000, 'Performance improvements', 'v1.2.1'),
(6, false, false, 1715057700000, 'Security patch', 'v1.2.2'),
(7, false, false, 1716722300000, 'Added support for new platform', 'v1.3.0'),
(8, false, false, 1718386900000, 'Deprecated old API', 'v1.3.1'),
(9, false, false, 1720051500000, 'Added new feature A', 'v1.4.0'),
(10, false, false, 1721716100000, 'Fixed critical bug B', 'v1.4.1'),
(11, false, false, 1723380700000, 'Improved dashboard filtering', 'v1.5.0'),
(12, false, false, 1725045300000, 'Bulk edit for change notes', 'v1.5.1'),
(13, false, false, 1726709900000, 'Refined release note export', 'v1.5.2'),
(14, false, false, 1728374500000, 'Added audit log metadata', 'v1.6.0'),
(15, false, false, 1730039100000, 'Improved API pagination', 'v1.6.1'),
(16, false, false, 1731703700000, 'Reduced page load time', 'v1.6.2'),
(17, false, false, 1733368300000, 'New role-based access checks', 'v1.7.0'),
(18, false, false, 1735032900000, 'Enhanced search relevance', 'v1.7.1'),
(19, false, false, 1736697500000, 'Improved sorting behavior', 'v1.7.2'),
(20, false, false, 1738362100000, 'Added release comparison view', 'v1.8.0'),
(21, false, false, 1740026700000, 'Smarter empty-state messages', 'v1.8.1'),
(22, false, false, 1741691300000, 'Improved keyboard navigation', 'v1.8.2'),
(23, false, false, 1743355900000, 'Stabilized backend validation', 'v1.9.0'),
(24, false, false, 1745020500000, 'Improved API error responses', 'v1.9.1'),
(25, false, false, 1746685100000, 'Added customer-level filters', 'v1.9.2'),
(26, false, false, 1748349700000, 'Faster release list rendering', 'v2.0.0'),
(27, false, false, 1750014300000, 'Improved dark mode contrast', 'v2.0.1'),
(28, false, false, 1751678900000, 'Added archive management tools', 'v2.0.2'),
(29, false, false, 1753343500000, 'Refined tag editing UX', 'v2.1.0'),
(30, false, false, 1755008100000, 'Improved localization support', 'v2.1.1'),
(31, false, false, 1756672700000, 'Added release duplication flow', 'v2.1.2'),
(32, false, false, 1758337300000, 'Improved loading state handling', 'v2.2.0'),
(33, false, false, 1760001900000, 'More resilient API retries', 'v2.2.1'),
(34, false, false, 1761666500000, 'Extended table column options', 'v2.2.2'),
(35, false, false, 1763331100000, 'Added quick-jump navigation', 'v2.3.0'),
(36, false, false, 1764995700000, 'Improved notification messages', 'v2.3.1'),
(37, false, false, 1766660300000, 'Hardened security headers', 'v2.3.2'),
(38, false, false, 1768324900000, 'Better empty release handling', 'v2.4.0'),
(39, false, false, 1769989500000, 'Improved release detail layout', 'v2.4.1'),
(40, false, false, 1771654100000, 'Optimized cache invalidation', 'v2.4.2')
ON CONFLICT (id) DO NOTHING;


-- Update auto-increment counter to avoid conflicts
SELECT setval('change_note_id_seq', (SELECT MAX(id) FROM change_note));
SELECT setval('release_note_id_seq', (SELECT MAX(id) FROM release_note));