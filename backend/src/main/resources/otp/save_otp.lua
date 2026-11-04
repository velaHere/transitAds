-- KEYS[1] = OTP Redis key
--
-- ARGV[1] = Hashed OTP
-- ARGV[2] = Expiration in seconds
-- ARGV[3] = Hashed OTP field name
-- ARGV[4] = Attempts field name

local hashedOtp = ARGV[1]
local expiration = tonumber(ARGV[2])

if not hashedOtp or hashedOtp == '' then
    return redis.error_reply('INVALID_OTP_HASH')
end

if not expiration or expiration <= 0 then
    return redis.error_reply('INVALID_EXPIRATION')
end

redis.call(
    'HSET',
    KEYS[1],
    ARGV[3], hashedOtp,
    ARGV[4], 0
)

redis.call('EXPIRE', KEYS[1], expiration)

return 1