CREATE TABLE customers (
	id INTEGER NOT NULL PRIMARY KEY,
	full_name TEXT,
	street_address TEXT,
	city TEXT,
	postal_code TEXT,
	state TEXT,
	country TEXT,
	email TEXT,
	phone TEXT,
	account_created TEXT,
	is_active INTEGER
);

CREATE TABLE payments (
	id INTEGER NOT NULL PRIMARY KEY,
	order_id INTEGER,
	customer_id INTEGER,
	payment_type TEXT,
	paid_amount REAL,
	payment_date TEXT
);
