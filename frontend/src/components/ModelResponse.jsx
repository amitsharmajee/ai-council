import React, { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import { ChevronDown, ChevronUp, Clock, Hash, Cpu, Zap, ShieldCheck, Flame, AlertTriangle } from 'lucide-react';

const PROVIDER_CONFIG = {
  openrouter: { name: 'OpenRouter free', badgeClass: 'badge-openrouter', icon: Cpu, color: '#a855f7' },
  claude: { name: 'Anthropic Claude 3.5', badgeClass: 'badge-claude', icon: Zap, color: '#fbbf24' },
  gemini: { name: 'Google Gemini 3.5', badgeClass: 'badge-gemini', icon: ShieldCheck, color: '#38bdf8' },
  groq: { name: 'Groq Llama 3.3 70B', badgeClass: 'badge-groq', icon: Flame, color: '#f97316' }
};

export default function ModelResponse({ response }) {
  const [isExpanded, setIsExpanded] = useState(true);

  const providerKey = response.provider?.toLowerCase() || 'openrouter';
  const config = PROVIDER_CONFIG[providerKey] || { name: response.provider, badgeClass: 'badge-openrouter', icon: Cpu, color: '#a855f7' };
  const IconComp = config.icon;

  const isFailed = response.status === 'FAILED' || (response.response && response.response.includes('Provider unavailable'));

  return (
    <div
      style={{
        background: isFailed ? 'rgba(239, 68, 68, 0.06)' : 'var(--bg-surface)',
        border: isFailed ? '1px solid rgba(239, 68, 68, 0.3)' : '1px solid var(--border-color)',
        borderRadius: 'var(--radius-md)',
        marginBottom: '1rem',
        overflow: 'hidden',
        transition: 'all 0.2s ease'
      }}
    >
      {/* Header */}
      <div
        onClick={() => setIsExpanded(!isExpanded)}
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '1rem 1.25rem',
          background: isFailed ? 'rgba(239, 68, 68, 0.08)' : 'var(--bg-secondary)',
          cursor: 'pointer',
          borderBottom: isExpanded ? (isFailed ? '1px solid rgba(239, 68, 68, 0.2)' : '1px solid var(--border-color)') : 'none'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div
            style={{
              width: '32px',
              height: '32px',
              borderRadius: '8px',
              background: `rgba(255, 255, 255, 0.05)`,
              border: `1px solid ${isFailed ? '#ef4444' : config.color}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: isFailed ? '#ef4444' : config.color
            }}
          >
            <IconComp size={18} />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span style={{ fontWeight: 700, fontSize: '1rem', color: 'var(--header-title)' }}>
                {response.modelName || config.name}
              </span>
              <span className={`badge ${config.badgeClass}`}>{providerKey}</span>
              {isFailed && (
                <span style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem', background: 'rgba(239, 68, 68, 0.2)', color: '#f87171', border: '1px solid rgba(239, 68, 68, 0.4)', padding: '2px 8px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 600 }}>
                  <AlertTriangle size={12} />
                  ⚠ Provider Unavailable
                </span>
              )}
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem' }}>
          {response.responseTime && (
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.8rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
              <Clock size={14} />
              {response.responseTime}ms
            </span>
          )}
          {response.tokenUsage && (
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.8rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
              <Hash size={14} />
              {response.tokenUsage} tokens
            </span>
          )}
          <button
            style={{
              background: 'transparent',
              border: 'none',
              color: 'var(--text-muted)',
              cursor: 'pointer',
              display: 'flex'
            }}
          >
            {isExpanded ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
          </button>
        </div>
      </div>

      {/* Expandable Body with React Markdown */}
      {isExpanded && (
        <div style={{ padding: '1.25rem' }} className="markdown-body">
          <ReactMarkdown>{response.response}</ReactMarkdown>
        </div>
      )}
    </div>
  );
}
