import { useState, type FormEvent } from 'react'
import { motion } from 'motion/react'
import { api, token } from './api'

type Mode = 'login' | 'register'

export function Auth({ onSignedIn }: { onSignedIn: () => void }) {
  const [mode, setMode] = useState<Mode>('login')
  const [form, setForm] = useState({ firstname: '', lastname: '', email: '', password: '' })
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const set = (key: keyof typeof form) => (event: { target: { value: string } }) =>
    setForm((current) => ({ ...current, [key]: event.target.value }))

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setBusy(true)
    try {
      const result = mode === 'login'
        ? await api.login({ email: form.email, password: form.password })
        : await api.register(form)
      token.set(result.token)
      onSignedIn()
    } catch (caught) {
      setError((caught as Error).message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-wrap">
      <motion.div className="card auth-card"
                  initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}>
        <h1>Expense Tracker</h1>
        <p className="sub">How much room is left before the end of the period?</p>

        <div className="auth-tabs">
          <button type="button" className={mode === 'login' ? '' : 'ghost'} onClick={() => setMode('login')}>
            Log in
          </button>
          <button type="button" className={mode === 'register' ? '' : 'ghost'} onClick={() => setMode('register')}>
            Register
          </button>
        </div>

        {error && <div className="banner" role="alert">{error}</div>}

        <form onSubmit={submit} className="grid" style={{ gap: '.75rem' }}>
          {mode === 'register' && (
            <div className="row">
              <label className="field"><span>First name</span>
                <input value={form.firstname} onChange={set('firstname')} required autoComplete="given-name" />
              </label>
              <label className="field"><span>Last name</span>
                <input value={form.lastname} onChange={set('lastname')} required autoComplete="family-name" />
              </label>
            </div>
          )}
          <label className="field"><span>Email</span>
            <input type="email" value={form.email} onChange={set('email')} required autoComplete="email" />
          </label>
          <label className="field"><span>Password</span>
            <input type="password" value={form.password} onChange={set('password')} required
                   autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                   minLength={mode === 'register' ? 8 : undefined} />
          </label>
          <button type="submit" disabled={busy}>
            {busy ? 'Working…' : mode === 'login' ? 'Log in' : 'Create account'}
          </button>
        </form>
      </motion.div>
    </div>
  )
}
