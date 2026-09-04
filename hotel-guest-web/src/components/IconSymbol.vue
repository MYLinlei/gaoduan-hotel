<template>
  <svg class="ui-icon" viewBox="0 0 24 24" fill="none" aria-hidden="true">
    <path v-if="name === 'menu'" d="M4 7h16M4 12h16M4 17h16" />
    <path v-else-if="name === 'search'" d="m20 20-4.4-4.4M18 11a7 7 0 1 1-14 0 7 7 0 0 1 14 0Z" />
    <path v-else-if="name === 'ticket'" d="M4 6h16v4a2 2 0 0 0 0 4v4H4v-4a2 2 0 0 0 0-4V6Zm8 0v12" />
    <path v-else-if="name === 'order'" d="M7 4h10v16H7zM9.5 8h5M9.5 12h5M9.5 16h3" />
    <path v-else-if="name === 'user'" d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-7 8a7 7 0 0 1 14 0" />
    <path v-else-if="name === 'cart'" d="M3 4h2l2.2 10h9.9l2-7H6M9 20h.01M17 20h.01" />
    <path v-else-if="name === 'truck'" d="M3 6h11v10H3zM14 10h4l3 3v3h-7zM7 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4Zm10 0a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z" />
    <path v-else-if="name === 'support'" d="M4 12a8 8 0 0 1 16 0v5h-4v-5h4M4 12v5h4v-5H4Zm12 7c-1 1-2.3 1.5-4 1.5" />
    <path v-else-if="name === 'arrow'" d="M5 12h14m-5-5 5 5-5 5" />
    <path v-else-if="name === 'heart'" d="M20.8 5.8a5 5 0 0 0-7.1 0L12 7.5l-1.7-1.7a5 5 0 1 0-7.1 7.1L12 21l8.8-8.1a5 5 0 0 0 0-7.1Z" />
    <path v-else-if="name === 'chevron'" d="m7 9 5 5 5-5" />
    <path v-else-if="name === 'chevron-left'" d="m15 18-6-6 6-6" />
    <path v-else-if="name === 'chevron-right'" d="m9 18 6-6-6-6" />
    <path v-else-if="name === 'info'" d="M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Zm0-10v6m0-10h.01" />
    <path v-else-if="name === 'wifi-off'" d="m3 3 18 18M8.5 8.8A9.6 9.6 0 0 1 20 11m-16 0c.8-.8 1.7-1.4 2.7-1.9M7.5 14.5a6.3 6.3 0 0 1 6.8-1.3M10.5 18a2.2 2.2 0 0 1 3 0" />
    <path v-else-if="name === 'plus'" d="M12 5v14M5 12h14" />
    <path v-else d="M12 4v16M4 12h16" />
  </svg>
</template>

<script setup>
defineProps({ name: { type: String, required: true } });
</script>
