export async function apiFetch(path, options = {}) {
  const response = await fetch(path, options)

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `Request failed with status ${response.status}`)
  }

  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    return response.json()
  }
  return response.text()
}
