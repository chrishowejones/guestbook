(ns guestbook.routes.auth
  (:require [compojure.core :refer [defroutes GET POST]]
            [guestbook.views.layout :as layout]
            [hiccup.form :refer [form-to label text-field password-field submit-button]]
            [noir.response :refer [redirect]]
            [noir.session :as session]
            [noir.validation :refer [rule has-value? errors? on-error]]
            [noir.util.crypt :as crypt]
            [guestbook.models.db :as db]))

(defn format-error [[error]]
  [:p.error error])

(defn control
  "Helper function to return functions to return appropriate label, field and a br tag."
  [field name text]
  (list (on-error name format-error)
        (label name text)
        (field name)
        [:br]))

(defn registration-page
  "Renders the user registration page."
  []
  (layout/common
   (form-to [:post "/register"]
            (control text-field :id  "Screen name: ")
            (control password-field :pass "Password: ")
            (control password-field :pass1 "Retype password :")
            (submit-button "create account"))))

(defn handle-registration [id pass pass1]
  "Store registration details."
  (rule (= pass pass1)
        [:pass "password was not retyped correctly"])
  (if (errors? :pass)
    (registration-page)
    (do
      (db/add-user-record {:id id :pass (crypt/encrypt pass)})
      (redirect "/login"))))

(defn login-page "Render the login page" []
  (layout/common
   (form-to [:post "/login"]
            (control text-field :id "Screen name")
            (control password-field :pass "Password")
            (submit-button "login"))))

(defn handle-login "Validates login page input" [id pass]
  (let [user (db/get-user id)]
    (rule (has-value? id)
          [:id "screen name is required"])
    (rule (has-value? pass)
          [:pass "password required"])
    (rule (and user (crypt/compare pass (:pass user)))
          [:pass "invalid password"])
    (if (errors? :id :pass)
      (login-page)
      (do
        (session/put! :user id)
        (redirect "/")))))

(defroutes auth-routes
  "Defines the authentication page routes."
  (GET "/register" [_] (registration-page))
  (POST "/register" [id pass pass1]
        (handle-registration id pass pass1))
  (GET "/login" [] (login-page))
  (POST "/login" [id pass]
        (handle-login id pass))
  (GET "/logout" []
       (layout/common
        (form-to [:post "/logout"]
                 (submit-button "logout"))))
  (POST "/logout" []
        (session/clear!)
        (redirect "/"))
  (GET "/records" []
       (noir.response/content-type "text/plain" "some plain text"))
  (GET "/get-message" []
       (noir.response/json {:message "everything went better than expected!"})))

