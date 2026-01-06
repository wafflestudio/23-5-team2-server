
ALTER TABLE articles MODIFY COLUMN content LONGTEXT NOT NULL;

ALTER TABLE articles ADD COLUMN title VARCHAR(500) NOT NULL AFTER board_id;

ALTER TABLE crawlers ADD COLUMN code VARCHAR(50) NOT NULL UNIQUE AFTER id;

INSERT INTO boards (id, name, source_url)
VALUES (1, '서비스 공지사항', NULL);


INSERT INTO boards (id, name, source_url)
VALUES (2, 'MySNU 전체공지', 'https://my.snu.ac.kr/ctt/bb/bulletin?b=1');


INSERT INTO crawlers (id, code, board_id)
VALUES (1, 'mysnu', 2);