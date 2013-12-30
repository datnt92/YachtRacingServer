SELECT money_betting, bet_choice, money_result, log_time
FROM game_choice
WHERE username = :username
ORDER BY log_time DESC
limit 0,10