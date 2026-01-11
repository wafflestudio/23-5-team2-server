START TRANSACTION;

ALTER TABLE image_metadata MODIFY article_id BIGINT NULL;
ALTER TABLE image_metadata MODIFY url VARCHAR(500) NOT NULL UNIQUE;
ALTER TABLE image_metadata
    ADD COLUMN author_id BIGINT NULL AFTER id,
    ADD CONSTRAINT fk_image_metadata_author
        FOREIGN KEY (author_id)
        REFERENCES users(id)
        ON UPDATE CASCADE ON DELETE SET NULL;

COMMIT;
