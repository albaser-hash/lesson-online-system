import request from '@/utils/request'

// 创建专门用于文件上传的axios实例，设置更长的超时时间
import axios from 'axios'

const uploadService = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 3000000 // 50分钟超时
  // 移除默认的Content-Type头部，让浏览器自动设置multipart边界
})

// 添加请求拦截器，处理token
import { getToken } from '@/utils/auth'
uploadService.interceptors.request.use(config => {
  // 每次请求都用最新token
  const currentToken = getToken()
  if (currentToken) {
    config.headers['online-token'] = currentToken // 按后端要求传递 token
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 添加响应拦截器
import { Message } from 'element-ui'
uploadService.interceptors.response.use(res => {
  return res.data
}, error => {
  let message = error.message
  if (message.includes("timeout")) {
    message = "文件上传超时，请检查网络连接或尝试上传较小的文件"
  } else if (message == "Network Error") {
    message = "网络连接异常，请检查网络"
  }
  Message({ message: message, type: 'error', duration: 5 * 1000 })
  return Promise.reject(error)
})

// 前端获取视频时长
function getVideoDuration(file) {
  return new Promise((resolve) => {
    // 创建一个隐藏的video元素
    const video = document.createElement('video');
    video.style.display = 'none';
    video.preload = 'metadata';
    
    // 添加到DOM中（虽然隐藏，但有助于某些浏览器的兼容性）
    document.body.appendChild(video);
    
    video.onloadedmetadata = () => {
      const duration = video.duration;
      
      // 格式化时长为 HH:MM:SS 格式
      if (duration && !isNaN(duration)) {
        const hours = Math.floor(duration / 3600);
        const minutes = Math.floor((duration % 3600) / 60);
        const seconds = Math.floor(duration % 60);
        
        const formattedDuration = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        
        // 清理
        document.body.removeChild(video);
        window.URL.revokeObjectURL(video.src);
        
        resolve(formattedDuration);
      } else {
        document.body.removeChild(video);
        window.URL.revokeObjectURL(video.src);
        resolve('00:00:00');
      }
    };
    
    video.onerror = () => {
      document.body.removeChild(video);
      window.URL.revokeObjectURL(video.src);
      resolve('00:00:00');
    };
    
    const url = URL.createObjectURL(file);
    video.src = url;
  });
}

export async function uploadVideo(file, onProgress) {
  try {
    // 获取视频时长
    const duration = await getVideoDuration(file);
    
    // 直接上传文件到后端
    const formData = new FormData()
    formData.append('file', file)
    formData.append('duration', duration)

    const response = await uploadService.post('/admin/common/upload-video', formData, {
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(percentCompleted)
        }
      }
    })
    
    if (response.code !== 200) {
      throw new Error(response.msg || '视频上传失败')
    }
    
    // 直接返回后端响应，保持数据结构一致
    return response
    
  } catch (error) {
    console.error('视频上传失败:', error);
    return {
      code: 500,
      msg: error.message || '视频上传失败'
    }
  }
}

// 上传文档并自动解析分页，写入doc_json
export function uploadDoc(file, chapterId, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('chapterId', chapterId)
  return uploadService.post('/admin/common/upload-doc', formData, {
    onUploadProgress: (progressEvent) => {
      if (onProgress && progressEvent.total) {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        onProgress(percentCompleted)
      }
    }
  })
}
