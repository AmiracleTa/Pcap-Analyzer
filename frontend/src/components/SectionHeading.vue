<script setup>
defineProps({
  eyebrow: { type: String, required: true },
  title: { type: String, required: true },
  text: { type: String, required: true },
})
</script>

<template>
  <div class="section-heading" data-reveal>
    <p class="eyebrow">{{ eyebrow }}</p>
    <h2>{{ title }}</h2>
    <p>{{ text }}</p>
  </div>
</template>
