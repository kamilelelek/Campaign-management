import { useState, type ChangeEvent, type SubmitEvent } from 'react'
import { getErrorMessage, getFieldErrors } from '../api.ts'
import type {
  Campaign,
  CampaignStatus,
  CreateCampaignRequest,
  FieldErrors,
  Keyword,
  Product,
  Town,
  UpdateCampaignRequest,
} from '../types.ts'
import KeywordTypeahead from './KeywordTypeahead.tsx'

const MIN_BID = '0.10'

interface FormValues {
  name: string
  productId: string
  bidAmount: string
  fund: string
  status: CampaignStatus
  townId: string
  radiusKm: string
}

const EMPTY_FORM: FormValues = {
  name: '',
  productId: '',
  bidAmount: '',
  fund: '',
  status: 'ON',
  townId: '',
  radiusKm: '',
}

function toFormValues(campaign: Campaign | null): FormValues {
  if (!campaign) {
    return EMPTY_FORM
  }

  return {
    name: campaign.name,
    productId: String(campaign.productId),
    bidAmount: String(campaign.bidAmount),
    fund: String(campaign.fund),
    status: campaign.status,
    townId: campaign.townId === null ? '' : String(campaign.townId),
    radiusKm: String(campaign.radiusKm),
  }
}

interface CampaignFormProps {
  campaign: Campaign | null
  products: Product[]
  towns: Town[]
  onCreate: (request: CreateCampaignRequest) => Promise<void>
  onUpdate: (id: number, request: UpdateCampaignRequest) => Promise<void>
  onCancel: () => void
}

function CampaignForm({ campaign, products, towns, onCreate, onUpdate, onCancel }: CampaignFormProps) {
  const [values, setValues] = useState<FormValues>(() => toFormValues(campaign))
  const [keywords, setKeywords] = useState<Keyword[]>(campaign?.keywords ?? [])
  const [message, setMessage] = useState('')
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [isSaving, setIsSaving] = useState(false)

  function handleChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = event.target
    setValues((current) => ({ ...current, [name]: value }))
  }

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()

    if (keywords.length === 0) {
      setMessage('')
      setFieldErrors({ keywordIds: 'Select at least one keyword.' })
      return
    }

    const request: UpdateCampaignRequest = {
      name: values.name,
      keywordIds: keywords.map((keyword) => keyword.id),
      bidAmount: Number(values.bidAmount),
      fund: Number(values.fund),
      status: values.status,
      townId: values.townId === '' ? null : Number(values.townId),
      radiusKm: Number(values.radiusKm),
    }

    setIsSaving(true)
    setMessage('')
    setFieldErrors({})

    try {
      if (campaign) {
        await onUpdate(campaign.id, request)
      } else {
        await onCreate({ ...request, productId: Number(values.productId) })
        setValues(EMPTY_FORM)
        setKeywords([])
      }
    } catch (error) {
      setMessage(getErrorMessage(error))
      setFieldErrors(getFieldErrors(error))
    } finally {
      setIsSaving(false)
    }
  }

  const submitLabel = campaign ? 'Save changes' : 'Create campaign'

  return (
    <form className="card form" onSubmit={handleSubmit}>
      <h2>{campaign ? 'Edit campaign' : 'New campaign'}</h2>

      {message && (
        <p className="alert" role="alert">
          {message}
        </p>
      )}

      <div className="field">
        <label htmlFor="name">Campaign name</label>
        <input id="name" name="name" value={values.name} onChange={handleChange} required />
        <FieldError message={fieldErrors.name} />
      </div>

      <div className="field">
        <label htmlFor="productId">Product</label>
        {campaign ? (
          <input id="productId" value={campaign.productName} disabled />
        ) : (
          <select
            id="productId"
            name="productId"
            value={values.productId}
            onChange={handleChange}
            required
          >
            <option value="">Select a product</option>
            {products.map((product) => (
              <option key={product.id} value={product.id}>
                {product.name}
              </option>
            ))}
          </select>
        )}
        <FieldError message={fieldErrors.productId} />
      </div>

      <KeywordTypeahead selected={keywords} onChange={setKeywords} error={fieldErrors.keywordIds} />

      <div className="field-row">
        <div className="field">
          <label htmlFor="bidAmount">Bid amount (min. {MIN_BID})</label>
          <input
            id="bidAmount"
            name="bidAmount"
            type="number"
            min={MIN_BID}
            step="0.01"
            value={values.bidAmount}
            onChange={handleChange}
            required
          />
          <FieldError message={fieldErrors.bidAmount} />
        </div>

        <div className="field">
          <label htmlFor="fund">Campaign fund</label>
          <input
            id="fund"
            name="fund"
            type="number"
            min="0.01"
            step="0.01"
            value={values.fund}
            onChange={handleChange}
            required
          />
          <FieldError message={fieldErrors.fund} />
        </div>
      </div>

      <div className="field-row">
        <div className="field">
          <label htmlFor="status">Status</label>
          <select id="status" name="status" value={values.status} onChange={handleChange}>
            <option value="ON">On</option>
            <option value="OFF">Off</option>
          </select>
          <FieldError message={fieldErrors.status} />
        </div>

        <div className="field">
          <label htmlFor="radiusKm">Radius (km)</label>
          <input
            id="radiusKm"
            name="radiusKm"
            type="number"
            min="1"
            step="1"
            value={values.radiusKm}
            onChange={handleChange}
            required
          />
          <FieldError message={fieldErrors.radiusKm} />
        </div>
      </div>

      <div className="field">
        <label htmlFor="townId">Town (optional)</label>
        <select id="townId" name="townId" value={values.townId} onChange={handleChange}>
          <option value="">No town</option>
          {towns.map((town) => (
            <option key={town.id} value={town.id}>
              {town.name}
            </option>
          ))}
        </select>
        <FieldError message={fieldErrors.townId} />
      </div>

      <div className="form-actions">
        <button type="submit" className="button-primary" disabled={isSaving}>
          {isSaving ? 'Saving...' : submitLabel}
        </button>
        {campaign && (
          <button type="button" onClick={onCancel}>
            Cancel
          </button>
        )}
      </div>
    </form>
  )
}

interface FieldErrorProps {
  message?: string
}

function FieldError({ message }: FieldErrorProps) {
  if (!message) {
    return null
  }

  return <p className="field-error">{message}</p>
}

export default CampaignForm
