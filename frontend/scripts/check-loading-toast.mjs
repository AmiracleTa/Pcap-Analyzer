import { readFileSync } from 'node:fs'

const app = readFileSync(new URL('../src/App.vue', import.meta.url), 'utf8')
const analysis = readFileSync(new URL('../src/views/AnalysisPage.vue', import.meta.url), 'utf8')

const checks = [
  {
    name: 'App handles analysis loading changes',
    pass: app.includes('function handleAnalysisLoadingChange(isLoading)'),
  },
  {
    name: 'App wires AnalysisPage loading-change event',
    pass: app.includes('@loading-change="handleAnalysisLoadingChange"'),
  },
  {
    name: 'AnalysisPage declares loading-change emit',
    pass: analysis.includes("defineEmits(['loading-change'])"),
  },
  {
    name: 'AnalysisPage emits loading start',
    pass: analysis.includes("emit('loading-change', true)"),
  },
  {
    name: 'AnalysisPage emits loading finish',
    pass: analysis.includes("emit('loading-change', false)"),
  },
  {
    name: 'AnalysisPage no longer imports inline loader icon',
    pass: !analysis.includes('LoaderCircle'),
  },
  {
    name: 'AnalysisPage no longer renders inline loading badge',
    pass: !analysis.includes('v-if="loading" class="badge"'),
  },
]

const failures = checks.filter((check) => !check.pass)

if (failures.length > 0) {
  console.error('Loading toast regression check failed:')
  failures.forEach((failure) => console.error(`- ${failure.name}`))
  process.exit(1)
}

console.log('Loading toast regression check passed.')
