// 网络请求封装
const baseURL = 'http://localhost:8080'

export const request = (options) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: baseURL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': uni.getStorageSync('token') || ''
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else if (res.statusCode === 401) {
          // 未登录或token失效
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          })
          uni.navigateTo({
            url: '/pages/login/index'
          })
          reject(res)
        } else {
          uni.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络连接失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

// GET请求
export const get = (url, data) => {
  return request({
    url,
    method: 'GET',
    data
  })
}

// POST请求
export const post = (url, data) => {
  return request({
    url,
    method: 'POST',
    data
  })
}

// PUT请求
export const put = (url, data) => {
  return request({
    url,
    method: 'PUT',
    data
  })
}

// DELETE请求
export const del = (url, data) => {
  return request({
    url,
    method: 'DELETE',
    data
  })
}