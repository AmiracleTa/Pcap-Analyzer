import { readFileSync } from 'node:fs'

const app = readFileSync(new URL('../src/App.vue', import.meta.url), 'utf8')
const filePage = readFileSync(new URL('../src/views/FilePage.vue', import.meta.url), 'utf8')

const checks = [
  {
    name: 'Progress stages include cloud save',
    pass: filePage.includes("label: '保存到云端'"),
  },
  {
    name: 'Progress stages no longer include standalone complete stage',
    pass: !filePage.includes("label: '完成'"),
  },
  {
    name: 'Progress meta does not render duplicated phase text',
    pass: !filePage.includes('{{ phaseText(analysisProgress.phase) }}'),
  },
  {
    name: 'AI phase packet text does not repeat AI report wording',
    pass: !filePage.includes('正在生成 AI 报告`'),
  },
  {
    name: 'Analyze completion selects analyzed file',
    pass: app.includes('selectedFile.value = updatedFile'),
  },
  {
    name: 'Analyze completion scrolls to analysis result',
    pass: app.includes("scrollToSection('analysis')"),
  },
]

const failures = checks.filter((check) => !check.pass)

if (failures.length > 0) {
  console.error('Analysis progress regression check failed:')
  failures.forEach((failure) => console.error(`- ${failure.name}`))
  process.exit(1)
}

console.log('Analysis progress regression check passed.')
