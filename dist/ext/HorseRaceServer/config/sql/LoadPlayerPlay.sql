SELECT sms_id, phone_number, horse_id, device_id, dest, cmdcode, msgbody, status, date, from_device, sponsor 
FROM  log_sms 
WHERE  date
BETWEEN  :time_start
AND  :time_end