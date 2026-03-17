<script setup lang="ts">

const { color } = defineProps<{
  color?: 'white' | 'black';
}>();

</script>

<template>
  <button open-type="contact"
    class="w-[30px] h-[30px] flex items-center justify-center bg-transparent p-0 m-0 after:border-none"
    >
    <uni-icons type="chat" size="30" :color="color"></uni-icons>
  </button>
</template>