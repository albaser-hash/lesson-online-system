import request from '@/utils/request'

// 获取视频播放地址
export function getVideoPlayUrl({ chapterId, userId }) {
  return request({
    url: '/api/video/play',
    method: 'get',
    params: { chapterId, userId }
  })
} 