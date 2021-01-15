if redis.call("get",KEYS[1]) == ARGV[1]
then
    if redis.call("del",KEYS[1]) == 1
    then
        return true
    else
        return false
    end
else
    return false
end