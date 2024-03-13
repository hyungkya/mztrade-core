DROP DATABASE IF EXISTS hkidb;

CREATE DATABASE hkidb;

CREATE TABLE hkidb.customers (
                                 uid         INT         NOT NULL    AUTO_INCREMENT  PRIMARY KEY,
                                 name        VARCHAR(30) NOT NULL    UNIQUE,
                                 password    VARCHAR(64) NOT NULL,
                                 role        VARCHAR(30) NOT NULL    DEFAULT 'ROLE_USER'
);

CREATE TABLE hkidb.tag (
                           tid         INT         NOT NULL     AUTO_INCREMENT     PRIMARY KEY,
                           tname       VARCHAR(16) NOT NULL,
                           tcolor      VARCHAR(10) NOT NULL,
                           uid         INT         NOT NULL,
                           category    INT         NOT NULL,

                           FOREIGN KEY (uid) REFERENCES hkidb.customers (uid)
);

CREATE TABLE hkidb.order_type (
                                  otid        INT         NOT NULL    PRIMARY KEY,
                                  name        VARCHAR(16) NOT NULL
);



CREATE TABLE hkidb.account (
                               aid         INT         NOT NULL    AUTO_INCREMENT  PRIMARY KEY,
                               uid         INT         NOT NULL,
                               balance     BIGINT      NOT NULL    DEFAULT 0,
                               type        VARCHAR(16) NOT NULL    DEFAULT 'BACKTEST',

                               FOREIGN KEY (uid) REFERENCES hkidb.customers (uid)
);

CREATE TABLE hkidb.account_history (
                               aid         INT         NOT NULL,
                               date        TIMESTAMP        NOT NULL,
                               balance     BIGINT      NOT NULL    DEFAULT 0,

                               FOREIGN KEY (aid) REFERENCES hkidb.account (aid) ON DELETE CASCADE
);

CREATE TABLE hkidb.stock_info (
                                  ticker      VARCHAR(16) NOT NULL,
                                  name        VARCHAR(64) NOT NULL,
                                  listed_date DATE        NOT NULL,
                                  market_capital INTEGER  NOT NULL,

                                  PRIMARY KEY (ticker)
);

CREATE TABLE hkidb.stock_info_tag (
                                      ticker      VARCHAR(16) NOT NULL,
                                      tid         INT         NOT NULL,

                                      PRIMARY KEY (ticker, tid),
                                      FOREIGN KEY (ticker) REFERENCES hkidb.stock_info (ticker)  ON DELETE CASCADE,
                                      FOREIGN KEY (tid) REFERENCES hkidb.tag (tid)  ON DELETE CASCADE
);

CREATE TABLE hkidb.game_history (
                                    aid         INT         NOT NULL,
                                    gid         INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                    start_date  TIMESTAMP   NOT NULL,
                                    turns       INT         NOT NULL DEFAULT 0,
                                    ticker      VARCHAR(16) NOT NULL,
                                    plratio     DOUBLE      NOT NULL DEFAULT 0,

                                    FOREIGN KEY (aid) REFERENCES hkidb.account (aid) ON DELETE CASCADE,
                                    FOREIGN KEY (ticker) REFERENCES hkidb.stock_info (ticker)  ON DELETE CASCADE
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
                                     gid         INT         NULL,

                                     FOREIGN KEY (aid) REFERENCES hkidb.account (aid) ON DELETE CASCADE,
                                     FOREIGN KEY (otid) REFERENCES hkidb.order_type (otid),
                                     FOREIGN KEY (gid) REFERENCES hkidb.game_history (gid) ON DELETE CASCADE
);

CREATE TABLE hkidb.position (
                                aid         INT         NOT NULL,
                                ticker      VARCHAR(16) NOT NULL,
                                qty         INT         NOT NULL,
                                avg_entry_price DECIMAL(10, 2)     NOT NULL,

                                PRIMARY KEY (aid, ticker),
                                FOREIGN KEY (aid) REFERENCES hkidb.account (aid) ON DELETE CASCADE,
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
                                        plratio DOUBLE NOT NULL,

                                        PRIMARY KEY (aid),
                                        FOREIGN KEY (uid) REFERENCES hkidb.customers (uid),
                                        FOREIGN KEY (aid) REFERENCES hkidb.account (aid) ON DELETE CASCADE
);

CREATE TABLE hkidb.backtest_history_tag (
                                            aid         INT         NOT NULL,
                                            tid         INT         NOT NULL,

                                            PRIMARY KEY (aid, tid),
                                            FOREIGN KEY (aid) REFERENCES hkidb.account (aid) ON DELETE CASCADE,
                                            FOREIGN KEY (tid) REFERENCES hkidb.tag (tid) ON DELETE CASCADE
);

CREATE TABLE hkidb.chart_setting (
                                        uid INT NOT NULL,
                                        indicator JSON NOT NULL,

                                        FOREIGN KEY (uid) REFERENCES hkidb.customers (uid)
);

DELIMITER //
CREATE TRIGGER hkidb.create_default_chart_setting
AFTER INSERT ON hkidb.customers
FOR EACH ROW
BEGIN
    INSERT INTO hkidb.chart_setting
SET
    uid = NEW.uid,
    indicator = '{}';
END //
DELIMITER ;

