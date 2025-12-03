const Redis = require('redis');
const redisClient = Redis.createClient();

// Simulate captcha generation and verification
async function debugCaptcha() {
    try {
        // Connect to Redis
        await redisClient.connect();
        
        // Simulate generating a captcha like "PFZC"
        const captcha = "PFZC";
        const checkCodeKey = "test-123456";
        
        // Store in Redis
        await redisClient.set(`captcha:${checkCodeKey}`, captcha, { EX: 300 }); // 5 minutes expiration
        console.log(`Stored captcha "${captcha}" with key "captcha:${checkCodeKey}"`);
        
        // Simulate incorrect verification
        const incorrectCaptcha = "a";
        const storedCaptcha = await redisClient.get(`captcha:${checkCodeKey}`);
        
        console.log(`User entered: "${incorrectCaptcha}"`);
        console.log(`Stored captcha: "${storedCaptcha}"`);
        console.log(`Case-sensitive comparison result: "${incorrectCaptcha === storedCaptcha}"`);
        console.log(`Case-insensitive comparison result: "${incorrectCaptcha.toLowerCase() === storedCaptcha.toLowerCase()}"`);
        
        // Clean up
        await redisClient.del(`captcha:${checkCodeKey}`);
        await redisClient.disconnect();
        
    } catch (error) {
        console.error("Error debugging captcha:", error);
        await redisClient.disconnect();
    }
}

debugCaptcha();
