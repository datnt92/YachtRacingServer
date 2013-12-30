UPDATE game_play SET  
win_count=:win_count,
win2nd_count = :win2nd_count,
win3rd_count = :win3rd_count,
win_horse=:win_horse,
second_horse = :second_horse,
third_horse = :third_horse
WHERE game_id=:game_id