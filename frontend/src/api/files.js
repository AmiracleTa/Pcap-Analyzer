import { apiFetch } from './client'

export function getHealth() {
  return apiFetch('/api/health')
}

export function listFiles() {
  return apiFetch('/api/files')
}

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return apiFetch('/api/files', {
    method: 'POST',
    body: formData,
  })
}

export function deleteFile(id) {
  return apiFetch(`/api/files/${id}`, {
    method: 'DELETE',
  })
}

export function analyzeFile(id) {
  return apiFetch(`/api/files/${id}/analyze`, {
    method: 'POST',
  })
}

export function createAnalyzeEventSource(id) {
  return new EventSource(`/api/files/${id}/analyze/events`)
}

export function listPackets(fileId) {
  return apiFetch(`/api/files/${fileId}/packets`)
}

export function getSummary(fileId) {
  return apiFetch(`/api/files/${fileId}/summary`)
}

export function getSecurityReport(fileId) {
  return apiFetch(`/api/files/${fileId}/security-report`)
}

export function exportCsv(fileId) {
  window.open(`/api/files/${fileId}/export/csv`, '_blank')
}

export function exportJson(fileId) {
  window.open(`/api/files/${fileId}/export/json`, '_blank')
}
