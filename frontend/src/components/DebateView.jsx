import React, { useState } from 'react';
import FinalConsensus from './FinalConsensus';
import ModelResponse from './ModelResponse';
import { Award, Layers, MessageSquare, ArrowRight, CheckCircle2, XCircle, ShieldAlert, Sparkles } from 'lucide-react';

export default function DebateView({ debate }) {
  const [activeTab, setActiveTab] = useState('consensus'); // consensus | responses | critiques

  if (!debate) return null;

  return (
    <div style={{ marginTop: '2rem' }}>
      {/* Question Headline Banner */}
      <div className="glass-panel" style={{ padding: '1.25rem 1.5rem', marginBottom: '1.5rem', borderLeft: '4px solid var(--accent-cyan)' }}>
        <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--accent-cyan)', fontWeight: 600 }}>
          Council Question ID #{debate.debateId}
        </span>
        <h2 style={{ fontSize: '1.2rem', color: 'var(--header-title)', fontWeight: 700, marginTop: '0.2rem' }}>
          "{debate.question}"
        </h2>
      </div>

      {/* Navigation Tabs */}
      <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.75rem', flexWrap: 'wrap' }}>
        <button
          onClick={() => setActiveTab('consensus')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.6rem 1.25rem',
            borderRadius: 'var(--radius-sm)',
            border: 'none',
            background: activeTab === 'consensus' ? 'var(--primary)' : 'var(--bg-secondary)',
            color: activeTab === 'consensus' ? 'white' : 'var(--text-muted)',
            fontFamily: 'var(--font-heading)',
            fontWeight: 600,
            cursor: 'pointer',
            transition: 'all 0.2s ease'
          }}
        >
          <Award size={18} />
          Final Consensus
        </button>

        <button
          onClick={() => setActiveTab('responses')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.6rem 1.25rem',
            borderRadius: 'var(--radius-sm)',
            border: 'none',
            background: activeTab === 'responses' ? 'var(--primary)' : 'var(--bg-secondary)',
            color: activeTab === 'responses' ? 'white' : 'var(--text-muted)',
            fontFamily: 'var(--font-heading)',
            fontWeight: 600,
            cursor: 'pointer',
            transition: 'all 0.2s ease'
          }}
        >
          <Layers size={18} />
          Independent Responses ({debate.responses?.length || 0})
        </button>

        <button
          onClick={() => setActiveTab('critiques')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.6rem 1.25rem',
            borderRadius: 'var(--radius-sm)',
            border: 'none',
            background: activeTab === 'critiques' ? 'var(--primary)' : 'var(--bg-secondary)',
            color: activeTab === 'critiques' ? 'white' : 'var(--text-muted)',
            fontFamily: 'var(--font-heading)',
            fontWeight: 600,
            cursor: 'pointer',
            transition: 'all 0.2s ease'
          }}
        >
          <MessageSquare size={18} />
          Cross-Critiques ({debate.critiques?.length || 0})
        </button>
      </div>

      {/* Tab Content */}
      {activeTab === 'consensus' && (
        <>
          <FinalConsensus debate={debate} />
          <h3 style={{ fontSize: '1.1rem', color: 'var(--header-title)', fontWeight: 700, marginBottom: '1rem', marginTop: '2rem' }}>
            Model Responses Breakdown
          </h3>
          {debate.responses?.map((resp) => (
            <ModelResponse key={resp.id} response={resp} />
          ))}
        </>
      )}

      {activeTab === 'responses' && (
        <div>
          {debate.responses?.map((resp) => (
            <ModelResponse key={resp.id} response={resp} />
          ))}
        </div>
      )}

      {activeTab === 'critiques' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '1.25rem' }}>
          {debate.critiques?.map((critique) => (
            <div
              key={critique.id}
              className="glass-panel"
              style={{ padding: '1.25rem', background: 'rgba(18, 25, 41, 0.5)', display: 'flex', flexDirection: 'column', gap: '0.85rem' }}
            >
              {/* Evaluator -> Target Header */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', paddingBottom: '0.5rem', borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
                <span className={`badge badge-${critique.provider?.toLowerCase()}`}>{critique.provider?.toUpperCase()}</span>
                <ArrowRight size={16} color="var(--text-muted)" />
                <span className={`badge badge-${critique.targetProvider?.toLowerCase()}`}>{critique.targetProvider?.toUpperCase()}</span>
              </div>

              {/* Structured Critique Detail Cards */}
              {critique.agreements && (
                <div style={{ fontSize: '0.85rem', color: '#34d399', background: 'rgba(16, 185, 129, 0.08)', padding: '0.6rem 0.8rem', borderRadius: '6px' }}>
                  <strong style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', marginBottom: '0.2rem' }}>
                    <CheckCircle2 size={14} /> Agreements:
                  </strong>
                  {critique.agreements}
                </div>
              )}

              {critique.disagreements && (
                <div style={{ fontSize: '0.85rem', color: '#fb7185', background: 'rgba(244, 63, 94, 0.08)', padding: '0.6rem 0.8rem', borderRadius: '6px' }}>
                  <strong style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', marginBottom: '0.2rem' }}>
                    <XCircle size={14} /> Disagreements:
                  </strong>
                  {critique.disagreements}
                </div>
              )}

              {/* Main Critique Narrative */}
              <div style={{ fontSize: '0.9rem', color: 'var(--text-main)', lineHeight: '1.6', whiteSpace: 'pre-wrap', background: 'rgba(9, 13, 22, 0.4)', padding: '0.85rem', borderRadius: '6px' }}>
                {critique.critique}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
