<script setup lang="ts">
import { getOrdersStatistics } from '@/api/orders';
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue';
import dayjs from 'dayjs';
import * as echarts from 'echarts';
import UpdateComp from '@/components/UpdateComp/index.vue'

defineOptions({ name: 'Welcome' });

interface OrderItem {
  id: number;
  unitPrice: number;
  count: number;
  subtotal: number;
  specGroupJson: string;
  goods?: { id: number; name: string; imgUrl?: string };
}

interface Order {
  id: number;
  orderNo: string;
  wxOrderNo: string;
  totalAmount: number;
  orderType: string;
  pickupTime: string;
  phone: string;
  remark: string;
  status: number;
  createdAt: string;
  payTime?: string;
  orderItem: OrderItem[];
}

interface RefundRecord {
  id: number;
  wxOrderNo: string;
  refundId: string;
  totalFee: number;
  refundFee: number;
  refundStatus: string;
  successTime: string;
  refundRecvAccout: string;
}

// ── State ────────────────────────────────────────────────
const loading = ref(false);
const orders = ref<Order[]>([]);
const refunds = ref<RefundRecord[]>([]);
const activeQuick = ref('');

const revenueChartRef = ref<HTMLDivElement>();
const typeChartRef = ref<HTMLDivElement>();
let revenueChart: echarts.ECharts | null = null;
let typeChart: echarts.ECharts | null = null;

const dateRange = ref<[string, string]>([
  dayjs().subtract(29, 'day').startOf('day').format('YYYY-MM-DD HH:mm:ss'),
  dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss'),
]);

// ── Shortcuts ────────────────────────────────────────────
const shortcuts = [
  {
    text: '今天',
    key: 'today',
    value: (): [string, string] => [
      dayjs().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
      dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss'),
    ],
  },
  {
    text: '昨天',
    key: 'yesterday',
    value: (): [string, string] => [
      dayjs().subtract(1, 'day').startOf('day').format('YYYY-MM-DD HH:mm:ss'),
      dayjs().subtract(1, 'day').endOf('day').format('YYYY-MM-DD HH:mm:ss'),
    ],
  },
  {
    text: '近7天',
    key: 'last7',
    value: (): [string, string] => [
      dayjs().subtract(6, 'day').startOf('day').format('YYYY-MM-DD HH:mm:ss'),
      dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss'),
    ],
  },
  {
    text: '近30天',
    key: 'last30',
    value: (): [string, string] => [
      dayjs().subtract(29, 'day').startOf('day').format('YYYY-MM-DD HH:mm:ss'),
      dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss'),
    ],
  },
];

// ── Status Map ───────────────────────────────────────────
const statusMap: Record<number, { label: string; type: 'success' | 'warning' | 'info' | 'danger' }> = {
  1: { label: '待支付', type: 'info' },
  2: { label: '已支付', type: 'success' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'info' },
  5: { label: '已退款', type: 'danger' },
};

// ── Computed Stats ───────────────────────────────────────
const validOrders = computed(() =>
  orders.value.filter((o) => ![1, 4, 5].includes(o.status))
);

const totalRevenue = computed(() =>
  validOrders.value.reduce((s, o) => s + (o.totalAmount || 0), 0)
);

const totalOrders = computed(() => validOrders.value.length);

const completedOrders = computed(() =>
  orders.value.filter((o) => o.status === 3).length
);

const avgOrderValue = computed(() =>
  totalOrders.value ? totalRevenue.value / totalOrders.value : 0
);

const dineInCount = computed(() =>
  validOrders.value.filter((o) => o.orderType === 'dinein').length
);

const takeoutCount = computed(() =>
  validOrders.value.filter((o) => o.orderType !== 'dinein').length
);

const totalRefundAmount = computed(() =>
  refunds.value.reduce((s, r) => s + (r.refundFee || 0), 0) / 100
);

const totalRefunds = computed(() => refunds.value.length);

// ── Daily Revenue Data ───────────────────────────────────
const dailyRevenue = computed(() => {
  const map: Record<string, number> = {};
  validOrders.value.forEach((o) => {
    const day = o.createdAt?.slice(0, 10) || '';
    if (day) map[day] = (map[day] || 0) + (o.totalAmount || 0);
  });
  return Object.entries(map).sort(([a], [b]) => a.localeCompare(b));
});

// ── Daily Order Count Data ───────────────────────────────
const dailyOrderCount = computed(() => {
  const map: Record<string, number> = {};
  validOrders.value.forEach((o) => {
    const day = o.createdAt?.slice(0, 10) || '';
    if (day) map[day] = (map[day] || 0) + 1;
  });
  return Object.entries(map).sort(([a], [b]) => a.localeCompare(b));
});

// ── Top Goods ────────────────────────────────────────────
const topGoods = computed(() => {
  const map: Record<number, { name: string; count: number; revenue: number; imgUrl?: string }> = {};
  validOrders.value.forEach((o) => {
    o.orderItem?.forEach((item) => {
      const gid = item.goods?.id ?? 0;
      const name = item.goods?.name || `商品${gid}`;
      const imgUrl = item.goods?.imgUrl;
      if (!map[gid]) map[gid] = { name, count: 0, revenue: 0, imgUrl };
      map[gid].count += item.count || 1;
      map[gid].revenue += item.subtotal || 0;
    });
  });
  return Object.values(map)
    .sort((a, b) => b.revenue - a.revenue)
    .slice(0, 5);
});

// ── ECharts ──────────────────────────────────────────────
function initRevenueChart() {
  if (!revenueChartRef.value) return;
  revenueChart = echarts.init(revenueChartRef.value);
  updateRevenueChart();
}

function updateRevenueChart() {
  if (!revenueChart) return;

  const days = dailyRevenue.value.map(([d]) => d.slice(5));
  const revenues = dailyRevenue.value.map(([, v]) => v);
  const counts = dailyOrderCount.value.map(([, v]) => v);

  revenueChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: { color: '#374151', fontSize: 13 },
      axisPointer: {
        type: 'cross',
        crossStyle: { color: '#d1d5db' },
      },
      formatter(params: any) {
        const date = dailyRevenue.value[params[0]?.dataIndex]?.[0] || '';
        let html = `<div style="font-weight:600;margin-bottom:6px;color:#111827">${date}</div>`;
        params.forEach((p: any) => {
          const dot = `<span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:${p.color};margin-right:6px"></span>`;
          const val = p.seriesName === '营业额' ? `¥${p.value.toFixed(2)}` : `${p.value} 笔`;
          html += `<div style="display:flex;align-items:center;justify-content:space-between;gap:16px;padding:2px 0">
            <span>${dot}${p.seriesName}</span>
            <span style="font-weight:600">${val}</span>
          </div>`;
        });
        return html;
      },
    },
    legend: {
      data: ['营业额', '订单数'],
      top: 0,
      right: 0,
      textStyle: { color: '#9ca3af', fontSize: 12 },
      itemWidth: 16,
      itemHeight: 8,
      itemGap: 16,
    },
    grid: {
      top: 60,
      left: 12,
      right: 12,
      bottom: 8,
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisTick: { show: false },
      axisLabel: { color: '#9ca3af', fontSize: 11 },
    },
    yAxis: [
      {
        type: 'value',
        name: '营业额',
        nameTextStyle: { color: '#9ca3af', fontSize: 11, padding: [0, 40, 0, 0] },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { color: '#f3f4f6', type: 'dashed' } },
        axisLabel: {
          color: '#9ca3af',
          fontSize: 11,
          formatter: (v: number) => (v >= 1000 ? `${(v / 1000).toFixed(1)}k` : `${v}`),
        },
      },
      {
        type: 'value',
        name: '订单数',
        nameTextStyle: { color: '#9ca3af', fontSize: 11, padding: [0, 0, 0, 20] },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#9ca3af', fontSize: 11 },
      },
    ],
    series: [
      {
        name: '营业额',
        type: 'bar',
        data: revenues,
        yAxisIndex: 0,
        barWidth: days.length > 15 ? 12 : 20,
        barMaxWidth: 28,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#fbbf24' },
            { offset: 1, color: '#f59e0b' },
          ]),
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#f59e0b' },
              { offset: 1, color: '#d97706' },
            ]),
          },
        },
      },
      {
        name: '订单数',
        type: 'line',
        data: counts,
        yAxisIndex: 1,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: '#6366f1' },
        itemStyle: { color: '#6366f1', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(99,102,241,0.15)' },
            { offset: 1, color: 'rgba(99,102,241,0.01)' },
          ]),
        },
      },
    ],
    animationDuration: 600,
    animationEasing: 'cubicOut',
  });
}

function initTypeChart() {
  if (!typeChartRef.value) return;
  typeChart = echarts.init(typeChartRef.value);
  updateTypeChart();
}

function updateTypeChart() {
  if (!typeChart) return;

  const statusData = Object.entries(statusMap).map(([code, s]) => ({
    name: s.label,
    value: orders.value.filter((o) => o.status === Number(code)).length,
  }));

  typeChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: { color: '#374151', fontSize: 13 },
      formatter: '{b}: {c} 笔 ({d}%)',
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: '#6b7280', fontSize: 12 },
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 12,
      icon: 'circle',
    },
    series: [
      {
        name: '订单类型',
        type: 'pie',
        radius: ['48%', '72%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        padAngle: 3,
        itemStyle: { borderRadius: 6 },
        label: {
          show: true,
          position: 'center',
          formatter: () => `{total|${totalOrders.value}}\n{label|总成交订单}`,
          rich: {
            total: { fontSize: 22, fontWeight: 'bold', color: '#111827', lineHeight: 30 },
            label: { fontSize: 12, color: '#9ca3af', lineHeight: 20 },
          },
        },
        emphasis: {
          label: { show: true },
          scaleSize: 6,
        },
        data: [
          {
            value: dineInCount.value,
            name: '堂食',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#fbbf24' },
                { offset: 1, color: '#f59e0b' },
              ]),
            },
          },
          {
            value: takeoutCount.value,
            name: '外带',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: '#818cf8' },
                { offset: 1, color: '#6366f1' },
              ]),
            },
          },
        ],
        animationType: 'scale',
        animationEasing: 'cubicOut',
        animationDuration: 600,
      },
      {
        name: '订单状态',
        type: 'pie',
        radius: ['80%', '88%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        padAngle: 2,
        itemStyle: { borderRadius: 4 },
        label: { show: false },
        emphasis: { scaleSize: 4 },
        data: statusData,
        color: ['#93c5fd', '#34d399', '#6ee7b7', '#d1d5db', '#fca5a5'],
        animationType: 'scale',
        animationEasing: 'cubicOut',
        animationDuration: 800,
        animationDelay: 200,
      },
    ],
  });
}

// ── Actions ──────────────────────────────────────────────
function applyShortcut(sc: (typeof shortcuts)[0]) {
  activeQuick.value = sc.key;
  dateRange.value = sc.value();
  fetchData();
}

applyShortcut(shortcuts[3])

function onDateChange() {
  activeQuick.value = '';
}

async function fetchData() {
  if (!dateRange.value?.[0] || !dateRange.value?.[1]) return;
  loading.value = true;
  try {
    const res: any = await getOrdersStatistics(dateRange.value[0], dateRange.value[1]);
    orders.value = res?.ordersList || [];
    refunds.value = res?.refundRecordList || [];
  } finally {
    loading.value = false;
  }
}

// ── Watch & Lifecycle ────────────────────────────────────
watch([dailyRevenue, dailyOrderCount], () => {
  nextTick(updateRevenueChart);
});

watch([dineInCount, takeoutCount, orders], () => {
  nextTick(updateTypeChart);
});

function handleResize() {
  revenueChart?.resize();
  typeChart?.resize();
}

onMounted(async () => {
  await fetchData();
  await nextTick();
  initRevenueChart();
  initTypeChart();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  revenueChart?.dispose();
  typeChart?.dispose();
});
</script>

<template>
  <div class="dashboard min-h-screen pb-8">
    <!-- Header -->
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-800 tracking-tight flex items-center gap-2">
          <span>营业统计</span>
          <UpdateComp />
        </h1>
        <p class="text-sm text-gray-400 mt-0.5">Sales Analytics Dashboard</p>
      </div>
      <div class="flex items-center gap-2">
        <el-tag type="info" size="small" class="!rounded-full">
          {{ totalOrders }} 笔订单
        </el-tag>
        <el-tag v-if="totalRefunds > 0" type="danger" size="small" class="!rounded-full">
          {{ totalRefunds }} 笔退款
        </el-tag>
      </div>
    </div>

    <!-- Filter Bar -->
    <el-card shadow="never" class="filter-bar mb-5 !rounded-2xl !border-gray-100">
      <div class="flex flex-wrap items-center gap-3">
        <div class="flex gap-2">
          <el-button v-for="sc in shortcuts" :key="sc.key" :type="activeQuick === sc.key ? 'primary' : 'default'"
            size="small" round @click="applyShortcut(sc)">
            {{ sc.text }}
          </el-button>
        </div>
        <el-divider direction="vertical" class="!h-6" />
        <div>
          <el-date-picker v-model="dateRange" type="datetimerange" range-separator="至" start-placeholder="开始时间"
            end-placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss" @change="onDateChange" />
        </div>
        <div>
          <el-button type="primary" :loading="loading" @click="fetchData">
            查询
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- KPI Cards -->
    <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-5">
      <el-card v-for="kpi in [
        {
          title: '总营业额',
          value: `¥${totalRevenue.toFixed(2)}`,
          sub: `${completedOrders} 笔已完成`,
          icon: '💰',
          iconBg: 'bg-amber-50',
          bar: 'from-amber-400 to-orange-400',
        },
        {
          title: '订单总数',
          value: totalOrders,
          sub: `客单价 ¥${avgOrderValue.toFixed(2)}`,
          icon: '📋',
          iconBg: 'bg-blue-50',
          bar: 'from-blue-400 to-indigo-400',
        },
        {
          title: '堂食 / 外带',
          value: `${dineInCount} / ${takeoutCount}`,
          sub: `堂食 ${totalOrders ? Math.round((dineInCount / totalOrders) * 100) : 0}%`,
          icon: '🪑',
          iconBg: 'bg-emerald-50',
          bar: 'from-emerald-400 to-teal-400',
        },
        {
          title: '退款总额',
          value: `¥${totalRefundAmount.toFixed(2)}`,
          sub: `共 ${totalRefunds} 笔退款`,
          icon: '↩️',
          iconBg: 'bg-red-50',
          bar: 'from-red-400 to-rose-400',
        },
      ]" :key="kpi.title" shadow="never" class="kpi-card !rounded-2xl !border-gray-100 overflow-hidden">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-xs text-gray-400 uppercase tracking-wider mb-2">{{ kpi.title }}</p>
            <p class="text-2xl font-bold text-gray-800">{{ kpi.value }}</p>
            <p class="text-xs text-gray-400 mt-1">{{ kpi.sub }}</p>
          </div>
          <div class="w-10 h-10 rounded-xl flex items-center justify-center text-xl flex-shrink-0" :class="kpi.iconBg">
            {{ kpi.icon }}
          </div>
        </div>
        <div class="mt-4 h-1 rounded-full bg-gradient-to-r" :class="kpi.bar" />
      </el-card>
    </div>

    <!-- Charts Row -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-5">
      <!-- Daily Revenue Chart (ECharts) -->
      <el-card shadow="never" class="lg:col-span-2 !rounded-2xl !border-gray-100">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-semibold text-gray-700">每日营业额趋势</span>
            <span class="text-xs text-gray-400">Revenue & Orders Trend</span>
          </div>
        </template>
        <div ref="revenueChartRef" class="w-full" style="height: 320px" />
        <el-empty v-if="!dailyRevenue.length && !loading" description="暂无数据" :image-size="80" />
      </el-card>

      <!-- Order Type Pie Chart (ECharts) -->
      <el-card shadow="never" class="!rounded-2xl !border-gray-100">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-semibold text-gray-700">订单类型分布</span>
            <span class="text-xs text-gray-400">Order Types</span>
          </div>
        </template>
        <div ref="typeChartRef" class="w-full" style="height: 320px" />
      </el-card>
    </div>

    <!-- Top Goods + Order Table -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-5">
      <!-- Top Goods -->
      <el-card shadow="never" class="!rounded-2xl !border-gray-100">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-semibold text-gray-700">热销商品 Top 5</span>
            <span class="text-xs text-gray-400">Best Sellers</span>
          </div>
        </template>
        <div v-if="topGoods.length" class="space-y-4">
          <div v-for="(g, i) in topGoods" :key="i" class="flex items-center gap-3">
            <div class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0" :class="{
              'bg-amber-100 text-amber-600': i === 0,
              'bg-gray-100 text-gray-500': i === 1,
              'bg-orange-100 text-orange-500': i === 2,
              'bg-gray-50 text-gray-400': i >= 3,
            }">
              {{ i + 1 }}
            </div>
            <img v-if="g.imgUrl" :src="g.imgUrl" class="w-8 h-8 rounded-lg object-cover flex-shrink-0" />
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-baseline mb-1.5">
                <span class="text-sm font-medium text-gray-700 truncate mr-2">{{ g.name }}</span>
                <span class="text-xs text-gray-400 flex-shrink-0">×{{ g.count }}</span>
              </div>
              <div class="h-1.5 bg-gray-100 rounded-full overflow-hidden">
                <div
                  class="h-full rounded-full bg-gradient-to-r from-amber-400 to-orange-400 transition-all duration-500"
                  :style="{ width: (g.revenue / topGoods[0].revenue) * 100 + '%' }" />
              </div>
            </div>
            <span class="text-sm font-semibold text-gray-800 flex-shrink-0">
              ¥{{ g.revenue.toFixed(2) }}
            </span>
          </div>
        </div>
        <el-empty v-else description="暂无数据" :image-size="80" />
      </el-card>

      <!-- Order List -->
      <el-card shadow="never" class="lg:col-span-2 !rounded-2xl !border-gray-100">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-semibold text-gray-700">近期订单明细</span>
            <span class="text-xs text-gray-400">
              {{
                orders.length > 10
                  ? `仅展示前 10 条，共 ${orders.length} 条`
                  : `共 ${orders.length} 条`
              }}
            </span>
          </div>
        </template>
        <el-table :data="orders.slice(0, 10)" v-loading="loading" size="small" style="width: 100%" :header-cell-style="{
          background: '#f9fafb',
          color: '#6b7280',
          fontWeight: '600',
          fontSize: '12px',
        }">
          <el-table-column prop="orderNo" label="订单号" width="72" />
          <el-table-column label="商品" min-width="110">
            <template #default="{ row }">
              <span class="text-gray-600 text-xs">
                {{
                  row.orderItem?.map((i: OrderItem) => i.goods?.name || '—').join('、') || '—'
                }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="80">
            <template #default="{ row }">
              <span class="font-semibold text-gray-800">¥{{ row.totalAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="68">
            <template #default="{ row }">
              <el-tag :type="row.orderType === 'dinein' ? 'warning' : 'primary'" size="small" round>
                {{ row.orderType === 'dinein' ? '堂食' : '外带' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="76">
            <template #default="{ row }">
              <el-tag :type="statusMap[row.status]?.type || 'info'" size="small" round>
                {{ statusMap[row.status]?.label || '未知' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="下单时间" min-width="130">
            <template #default="{ row }">
              <span class="text-gray-400 text-xs">
                {{ row.createdAt?.replace('T', ' ').slice(0, 19) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- Refund Records -->
    <el-card v-if="refunds.length" shadow="never" class="!rounded-2xl !border-gray-100">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-semibold text-gray-700">退款记录</span>
          <span class="text-xs text-gray-400">
            共 {{ refunds.length }} 笔 · 合计 ¥{{ totalRefundAmount.toFixed(2) }}
          </span>
        </div>
      </template>
      <el-table :data="refunds" size="small" style="width: 100%" :header-cell-style="{
        background: '#f9fafb',
        color: '#6b7280',
        fontWeight: '600',
        fontSize: '12px',
      }">
        <el-table-column prop="refundId" label="退款单号" min-width="180">
          <template #default="{ row }">
            <span class="text-gray-500 text-xs font-mono">{{ row.refundId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="退款金额" width="90">
          <template #default="{ row }">
            <span class="font-semibold text-red-500">
              ¥{{ (row.refundFee / 100).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.refundStatus === 'SUCCESS' ? 'success' : 'warning'" size="small" round>
              {{ row.refundStatus === 'SUCCESS' ? '退款成功' : row.refundStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="refundRecvAccout" label="退款去向" min-width="110">
          <template #default="{ row }">
            <span class="text-gray-500 text-xs">{{ row.refundRecvAccout }}</span>
          </template>
        </el-table-column>
        <el-table-column label="退款时间" min-width="140">
          <template #default="{ row }">
            <span class="text-gray-400 text-xs">{{ row.successTime }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard {
  --card-transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.kpi-card {
  transition: var(--card-transition);
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}

:deep(.el-table) {
  --el-table-border-color: #f3f4f6;
  --el-table-row-hover-bg-color: #fafbfc;
}

:deep(.el-table td.el-table__cell) {
  border-bottom-color: #f3f4f6;
}
</style>