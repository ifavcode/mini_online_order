<script setup>
import { useSystemStore } from './stores/system';
import { onLaunch, onLoad } from '@dcloudio/uni-app'
import { useUserStore } from './stores/user';

const systemStore = useSystemStore()
const userStore = useUserStore()

onLaunch(() => {
  systemStore.initSystem();
  userStore.initUser()
});


</script>

<style lang="scss">
@import 'tailwindcss/base';
@import 'tailwindcss/components';
@import 'tailwindcss/utilities';
</style>
