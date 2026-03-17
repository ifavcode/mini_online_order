import dayjs from "dayjs";
import { useSystemStore } from "../stores/system";
import http from "./http";

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

/**
 * 判断当前时间是否在营业时间内
 * @returns {boolean} true-在营业时间内，false-不在营业时间内
 */
export function isBusinessHours() {
  try {
    // 1. 获取营业信息并做空值校验
    const businessInfo = useSystemStore().businessInfo
    if (!businessInfo || !businessInfo.businessHours) {
      console.warn('营业信息未配置')
      return false
    }

    const { weekDays, startTime, endTime } = businessInfo.businessHours

    // 2. 基础参数校验
    if (!weekDays || !Array.isArray(weekDays) || weekDays.length === 0) {
      console.warn('营业星期未配置')
      return false
    }
    if (!startTime || !endTime || !/^\d{2}:\d{2}$/.test(startTime) || !/^\d{2}:\d{2}$/.test(endTime)) {
      console.warn('营业时间格式错误，需为 HH:mm 格式')
      return false
    }

    // 3. 获取当前时间相关信息
    const now = new Date()
    // 获取当前星期（0-6，0是周日，1是周一...6是周六）
    const currentWeekDay = now.getDay()
    // 转换为数字格式的当前时间（如 09:30 → 9.5，14:45 → 14.75）
    const currentHour = now.getHours() + now.getMinutes() / 60

    // 4. 转换营业开始/结束时间为数字格式
    const [startHour, startMinute] = startTime.split(':').map(Number)
    const [endHour, endMinute] = endTime.split(':').map(Number)
    const startHourNum = startHour + startMinute / 60
    const endHourNum = endHour + endMinute / 60

    // 5. 核心判断逻辑
    // 第一步：判断当前星期是否在营业星期列表中
    const isInBusinessWeek = weekDays.includes(currentWeekDay)
    if (!isInBusinessWeek) {
      return false
    }

    // 第二步：判断当前时间是否在营业时间段内
    const isInBusinessTime = currentHour >= startHourNum && currentHour <= endHourNum

    return isInBusinessTime
  } catch (error) {
    console.error('判断营业时间出错：', error)
    return false
  }
}

/**
 * 格式化营业信息为友好的文字描述
 * @param {Object} businessHours 营业信息对象 {weekDays: [], startTime: string, endTime: string}
 * @returns {string} 格式化后的营业文字，如"周一至周五 09:00-21:00"，异常时返回"暂未配置营业时间"
 */
export function formatBusinessHours() {
  const businessInfo = useSystemStore().businessInfo
  const businessHours = businessInfo.businessHours
  // 1. 空值校验
  if (!businessHours || !Array.isArray(businessHours.weekDays) || businessHours.weekDays.length === 0) {
    return '暂未配置营业时间'
  }

  // 2. 定义星期映射表（对应 getDay() 返回值：0=周日，1=周一...6=周六）
  const weekDayMap = {
    0: '周日',
    1: '周一',
    2: '周二',
    3: '周三',
    4: '周四',
    5: '周五',
    6: '周六'
  }

  // 3. 处理星期部分
  // 去重并排序（避免乱序，如 [3,1,2] → [1,2,3]）
  const sortedWeekDays = [...new Set(businessHours.weekDays)].sort((a, b) => a - b)
  let weekDesc = ''

  // 3.1 处理连续星期（如 [1,2,3,4,5] → 周一至周五）
  if (sortedWeekDays.length === 1) {
    // 单个星期
    weekDesc = weekDayMap[sortedWeekDays[0]]
  } else {
    // 检查是否是连续的星期
    let isContinuous = true
    for (let i = 1; i < sortedWeekDays.length; i++) {
      if (sortedWeekDays[i] - sortedWeekDays[i - 1] !== 1) {
        isContinuous = false
        break
      }
    }

    if (isContinuous) {
      // 连续星期：取第一个和最后一个
      weekDesc = `${weekDayMap[sortedWeekDays[0]]}至${weekDayMap[sortedWeekDays[sortedWeekDays.length - 1]]}`
    } else {
      // 非连续星期：拼接所有（如 [1,3,5] → 周一、周三、周五）
      weekDesc = sortedWeekDays.map(day => weekDayMap[day]).join('、')
    }
  }

  // 4. 处理时间部分
  let timeDesc = ''
  const { startTime, endTime } = businessHours
  if (startTime && endTime && /^\d{2}:\d{2}$/.test(startTime) && /^\d{2}:\d{2}$/.test(endTime)) {
    timeDesc = `${startTime}-${endTime}`
  } else {
    timeDesc = '时间未配置'
  }

  // 5. 组合最终结果
  return `${weekDesc} ${timeDesc}`
}

function base64Decode(str) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/='
  let output = ''
  str = str.replace(/[^A-Za-z0-9+/=]/g, '')
  let i = 0
  while (i < str.length) {
    const enc1 = chars.indexOf(str[i++])
    const enc2 = chars.indexOf(str[i++])
    const enc3 = chars.indexOf(str[i++])
    const enc4 = chars.indexOf(str[i++])
    const chr1 = (enc1 << 2) | (enc2 >> 4)
    const chr2 = ((enc2 & 15) << 4) | (enc3 >> 2)
    const chr3 = ((enc3 & 3) << 6) | enc4
    output += String.fromCharCode(chr1)
    if (enc3 !== 64) output += String.fromCharCode(chr2)
    if (enc4 !== 64) output += String.fromCharCode(chr3)
  }
  // 处理 UTF-8 编码
  return decodeURIComponent(output.split('').map(c =>
    '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
  ).join(''))
}

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

export const getEnvType = () => {
  // #ifdef MP-WEIXIN
  return 'weapp'; // 微信小程序（包括开发者工具、真机）
  // #endif

  // #ifdef H5
  return 'h5'; // H5/浏览器环境（包括微信内置浏览器、普通浏览器）
  // #endif

  // 其他环境（如App/支付宝小程序等）
  return 'other';
};