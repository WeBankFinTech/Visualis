(ns metabase.query-processor.middleware.mbql-to-native
  "Middleware responsible for converting MBQL queries to native queries (by calling the driver's QP methods)
   so the query can then be executed."
  (:require [clojure.tools.logging :as log]
            [metabase.driver :as driver]
            [metabase.api.common :as api]
            [toucan.db :as db]
            [metabase.models.user :as user :refer [User]]
            [metabase.util :as u]))

(defn get-user-name
      "get username from creator_id."
      [creator_id]
      (let [columns (cons User user/admin-or-self-visible-columns)
            id-seq (seq [:id creator_id])
            execute-user (apply db/select-one (vec columns) id-seq)
            email (:email execute-user)
            [username suffix] (clojure.string/split email #"@")]
           username))

(defn query->native-with-proxy-user
  "Add proxy user for native query."
  [{info :info, :as query}]
  ;; {query :query, :as native-query}
  (let [creator_id (:creator_id info)
        executed-by (:executed-by info)
        user-id (if (= nil creator_id)
                  executed-by
                  creator_id)
        username (if (= nil user-id)
                   user-id
                   (get-user-name user-id))
        native-query (driver/mbql->native driver/*driver* query)
        from-sql (:query native-query)
        to-sql (if (= nil user-id)
                 from-sql
                 (format "-- set proxy.user=%s\n%s" username from-sql))]
    (log/infof "login-user: %s, from-sql: %s, to-sql: %s." username from-sql to-sql)
    (assoc native-query :query to-sql)))

(defn query->query-with-proxy-user
      "Add proxy user for native query."
      [{info :info, :as query}]
      ;; {query :query, :as native-query}
      (let [creator_id (:creator_id info)
            executed-by (:executed-by info)
            user-id (if (= nil creator_id)
                      executed-by
                      creator_id)
            username (get-user-name user-id)
            ;native-query (driver/mbql->native driver/*driver* query)
            native (:native query)
            from-sql (:query native)
            to-sql (format "-- set proxy.user=%s\n%s" username from-sql)]
           (assoc native :query to-sql))
      )

(defn query->native-form
      "Return a `:native` query form for `query`, converting it from MBQL if needed."
      [{query-type :type, :as query}]
      (if (and (not= :sparksql driver/*driver*) (not= :query query-type))
        (:native query)
        (if (and (= :sparksql driver/*driver*) (not= :query query-type))
          (query->query-with-proxy-user query)
          (if (and (= :sparksql driver/*driver*) (= :query query-type))
            (query->native-with-proxy-user query)
            (driver/mbql->native driver/*driver* query)
            )
          )
        )
      )


(defn mbql->native
  "Middleware that handles conversion of MBQL queries to native (by calling driver QP methods) so the queries
   can be executed. For queries that are already native, this function is effectively a no-op."
  [qp]
  (fn [query rff context]
    (let [native-query (query->native-form query)]
      (log/info (u/format-color 'yellow "\nPreprocessed:\n%s" (u/pprint-to-str query)))
      (log/info (u/format-color 'green "Native form: \n%s" (u/pprint-to-str native-query)))
      (qp
       (assoc query :native native-query)
       (fn [metadata]
         (rff (assoc metadata :native_form native-query)))
       context))))
