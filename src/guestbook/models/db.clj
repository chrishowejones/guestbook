(ns guestbook.models.db
  (:require [clojure.java.jdbc :as sql])
  (:import java.sql.DriverManager))

(def db {:classname "org.sqlite.JDBC" :subprotocol "sqlite" :subname "db.sq3"})

(defn create-guestbook-table []
  (sql/with-connection
    db
    (sql/create-table
     :guestbook
     [:id "INTEGER PRIMARY KEY AUTOINCREMENT"]
     [:timestamp "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"]
     [:name "TEXT"]
     [:message "TEXT"])
    (sql/do-commands "CREATE INDEX timestamp_index ON guestbook (timestamp)")))

(defn create-user-table [] "Create user table."
  (sql/with-connection db
    (sql/create-table
            :users
            [:id "varchar(20) PRIMARY KEY"]
            [:pass "varchar(100)"])))

(defn add-user-record [user] "Add a user record."
  (sql/with-connection db
    (sql/insert-record :users user)))

(defn get-user [id] "Get user based on id."
  (sql/with-connection db
    (sql/with-query-results
      res ["select * from users where id = ?" id]
      (first res))))

(defn read-guests []
  (sql/with-connection
    db
    (sql/with-query-results res
      ["SELECT * FROM guestbook ORDER BY timestamp DESC"]
      (doall res))))

(defn save-message [name message]
  (sql/with-connection
    db
    (sql/insert-values
     :guestbook
     [:name :message :timestamp]
     [name message (new java.util.Date)])))

