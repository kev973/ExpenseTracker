import { useState } from 'react'
import { Auth } from './Auth'
import { Dashboard } from './Dashboard'
import { token } from './api'

export function App() {
  const [signedIn, setSignedIn] = useState(() => token.get() !== null)

  if (!signedIn) return <Auth onSignedIn={() => setSignedIn(true)} />

  return <Dashboard onSignedOut={() => { token.clear(); setSignedIn(false) }} />
}
