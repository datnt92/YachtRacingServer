SELECT game_id, game_name, game_type, time_start, time_end, win_count, game_create,status 
FROM game_play WHERE status = 2 ORDER BY game_create DESC LIMIT 0 , 1