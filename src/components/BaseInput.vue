<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  label: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  },
  error: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  required: {
    type: Boolean,
    default: false
  },
  maxlength: {
    type: [String, Number],
    default: ''
  },
  showCount: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'blur', 'focus'])

const inputClasses = computed(() => [
  'w-full px-4 py-2 border rounded-lg outline-none transition-all duration-200',
  'focus:ring-2 focus:ring-primary-500 focus:border-primary-500',
  props.error
    ? 'border-danger-500 focus:ring-danger-500 focus:border-danger-500'
    : 'border-gray-300',
  props.disabled ? 'bg-gray-100 cursor-not-allowed opacity-60' : 'bg-white'
])

const charCount = computed(() => {
  const val = String(props.modelValue || '')
  return val.length
})
</script>

<template>
  <div class="w-full">
    <label v-if="label" class="block text-sm font-medium text-gray-700 mb-1">
      {{ label }}
      <span v-if="required" class="text-danger-500">*</span>
    </label>
    <div class="relative">
      <input
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :required="required"
        :maxlength="maxlength"
        :class="inputClasses"
        @input="emit('update:modelValue', $event.target.value)"
        @blur="emit('blur', $event)"
        @focus="emit('focus', $event)"
      />
      <slot name="prefix" />
      <slot name="suffix" />
    </div>
    <div class="flex justify-between mt-1">
      <p v-if="error" class="text-sm text-danger-500">{{ error }}</p>
      <p v-if="showCount && maxlength" class="text-sm text-gray-500 ml-auto">
        {{ charCount }}/{{ maxlength }}
      </p>
    </div>
  </div>
</template>
