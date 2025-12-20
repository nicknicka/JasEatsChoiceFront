// Handle avatar upload
const handleAvatarUpload = (event) => {
  const file = event.target.files[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    // Extract base64 part from data URL
    const base64Data = e.target.result.split(',')[1]
    const imageData = {
      base64: base64Data,
      type: file.type,
      name: file.name
    }

    // Upload image to main process for processing
    api.uploadImage(imageData)
      .then(result => {
        if (result.error) {
          ElMessage.error('头像上传失败: ' + result.error)
          return
        }

        // Update user store and localStorage with the new avatar path
        userStore.userInfo.avatar = result.thumbnail // Use thumbnail as default
        userStore.userInfo.realAvatar = result.original // Store original path for full view
        localStorage.setItem('userAvatar', result.thumbnail)

        // Update local userInfo
        userInfo.value.avatar = result.thumbnail

        ElMessage.success('头像上传成功')
      })
      .catch(error => {
        console.error('Avatar upload failed:', error)
        ElMessage.error('头像上传失败')
      })
  }

  reader.readAsDataURL(file)
}
