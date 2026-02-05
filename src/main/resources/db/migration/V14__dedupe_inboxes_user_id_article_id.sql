DELETE i1
FROM inboxes i1
JOIN inboxes i2
  ON i1.user_id = i2.user_id
 AND i1.article_id = i2.article_id
 AND i1.id > i2.id;