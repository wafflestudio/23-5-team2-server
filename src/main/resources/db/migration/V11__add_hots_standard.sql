CREATE TABLE hotstandards (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              hot_score BIGINT NOT NULL,
                              views_weight DOUBLE NOT NULL
);

INSERT INTO hotstandards (id,hot_score, views_weight)
VALUES (1,10, 1.0);
