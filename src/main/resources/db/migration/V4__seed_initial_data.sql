ALTER TABLE books
    ALTER COLUMN created_at SET DEFAULT LOCALTIMESTAMP;
ALTER TABLE books
    ALTER COLUMN updated_at SET DEFAULT LOCALTIMESTAMP;
ALTER TABLE book_copies
    ALTER COLUMN created_at SET DEFAULT LOCALTIMESTAMP;
ALTER TABLE book_copies
    ALTER COLUMN UPDATED_AT SET DEFAULT LOCALTIMESTAMP;
ALTER TABLE book_copies
    ALTER COLUMN version SET DEFAULT 0;

INSERT INTO departments (name, description) VALUES
    ('Fiction', 'Focuses on creative storytelling, including novels, short stories, and literary prose across diverse genres.'),
    ('Non-Fiction', 'Covers factual and educational material, including biographies, history, science, philosophy, and self-help.'),
    ('Academic & Research', 'Manages scholarly texts, textbooks, peer-reviewed publications, and university-level research materials.'),
    ('Children & Young Adult', 'Curates illustrated storybooks, middle-grade literature, and engaging fiction tailored for younger audiences.'),
    ('Rare Books & Manuscripts', 'Handles the acquisition, preservation, and archival cataloging of historical, out-of-print, and antique texts.'),
    ('Comics & Graphic Novels', 'Specializes in visual storytelling, manga, illustrated series, and serialized graphic works.'),
    ('Reference & Periodicals', 'Maintains encyclopedias, dictionaries, journals, magazines, and technical reference archives.'),
    ('Digital & Audiobooks', 'Manages e-book conversions, audiobook production, digital licensing, and online media streaming.');

INSERT INTO authors (name) VALUES
    ('Arthur C. Pendelton'),
    ('Evelyn Vance'),
    ('Marcus Sterling'),
    ('Elena Rostova'),
    ('Julian Hawthorne'),
    ('Clara Montgomery'),
    ('Gideon Cross'),
    ('Sophia Al-Mansoor');

INSERT INTO categories (name) VALUES
    ('Science Fiction & Fantasy'),
    ('Mystery & Thriller'),
    ('Romance & Contemporary'),
    ('Historical Fiction'),
    ('Biography & Memoir'),
    ('Self-Help & Personal Growth'),
    ('Technology & Computer Science'),
    ('Business & Economics');

INSERT INTO books (isbn, title, description, publisher, publication_year, language, department_id, created_at) VALUES
    ('978-0-13-235088-4', 'The Chronicles of Aetheria', 'An epic journey across floating islands in search of a lost civilization.', 'Apex Press', 2023, 'English', 1, LOCALTIMESTAMP),
    ('978-1-49-195766-0', 'Shadows in the Mist', 'A gripping detective novel set in the foggy streets of 19th-century London.', 'Chronicle Books', 2022, 'English', 1, LOCALTIMESTAMP),
    ('978-0-32-112521-7', 'Architects of Tomorrow', 'A deep dive into distributed systems, cloud architecture, and software design principles.', 'Vanguard Academic', 2024, 'English', 3, LOCALTIMESTAMP),
    ('978-0-59-600712-6', 'Echoes of the Steppes', 'Historical narrative following the rise and fall of ancient nomadic empires.', 'Starlight Media', 2021, 'English', 2, LOCALTIMESTAMP),
    ('978-0-20-163361-0', 'The Clockwork Heir', 'A steampunk adventure blending intricate clockwork mechanisms with royal intrigue.', 'Meridian House', 2023, 'English', 1, LOCALTIMESTAMP),
    ('978-0-13-468599-1', 'Mind Over Habits', 'Practical frameworks for breaking cognitive biases and achieving peak professional productivity.', 'Beacon Publishing', 2024, 'English', 2, LOCALTIMESTAMP),
    ('978-0-32-133487-9', 'Cryptic Legacies', 'An archival collection of handwritten journals and cryptographic notes from early cryptographers.', 'Nexus Press', 2020, 'English', 5, LOCALTIMESTAMP),
    ('978-1-59-327950-9', 'Global Market Dynamics', 'An analytical overview of modern macroeconomic trends and international trade strategies.', 'Summit Books', 2025, 'English', 3, LOCALTIMESTAMP);

INSERT INTO book_authors (book_id, author_id) VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4),
    (5, 5),
    (6, 6),
    (7, 7),
    (8, 8);

INSERT INTO book_categories (book_id, category_id) VALUES
    (1, 1),
    (2, 2),
    (3, 7),
    (4, 4),
    (4, 5),
    (5, 1),
    (5, 4),
    (6, 6),
    (6, 8),
    (7, 2),
    (7, 7),
    (8, 8);

INSERT INTO book_copies (book_id, copy_number, status) VALUES
    (1, 'A-001', 'AVAILABLE'),
    (1, 'A-002', 'AVAILABLE'),
    (2, 'B-010', 'AVAILABLE'),
    (3, 'C-101', 'AVAILABLE'),
    (3, 'C-102', 'AVAILABLE'),
    (4, 'D-045', 'AVAILABLE'),
    (5, 'E-012', 'AVAILABLE'),
    (6, 'F-088', 'AVAILABLE'),
    (7, 'G-005', 'AVAILABLE'),
    (8, 'H-301', 'AVAILABLE');