import dayjs from 'dayjs';
import { ElMessage } from 'element-plus';

const BASE_PREFIX = import.meta.env.VITE_APP_BASE_API;

/**
 * 日期格式化函数：根据时间差展示相对时间或标准格式
 * @param {string} dateStr - 传入的字符串日期（支持dayjs可解析的格式，如'2026-01-20 12:00:00'、'2026/01/20'等）
 * @returns {string} 格式化后的时间（相对时间/标准格式）
 */
export function formatDateWithRelativeTime(dateStr) {
  // 1. 解析传入的日期字符串，失败则返回空/提示（根据业务调整）
  const targetDate = dayjs(dateStr);
  if (!targetDate.isValid()) {
    console.warn('传入的日期格式无效：', dateStr);
    return ''; // 或返回默认值如'1970-01-01 00:00:00'
  }

  // 2. 获取当前时间 & 计算时间差（毫秒）
  const now = dayjs();
  const diffMs = now.diff(targetDate); // 当前时间 - 目标时间（毫秒）
  const diffSeconds = Math.floor(diffMs / 1000); // 秒
  const diffMinutes = Math.floor(diffSeconds / 60); // 分钟
  const diffHours = Math.floor(diffMinutes / 60); // 小时

  // 3. 按规则返回对应格式
  // 3.1 1小时内（0~59分钟）
  if (diffHours < 1) {
    // 0~59秒：刚刚/几秒前
    if (diffSeconds < 60) {
      return diffSeconds === 0 ? '刚刚' : `${diffSeconds}秒前`;
    }
    // 1~59分钟：几分钟前
    return `${diffMinutes}分钟前`;
  }

  // 3.2 1~24小时内：几小时前
  if (diffHours < 24) {
    return `${diffHours}小时前`;
  }

  // 3.3 超过24小时：标准格式 yyyy-MM-dd HH:mm:ss
  return targetDate.format('YYYY-MM-DD HH:mm:ss');
}

export const shortcuts = [
  {
    text: "过去1小时",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 60 * 60 * 1000);
      return [start, end];
    }
  },
  {
    text: "过去3小时",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 60 * 60 * 1000 * 3);
      return [start, end];
    }
  },
  {
    text: "过去12小时",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setTime(start.getTime() - 60 * 60 * 1000 * 12);
      return [start, end];
    }
  },
  {
    text: "过去24小时",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setDate(start.getDate() - 1);
      return [start, end];
    }
  },
  {
    text: "过去7天",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setDate(start.getDate() - 7);
      return [start, end];
    }
  },
  {
    text: "过去30天",
    value: () => {
      const end = new Date();
      const start = new Date();
      start.setDate(start.getDate() - 30);
      return [start, end];
    }
  }
];

/**
 *
 * @param {*} goods
 * 返回一个商品算上规格的总价
 */
export function calcGoodsTotalPrice(goods) {
  if (!goods) {
    console.error("计算商品价格失败，传入参数goods不存在");
    return 0
  }
  let total = goods.basePrice;
  if (goods.hasSpec && goods.specGroups && goods.specGroups.length > 0) {
    goods.specGroups.forEach((group) => {
      if (group.specItems && group.specItems.length > 0) {
        group.specItems.forEach((spec) => {
          if (spec.extraPrice) {
            total += spec.extraPrice;
          }
        });
      }
    });
  }
  return total;
}

export const downloadImage = async (imgUrl, fileName) => {
  if (!imgUrl) {
    return
  }
  try {
    const link = document.createElement('a')
    const proxyUrl = `${BASE_PREFIX}/upload/image/download?url=${encodeURIComponent(imgUrl)}`
    const response = await fetch(proxyUrl)
    const blob = await response.blob()
    link.href = URL.createObjectURL(blob)
    link.download = fileName
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(link.href)
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败,请稍后重试')
  }
}