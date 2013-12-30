select username,amount,time_trade
    from log_tradexu
        where username = :username
           order by time_trade DESC
            limit 15