SELECT sum( money_betting ) as money
FROM game_choice
WHERE game_id =:game_id
AND bet_choice = :bet_choice
AND username = :username
LIMIT 0 , 30
