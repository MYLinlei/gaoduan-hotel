local voucherId = ARGV[1]
local userId = ARGV[2]

local stockKey = 'hotel:voucher:stock:' .. voucherId
local orderKey = 'hotel:voucher:order:' .. voucherId

local stock = tonumber(redis.call('get', stockKey))
if (not stock) or (stock <= 0) then
    return 1
end

if redis.call('sismember', orderKey, userId) == 1 then
    return 2
end

redis.call('decr', stockKey)
redis.call('sadd', orderKey, userId)
return 0
