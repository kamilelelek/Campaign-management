import type {
  ApiErrorBody,
  Campaign,
  CreateCampaignRequest,
  FieldErrors,
  Keyword,
  Product,
  Seller,
  Town,
  UpdateCampaignRequest,
} from './types.ts'

const BASE_URL = '/api'
const DEFAULT_ERROR_MESSAGE = 'Something went wrong. Is the backend running?'

export class ApiError extends Error {
  status: number
  fieldErrors: FieldErrors

  constructor(status: number, message: string, fieldErrors: FieldErrors = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

export function getErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : DEFAULT_ERROR_MESSAGE
}

export function getFieldErrors(error: unknown): FieldErrors {
  return error instanceof ApiError ? error.fieldErrors : {}
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })

  if (response.status === 204) {
    return undefined as T
  }

  const contentType = response.headers.get('content-type') ?? ''
  const data: unknown = contentType.includes('application/json') ? await response.json() : null

  if (!response.ok) {
    const body = data as ApiErrorBody | null
    throw new ApiError(
      response.status,
      body?.message ?? DEFAULT_ERROR_MESSAGE,
      body?.fieldErrors ?? {},
    )
  }

  return data as T
}

export const getSeller = () => request<Seller>('/seller')
export const getProducts = () => request<Product[]>('/products')
export const getTowns = () => request<Town[]>('/towns')
export const getCampaigns = () => request<Campaign[]>('/campaigns')

export const searchKeywords = (query: string) =>
  request<Keyword[]>(`/keywords?query=${encodeURIComponent(query)}`)

export const createCampaign = (campaign: CreateCampaignRequest) =>
  request<Campaign>('/campaigns', {
    method: 'POST',
    body: JSON.stringify(campaign),
  })

export const updateCampaign = (id: number, campaign: UpdateCampaignRequest) =>
  request<Campaign>(`/campaigns/${id}`, {
    method: 'PUT',
    body: JSON.stringify(campaign),
  })

export const deleteCampaign = (id: number) =>
  request<void>(`/campaigns/${id}`, { method: 'DELETE' })
