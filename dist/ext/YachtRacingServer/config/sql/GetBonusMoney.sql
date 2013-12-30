SELECT *,sum( win_money ) as bonus, username
FROM log_win
WHERE game_id = :game_id
GROUP BY username