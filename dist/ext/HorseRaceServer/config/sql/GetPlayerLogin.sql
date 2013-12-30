SELECT  increment_id ,  player_id ,  client_type ,  phone_number ,  status ,  date_insert 
FROM  game_player 
WHERE player_id =  :player_id
AND phone_number =  :phone_number 
AND status = 0