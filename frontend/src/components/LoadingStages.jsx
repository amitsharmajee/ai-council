import React, { useEffect, useState } from 'react';
import { CheckCircle2, Loader2, Circle, ShieldCheck, Scale, MessageSquareCode } from 'lucide-react';

const STAGES = [
  { id: 'received', title: 'Question received by AI Council Engine', icon: Circle },
  { id: 'models', title: 'Parallel Independent AI Analysis (OpenRouter, Claude, Gemini, Groq)', icon: MessageSquareCode },
  { id: 'collected', title: 'Initial independent model responses collected', icon: CheckCircle2 },
  { id: 'debating', title: 'Cross-Critique Debate Engine in progress', icon: Scale },
  { id: 'judging', title: 'Judge Synthesizer debating consensus & caveats', icon: ShieldCheck }
];

export default function LoadingStages({ selectedModels }) {
  const [currentStep, setCurrentStep] = useState(0);

  useEffect(() => {
    const timer1 = setTimeout(() => setCurrentStep(1), 500);
    const timer2 = setTimeout(() => setCurrentStep(2), 2200);
    const timer3 = setTimeout(() => setCurrentStep(3), 3600);
    const timer4 = setTimeout(() => setCurrentStep(4), 5000);

    return () => {
      clearTimeout(timer1);
      clearTimeout(timer2);
      clearTimeout(timer3);
      clearTimeout(timer4);
    };
  }, []);

  return (
    <div className="glass-panel" style={{ padding: '2rem', marginBottom: '2rem', textAlign: 'left' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
        <Loader2 className="pulse-glow" size={24} color="var(--primary)" style={{ animation: 'spin 2s linear infinite' }} />
        <div>
          <h3 style={{ fontSize: '1.2rem', color: 'var(--header-title)', fontWeight: 700 }}>AI Council Execution Pipeline</h3>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Processing multi-model query and debating independent responses...
          </p>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        {STAGES.map((stage, idx) => {
          const isDone = idx < currentStep;
          const isCurrent = idx === currentStep;
          const isPending = idx > currentStep;

          return (
            <div
              key={stage.id}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '1rem',
                padding: '0.75rem 1rem',
                borderRadius: 'var(--radius-sm)',
                background: isCurrent ? 'var(--bg-secondary)' : isDone ? 'rgba(16, 185, 129, 0.05)' : 'transparent',
                border: isCurrent ? '1px solid var(--primary)' : '1px solid transparent',
                transition: 'all 0.3s ease'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                {isDone && <CheckCircle2 size={20} color="var(--accent-emerald)" />}
                {isCurrent && <Loader2 size={20} color="var(--primary)" style={{ animation: 'spin 1.5s linear infinite' }} />}
                {isPending && <Circle size={20} color="var(--text-dim)" />}
              </div>

              <div style={{ flex: 1 }}>
                <span
                  style={{
                    fontSize: '0.95rem',
                    fontWeight: isCurrent ? 600 : 400,
                    color: isDone ? 'var(--accent-emerald)' : isCurrent ? 'var(--text-main)' : 'var(--text-muted)'
                  }}
                >
                  {stage.title}
                </span>

                {isCurrent && stage.id === 'models' && (
                  <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.4rem' }}>
                    {selectedModels.map(m => (
                      <span key={m} className={`badge badge-${m}`}>
                        {m.toUpperCase()} Debating...
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}
