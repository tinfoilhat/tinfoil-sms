/*
 * This table holds information about the phone user
 * Shouldn't change too often.
 */
CREATE TABLE user
(
    public_key BLOB,
    private_key BLOB,
    signature BLOB
)

/*
 * Holds information about the user's contacts
 * Most tables reference this table through id
 */
CREATE TABLE trusted_contact
(
    id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,
    name TEXT,
    public_key BLOB,
    signature BLOB 
)

/*
 * Creates the book paths table which stores the path to the
 * book stored on phone. 
 */
CREATE TABLE book_paths
(
    id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,
    reference INTEGER REFERENCES trusted_contact (id)
        ON DELETE NO ACTION ON UPDATE CASCADE,
    book_path TEXT,
    book_inverse_path TEXT
)


/* TODO test this.
 * This creates the shared information table
 * Please not that on delete is set to cascade, the default value should not be
 * deleted since it does not reference a column rather it is referenced because
 * of the lack of number in this table. 
 */
CREATE TABLE shared_information 
(
    id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL, 
    reference INTEGER REFERENCES trusted_contact (id)
        ON DELETE NO ACTION ON UPDATE CASCADE,
    shared_info_1 TEXT,
    shared_info_2 TEXT
)

CREATE TABLE numbers
(
    id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,
    reference INTEGER REFERENCES trusted_contact (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    number TEXT,
    type INTEGER,
    unread INTEGER
)

CREATE TABLE messages
(
    id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,
    reference INTEGER REFERENCES numbers (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    message TEXT,
    date INTEGER,
    sent INTEGER
)

CREATE TABLE queue
(
    id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,
    number_reference INTEGER REFERENCES numbers (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    message TEXT
)