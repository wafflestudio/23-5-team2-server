INSERT INTO boards (id, name, source_url)
VALUES (3, 'CSE Notice', 'https://cse.snu.ac.kr/community/notice');

INSERT INTO crawlers (id, code, board_id)
VALUES (2, 'cse', 3);
