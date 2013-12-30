SELECT SUM( money_betting ) * rate AS total_money, bet_choice
FROM game_choice
WHERE game_id =:game_id And isDemo = 1
GROUP BY bet_choice
ORDER BY total_money ASC 
LIMIT 0 , 30