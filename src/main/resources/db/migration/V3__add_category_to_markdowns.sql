ALTER TABLE markdowns
    ADD COLUMN category_id BIGINT,
    ADD CONSTRAINT fk_markdowns_category
        FOREIGN KEY (category_id) REFERENCES categories(id);