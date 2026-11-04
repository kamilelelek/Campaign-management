export interface Seller {
  id: number
  name: string
  emeraldBalance: number
}

export interface Product {
  id: number
  name: string
}

export interface Town {
  id: number
  name: string
}

export interface Keyword {
  id: number
  name: string
}

export type CampaignStatus = 'ON' | 'OFF'

export interface Campaign {
  id: number
  name: string
  bidAmount: number
  fund: number
  status: CampaignStatus
  radiusKm: number
  productId: number
  productName: string
  townId: number | null
  townName: string | null
  keywords: Keyword[]
}

export interface UpdateCampaignRequest {
  name: string
  keywordIds: number[]
  bidAmount: number
  fund: number
  status: CampaignStatus
  townId: number | null
  radiusKm: number
}

export interface CreateCampaignRequest extends UpdateCampaignRequest {
  productId: number
}

export type FieldErrors = Record<string, string>

export interface ApiErrorBody {
  message?: string
  fieldErrors?: FieldErrors
}
