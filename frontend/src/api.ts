export type Budget = {
  id: string
  startDate: string
  endDate: string
  plannedTotal: number
  envelopes: { labelId: string; labelName: string | null; limitMinorUnits: number }[]
}

export type Label = { id: string; name: string; parentId: string | null }

export type SummaryLine = {
  labelId: string
  labelName: string | null
  limitMinorUnits: number
  consumed: number
  remaining: number
}

export type Summary = {
  budgetId: string
  plannedTotal: number
  totalIncome: number
  totalConsumed: number
  lines: SummaryLine[]
}

export type Transaction = {
  id: string
  type: 'EXPENSE' | 'INCOME' | 'REFUND'
  amountMinorUnits: number
  description: string
  date: string
  labelId: string | null
  expenseId: string | null
  source: string | null
}

const TOKEN_KEY = 'expense-tracker.token'

export const token = {
  get: () => sessionStorage.getItem(TOKEN_KEY),
  set: (value: string) => sessionStorage.setItem(TOKEN_KEY, value),
  clear: () => sessionStorage.removeItem(TOKEN_KEY),
}

export class ApiError extends Error {
  readonly status: number
  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const stored = token.get()
  const response = await fetch(path, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(stored ? { Authorization: `Bearer ${stored}` } : {}),
      ...(init.headers ?? {}),
    },
  })

  if (response.status === 401) {
    token.clear()
    throw new ApiError(401, 'Your session expired. Please log in again.')
  }

  const body = response.status === 204 ? null : await response.json().catch(() => null)
  if (!response.ok) {
    throw new ApiError(response.status, body?.message ?? response.statusText)
  }
  return body as T
}

const post = <T,>(path: string, body: unknown) =>
  request<T>(path, { method: 'POST', body: JSON.stringify(body) })

export const api = {
  register: (body: { firstname: string; lastname: string; email: string; password: string }) =>
    post<{ token: string }>('/auth/register', body),

  login: (body: { email: string; password: string }) =>
    post<{ token: string }>('/auth/login', body),

  listBudgets: () => request<Budget[]>('/budgets'),

  createBudget: (body: { startDate: string; endDate: string }) =>
    post<Budget>('/budgets', body),

  summary: (budgetId: string) => request<Summary>(`/budgets/${budgetId}/summary`),

  transactions: (budgetId: string) => request<Transaction[]>(`/budgets/${budgetId}/transactions`),

  listLabels: () => request<Label[]>('/labels'),

  createLabel: (body: { name: string; parentId?: string | null }) =>
    post<Label>('/labels', body),

  setEnvelope: (budgetId: string, labelId: string, limitMinorUnits: number) =>
    request<Budget>(`/budgets/${budgetId}/envelopes/${labelId}`, {
      method: 'PUT',
      body: JSON.stringify({ limitMinorUnits }),
    }),

  addExpense: (budgetId: string, body: { labelId: string; amountMinorUnits: number; description: string; date: string }) =>
    post<Transaction>(`/budgets/${budgetId}/expenses`, body),

  addIncome: (budgetId: string, body: { amountMinorUnits: number; source: string; description: string; date: string }) =>
    post<Transaction>(`/budgets/${budgetId}/incomes`, body),

  addRefund: (expenseId: string, body: { amountMinorUnits: number; description: string; date: string }) =>
    post<Transaction>(`/transactions/${expenseId}/refunds`, body),
}

/** Minor units are converted in exactly one place, so no component does arithmetic on amounts. */
export const money = {
  format: (minorUnits: number) =>
    (minorUnits / 100).toLocaleString(undefined, { style: 'currency', currency: 'EUR' }),
  toMinor: (input: string) => Math.round(Number(input) * 100),
}
