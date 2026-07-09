INSERT INTO product (id, name) VALUES
(1, 'WMS'), (2, 'TMS'), (3, 'OMS')
ON CONFLICT (id) DO NOTHING;

INSERT INTO feature (id, name) VALUES
(1, 'Invoicing'), (2, 'Picking'), (3, 'Putaway'), (4, 'Receiving'), (5, 'Cycle Counting'),
(6, 'Replenishment'), (7, 'Route Planning'), (8, 'Carrier Management'), (9, 'Freight Tracking'),
(10, 'Load Optimization'), (11, 'Order Fulfillment'), (12, 'Returns Management'), (13, 'Backorder Handling')
ON CONFLICT (id) DO NOTHING;

INSERT INTO customer (id, name) VALUES
(1, 'Liddle'), (2, 'MaksiMatt'), (3, 'Whalemart'), (4, 'Coup')
ON CONFLICT (id) DO NOTHING;

SELECT setval('product_id_seq', (SELECT MAX(id) FROM product));
SELECT setval('feature_id_seq', (SELECT MAX(id) FROM feature));
SELECT setval('customer_id_seq', (SELECT MAX(id) FROM customer));

-- AI generated sample data for change_note and release_note tables
INSERT INTO change_note (id, archived, published, title, reference, description, developer_notes, upgrade_notes, product_id, customer_id, feature_id, scope_id, creation_timestamp, viewable_by_everyone) VALUES
(1, false, true, 'Initial Release', 'REF-001', 'Initial release', 'Dev notes 1', 'Upgrade notes 1', 1, 1, 1, 1, to_timestamp(1706745600), false),
(2, false, false, 'Introducing Feature X', 'REF-002', 'Added new feature X', 'Dev notes 2', 'Upgrade notes 2', 2, 2, 2, 2, to_timestamp(1708300200), false),
(3, false, true, 'Bug Y Resolved', 'REF-003', 'Fixed bug Y', 'Dev notes 3', 'Upgrade notes 3', 3, 3, 2, 3, to_timestamp(1710063900), false),
(4, false, true, 'Feature Z Now Available', 'REF-004', 'Added new feature Z', 'Dev notes 4', 'Upgrade notes 4', 1, 1, 1, 1, to_timestamp(1711728500), false),
(5, false, true, 'Faster and Smoother', 'REF-005', 'Performance improvements', 'Dev notes 5', 'Upgrade notes 5', 2, 2, 2, 2, to_timestamp(1713393100), false),
(6, false, true, 'Critical Security Patch', 'REF-006', 'Security patch', 'Dev notes 6', 'Upgrade notes 6', 3, 3, 1, 3, to_timestamp(1715057700), false),
(7, false, true, 'New Platform Support', 'REF-007', 'Added support for new platform', 'Dev notes 7', 'Upgrade notes 7', 1, 1, 1, 1, to_timestamp(1716722300), false),
(8, false, false, 'Old API Deprecated', 'REF-008', 'Deprecated old API', 'Dev notes 8', 'Upgrade notes 8', 2, 2, 2, 2, to_timestamp(1718386900), false),
(9, false, false, 'Say Hello to Feature A', 'REF-009', 'Added new feature A', 'Dev notes 9', 'Upgrade notes 9', 3, 3, 2, 3, to_timestamp(1720051500), false),
(10, false, false, 'Critical Bug B Fixed', 'REF-010', 'Fixed critical bug B', 'Dev notes 10', 'Upgrade notes 10', 1, 1, 1, 1, to_timestamp(1721716100), false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO release_note (id, archived, published, synced_to_git, created_at, summary, tag, preview_available_from, recommended_test_phase_from, recommended_test_phase_to, planned_production_deployment) VALUES
-- Full timeline: preview, test window, and planned deployment all set
(1, false, false, false, to_timestamp(1706745600), 'Initial release containing core capabilities for managing release notes, linking change notes, and publishing version summaries in one place.', 'v1.0.0', to_timestamp(1706140800), to_timestamp(1706659200), to_timestamp(1707264000), to_timestamp(1707868800)),
(2, false, false, false, to_timestamp(1708300200), 'Added new feature X with configurable behavior, clearer user prompts, and improved data validation across form workflows.', 'v1.1.0', NULL, NULL, NULL, NULL),
(3, false, false, false, to_timestamp(1710063900), 'Fixed bug Y that produced inconsistent output in uncommon scenarios and added safeguards for retry and recovery handling.', 'v1.1.1', NULL, NULL, NULL, NULL),
(4, false, false, false, to_timestamp(1711728500), 'Added new feature Z to improve metadata capture and streamline the process of grouping related change notes.', 'v1.2.0', NULL, NULL, NULL, NULL),
-- Full timeline
(5, false, true, false, to_timestamp(1713393100), 'Implemented performance improvements that reduce API latency and improve rendering speed for larger release note datasets.', 'v1.2.1', to_timestamp(1712707200), to_timestamp(1712966400), to_timestamp(1713571200), to_timestamp(1714176000)),
(6, false, false, false, to_timestamp(1715057700), 'Delivered a security patch that strengthens request validation and improves protection for sensitive administrative operations.', 'v1.2.2', NULL, NULL, NULL, NULL),
(7, false, false, false, to_timestamp(1716722300), 'Added support for a new platform integration path with better compatibility checks and clearer fallback behavior.', 'v1.3.0', NULL, NULL, NULL, NULL),
-- Deployment-only: a release with just a planned production date announced
(8, false, true, false, to_timestamp(1718386900), 'Deprecated the old API contract and documented migration steps with transitional guidance for dependent clients.', 'v1.3.1', NULL, NULL, NULL, to_timestamp(1718900000)),
(9, false, false, false, to_timestamp(1720051500), 'Added new feature A focused on higher editorial throughput through bulk actions and cleaner interaction patterns.', 'v1.4.0', NULL, NULL, NULL, NULL),
(10, false, false, false, to_timestamp(1721716100), 'Fixed critical bug B that could interrupt publication flows and added diagnostics to simplify incident investigation.', 'v1.4.1', NULL, NULL, NULL, NULL),
(11, false, false, false, to_timestamp(1723380700), 'Improved dashboard filtering by adding flexible combinations, clearer labels, and more predictable filter persistence behavior.', 'v1.5.0', NULL, NULL, NULL, NULL),
(12, false, false, false, to_timestamp(1725045300), 'Introduced bulk editing for change notes so editors can update repeated fields faster with fewer manual steps.', 'v1.5.1', NULL, NULL, NULL, NULL),
(13, false, false, false, to_timestamp(1726709900), 'Refined release note export generation to produce more consistent formatting and clearer section separation in outputs.', 'v1.5.2', NULL, NULL, NULL, NULL),
(14, false, false, false, to_timestamp(1728374500), 'Added audit log metadata for better traceability, including actor context, action details, and timestamp clarity.', 'v1.6.0', NULL, NULL, NULL, NULL),
-- Preview + deployment, no formal test phase
(15, false, true, false, to_timestamp(1730039100), 'Improved API pagination defaults and edge-case handling to reduce duplicate reads and improve response consistency.', 'v1.6.1', to_timestamp(1729468800), NULL, NULL, to_timestamp(1730646000)),
(16, false, false, false, to_timestamp(1731703700), 'Reduced page load time through query tuning, caching adjustments, and more efficient component initialization.', 'v1.6.2', NULL, NULL, NULL, NULL),
(17, false, false, false, to_timestamp(1733368300), 'Implemented new role-based access checks to tighten permission boundaries and improve unauthorized action feedback.', 'v1.7.0', NULL, NULL, NULL, NULL),
(18, false, false, false, to_timestamp(1735032900), 'Enhanced search relevance with improved ranking signals, better token handling, and smarter matching heuristics.', 'v1.7.1', NULL, NULL, NULL, NULL),
(19, false, false, false, to_timestamp(1736697500), 'Improved sorting behavior to be more stable, intuitive, and consistent across pagination and filtering states.', 'v1.7.2', NULL, NULL, NULL, NULL),
(20, false, false, false, to_timestamp(1738362100), 'Added a release comparison view that highlights meaningful differences and makes version-to-version review easier.', 'v1.8.0', NULL, NULL, NULL, NULL),
-- Test window + deployment, no preview
(21, false, true, false, to_timestamp(1740026700), 'Introduced smarter empty-state messages that offer contextual guidance and suggest next actions for editors.', 'v1.8.1', NULL, to_timestamp(1739836800), to_timestamp(1740441600), to_timestamp(1741046400)),
(22, false, false, false, to_timestamp(1741691300), 'Improved keyboard navigation and focus management to support faster data entry and accessibility-friendly workflows.', 'v1.8.2', NULL, NULL, NULL, NULL),
(23, false, false, false, to_timestamp(1743355900), 'Stabilized backend validation rules to reduce ambiguous errors and align behavior across API entry points.', 'v1.9.0', NULL, NULL, NULL, NULL),
(24, false, false, false, to_timestamp(1745020500), 'Improved API error responses with clearer messages, better status mapping, and more actionable diagnostics.', 'v1.9.1', NULL, NULL, NULL, NULL),
(25, false, false, false, to_timestamp(1746685100), 'Added customer-level filters to support multi-tenant reporting and simplify segmentation in larger environments.', 'v1.9.2', NULL, NULL, NULL, NULL),
(26, false, false, false, to_timestamp(1748349700), 'Delivered faster release list rendering by reducing redundant calculations and optimizing view update frequency.', 'v2.0.0', NULL, NULL, NULL, NULL),
(27, false, true, false, to_timestamp(1750014300), 'Improved dark mode contrast values for readability while maintaining visual consistency across major interface areas.', 'v2.0.1', NULL, NULL, NULL, NULL),
(28, false, false, false, to_timestamp(1751678900), 'Added archive management tools that make restoring, reviewing, and cleaning historical records more efficient.', 'v2.0.2', NULL, NULL, NULL, NULL),
(29, false, false, false, to_timestamp(1753343500), 'Refined tag editing user experience with better validation, clearer affordances, and improved inline feedback.', 'v2.1.0', NULL, NULL, NULL, NULL),
(30, false, true, false, to_timestamp(1755008100), 'Improved localization support with broader string coverage, fallback handling, and cleaner language switching behavior.', 'v2.1.1', NULL, NULL, NULL, NULL),
(31, false, false, false, to_timestamp(1756672700), 'Added a release duplication flow to accelerate repetitive release setup while preserving essential metadata links.', 'v2.1.2', NULL, NULL, NULL, NULL),
(32, false, false, false, to_timestamp(1758337300), 'Improved loading state handling to reduce interface flicker and provide clearer progress communication to users.', 'v2.2.0', NULL, NULL, NULL, NULL),
(33, false, false, false, to_timestamp(1760001900), 'Introduced more resilient API retry strategies that lower transient failure impact and improve stability in peak traffic.', 'v2.2.1', NULL, NULL, NULL, NULL),
-- Preview only: early access announced, later milestones not yet scheduled
(34, false, true, false, to_timestamp(1761666500), 'Extended table column options with configurable visibility and ordering for more personalized workspace layouts.', 'v2.2.2', to_timestamp(1761000000), NULL, NULL, NULL),
(35, false, false, false, to_timestamp(1763331100), 'Added quick-jump navigation to help users move between key sections without excessive scrolling or repeated clicks.', 'v2.3.0', NULL, NULL, NULL, NULL),
(36, false, false, false, to_timestamp(1764995700), 'Improved notification messages to be more specific, timely, and aligned with the action users just performed.', 'v2.3.1', NULL, NULL, NULL, NULL),
(37, false, false, false, to_timestamp(1766660300), 'Hardened security headers and request policies to improve baseline browser protections across all frontend routes.', 'v2.3.2', NULL, NULL, NULL, NULL),
(38, false, true, false, to_timestamp(1768324900), 'Improved empty release handling to avoid dead ends and present guided options when no related content exists.', 'v2.4.0', NULL, NULL, NULL, NULL),
(39, false, false, false, to_timestamp(1769989500), 'Improved release detail layout with clearer hierarchy and spacing to make dense information easier to scan.', 'v2.4.1', NULL, NULL, NULL, NULL),
(40, false, false, false, to_timestamp(1771654100), 'Optimized cache invalidation behavior to reduce stale data windows and improve consistency after edits.', 'v2.4.2', NULL, NULL, NULL, NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO change_impact (id, release_note_id, position, feature_id, what_is_changed, what_should_be_tested, testing_need) VALUES
(1, 1, 0, 3, 'Introduced the core release note management module, including creating, editing, and publishing release notes.', 'Verify full create/edit/publish lifecycle and that published notes are visible to end users.', 'HIGH'),
(2, 1, 1, 2, 'Added the ability to link change notes to a release note.', 'Confirm change notes can be attached and detached, and that links survive an edit.', 'MEDIUM'),
(3, 5, 0, 1, 'Reduced API latency through query tuning on the release note list endpoints.', 'Load test the list endpoints with large datasets and compare response times against the previous version.', 'MEDIUM_HIGH'),
(4, 5, 1, 1, 'Improved rendering speed for large release note collections in the UI.', 'Render a release note overview with 1000+ entries and check for noticeable lag or layout issues.', 'MEDIUM'),
(5, 6, 0, 1, 'Strengthened request validation for administrative operations.', 'Attempt unauthorized and malformed admin requests and confirm they are rejected with appropriate errors.', 'HIGH'),
(6, 6, 1, 1, 'Hardened input sanitization on form submissions.', 'Submit payloads containing script and SQL-like content and verify they are safely handled.', 'MEDIUM_HIGH'),
(7, 8, 0, 3, 'Deprecated the old API contract and added documented migration steps.', 'Run legacy clients against the new contract and confirm migration guidance resolves breaking changes.', 'HIGH'),
(8, 8, 1, 2, 'Added a transitional compatibility layer for deprecated endpoints.', 'Verify deprecated endpoints still respond with the expected fallback behavior during the transition window.', 'MEDIUM')
ON CONFLICT (id) DO NOTHING;


-- Update auto-increment counter to avoid conflicts
SELECT setval('change_note_id_seq', (SELECT MAX(id) FROM change_note));
SELECT setval('release_note_id_seq', (SELECT MAX(id) FROM release_note));
SELECT setval('change_impact_id_seq', (SELECT MAX(id) FROM change_impact));
