import { useEffect, useState } from 'react'
import {
  createCampaign,
  deleteCampaign,
  getCampaigns,
  getErrorMessage,
  getProducts,
  getSeller,
  getTowns,
  updateCampaign,
} from './api.ts'
import CampaignForm from './components/CampaignForm.tsx'
import CampaignList from './components/CampaignList.tsx'
import SellerBalance from './components/SellerBalance.tsx'
import type {
  Campaign,
  CreateCampaignRequest,
  Product,
  Seller,
  Town,
  UpdateCampaignRequest,
} from './types.ts'

function App() {
  const [seller, setSeller] = useState<Seller | null>(null)
  const [campaigns, setCampaigns] = useState<Campaign[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [towns, setTowns] = useState<Town[]>([])
  const [editingCampaign, setEditingCampaign] = useState<Campaign | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [pageError, setPageError] = useState('')

  useEffect(() => {
    Promise.all([getSeller(), getCampaigns(), getProducts(), getTowns()])
      .then(([sellerData, campaignData, productData, townData]) => {
        setSeller(sellerData)
        setCampaigns(campaignData)
        setProducts(productData)
        setTowns(townData)
      })
      .catch((error: unknown) => setPageError(getErrorMessage(error)))
      .finally(() => setIsLoading(false))
  }, [])

  async function refreshData() {
    const [sellerData, campaignData] = await Promise.all([getSeller(), getCampaigns()])
    setSeller(sellerData)
    setCampaigns(campaignData)
  }

  async function handleCreate(request: CreateCampaignRequest) {
    await createCampaign(request)
    setPageError('')
    await refreshData()
  }

  async function handleUpdate(id: number, request: UpdateCampaignRequest) {
    await updateCampaign(id, request)
    setPageError('')
    await refreshData()
    setEditingCampaign(null)
  }

  async function handleDelete(campaign: Campaign) {
    if (!window.confirm(`Delete campaign "${campaign.name}"?`)) {
      return
    }

    try {
      await deleteCampaign(campaign.id)
      setPageError('')
      await refreshData()
      if (editingCampaign?.id === campaign.id) {
        setEditingCampaign(null)
      }
    } catch (error) {
      setPageError(getErrorMessage(error))
    }
  }

  if (isLoading) {
    return <p className="page-message">Loading...</p>
  }

  const takenProductIds = new Set(campaigns.map((campaign) => campaign.productId))
  const availableProducts = products.filter((product) => !takenProductIds.has(product.id))

  return (
    <div className="page">
      <header className="page-header">
        <h1>Campaign Management</h1>
        <SellerBalance seller={seller} />
      </header>

      {pageError && (
        <p className="alert" role="alert">
          {pageError}
        </p>
      )}

      <main className="layout">
        <CampaignForm
          key={editingCampaign?.id ?? 'new'}
          campaign={editingCampaign}
          products={availableProducts}
          towns={towns}
          onCreate={handleCreate}
          onUpdate={handleUpdate}
          onCancel={() => setEditingCampaign(null)}
        />
        <CampaignList
          campaigns={campaigns}
          editingId={editingCampaign?.id}
          onEdit={setEditingCampaign}
          onDelete={handleDelete}
        />
      </main>
    </div>
  )
}

export default App
