import interconnect from '@system.interconnect'

const connection = interconnect.instance()
const listeners = []
let state = 'disconnected'
let lastError = ''

function publish(extra) {
  const event = Object.assign({ state, error: lastError }, extra || {})
  listeners.slice().forEach((listener) => {
    try { listener(event) } catch (error) { console.error('Gallery Mini listener error', error) }
  })
}
function setState(next, error) {
  state = next
  lastError = error || ''
  publish()
}
function parseMessage(event) {
  let value = event && event.data !== undefined ? event.data : event
  if (typeof value === 'string') {
    try { value = JSON.parse(value) } catch (error) { return null }
  }
  if (!value || value.v !== 1 || typeof value.type !== 'string') return null
  return value
}
connection.onopen = () => setState('connected', '')
connection.onclose = (event) => setState('disconnected', event && event.data ? String(event.data) : '')
connection.onerror = (event) => setState('disconnected', event && event.data ? String(event.data) : 'Connection error')
connection.onmessage = (event) => {
  const message = parseMessage(event)
  if (message) publish({ message })
}
export default {
  getState() { return state },
  subscribe(listener) {
    if (typeof listener !== 'function') return () => {}
    listeners.push(listener)
    return () => {
      const index = listeners.indexOf(listener)
      if (index >= 0) listeners.splice(index, 1)
    }
  },
  checkConnection() {
    setState('connecting', '')
    connection.diagnosis({
      timeout: 10000,
      success: (result) => setState(result && result.status === 0 ? 'connected' : 'disconnected',
        result && result.status === 0 ? '' : 'Connection unavailable'),
      fail: (error, code) => setState('disconnected', String(error || code || 'Connection unavailable'))
    })
  },
  send(type, payload) {
    if (state !== 'connected') return false
    connection.send({
      data: { v: 1, type: type, payload: payload || {} },
      success: function () {},
      fail: (error, code) => setState('disconnected', String(error || code || 'Send failed'))
    })
    return true
  }
}