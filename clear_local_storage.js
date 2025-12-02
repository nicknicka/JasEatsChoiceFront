// 这是一个用于清除localStorage的脚本，在浏览器中运行

// 清除所有与登录相关的信息
localStorage.removeItem('token');
localStorage.removeItem('isLoggedIn');
localStorage.removeItem('currentRole');

console.log('Local storage cleaned!');
