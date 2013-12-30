select item_name,log_time,info,status,username,price 
    from log_tradegift
        where username = :username
order by log_time DESC
limit 20