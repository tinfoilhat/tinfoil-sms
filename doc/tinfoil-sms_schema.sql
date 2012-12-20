/*
 * TODO add foreign key constrants, add NOT NULL for primary keys, 
 * and add unique for primary keys
 */
CREATE TABLE shared_information 
(
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    reference INTEGER,
    shared_info_1 TEXT,
    shared_info_2 TEXT
)

CREATE TABLE book_paths
(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    reference INTEGER,
    book_path TEXT,
    book_inverse_path TEXT
)

CREATE TABLE user
(
	public_key BLOB,
	private_key BLOB,
	signature BLOB
)

CREATE TABLE trusted_contact
(
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT,
	public_key BLOB,
	signature BLOB 
)

CREATE TABLE numbers
(
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	reference INTEGER,
	number TEXT,
	type INTEGER,
	unread INTEGER
)

CREATE TABLE messages
(
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	reference INTEGER,
	message TEXT,
    date INTEGER,
    sent INTEGER
)

CREATE TABLE queue
(
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	number_reference INTEGER,
	message TEXT
)