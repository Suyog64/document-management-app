-- Query to find documents by tag
SELECT d.* FROM documents d
JOIN document_tags dt ON d.id = dt.document_id
JOIN tags t ON dt.tag_id = t.id
WHERE t.name = 'financial';

-- Query to find all documents authored by a specific user
SELECT d.* FROM documents d
JOIN users u ON d.author_id = u.id
WHERE u.username = 'editor1';

-- Query to find documents containing specific text (H2 version)
SELECT * FROM documents
WHERE LOWER(content_text) LIKE LOWER('%artificial intelligence%');

-- Query to get user roles
SELECT u.username, r.name FROM users u
JOIN user_roles ur ON u.id = ur.user_id
JOIN roles r ON ur.role_id = r.id
ORDER BY u.username;

-- Query to find recent documents (last 30 days) - H2 version
SELECT * FROM documents
WHERE created_at >= DATEADD('DAY', -30, CURRENT_TIMESTAMP())
ORDER BY created_at DESC;

-- Query to count documents by file type
SELECT file_type, COUNT(*) FROM documents
GROUP BY file_type
ORDER BY COUNT(*) DESC;
