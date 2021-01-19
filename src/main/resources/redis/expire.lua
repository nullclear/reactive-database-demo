local elems = redis.call("SMEMBERS", KEYS[1])
for k, v in pairs(elems) do
    --对集合中每一个元素进行循环续命
    redis.call("EXPIRE", v, ARGV[1])
end
return true