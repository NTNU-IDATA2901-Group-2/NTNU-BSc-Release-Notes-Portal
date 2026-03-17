-- AI generated sample data for change_note and release_note tables
INSERT INTO change_note (id, archived, published, reference, description, developer_notes, upgrade_notes, change_source, product_id, customer_id, feature_id, scope_id, timestamp) VALUES
(1, false, false, 'REF-001', 'Initial portal release with core workflows for drafting, reviewing, and publishing release notes. Added shared validation rules, clear required-field messages, and stable defaults to improve onboarding and reduce submission errors.', 'Dev notes 1', 'Upgrade notes 1', 'JIRA-11', 1, 1, 1, 1, 1706745600000),
(2, false, false, 'REF-002', 'Added feature X with broader configuration options, safer defaults, and clearer validation guidance. Users can resolve conflicting inputs earlier, reducing rework and improving consistency across products and customer contexts.', 'Dev notes 2', 'Upgrade notes 2', 'JIRA-12', 2, 2, 2, 2, 1708300200000),
(3, false, false, 'REF-003', 'Fixed bug Y causing inconsistent rendering in edge cases and improved state sync after failed updates. Recovery paths now avoid stale values, and transitions between loading, error, and success are more predictable during retries.', 'Dev notes 3', 'Upgrade notes 3', 'JIRA-13', 3, 3, 2, 3, 1710063900000),
(4, false, false, 'REF-004', 'Added feature Z for richer metadata selection with better filtering, sensible defaults, and clearer guidance text. Editors can capture precise context with fewer misclassifications, improving reporting quality and cross-team consistency.', 'Dev notes 4', 'Upgrade notes 4', 'JIRA-14', 1, 1, 1, 1, 1711728500000),
(5, false, false, 'REF-005', 'Implemented performance improvements for list retrieval and table rendering to reduce wait times on large datasets. Optimized query paths and reduced client recalculations, resulting in smoother navigation and faster feedback.', 'Dev notes 5', 'Upgrade notes 5', 'JIRA-15', 2, 2, 2, 2, 1713393100000),
(6, false, false, 'REF-006', 'Applied a security patch for token validation edge cases and privileged request handling. Added stricter checks for malformed or expired credentials, stronger endpoint safeguards, and clearer diagnostics for incident analysis.', 'Dev notes 6', 'Upgrade notes 6', 'JIRA-16', 3, 3, 1, 3, 1715057700000),
(7, false, false, 'REF-007', 'Added support for a new platform integration layer with adapter mapping, fallback behavior, and stronger compatibility checks. Early validation now catches unsupported combinations sooner, reducing sync failures and troubleshooting time.', 'Dev notes 7', 'Upgrade notes 7', 'JIRA-17', 1, 1, 1, 1, 1716722300000),
(8, false, false, 'REF-008', 'Deprecated the old API surface and added migration guidance with warnings, compatibility notes, and timeline expectations. Consumers get clearer upgrade signals while teams receive practical steps to reduce migration risk.', 'Dev notes 8', 'Upgrade notes 8', 'JIRA-18', 2, 2, 2, 2, 1718386900000),
(9, false, false, 'REF-009', 'Added feature A to improve bulk workflow productivity and reduce repetitive editing. Users can apply coordinated changes across records with guardrails and clearer previews, cutting routine effort for high-volume maintenance.', 'Dev notes 9', 'Upgrade notes 9', 'JIRA-19', 3, 3, 2, 3, 1720051500000),
(10, false, false, 'REF-010', 'Fixed critical bug B that intermittently blocked publication and improved diagnostics for incident response. Recovery behavior is now more reliable, and operators can resolve publish issues faster during deadline-sensitive releases.', 'Dev notes 10', 'Upgrade notes 10', 'JIRA-20', 1, 1, 1, 1, 1721716100000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO release_note (id, archived, published, created_at, summary, tag) VALUES
(1, false, false, 1706745600000, 'Initial release containing core capabilities for managing release notes, linking change notes, and publishing version summaries in one place.', 'v1.0.0'),
(2, false, false, 1708300200000, 'Added new feature X with configurable behavior, clearer user prompts, and improved data validation across form workflows.', 'v1.1.0'),
(3, false, false, 1710063900000, 'Fixed bug Y that produced inconsistent output in uncommon scenarios and added safeguards for retry and recovery handling.', 'v1.1.1'),
(4, false, false, 1711728500000, 'Added new feature Z to improve metadata capture and streamline the process of grouping related change notes.', 'v1.2.0'),
(5, false, false, 1713393100000, 'Implemented performance improvements that reduce API latency and improve rendering speed for larger release note datasets.', 'v1.2.1'),
(6, false, false, 1715057700000, 'Delivered a security patch that strengthens request validation and improves protection for sensitive administrative operations.', 'v1.2.2'),
(7, false, false, 1716722300000, 'Added support for a new platform integration path with better compatibility checks and clearer fallback behavior.', 'v1.3.0'),
(8, false, false, 1718386900000, 'Deprecated the old API contract and documented migration steps with transitional guidance for dependent clients.', 'v1.3.1'),
(9, false, false, 1720051500000, 'Added new feature A focused on higher editorial throughput through bulk actions and cleaner interaction patterns.', 'v1.4.0'),
(10, false, false, 1721716100000, 'Fixed critical bug B that could interrupt publication flows and added diagnostics to simplify incident investigation.', 'v1.4.1'),
(11, false, false, 1723380700000, 'Improved dashboard filtering by adding flexible combinations, clearer labels, and more predictable filter persistence behavior.', 'v1.5.0'),
(12, false, false, 1725045300000, 'Introduced bulk editing for change notes so editors can update repeated fields faster with fewer manual steps.', 'v1.5.1'),
(13, false, false, 1726709900000, 'Refined release note export generation to produce more consistent formatting and clearer section separation in outputs.', 'v1.5.2'),
(14, false, false, 1728374500000, 'Added audit log metadata for better traceability, including actor context, action details, and timestamp clarity.', 'v1.6.0'),
(15, false, false, 1730039100000, 'Improved API pagination defaults and edge-case handling to reduce duplicate reads and improve response consistency.', 'v1.6.1'),
(16, false, false, 1731703700000, 'Reduced page load time through query tuning, caching adjustments, and more efficient component initialization.', 'v1.6.2'),
(17, false, false, 1733368300000, 'Implemented new role-based access checks to tighten permission boundaries and improve unauthorized action feedback.', 'v1.7.0'),
(18, false, false, 1735032900000, 'Enhanced search relevance with improved ranking signals, better token handling, and smarter matching heuristics.', 'v1.7.1'),
(19, false, false, 1736697500000, 'Improved sorting behavior to be more stable, intuitive, and consistent across pagination and filtering states.', 'v1.7.2'),
(20, false, false, 1738362100000, 'Added a release comparison view that highlights meaningful differences and makes version-to-version review easier.', 'v1.8.0'),
(21, false, false, 1740026700000, 'Introduced smarter empty-state messages that offer contextual guidance and suggest next actions for editors.', 'v1.8.1'),
(22, false, false, 1741691300000, 'Improved keyboard navigation and focus management to support faster data entry and accessibility-friendly workflows.', 'v1.8.2'),
(23, false, false, 1743355900000, 'Stabilized backend validation rules to reduce ambiguous errors and align behavior across API entry points.', 'v1.9.0'),
(24, false, false, 1745020500000, 'Improved API error responses with clearer messages, better status mapping, and more actionable diagnostics.', 'v1.9.1'),
(25, false, false, 1746685100000, 'Added customer-level filters to support multi-tenant reporting and simplify segmentation in larger environments.', 'v1.9.2'),
(26, false, false, 1748349700000, 'Delivered faster release list rendering by reducing redundant calculations and optimizing view update frequency.', 'v2.0.0'),
(27, false, false, 1750014300000, 'Improved dark mode contrast values for readability while maintaining visual consistency across major interface areas.', 'v2.0.1'),
(28, false, false, 1751678900000, 'Added archive management tools that make restoring, reviewing, and cleaning historical records more efficient.', 'v2.0.2'),
(29, false, false, 1753343500000, 'Refined tag editing user experience with better validation, clearer affordances, and improved inline feedback.', 'v2.1.0'),
(30, false, false, 1755008100000, 'Improved localization support with broader string coverage, fallback handling, and cleaner language switching behavior.', 'v2.1.1'),
(31, false, false, 1756672700000, 'Added a release duplication flow to accelerate repetitive release setup while preserving essential metadata links.', 'v2.1.2'),
(32, false, false, 1758337300000, 'Improved loading state handling to reduce interface flicker and provide clearer progress communication to users.', 'v2.2.0'),
(33, false, false, 1760001900000, 'Introduced more resilient API retry strategies that lower transient failure impact and improve stability in peak traffic.', 'v2.2.1'),
(34, false, false, 1761666500000, 'Extended table column options with configurable visibility and ordering for more personalized workspace layouts.', 'v2.2.2'),
(35, false, false, 1763331100000, 'Added quick-jump navigation to help users move between key sections without excessive scrolling or repeated clicks.', 'v2.3.0'),
(36, false, false, 1764995700000, 'Improved notification messages to be more specific, timely, and aligned with the action users just performed.', 'v2.3.1'),
(37, false, false, 1766660300000, 'Hardened security headers and request policies to improve baseline browser protections across all frontend routes.', 'v2.3.2'),
(38, false, false, 1768324900000, 'Improved empty release handling to avoid dead ends and present guided options when no related content exists.', 'v2.4.0'),
(39, false, false, 1769989500000, 'Improved release detail layout with clearer hierarchy and spacing to make dense information easier to scan.', 'v2.4.1'),
(40, false, false, 1771654100000, 'Optimized cache invalidation behavior to reduce stale data windows and improve consistency after edits.', 'v2.4.2')
ON CONFLICT (id) DO NOTHING;


-- Update auto-increment counter to avoid conflicts
SELECT setval('change_note_id_seq', (SELECT MAX(id) FROM change_note));
SELECT setval('release_note_id_seq', (SELECT MAX(id) FROM release_note));