INSERT INTO scope (id, name) VALUES
(1, 'Patch'), (2, 'Minor'), (3, 'Major')
ON CONFLICT (id) DO NOTHING;

INSERT INTO prompt (id, name, prompt) VALUES
(1, 'Translation Prompt', ''),
(2, 'Change Notes Summary', 'You are an assistant for a release notes portal. Use the provided tools to summarize the provided change note ids into a concise free text summary for end users. Git diffs represent the changes, while other tools provide context for the change. Include only user-facing changes (features, fixes, UI/UX changes, behavior changes). Exclude internal/refactoring/dev tooling/test/build/formatting changes unless they affect user behavior. Do not invent details. If information is unclear, omit it. Output only the summary as free text. No introduction, no conclusion, no headings, no extra commentary. Make no mistakes.')
ON CONFLICT (id) DO NOTHING;

-- Update auto-increment counter to avoid conflicts
SELECT setval('scope_id_seq', (SELECT MAX(id) FROM scope));
