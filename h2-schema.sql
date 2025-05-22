-- Create roles table to match Role.java
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

-- Create users table to match User.java
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create user_roles table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Create tags table to match Tag.java
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Create documents table to match Document.java
CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_path VARCHAR(255),
    file_type VARCHAR(50),
    file_size BIGINT,
    content_text TEXT,
    author_id BIGINT,
    indexed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    search_vector TEXT,
    FOREIGN KEY (author_id) REFERENCES users (id)
);

-- Create document_tags table to link documents and tags
CREATE TABLE IF NOT EXISTS document_tags (
    document_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (document_id, tag_id),
    FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);

-- Insert roles based on ERole enum
INSERT INTO roles (name) VALUES 
('ROLE_ADMIN'),
('ROLE_EDITOR'),
('ROLE_VIEWER');

-- Insert users (passwords are bcrypt hashed, this example uses 'password123' for all users)
INSERT INTO users (username, email, password, created_at, updated_at) VALUES
('admin', 'admin@example.com', '$2a$10$OwuE0YxOsAJGYVzOhBuhwO6cV2QlK2gJyHfIUm1yZXsxigKVEtHqO', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('editor1', 'editor1@example.com', '$2a$10$OwuE0YxOsAJGYVzOhBuhwO6cV2QlK2gJyHfIUm1yZXsxigKVEtHqO', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('editor2', 'editor2@example.com', '$2a$10$OwuE0YxOsAJGYVzOhBuhwO6cV2QlK2gJyHfIUm1yZXsxigKVEtHqO', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('viewer1', 'viewer1@example.com', '$2a$10$OwuE0YxOsAJGYVzOhBuhwO6cV2QlK2gJyHfIUm1yZXsxigKVEtHqO', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('viewer2', 'viewer2@example.com', '$2a$10$OwuE0YxOsAJGYVzOhBuhwO6cV2QlK2gJyHfIUm1yZXsxigKVEtHqO', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Assign roles to users
-- admin has all roles
INSERT INTO user_roles (user_id, role_id) VALUES
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')),
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'ROLE_EDITOR')),
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'ROLE_VIEWER'));

-- editors have editor and viewer roles
INSERT INTO user_roles (user_id, role_id) VALUES
((SELECT id FROM users WHERE username = 'editor1'), (SELECT id FROM roles WHERE name = 'ROLE_EDITOR')),
((SELECT id FROM users WHERE username = 'editor1'), (SELECT id FROM roles WHERE name = 'ROLE_VIEWER')),
((SELECT id FROM users WHERE username = 'editor2'), (SELECT id FROM roles WHERE name = 'ROLE_EDITOR')),
((SELECT id FROM users WHERE username = 'editor2'), (SELECT id FROM roles WHERE name = 'ROLE_VIEWER'));

-- viewers have only viewer role
INSERT INTO user_roles (user_id, role_id) VALUES
((SELECT id FROM users WHERE username = 'viewer1'), (SELECT id FROM roles WHERE name = 'ROLE_VIEWER')),
((SELECT id FROM users WHERE username = 'viewer2'), (SELECT id FROM roles WHERE name = 'ROLE_VIEWER'));

-- Insert sample tags
INSERT INTO tags (name, description) VALUES
('financial', 'Financial reports and analysis'),
('annual', 'Annual documents and reports'),
('report', 'Various reports'),
('quarterly', 'Quarterly documents and reports'),
('Q1', 'First quarter documents'),
('product', 'Product related documents'),
('roadmap', 'Strategic roadmaps'),
('strategy', 'Strategic documents'),
('HR', 'Human Resources documents'),
('policies', 'Company policies'),
('employees', 'Employee related documents'),
('AI', 'Artificial Intelligence topics'),
('market analysis', 'Market research and analysis'),
('research', 'Research documents');

-- Insert sample documents
INSERT INTO documents (title, description, file_path, file_type, file_size, content_text, author_id, created_at, updated_at, indexed) 
VALUES
(
    'Annual Report 2024', 
    'Financial and operational results for fiscal year 2024', 
    '/storage/documents/annual_report_2024.pdf', 
    'pdf', 
    2048576, 
    'Executive Summary
    
    The fiscal year 2024 has been a transformative period for our company, marked by significant growth across all business segments. We achieved a total revenue of $87.5 million, representing a 15% increase compared to the previous year. Our operating margin improved to 28%, and we successfully launched three new product lines that have been well-received by the market.
    
    Financial Highlights:
    - Revenue: $87.5 million (+15% YoY)
    - Operating Income: $24.5 million (+18% YoY)
    - Net Income: $18.2 million (+12% YoY)
    - EPS: $1.45 (+10% YoY)
    
    We continued our strategic investments in R&D, allocating $12.3 million to innovation initiatives that will drive future growth. The Board of Directors has approved a dividend of $0.35 per share, payable to shareholders of record as of June 15, 2024.',
    (SELECT id FROM users WHERE username = 'admin'),
    CURRENT_TIMESTAMP(),
    CURRENT_TIMESTAMP(),
    TRUE
),
(
    'Q1 2024 Financial Results', 
    'First quarter financial performance', 
    '/storage/documents/q1_2024_results.pdf', 
    'pdf', 
    1536000, 
    'Q1 2024 Financial Performance
    
    Revenue increased by 15% to $24.3 million in Q1 2024, compared to $21.1 million in the same period last year. Operating expenses were $18.7 million, resulting in an operating income of $5.6 million. Net income for the quarter was $4.2 million, or $0.33 per share.
    
    The company experienced strong performance in all business units:
    - Software Services: $12.5 million (+18%)
    - Hardware Solutions: $8.3 million (+12%)
    - Consulting Services: $3.5 million (+13%)
    
    Cash flow from operations was $3.8 million, and we ended the quarter with $28.5 million in cash and equivalents.',
    (SELECT id FROM users WHERE username = 'editor1'),
    CURRENT_TIMESTAMP(),
    CURRENT_TIMESTAMP(),
    TRUE
),
(
    'Product Roadmap 2024-2025', 
    'Strategic product development plan', 
    '/storage/documents/product_roadmap.docx', 
    'docx', 
    1024768, 
    'Product Development Strategy 2024-2025
    
    Our product roadmap for the coming year focuses on three key areas: AI integration, sustainability features, and enhanced user experience. We plan to release quarterly updates to our core products while developing two entirely new product lines for launch in Q4 2024 and Q2 2025 respectively.
    
    Key Initiatives:
    1. Enhanced ML capabilities in our analytics platform (Q2 2024)
    2. Carbon footprint tracking features for our entire product line (Q3 2024)
    3. Complete UX redesign of our mobile applications (Q3-Q4 2024)
    4. New cloud-native enterprise solution (Q4 2024)
    5. Sustainability reporting platform for corporate clients (Q2 2025)
    
    The roadmap prioritizes customer-requested features while strategically positioning us to enter new market segments.',
    (SELECT id FROM users WHERE username = 'editor2'),
    CURRENT_TIMESTAMP(),
    CURRENT_TIMESTAMP(),
    TRUE
),
(
    'Employee Handbook 2024', 
    'Updated policies and procedures for staff', 
    '/storage/documents/employee_handbook.pdf', 
    'pdf', 
    3145728, 
    'Company Policies and Procedures
    
    This handbook outlines our company policies, benefits, and expectations for all employees. It includes information on our remote work policy, health benefits, professional development opportunities, and code of conduct.
    
    Key sections include:
    - Employment Policies
    - Compensation and Benefits
    - Time Off and Leave Policies
    - Performance Evaluation
    - Professional Development
    - Code of Conduct
    - Health and Safety
    
    All employees are required to review this handbook annually and acknowledge their understanding of its contents.',
    (SELECT id FROM users WHERE username = 'admin'),
    CURRENT_TIMESTAMP(),
    CURRENT_TIMESTAMP(),
    TRUE
),
(
    'Market Analysis: AI Industry Trends', 
    'Research report on artificial intelligence market trends', 
    '/storage/documents/ai_market_analysis.pptx', 
    'pptx', 
    4194304, 
    'AI Industry Trends 2024-2026
    
    This market analysis examines the current state and future projections of the artificial intelligence industry. Global AI market size reached $150 billion in 2023 and is projected to grow at a CAGR of 38% through 2026.
    
    Key findings:
    - Generative AI applications are seeing the fastest adoption across industries
    - Healthcare, financial services, and manufacturing remain the top three sectors for AI implementation
    - SMEs are increasing AI adoption at an unprecedented rate
    - Regulatory frameworks are evolving rapidly, with significant regional differences
    - AI ethics and responsible AI development are becoming key differentiators
    
    The report includes competitive analysis of major players, regional market dynamics, and emerging opportunities.',
    (SELECT id FROM users WHERE username = 'editor1'),
    CURRENT_TIMESTAMP(),
    CURRENT_TIMESTAMP(),
    TRUE
);

-- Insert document tags - linking documents with tags
INSERT INTO document_tags (document_id, tag_id) VALUES
(1, (SELECT id FROM tags WHERE name = 'financial')),
(1, (SELECT id FROM tags WHERE name = 'annual')),
(1, (SELECT id FROM tags WHERE name = 'report')),
(2, (SELECT id FROM tags WHERE name = 'financial')),
(2, (SELECT id FROM tags WHERE name = 'quarterly')),
(2, (SELECT id FROM tags WHERE name = 'Q1')),
(3, (SELECT id FROM tags WHERE name = 'product')),
(3, (SELECT id FROM tags WHERE name = 'roadmap')),
(3, (SELECT id FROM tags WHERE name = 'strategy')),
(4, (SELECT id FROM tags WHERE name = 'HR')),
(4, (SELECT id FROM tags WHERE name = 'policies')),
(4, (SELECT id FROM tags WHERE name = 'employees')),
(5, (SELECT id FROM tags WHERE name = 'AI')),
(5, (SELECT id FROM tags WHERE name = 'market analysis')),
(5, (SELECT id FROM tags WHERE name = 'research'));
