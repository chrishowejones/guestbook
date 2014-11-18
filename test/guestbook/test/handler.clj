(ns guestbook.test.handler
  (:use clojure.test
        ring.mock.request
        guestbook.handler)
  (:require [guestbook.models.db :as db]))

(deftest test-app
  (let [test-date (java.util.Date.)]
    (with-redefs [db/read-guests (fn [] [{:name "Chris" :message "Hello" :timestamp test-date}])]
      (testing "main route"
        (let [response (app (request :get "/"))]
          (is (= (:status response) 200))
          (is (.contains (:body response) "<cite>Chris</cite>"))
          (is (.contains (:body response) "<blockquote>Hello</blockquote>"))
          (is (.contains (:body response) (str "<time>" (.format (java.text.SimpleDateFormat. "dd/MM/yyyy") test-date) "</time>")))))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404)))))


