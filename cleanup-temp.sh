#!/bin/bash

# 临时文件清理脚本
# 清理超过1小时的临时上传文件

TEMP_DIR="/tmp/upload"
LOG_FILE="/var/log/upload-cleanup.log"

# 创建临时目录（如果不存在）
mkdir -p $TEMP_DIR

# 清理超过1小时的临时文件
find $TEMP_DIR -type f -mmin +60 -delete

# 记录清理日志
echo "$(date): 清理临时文件完成" >> $LOG_FILE

# 显示当前临时目录大小
du -sh $TEMP_DIR >> $LOG_FILE 