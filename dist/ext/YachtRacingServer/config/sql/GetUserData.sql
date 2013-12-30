SELECT 
increment_id, username, email,Uid,
flag, status, date_insert,device_token
FROM game_player WHERE username = :username
