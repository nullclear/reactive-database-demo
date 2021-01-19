--如果value对比一致，执行删除
if redis.call("get", KEYS[1]) == ARGV[1]
then
    --删除key
    if redis.call("del", KEYS[1]) == 1
    then
        --从锁Key的集合中删除此key
        redis.call("SREM", KEYS[2], KEYS[1])
        return true
    else
        return false
    end
else
    return false
end