SELECT SUM(money) AS sumMoney
FROM log_money
WHERE username = :username