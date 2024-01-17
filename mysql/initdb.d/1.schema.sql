DROP DATABASE hkidb;

CREATE DATABASE hkidb;

CREATE TABLE hkidb.customers (
                                 uid         INT         NOT NULL    AUTO_INCREMENT  PRIMARY KEY,
                                 name        VARCHAR(30) NOT NULL    UNIQUE,
                                 password    VARCHAR(30) NOT NULL
);



CREATE TABLE hkidb.order_type (
                                  otid        INT         NOT NULL    PRIMARY KEY,
                                  name        VARCHAR(16) NOT NULL
);



CREATE TABLE hkidb.account (
                               aid         INT         NOT NULL    AUTO_INCREMENT  PRIMARY KEY,
                               uid         INT         NOT NULL,
                               balance     BIGINT      NOT NULL    DEFAULT 0,

                               FOREIGN KEY (uid) REFERENCES hkidb.customers (uid)
);

CREATE TABLE hkidb.stock_info (
                                  ticker      VARCHAR(16) NOT NULL,
                                  name        VARCHAR(64) NOT NULL,
                                  listed_date DATE        NOT NULL,
                                  market_capital INTEGER  NOT NULL,

                                  PRIMARY KEY (ticker)
);

CREATE TABLE hkidb.order_history (
                                     oid         INT         NOT NULL    AUTO_INCREMENT  PRIMARY KEY,
                                     aid         INT         NOT NULL,
                                     filled_time TIMESTAMP   NOT NULL,
                                     otid        INT         NOT NULL,
                                     ticker      VARCHAR(16) NOT NULL,
                                     qty         INT         NOT NULL,
                                     price       INT         NOT NULL,
                                     avg_entry_price DECIMAL(10, 2)     NULL,

                                     FOREIGN KEY (aid) REFERENCES hkidb.account (aid),
                                     FOREIGN KEY (otid) REFERENCES hkidb.order_type (otid)
);

CREATE TABLE hkidb.position (
                                aid         INT         NOT NULL,
                                ticker      VARCHAR(16) NOT NULL,
                                qty         INT         NOT NULL,
                                avg_entry_price DECIMAL(10, 2)     NOT NULL,

                                PRIMARY KEY (aid, ticker),
                                FOREIGN KEY (aid) REFERENCES hkidb.account (aid),
                                FOREIGN KEY (ticker) REFERENCES hkidb.stock_info (ticker)
);

CREATE TABLE hkidb.stock_price (
                                   ticker      VARCHAR(16) NOT NULL,
                                   date        TIMESTAMP   NOT NULL,
                                   open        INT         NOT NULL,
                                   high        INT         NOT NULL,
                                   low         INT         NOT NULL,
                                   close       INT         NOT NULL,
                                   volume      BIGINT      NOT NULL,

                                   PRIMARY KEY (ticker, date),
                                   FOREIGN KEY (ticker) REFERENCES hkidb.stock_info (ticker)
);

CREATE TABLE hkidb.backtest_history (
                                        uid INT NOT NULL,
                                        aid INT NOT NULL,
                                        param JSON NOT NULL,

                                        PRIMARY KEY (aid),
                                        FOREIGN KEY (uid) REFERENCES hkidb.customers (uid),
                                        FOREIGN KEY (aid) REFERENCES hkidb.account (aid)
);

INSERT INTO hkidb.customers (name, password) VALUES ('ADMIN', 'ADMIN');
INSERT INTO hkidb.order_type (otid, name) VALUES (1, 'BUY'), (2, 'SELL');