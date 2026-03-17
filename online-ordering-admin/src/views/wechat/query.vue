<script setup lang="ts">
import { ref } from 'vue'
import { getWxOrdersByWxOrderNo } from '@/api/wxOrders'

const orderNo = ref('') // 5183657a32c74c949e438941a06fa160
const loading = ref(false)
const orderData = ref<any>(null)
const errorMsg = ref('')

async function queryOrder() {
  if (!orderNo.value.trim()) {
    errorMsg.value = '请输入订单号'
    return
  }
  errorMsg.value = ''
  orderData.value = null
  loading.value = true
  try {
    const res = await getWxOrdersByWxOrderNo(orderNo.value.trim())
    orderData.value = res
  } catch (e: any) {
    errorMsg.value = e?.message || '查询失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function getTradeStateTag(state: string) {
  const map: Record<string, { type: string; label: string }> = {
    SUCCESS: { type: 'success', label: '支付成功' },
    REFUND: { type: 'warning', label: '转入退款' },
    NOTPAY: { type: 'info', label: '未支付' },
    CLOSED: { type: 'danger', label: '已关闭' },
    REVOKED: { type: 'danger', label: '已撤销' },
    USERPAYING: { type: 'warning', label: '用户支付中' },
    PAYERROR: { type: 'danger', label: '支付失败' },
  }
  return map[state] ?? { type: 'info', label: state }
}

function formatFee(fee: number | null | undefined) {
  if (fee == null || isNaN(Number(fee))) return '—'
  return (Number(fee) / 100).toFixed(2)
}

function formatTime(t: string) {
  if (!t || t.length !== 14) return t
  return `${t.slice(0, 4)}-${t.slice(4, 6)}-${t.slice(6, 8)} ${t.slice(8, 10)}:${t.slice(10, 12)}:${t.slice(12, 14)}`
}

const fieldGroups = [
  {
    title: '交易信息',
    icon: '💳',
    fields: [
      { label: '商户订单号', key: 'outTradeNo' },
      { label: '微信交易号', key: 'transactionId' },
      { label: '交易类型', key: 'tradeType' },
      { label: '交易状态', key: 'tradeState', special: 'state' },
      { label: '状态描述', key: 'tradeStateDesc' },
      { label: '支付完成时间', key: 'timeEnd', special: 'time' },
    ],
  },
  {
    title: '金额信息',
    icon: '¥',
    fields: [
      { label: '订单总金额', key: 'totalFee', special: 'fee', suffix: 'CNY' },
      { label: '现金支付金额', key: 'cashFee', special: 'fee', suffix: 'CNY' },
      { label: '货币类型', key: 'feeType' },
      { label: '现金货币类型', key: 'cashFeeType' },
    ],
  },
  {
    title: '商户信息',
    icon: '🏦',
    fields: [
      { label: 'APPID', key: 'appid' },
      { label: '商户号', key: 'mchId' },
      { label: '银行类型', key: 'bankType' },
    ],
  },
  {
    title: '用户信息',
    icon: '👤',
    fields: [
      { label: 'OpenID', key: 'openid' },
      { label: '是否关注公众号', key: 'isSubscribe', special: 'subscribe' },
      { label: '附加数据', key: 'attach' },
    ],
  },
]
</script>

<template>
  <div class="page-root">
    <!-- Ambient background grid -->
    <div class="bg-grid" aria-hidden="true" />
    <!-- Glow blobs -->
    <div class="glow glow-1" aria-hidden="true" />
    <div class="glow glow-2" aria-hidden="true" />

    <div class="content-wrap">
      <!-- ── Header ── -->
      <header class="page-header">
        <div class="header-badge">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"
            stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="5" width="20" height="14" rx="3" />
            <path d="M2 10h20" />
          </svg>
          <span>支付中心</span>
        </div>
        <h1 class="page-title">商户订单号查询</h1>
        <p class="page-sub">WX ORDER LOOKUP</p>
      </header>

      <!-- ── Search ── -->
      <section class="search-card">
        <div class="search-inner">
          <div class="search-label">订单号</div>
          <div class="search-row">
            <div class="input-wrap" :class="{ 'has-error': errorMsg }">
              <span class="input-prefix">NO.</span>
              <el-input v-model="orderNo" placeholder="请输入微信订单号 (out_trade_no)" size="large" clearable
                @keyup.enter="queryOrder" class="wx-input" />
            </div>
            <button class="query-btn" :disabled="loading" @click="queryOrder">
              <span v-if="!loading" class="btn-text">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
                  stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8" />
                  <path d="m21 21-4.35-4.35" />
                </svg>
                查询
              </span>
              <span v-else class="btn-text">
                <svg class="spin-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                  stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 12a9 9 0 1 1-6.219-8.56" />
                </svg>
                查询中
              </span>
            </button>
          </div>
          <p v-if="errorMsg" class="error-msg">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
              stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            {{ errorMsg }}
          </p>
        </div>
      </section>

      <!-- ── Loading ── -->
      <div v-if="loading" class="state-loading">
        <div class="loading-ring">
          <div class="ring-track" />
          <div class="ring-head" />
        </div>
        <p class="loading-text">正在查询订单数据…</p>
        <div class="loading-dots">
          <span /><span /><span />
        </div>
      </div>

      <!-- ── Results ── -->
      <div v-else-if="orderData" class="results-wrap">

        <!-- Status Banner -->
        <div class="status-banner" :class="orderData.tradeState === 'SUCCESS' ? 'banner-success' : 'banner-fail'">
          <div class="banner-left">
            <div class="banner-icon-wrap" :class="orderData.tradeState === 'SUCCESS' ? 'icon-success' : 'icon-fail'">
              <svg v-if="orderData.tradeState === 'SUCCESS'" width="22" height="22" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 6 9 17l-5-5" />
              </svg>
              <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </div>
            <div>
              <p class="banner-state"
                :class="orderData.tradeState === 'SUCCESS' ? 'state-success-text' : 'state-fail-text'">
                {{ orderData.tradeStateDesc || orderData.tradeState }}
              </p>
              <p class="banner-time">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                  stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
                {{ formatTime(orderData.timeEnd) }}
              </p>
            </div>
          </div>
          <div class="banner-right">
            <p class="banner-fee-label">实付金额</p>
            <p class="banner-fee" :class="orderData.tradeState === 'SUCCESS' ? 'fee-success' : 'fee-dim'">
              <span class="fee-currency">¥</span>{{ formatFee(orderData.cashFee != null ? orderData.cashFee :
                orderData.totalFee) }}
            </p>
          </div>
        </div>

        <!-- Field Groups -->
        <div v-for="group in fieldGroups" :key="group.title" class="field-card">
          <div class="field-card-header">
            <span class="card-icon">{{ group.icon }}</span>
            <h2 class="card-title">{{ group.title }}</h2>
            <div class="card-line" />
          </div>
          <div class="field-list">
            <div v-for="(field, idx) in group.fields" :key="field.key" class="field-row"
              :style="{ animationDelay: `${idx * 40}ms` }">
              <span class="field-label">{{ field.label }}</span>

              <template v-if="field.special === 'fee'">
                <span class="field-value-fee">
                  <template v-if="orderData[field.key] != null && !isNaN(Number(orderData[field.key]))">
                    <span class="fee-sign">¥</span>{{ formatFee(orderData[field.key]) }}
                    <span class="fee-fen">({{ orderData[field.key] }} 分)</span>
                  </template>
                  <span v-else class="field-empty">—</span>
                </span>
              </template>

              <template v-else-if="field.special === 'state'">
                <el-tag :type="getTradeStateTag(orderData[field.key]).type as any" size="small" effect="dark"
                  class="custom-tag">
                  {{ getTradeStateTag(orderData[field.key]).label }}
                </el-tag>
              </template>

              <template v-else-if="field.special === 'time'">
                <span class="field-value-time">{{ formatTime(orderData[field.key]) }}</span>
              </template>

              <template v-else-if="field.special === 'subscribe'">
                <el-tag :type="orderData[field.key] === 'Y' ? 'success' : 'info'" size="small" effect="dark"
                  class="custom-tag">
                  {{ orderData[field.key] === 'Y' ? '已关注' : '未关注' }}
                </el-tag>
              </template>

              <template v-else>
                <span :class="['field-value', orderData[field.key] ? '' : 'field-empty']">
                  {{ orderData[field.key] || '—' }}
                </span>
              </template>
            </div>
          </div>
        </div>

        <!-- Footer Meta -->
        <div class="meta-footer">
          <div class="meta-item">
            <span class="meta-key">returnCode</span>
            <el-tag :type="orderData.returnCode === 'SUCCESS' ? 'success' : 'danger'" size="small" effect="dark"
              class="custom-tag">
              {{ orderData.returnCode }}
            </el-tag>
          </div>
          <div class="meta-divider" />
          <div class="meta-item">
            <span class="meta-key">resultCode</span>
            <el-tag :type="orderData.resultCode === 'SUCCESS' ? 'success' : 'danger'" size="small" effect="dark"
              class="custom-tag">
              {{ orderData.resultCode }}
            </el-tag>
          </div>
          <div class="meta-divider" />
          <div class="meta-item meta-nonce">
            <span class="meta-key">nonceStr</span>
            <span class="meta-val">{{ orderData.nonceStr }}</span>
          </div>
        </div>
      </div>

      <!-- ── Empty State ── -->
      <div v-else class="state-empty">
        <div class="empty-icon-wrap">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
            stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
        </div>
        <p class="empty-title">暂无数据</p>
        <p class="empty-sub">输入微信订单号后点击查询</p>
      </div>

    </div>
  </div>
</template>

<style scoped>
:root {
  --bg: #07090f;
  --surface-1: #0d1117;
  --surface-2: #131822;
  --surface-3: #1a2233;
  --border: rgba(255, 255, 255, 0.07);
  --border-hover: rgba(255, 255, 255, 0.14);
  --accent: #07e8a4;
  --accent-dim: rgba(7, 232, 164, 0.15);
  --accent-glow: rgba(7, 232, 164, 0.35);
  --red: #ff4d6d;
  --red-dim: rgba(255, 77, 109, 0.15);
  --text-primary: #e8edf5;
  --text-secondary: #7a8aaa;
  --text-muted: #3d4a62;
  --font-mono: 'JetBrains Mono', 'Fira Code', 'Cascadia Code', ui-monospace, monospace;
  --radius: 14px;
  --radius-sm: 8px;
}

/* ─── Base ──────────────────────────────────────── */
.page-root {
  position: relative;
  min-height: 100vh;
  color: var(--text-primary);
  font-family: var(--font-mono);
  overflow-x: hidden;
  padding: 40px 20px 80px;
}

/* ─── Background Grid ───────────────────────────── */
.bg-grid {
  position: fixed;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.022) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.022) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 80% 80% at 50% 50%, black 0%, transparent 100%);
  z-index: 0;
}

/* ─── Glow Blobs ────────────────────────────────── */
.glow {
  position: fixed;
  border-radius: 50%;
  filter: blur(100px);
  pointer-events: none;
  z-index: 0;
}

.glow-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(7, 232, 164, 0.06) 0%, transparent 70%);
  top: -200px;
  right: -100px;
}

.glow-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.05) 0%, transparent 70%);
  bottom: 0;
  left: -150px;
}

/* ─── Layout ────────────────────────────────────── */
.content-wrap {
  position: relative;
  z-index: 1;
  max-width: 860px;
  margin: 0 auto;
}

/* ─── Header ────────────────────────────────────── */
.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.header-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 5px 14px;
  border-radius: 100px;
  background: var(--accent-dim);
  border: 1px solid rgba(7, 232, 164, 0.3);
  color: var(--accent);
  font-size: 11px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  font-weight: 600;
  margin-bottom: 20px;
}

.page-title {
  font-size: clamp(26px, 4vw, 36px);
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--text-primary);
  margin: 0 0 8px;
  line-height: 1.2;
}

.page-sub {
  font-size: 11px;
  letter-spacing: 0.35em;
  color: var(--text-muted);
  margin: 0;
  text-transform: uppercase;
}

/* ─── Search Card ───────────────────────────────── */
.search-card {
  background: var(--surface-1);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  margin-bottom: 32px;
  overflow: hidden;
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.04) inset,
    0 12px 24px rgba(0, 0, 0, 0.1);
  transition: border-color 0.3s;
}

.search-card:focus-within {
  border-color: rgba(7, 232, 164, 0.3);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.04) inset,
    0 12px 24px rgba(0, 0, 0, 0.12),
    0 0 20px rgba(7, 232, 164, 0.06);
}

.search-inner {
  padding: 28px 32px;
}

.search-label {
  font-size: 10px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: var(--text-muted);
  margin-bottom: 12px;
  font-weight: 600;
}

.search-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.input-wrap {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.input-prefix {
  position: absolute;
  left: 16px;
  z-index: 2;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: var(--accent);
  pointer-events: none;
}

/* Query button */
.query-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: var(--radius-sm);
  background: var(--accent);
  border: none;
  color: #07090f;
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 2px 10px rgba(7, 232, 164, 0.3);
  white-space: nowrap;
  flex-shrink: 0;
}

.query-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(7, 232, 164, 0.45);
  background: #22f5b6;
}

.query-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 1px 5px rgba(7, 232, 164, 0.2);
}

.query-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.spin-icon {
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.error-msg {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  color: var(--red);
  font-size: 12px;
  letter-spacing: 0.04em;
}

/* ─── Loading State ─────────────────────────────── */
.state-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  gap: 20px;
}

.loading-ring {
  position: relative;
  width: 52px;
  height: 52px;
}

.ring-track {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 2px solid var(--surface-3);
}

.ring-head {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 2px solid transparent;
  border-top-color: var(--accent);
  animation: spin 0.85s cubic-bezier(0.42, 0.01, 0.58, 1) infinite;
  box-shadow: 0 0 6px var(--accent-glow);
}

.loading-text {
  color: var(--text-secondary);
  font-size: 12px;
  letter-spacing: 0.12em;
  margin: 0;
}

.loading-dots {
  display: flex;
  gap: 6px;
}

.loading-dots span {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--accent);
  animation: blink 1.2s ease-in-out infinite;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes blink {

  0%,
  80%,
  100% {
    opacity: 0.2;
  }

  40% {
    opacity: 1;
  }
}

/* ─── Results ───────────────────────────────────── */
.results-wrap {
  display: flex;
  flex-direction: column;
  gap: 20px;
  animation: slide-up 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(16px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ─── Status Banner ─────────────────────────────── */
.status-banner {
  border-radius: var(--radius);
  padding: 24px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  position: relative;
  overflow: hidden;
}

.status-banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(-45deg,
      transparent,
      transparent 20px,
      rgba(255, 255, 255, 0.012) 20px,
      rgba(255, 255, 255, 0.012) 40px);
  pointer-events: none;
}

.banner-success {
  background: linear-gradient(135deg, rgba(7, 232, 164, 0.1), rgba(7, 232, 164, 0.04));
  border: 1px solid rgba(7, 232, 164, 0.3);
  box-shadow: 0 0 30px rgba(7, 232, 164, 0.08) inset;
}

.banner-fail {
  background: linear-gradient(135deg, rgba(255, 77, 109, 0.1), rgba(255, 77, 109, 0.04));
  border: 1px solid rgba(255, 77, 109, 0.3);
  box-shadow: 0 0 30px rgba(255, 77, 109, 0.08) inset;
}

.banner-left {
  display: flex;
  align-items: center;
  gap: 18px;
}

.banner-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-success {
  background: rgba(7, 232, 164, 0.15);
  color: var(--accent);
  border: 1px solid rgba(7, 232, 164, 0.3);
  box-shadow: 0 0 10px rgba(7, 232, 164, 0.2);
}

.icon-fail {
  background: var(--red-dim);
  color: var(--red);
  border: 1px solid rgba(255, 77, 109, 0.3);
  box-shadow: 0 0 10px rgba(255, 77, 109, 0.15);
}

.banner-state {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.01em;
  margin: 0 0 5px;
  line-height: 1;
}

.state-success-text {
  color: var(--accent);
}

.state-fail-text {
  color: var(--red);
}

.banner-time {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--text-secondary);
  font-size: 12px;
  margin: 0;
}

.banner-right {
  text-align: right;
  flex-shrink: 0;
}

.banner-fee-label {
  font-size: 10px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-muted);
  margin: 0 0 6px;
}

.banner-fee {
  font-size: 42px;
  font-weight: 700;
  letter-spacing: -0.03em;
  margin: 0;
  line-height: 1;
}

.fee-success {
  color: var(--accent);
  text-shadow: 0 0 30px var(--accent-glow);
}

.fee-dim {
  color: var(--text-muted);
}

.fee-currency {
  font-size: 22px;
  font-weight: 400;
  vertical-align: super;
  margin-right: 2px;
  opacity: 0.7;
}

/* ─── Field Cards ───────────────────────────────── */
.field-card {
  background: var(--surface-1);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.03) inset,
    0 8px 32px rgba(0, 0, 0, 0.35);
  transition: border-color 0.25s, box-shadow 0.25s;
}

.field-card:hover {
  border-color: var(--border-hover);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.05) inset,
    0 8px 24px rgba(0, 0, 0, 0.5);
}

.field-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border);
  background: rgba(255, 255, 255, 0.02);
}

.card-icon {
  font-size: 15px;
  line-height: 1;
}

.card-title {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--text-secondary);
  margin: 0;
}

.card-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, var(--border), transparent);
}

.field-list {
  padding: 4px 0;
}

.field-row {
  display: flex;
  align-items: baseline;
  gap: 16px;
  padding: 14px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  transition: background 0.15s;
  animation: row-in 0.3s ease both;
}

.field-row:last-child {
  border-bottom: none;
}

.field-row:hover {
  background: rgba(255, 255, 255, 0.025);
}

@keyframes row-in {
  from {
    opacity: 0;
    transform: translateX(-6px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.field-label {
  font-size: 11px;
  letter-spacing: 0.04em;
  color: var(--text-muted);
  width: 128px;
  flex-shrink: 0;
  line-height: 1.4;
}

.field-value {
  font-size: 13px;
  color: var(--text-primary);
  word-break: break-all;
  line-height: 1.5;
}

.field-empty {
  color: var(--text-muted) !important;
  font-style: italic;
}

.field-value-fee {
  font-size: 15px;
  font-weight: 700;
  color: var(--accent);
}

.fee-sign {
  font-size: 11px;
  font-weight: 400;
  opacity: 0.7;
  margin-right: 1px;
}

.fee-fen {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 400;
  margin-left: 6px;
}

.field-value-time {
  font-size: 13px;
  color: var(--text-primary);
  letter-spacing: 0.03em;
}

/* ─── Custom Tags ───────────────────────────────── */
.custom-tag {
  font-family: var(--font-mono) !important;
  letter-spacing: 0.06em !important;
  font-size: 11px !important;
  border-radius: 6px !important;
}

/* ─── Footer Meta ───────────────────────────────── */
.meta-footer {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 18px 24px;
  background: var(--surface-1);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-nonce {
  margin-left: auto;
}

.meta-key {
  font-size: 10px;
  letter-spacing: 0.1em;
  color: var(--text-muted);
  text-transform: lowercase;
}

.meta-val {
  font-size: 11px;
  color: var(--text-secondary);
  word-break: break-all;
}

.meta-divider {
  width: 1px;
  height: 20px;
  background: var(--border);
}

/* ─── Empty State ───────────────────────────────── */
.state-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 16px;
  border: 1px dashed rgba(255, 255, 255, 0.07);
  border-radius: var(--radius);
  background: rgba(255, 255, 255, 0.015);
}

.empty-icon-wrap {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
}

.empty-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  margin: 0;
  letter-spacing: 0.06em;
}

.empty-sub {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0;
  letter-spacing: 0.04em;
}

/* ─── Element Plus Overrides ────────────────────── */
:deep(.wx-input .el-input__wrapper) {
  background-color: var(--surface-2) !important;
  border: 1px solid var(--border) !important;
  border-radius: var(--radius-sm) !important;
  box-shadow: none !important;
  height: 44px;
  padding-left: 52px !important;
  transition: border-color 0.2s, box-shadow 0.2s;
}

:deep(.wx-input .el-input__wrapper:hover) {
  border-color: var(--border-hover) !important;
}

:deep(.wx-input .el-input__wrapper.is-focus) {
  border-color: rgba(7, 232, 164, 0.5) !important;
  box-shadow: 0 0 0 3px rgba(7, 232, 164, 0.1) !important;
}

:deep(.wx-input .el-input__inner) {
  color: var(--text-primary) !important;
  font-family: var(--font-mono) !important;
  font-size: 13px;
  letter-spacing: 0.04em;
  background: transparent !important;
}

:deep(.wx-input .el-input__inner::placeholder) {
  color: var(--text-muted) !important;
}

:deep(.wx-input .el-input__prefix) {
  display: none;
}

:deep(.wx-input .el-input__clear) {
  color: var(--text-muted);
}

:deep(.wx-input .el-input__clear:hover) {
  color: var(--text-secondary);
}

.has-error :deep(.wx-input .el-input__wrapper) {
  border-color: rgba(255, 77, 109, 0.5) !important;
}

/* ─── Responsive ────────────────────────────────── */
@media (max-width: 600px) {
  .page-root {
    padding: 24px 16px 60px;
  }

  .search-inner {
    padding: 20px;
  }

  .search-row {
    flex-direction: column;
  }

  .query-btn {
    width: 100%;
    justify-content: center;
  }

  .status-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .banner-right {
    text-align: left;
  }

  .banner-fee {
    font-size: 32px;
  }

  .meta-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .meta-nonce {
    margin-left: 0;
  }

  .meta-divider {
    display: none;
  }

  .field-label {
    width: 100px;
    font-size: 10px;
  }
}
</style>