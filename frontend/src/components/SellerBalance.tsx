import { formatMoney } from '../format.ts'
import type { Seller } from '../types.ts'

interface SellerBalanceProps {
  seller: Seller | null
}

function SellerBalance({ seller }: SellerBalanceProps) {
  if (!seller) {
    return null
  }

  return (
    <div className="balance">
      <span className="balance-name">{seller.name}</span>
      <span className="balance-label">Emerald balance</span>
      <strong className="balance-value">{formatMoney(seller.emeraldBalance)}</strong>
    </div>
  )
}

export default SellerBalance
