// 验证JWT生成的正确性
const payload = { userId: 1, username: "test123", iat: 1620000000, exp: 1621000000 };

// 将负载转换为JSON字符串
const jsonPayload = JSON.stringify(payload);
console.log('JSON Payload:', jsonPayload);

// 使用Buffer进行Base64URL编码
const base64UrlPayload = Buffer.from(jsonPayload, 'utf8').toString('base64url');
console.log('Base64URL Payload:', base64UrlPayload);

// 构造完整JWT
const mockToken = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${base64UrlPayload}.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c`;
console.log('Full JWT:', mockToken);

// 验证解码
function decodeJwt(token) {
  try {
    if (!token || typeof token !== 'string') return null;
    const tokenParts = token.split('.');
    if (tokenParts.length !== 3) return null;

    const payload = tokenParts[1].replace(/-/g, '+').replace(/_/g, '/');
    const paddedPayload = payload + '='.repeat((4 - (payload.length % 4)) % 4);

    return JSON.parse(Buffer.from(paddedPayload, 'base64').toString('utf8'));
  } catch (error) {
    console.error('Decode error:', error);
    return null;
  }
}

const decoded = decodeJwt(mockToken);
console.log('Decoded:', decoded);
