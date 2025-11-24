<script setup>
// App.vue now acts as the root container for router views
</script>

<template>
	<div class="app-container">
		<router-view v-slot="{ Component }">
			<keep-alive>
				<component :is="Component"></component>
			</keep-alive>
		</router-view>
	</div>
</template>

<style scoped lang="less">
.app-container {
	height: 100vh;
	width: 100%;
}
</style>
