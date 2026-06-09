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

INSERT INTO release_note (id, archived, published, synced_to_git, created_at, summary, tag) VALUES
(1, false, false, false, to_timestamp(1706745600), 'Initial release containing core capabilities for managing release notes, linking change notes, and publishing version summaries in one place.', 'v1.0.0'),
(2, false, false, false, to_timestamp(1708300200), 'Added new feature X with configurable behavior, clearer user prompts, and improved data validation across form workflows.', 'v1.1.0'),
(3, false, false, false, to_timestamp(1710063900), 'Fixed bug Y that produced inconsistent output in uncommon scenarios and added safeguards for retry and recovery handling.', 'v1.1.1'),
(4, false, false, false, to_timestamp(1711728500), 'Added new feature Z to improve metadata capture and streamline the process of grouping related change notes.', 'v1.2.0'),
(5, false, true, false, to_timestamp(1713393100), 'Implemented performance improvements that reduce API latency and improve rendering speed for larger release note datasets.', 'v1.2.1'),
(6, false, false, false, to_timestamp(1715057700), 'Delivered a security patch that strengthens request validation and improves protection for sensitive administrative operations.', 'v1.2.2'),
(7, false, false, false, to_timestamp(1716722300), 'Added support for a new platform integration path with better compatibility checks and clearer fallback behavior.', 'v1.3.0'),
(8, false, true, false, to_timestamp(1718386900), 'Deprecated the old API contract and documented migration steps with transitional guidance for dependent clients.', 'v1.3.1'),
(9, false, false, false, to_timestamp(1720051500), 'Added new feature A focused on higher editorial throughput through bulk actions and cleaner interaction patterns.', 'v1.4.0'),
(10, false, false, false, to_timestamp(1721716100), 'Fixed critical bug B that could interrupt publication flows and added diagnostics to simplify incident investigation.', 'v1.4.1'),
(11, false, false, false, to_timestamp(1723380700), 'Improved dashboard filtering by adding flexible combinations, clearer labels, and more predictable filter persistence behavior.', 'v1.5.0'),
(12, false, false, false, to_timestamp(1725045300), 'Introduced bulk editing for change notes so editors can update repeated fields faster with fewer manual steps.', 'v1.5.1'),
(13, false, false, false, to_timestamp(1726709900), 'Refined release note export generation to produce more consistent formatting and clearer section separation in outputs.', 'v1.5.2'),
(14, false, false, false, to_timestamp(1728374500), 'Added audit log metadata for better traceability, including actor context, action details, and timestamp clarity.', 'v1.6.0'),
(15, false, true, false, to_timestamp(1730039100), 'Improved API pagination defaults and edge-case handling to reduce duplicate reads and improve response consistency.', 'v1.6.1'),
(16, false, false, false, to_timestamp(1731703700), 'Reduced page load time through query tuning, caching adjustments, and more efficient component initialization.', 'v1.6.2'),
(17, false, false, false, to_timestamp(1733368300), 'Implemented new role-based access checks to tighten permission boundaries and improve unauthorized action feedback.', 'v1.7.0'),
(18, false, false, false, to_timestamp(1735032900), 'Enhanced search relevance with improved ranking signals, better token handling, and smarter matching heuristics.', 'v1.7.1'),
(19, false, false, false, to_timestamp(1736697500), 'Improved sorting behavior to be more stable, intuitive, and consistent across pagination and filtering states.', 'v1.7.2'),
(20, false, false, false, to_timestamp(1738362100), 'Added a release comparison view that highlights meaningful differences and makes version-to-version review easier.', 'v1.8.0'),
(21, false, true, false, to_timestamp(1740026700), 'Introduced smarter empty-state messages that offer contextual guidance and suggest next actions for editors.', 'v1.8.1'),
(22, false, false, false, to_timestamp(1741691300), 'Improved keyboard navigation and focus management to support faster data entry and accessibility-friendly workflows.', 'v1.8.2'),
(23, false, false, false, to_timestamp(1743355900), 'Stabilized backend validation rules to reduce ambiguous errors and align behavior across API entry points.', 'v1.9.0'),
(24, false, false, false, to_timestamp(1745020500), 'Improved API error responses with clearer messages, better status mapping, and more actionable diagnostics.', 'v1.9.1'),
(25, false, false, false, to_timestamp(1746685100), 'Added customer-level filters to support multi-tenant reporting and simplify segmentation in larger environments.', 'v1.9.2'),
(26, false, false, false, to_timestamp(1748349700), 'Delivered faster release list rendering by reducing redundant calculations and optimizing view update frequency.', 'v2.0.0'),
(27, false, true, false, to_timestamp(1750014300), 'Improved dark mode contrast values for readability while maintaining visual consistency across major interface areas.', 'v2.0.1'),
(28, false, false, false, to_timestamp(1751678900), 'Added archive management tools that make restoring, reviewing, and cleaning historical records more efficient.', 'v2.0.2'),
(29, false, false, false, to_timestamp(1753343500), 'Refined tag editing user experience with better validation, clearer affordances, and improved inline feedback.', 'v2.1.0'),
(30, false, true, false, to_timestamp(1755008100), 'Improved localization support with broader string coverage, fallback handling, and cleaner language switching behavior.', 'v2.1.1'),
(31, false, false, false, to_timestamp(1756672700), 'Added a release duplication flow to accelerate repetitive release setup while preserving essential metadata links.', 'v2.1.2'),
(32, false, false, false, to_timestamp(1758337300), 'Improved loading state handling to reduce interface flicker and provide clearer progress communication to users.', 'v2.2.0'),
(33, false, false, false, to_timestamp(1760001900), 'Introduced more resilient API retry strategies that lower transient failure impact and improve stability in peak traffic.', 'v2.2.1'),
(34, false, true, false, to_timestamp(1761666500), 'Extended table column options with configurable visibility and ordering for more personalized workspace layouts.', 'v2.2.2'),
(35, false, false, false, to_timestamp(1763331100), 'Added quick-jump navigation to help users move between key sections without excessive scrolling or repeated clicks.', 'v2.3.0'),
(36, false, false, false, to_timestamp(1764995700), 'Improved notification messages to be more specific, timely, and aligned with the action users just performed.', 'v2.3.1'),
(37, false, false, false, to_timestamp(1766660300), 'Hardened security headers and request policies to improve baseline browser protections across all frontend routes.', 'v2.3.2'),
(38, false, true, false, to_timestamp(1768324900), 'Improved empty release handling to avoid dead ends and present guided options when no related content exists.', 'v2.4.0'),
(39, false, false, false, to_timestamp(1769989500), 'Improved release detail layout with clearer hierarchy and spacing to make dense information easier to scan.', 'v2.4.1'),
(40, false, false, false, to_timestamp(1771654100), 'Optimized cache invalidation behavior to reduce stale data windows and improve consistency after edits.', 'v2.4.2')
ON CONFLICT (id) DO NOTHING;


-- Update auto-increment counter to avoid conflicts
SELECT setval('change_note_id_seq', (SELECT MAX(id) FROM change_note));
SELECT setval('release_note_id_seq', (SELECT MAX(id) FROM release_note));
