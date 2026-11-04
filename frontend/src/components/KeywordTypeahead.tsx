import { useEffect, useState, type KeyboardEvent } from 'react'
import { searchKeywords } from '../api.ts'
import type { Keyword } from '../types.ts'

const DEBOUNCE_MS = 250

interface KeywordTypeaheadProps {
  selected: Keyword[]
  onChange: (keywords: Keyword[]) => void
  error?: string
}

function KeywordTypeahead({ selected, onChange, error }: KeywordTypeaheadProps) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<Keyword[]>([])
  const [isOpen, setIsOpen] = useState(false)

  useEffect(() => {
    if (!isOpen) {
      return
    }

    let isCurrent = true
    const timer = setTimeout(() => {
      searchKeywords(query)
        .then((keywords) => {
          if (isCurrent) {
            setResults(keywords)
          }
        })
        .catch(() => {
          if (isCurrent) {
            setResults([])
          }
        })
    }, DEBOUNCE_MS)

    return () => {
      isCurrent = false
      clearTimeout(timer)
    }
  }, [query, isOpen])

  const selectedIds = new Set(selected.map((keyword) => keyword.id))
  const suggestions = results.filter((keyword) => !selectedIds.has(keyword.id))

  function addKeyword(keyword: Keyword) {
    onChange([...selected, keyword])
    setQuery('')
  }

  function removeKeyword(id: number) {
    onChange(selected.filter((keyword) => keyword.id !== id))
  }

  function handleKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'Enter') {
      event.preventDefault()
      if (suggestions.length > 0) {
        addKeyword(suggestions[0])
      }
    }

    if (event.key === 'Escape') {
      setIsOpen(false)
    }
  }

  return (
    <div className="field">
      <label htmlFor="keywords">Keywords</label>

      {selected.length > 0 && (
        <ul className="chips">
          {selected.map((keyword) => (
            <li key={keyword.id} className="chip">
              {keyword.name}
              <button
                type="button"
                aria-label={`Remove ${keyword.name}`}
                onClick={() => removeKeyword(keyword.id)}
              >
                ×
              </button>
            </li>
          ))}
        </ul>
      )}

      <div className="typeahead">
        <input
          id="keywords"
          type="text"
          autoComplete="off"
          placeholder="Start typing..."
          value={query}
          onChange={(event) => {
            setQuery(event.target.value)
            setIsOpen(true)
          }}
          onFocus={() => setIsOpen(true)}
          onBlur={() => setIsOpen(false)}
          onKeyDown={handleKeyDown}
        />

        {isOpen && suggestions.length > 0 && (
          <ul className="suggestions">
            {suggestions.map((keyword) => (
              <li key={keyword.id}>
                <button
                  type="button"
                  onMouseDown={(event) => event.preventDefault()}
                  onClick={() => addKeyword(keyword)}
                >
                  {keyword.name}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {error && <p className="field-error">{error}</p>}
    </div>
  )
}

export default KeywordTypeahead
