local value = redis.call("incr", KEYS[1])
if tonumber(value) == 1 then
    redis.call("expire", KEYS[1], ARGV[1])
end
return value