import { formatMoney } from '../format.ts'
import type { Campaign } from '../types.ts'

interface CampaignListProps {
  campaigns: Campaign[]
  editingId?: number
  onEdit: (campaign: Campaign) => void
  onDelete: (campaign: Campaign) => void
}

function CampaignList({ campaigns, editingId, onEdit, onDelete }: CampaignListProps) {
  return (
    <section className="card">
      <h2>Campaigns</h2>

      {campaigns.length === 0 ? (
        <p className="empty">No campaigns yet. Create the first one.</p>
      ) : (
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Product</th>
                <th>Keywords</th>
                <th className="number">Bid</th>
                <th className="number">Fund</th>
                <th>Status</th>
                <th>Town</th>
                <th className="number">Radius</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {campaigns.map((campaign) => (
                <tr key={campaign.id} className={campaign.id === editingId ? 'row-active' : ''}>
                  <td>{campaign.name}</td>
                  <td>{campaign.productName}</td>
                  <td>{campaign.keywords.map((keyword) => keyword.name).join(', ')}</td>
                  <td className="number">{formatMoney(campaign.bidAmount)}</td>
                  <td className="number">{formatMoney(campaign.fund)}</td>
                  <td>
                    <span className={`status status-${campaign.status.toLowerCase()}`}>
                      {campaign.status}
                    </span>
                  </td>
                  <td>{campaign.townName ?? '-'}</td>
                  <td className="number">{campaign.radiusKm} km</td>
                  <td className="actions">
                    <button type="button" className="button-small" onClick={() => onEdit(campaign)}>
                      Edit
                    </button>
                    <button
                      type="button"
                      className="button-small button-danger"
                      onClick={() => onDelete(campaign)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}

export default CampaignList
