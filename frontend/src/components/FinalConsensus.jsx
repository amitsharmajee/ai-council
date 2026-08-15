import React from 'react';
import ReactMarkdown from 'react-markdown';
import { Award, CheckCircle, XCircle, AlertTriangle, Sparkles } from 'lucide-react';

export default function FinalConsensus({ debate }) {
  if (!debate) return null;

  return (
    <div className="glass-panel" style={{ padding: '2rem', marginBottom: '2rem', border: '1px solid rgba(168, 85, 247, 0.3)' }}>
      {/* Header Badge */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{ background: 'var(--primary)', padding: '0.5rem', borderRadius: '10px', color: 'white', display: 'flex' }}>
            <Award size={22} />
          </div>
          <div>
            <h2 style={{ fontSize: '1.3rem', color: 'var(--header-title)', fontWeight: 800 }}>Final Council Consensus</h2>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Synthesized by Judge Engine across all AI perspectives</span>
          </div>
        </div>

        <span style={{ background: 'rgba(16, 185, 129, 0.15)', color: '#34d399', border: '1px solid rgba(16, 185, 129, 0.3)', padding: '0.4rem 0.85rem', borderRadius: '20px', fontSize: '0.8rem', fontWeight: 600 }}>
          Status: {debate.status}
        </span>
      </div>

      {/* Primary Recommendation */}
      <div style={{ background: 'var(--bg-surface)', borderLeft: '4px solid var(--primary)', borderRadius: '0 var(--radius-md) var(--radius-md) 0', padding: '1.25rem', marginBottom: '1.5rem', border: '1px solid var(--border-color)' }}>
        <h3 style={{ fontSize: '1rem', color: 'var(--primary)', fontWeight: 700, marginBottom: '0.4rem', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
          <Sparkles size={18} />
          Consensus Recommendation
        </h3>
        <div className="markdown-body" style={{ fontSize: '1rem', color: 'var(--text-main)', fontWeight: 500 }}>
          <ReactMarkdown>{debate.finalAnswer}</ReactMarkdown>
        </div>
        {debate.consensus && (
          <div className="markdown-body" style={{ fontSize: '0.92rem', color: 'var(--text-muted)', marginTop: '0.6rem' }}>
            <ReactMarkdown>{debate.consensus}</ReactMarkdown>
          </div>
        )}
      </div>

      <div className="grid-2" style={{ marginBottom: '1.5rem' }}>
        {/* Agreement Section */}
        <div style={{ background: 'rgba(16, 185, 129, 0.05)', border: '1px solid rgba(16, 185, 129, 0.2)', borderRadius: 'var(--radius-md)', padding: '1.25rem' }}>
          <h4 style={{ fontSize: '0.95rem', color: '#34d399', fontWeight: 700, marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <CheckCircle size={18} />
            Points of Agreement
          </h4>
          <div className="markdown-body">
            <ReactMarkdown>{debate.agreementPoints}</ReactMarkdown>
          </div>
        </div>

        {/* Disagreement Section */}
        <div style={{ background: 'rgba(244, 63, 94, 0.05)', border: '1px solid rgba(244, 63, 94, 0.2)', borderRadius: 'var(--radius-md)', padding: '1.25rem' }}>
          <h4 style={{ fontSize: '0.95rem', color: '#fb7185', fontWeight: 700, marginBottom: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <XCircle size={18} />
            Points of Disagreement
          </h4>
          <div className="markdown-body">
            <ReactMarkdown>{debate.disagreementPoints}</ReactMarkdown>
          </div>
        </div>
      </div>

      {/* Caveats / Warning */}
      {debate.caveats && (
        <div style={{ background: 'rgba(245, 158, 11, 0.08)', border: '1px solid rgba(245, 158, 11, 0.25)', borderRadius: 'var(--radius-md)', padding: '1rem 1.25rem', display: 'flex', gap: '0.75rem', alignItems: 'flex-start' }}>
          <AlertTriangle size={20} color="var(--accent-amber)" style={{ flexShrink: 0, marginTop: '2px' }} />
          <div className="markdown-body" style={{ color: '#fcd34d' }}>
            <ReactMarkdown>{debate.caveats}</ReactMarkdown>
          </div>
        </div>
      )}
    </div>
  );
}
