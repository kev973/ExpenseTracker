import { useCallback, useEffect, useMemo, useState } from 'react'
import { AnimatePresence, motion } from 'motion/react'
import { EnvelopeChart } from './EnvelopeChart'
import { api, money, type Budget, type Label, type Summary, type Transaction } from './api'

const today = () => new Date().toISOString().slice(0, 10)

function monthBounds() {
  const now = new Date()
  const first = new Date(now.getFullYear(), now.getMonth(), 1)
  const last = new Date(now.getFullYear(), now.getMonth() + 1, 0)
  const iso = (date: Date) => date.toLocaleDateString('sv-SE')
  return { startDate: iso(first), endDate: iso(last) }
}

function Tile({ label, value, note, tone }: {
  label: string; value: string; note?: string; tone?: 'over' | 'good'
}) {
  return (
    <motion.div className="card tile" layout
                initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.3 }}>
      <div className="label">{label}</div>
      <div className="value">{value}</div>
      {note && <div className={`note ${tone ?? ''}`}>{note}</div>}
    </motion.div>
  )
}

export function Dashboard({ onSignedOut }: { onSignedOut: () => void }) {
  const [budgets, setBudgets] = useState<Budget[]>([])
  const [budgetId, setBudgetId] = useState<string>('')
  const [labels, setLabels] = useState<Label[]>([])
  const [summary, setSummary] = useState<Summary | null>(null)
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [error, setError] = useState<string | null>(null)

  const guard = useCallback(async (action: () => Promise<void>) => {
    setError(null)
    try {
      await action()
    } catch (caught) {
      const message = (caught as Error).message
      setError(message)
      if (message.includes('session expired')) onSignedOut()
    }
  }, [onSignedOut])

  const loadBudgets = useCallback((select?: string) => guard(async () => {
    const loaded = await api.listBudgets()
    setBudgets(loaded)
    setBudgetId((current) => select ?? (loaded.some((b) => b.id === current) ? current : loaded[0]?.id ?? ''))
  }), [guard])

  const refresh = useCallback(() => guard(async () => {
    setLabels(await api.listLabels())
    if (!budgetId) {
      setSummary(null)
      setTransactions([])
      return
    }
    setSummary(await api.summary(budgetId))
    setTransactions(await api.transactions(budgetId))
  }), [budgetId, guard])

  useEffect(() => { void loadBudgets() }, [loadBudgets])
  useEffect(() => { void refresh() }, [refresh])

  const expenses = useMemo(() => transactions.filter((t) => t.type === 'EXPENSE'), [transactions])
  const budget = budgets.find((b) => b.id === budgetId)
  const remaining = summary ? summary.plannedTotal - summary.totalConsumed : 0
  const gap = summary ? summary.totalIncome - summary.plannedTotal : 0

  return (
    <div className="shell">
      <header className="topbar">
        <div>
          <h1>Expense Tracker</h1>
          {budget && <div className="who">{budget.startDate} → {budget.endDate}</div>}
        </div>
        <div className="row" style={{ flex: '0 1 auto' }}>
          {budgets.length > 0 && (
            <select value={budgetId} onChange={(e) => setBudgetId(e.target.value)} aria-label="Budget">
              {budgets.map((b) => <option key={b.id} value={b.id}>{b.startDate} → {b.endDate}</option>)}
            </select>
          )}
          <button className="ghost" onClick={onSignedOut}>Log out</button>
        </div>
      </header>

      <AnimatePresence>
        {error && (
          <motion.div className="banner" role="alert"
                      initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }}
                      exit={{ opacity: 0, height: 0 }}>
            {error}
          </motion.div>
        )}
      </AnimatePresence>

      {summary && (
        <div className="grid tiles" style={{ marginBottom: '1rem' }}>
          <Tile label="Planned" value={money.format(summary.plannedTotal)}
                note={`${summary.lines.length} envelope${summary.lines.length === 1 ? '' : 's'}`} />
          <Tile label="Income" value={money.format(summary.totalIncome)}
                note={gap >= 0 ? `${money.format(gap)} above plan` : `${money.format(-gap)} short of plan`}
                tone={gap >= 0 ? 'good' : 'over'} />
          <Tile label="Consumed" value={money.format(summary.totalConsumed)} />
          <Tile label="Remaining" value={money.format(remaining)}
                note={remaining < 0 ? '⚠ over the plan' : 'left to spend'}
                tone={remaining < 0 ? 'over' : undefined} />
        </div>
      )}

      <div className="card" style={{ marginBottom: '1rem' }}>
        <h2>Consumed against limit</h2>
        {summary
          ? <EnvelopeChart lines={summary.lines} />
          : <p className="empty">Create a budget to get started.</p>}
      </div>

      <div className="grid two" style={{ marginBottom: '1rem' }}>
        <BudgetForm onCreated={(id) => loadBudgets(id)} />
        <LabelForm onCreated={refresh} />
        <EnvelopeForm budgetId={budgetId} labels={labels} onSaved={refresh} />
        <MoneyForms className="span-all" budgetId={budgetId} labels={labels} expenses={expenses} onSaved={refresh} />
      </div>

      <div className="card">
        <h2>Transactions</h2>
        {transactions.length === 0
          ? <p className="empty">Nothing recorded on this budget yet.</p>
          : (
            <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Date</th><th>Type</th><th>Description</th><th className="num">Amount</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((t) => (
                  <tr key={t.id}>
                    <td>{t.date}</td>
                    <td><span className="pill">{t.type.toLowerCase()}</span></td>
                    <td>{t.description}</td>
                    <td className="num">{money.format(t.amountMinorUnits)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            </div>
          )}
      </div>
    </div>
  )
}

function BudgetForm({ onCreated }: { onCreated: (id: string) => void }) {
  const [dates, setDates] = useState(monthBounds())
  const [busy, setBusy] = useState(false)

  return (
    <div className="card">
      <h2>New budget</h2>
      <div className="row">
        <label className="field"><span>From</span>
          <input type="date" value={dates.startDate}
                 onChange={(e) => setDates({ ...dates, startDate: e.target.value })} />
        </label>
        <label className="field"><span>To</span>
          <input type="date" value={dates.endDate}
                 onChange={(e) => setDates({ ...dates, endDate: e.target.value })} />
        </label>
        <button disabled={busy} onClick={async () => {
          setBusy(true)
          try { onCreated((await api.createBudget(dates)).id) } finally { setBusy(false) }
        }}>Create</button>
      </div>
    </div>
  )
}

function LabelForm({ onCreated }: { onCreated: () => void }) {
  const [name, setName] = useState('')

  return (
    <div className="card">
      <h2>New label</h2>
      <div className="row">
        <label className="field"><span>Name</span>
          <input value={name} onChange={(e) => setName(e.target.value)} placeholder="food" />
        </label>
        <button disabled={!name.trim()} onClick={async () => {
          await api.createLabel({ name })
          setName('')
          onCreated()
        }}>Add</button>
      </div>
    </div>
  )
}

function EnvelopeForm({ budgetId, labels, onSaved }: {
  budgetId: string; labels: Label[]; onSaved: () => void
}) {
  const [labelId, setLabelId] = useState('')
  const [limit, setLimit] = useState('500')
  const chosen = labelId || labels[0]?.id || ''

  return (
    <div className="card">
      <h2>Planned limit</h2>
      <div className="row">
        <label className="field"><span>Label</span>
          <select value={chosen} onChange={(e) => setLabelId(e.target.value)}>
            {labels.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
          </select>
        </label>
        <label className="field"><span>Limit</span>
          <input type="number" min="0" step="0.01" value={limit} onChange={(e) => setLimit(e.target.value)} />
        </label>
        <button disabled={!budgetId || !chosen} onClick={async () => {
          await api.setEnvelope(budgetId, chosen, money.toMinor(limit))
          onSaved()
        }}>Set</button>
      </div>
    </div>
  )
}

function MoneyForms({ className, budgetId, labels, expenses, onSaved }: {
  className?: string; budgetId: string; labels: Label[]; expenses: Transaction[]; onSaved: () => void
}) {
  const [tab, setTab] = useState<'expense' | 'income' | 'refund'>('expense')
  const [labelId, setLabelId] = useState('')
  const [expenseId, setExpenseId] = useState('')
  const [amount, setAmount] = useState('20')
  const [text, setText] = useState('groceries')
  const chosenLabel = labelId || labels[0]?.id || ''
  const chosenExpense = expenseId || expenses[0]?.id || ''

  async function submit() {
    const date = today()
    const amountMinorUnits = money.toMinor(amount)
    if (tab === 'expense') {
      await api.addExpense(budgetId, { labelId: chosenLabel, amountMinorUnits, description: text, date })
    } else if (tab === 'income') {
      await api.addIncome(budgetId, { amountMinorUnits, source: text, description: text, date })
    } else {
      await api.addRefund(chosenExpense, { amountMinorUnits, description: text, date })
    }
    onSaved()
  }

  return (
    <div className={`card ${className ?? ''}`}>
      <h2>Record money</h2>
      <div className="auth-tabs">
        {(['expense', 'income', 'refund'] as const).map((option) => (
          <button key={option} className={tab === option ? '' : 'ghost'} onClick={() => setTab(option)}>
            {option}
          </button>
        ))}
      </div>
      <div className="row">
        {tab === 'expense' && (
          <label className="field"><span>Label</span>
            <select value={chosenLabel} onChange={(e) => setLabelId(e.target.value)}>
              {labels.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
            </select>
          </label>
        )}
        {tab === 'refund' && (
          <label className="field"><span>Expense</span>
            <select value={chosenExpense} onChange={(e) => setExpenseId(e.target.value)}>
              {expenses.map((e) => (
                <option key={e.id} value={e.id}>{e.description} · {money.format(e.amountMinorUnits)}</option>
              ))}
            </select>
          </label>
        )}
        <label className="field"><span>Amount</span>
          <input type="number" min="0.01" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} />
        </label>
        <label className="field"><span>{tab === 'income' ? 'Source' : 'Description'}</span>
          <input value={text} onChange={(e) => setText(e.target.value)} />
        </label>
        <button disabled={!budgetId || (tab === 'refund' && !chosenExpense)} onClick={submit}>Add</button>
      </div>
    </div>
  )
}
