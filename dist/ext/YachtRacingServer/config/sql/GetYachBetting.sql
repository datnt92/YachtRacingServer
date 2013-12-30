SELECT rate,money_betting,bet_choice,username,money_result,log_time
FROM  game_choice 
WHERE game_id =:game_id
AND username = :username
