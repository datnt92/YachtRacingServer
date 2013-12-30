select count(trade_id) as count from log_tradexu 
   where transaction =  :transaction and username=:username
