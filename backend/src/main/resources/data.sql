-- 1. Insert a seller
INSERT INTO seller (name, emerald_balance)
VALUES ('John Smith', 1000.00);


-- 2. Insert products (seller_id = 1)
INSERT INTO product (name, seller_id)
VALUES
    ('Lenovo Laptop', 1),
    ('Logitech Mouse', 1),
    ('Gaming Keyboard', 1),
    ('Samsung Monitor', 1);


-- 3. Insert towns
INSERT INTO town (name)
VALUES
    ('Warsaw'),
    ('Krakow'),
    ('Gdansk'),
    ('Wroclaw'),
    ('Poznan'),
    ('Lodz'),
    ('Katowice'),
    ('Lublin');


-- 4. Insert keywords
INSERT INTO keyword (name)
VALUES
    ('electronics'),
    ('computer'),
    ('laptop'),
    ('mouse'),
    ('keyboard'),
    ('monitor'),
    ('gaming'),
    ('technology'),
    ('accessories'),
    ('equipment'),
    ('promotion'),
    ('new arrival');