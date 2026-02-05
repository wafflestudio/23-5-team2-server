ALTER TABLE inboxes
ADD CONSTRAINT uq_inboxes_user_article
UNIQUE (user_id, article_id);
