select count(email) as count
FROM game_player
where email = :email