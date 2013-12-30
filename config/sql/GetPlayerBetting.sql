SELECT 
bet_id, username, game_id, bet_choice, log_time,money_betting
FROM game_choice WHERE game_id = :game_id AND username = :username