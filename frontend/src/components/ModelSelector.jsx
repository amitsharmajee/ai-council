import React from 'react';
import { Check, ShieldCheck, Zap, Cpu, Flame } from 'lucide-react';

export const AVAILABLE_MODELS = [
  {
    id: 'gemini',
    name: 'Gemini',
    fullName: 'Google Gemini 3.5',
    modelName: 'Gemini 3.5 Flash',
    badgeClass: 'badge-gemini',
    icon: ShieldCheck,
    color: '#38bdf8'
  },
  {
    id: 'groq',
    name: 'Groq',
    fullName: 'Groq Llama 3.3',
    modelName: 'Llama 3.3 70B',
    badgeClass: 'badge-groq',
    icon: Flame,
    color: '#f97316'
  },
  {
    id: 'openrouter',
    name: 'OpenRouter',
    fullName: 'OpenRouter free',
    modelName: 'openrouter/free',
    badgeClass: 'badge-openrouter',
    icon: Cpu,
    color: '#a855f7'
  }
];

export default function ModelSelector({ selectedModels, setSelectedModels, disabled }) {
  const toggleModel = (id) => {
    if (disabled) return;
    if (selectedModels.includes(id)) {
      if (selectedModels.length === 1) return; // Require at least 1 model
      setSelectedModels(selectedModels.filter(m => m !== id));
    } else {
      setSelectedModels([...selectedModels, id]);
    }
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
      {AVAILABLE_MODELS.map((model) => {
        const isSelected = selectedModels.includes(model.id);

        return (
          <button
            key={model.id}
            type="button"
            disabled={disabled}
            onClick={() => toggleModel(model.id)}
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.4rem',
              padding: '0.35rem 0.75rem',
              borderRadius: '20px',
              fontSize: '0.82rem',
              fontWeight: 600,
              fontFamily: 'var(--font-heading)',
              cursor: disabled ? 'not-allowed' : 'pointer',
              transition: 'all 0.2s ease',
              background: isSelected ? `${model.color}20` : 'var(--bg-secondary)',
              color: isSelected ? model.color : 'var(--text-muted)',
              border: isSelected ? `1px solid ${model.color}60` : '1px solid var(--border-color)',
              opacity: disabled && !isSelected ? 0.4 : 1
            }}
          >
            {isSelected && (
              disabled ? (
                <span className="pulsing-dot" style={{ color: model.color, fontSize: '0.9rem', lineHeight: '1' }}>●</span>
              ) : (
                <Check size={14} strokeWidth={2.5} />
              )
            )}
            <span>{model.name}</span>
          </button>
        );
      })}
    </div>
  );
}
