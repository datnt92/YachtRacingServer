SELECT rate,money_betting,bet_choice
FROM  game_choice 
WHERE game_id =:game_id
AND bet_choice = :bet_choice
