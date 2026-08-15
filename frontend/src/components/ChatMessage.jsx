import React from 'react';
import ReactMarkdown from 'react-markdown';
import { Sparkles } from 'lucide-react';
import { AVAILABLE_MODELS } from './ModelSelector';

export default function ChatMessage({ message }) {
  if (message.role === 'user') {
    return (
      <div className="chat-message-user">
        <div className="chat-bubble-user">
          {message.content}
        </div>
      </div>
    );
  }

  // Loading state: "✨ AI Council is debating..." with active model chips
  if (message.isLoading) {
    const selectedIds = message.selectedModels || ['gemini', 'groq', 'openrouter'];

    return (
      <div className="chat-message-assistant">
        <div className="chat-bubble-assistant loading-bubble">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600, color: 'var(--primary)', marginBottom: '0.65rem', fontSize: '0.92rem' }}>
            <Sparkles size={16} />
            <span>AI Council is debating...</span>
          </div>

          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            {selectedIds.map(mId => {
              const modelObj = AVAILABLE_MODELS.find(x => x.id === mId) || { name: mId.toUpperCase(), color: '#818cf8' };
              return (
                <span
                  key={mId}
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '0.35rem',
                    padding: '0.25rem 0.65rem',
                    borderRadius: '16px',
                    fontSize: '0.78rem',
                    fontWeight: 600,
                    fontFamily: 'var(--font-heading)',
                    background: `${modelObj.color}15`,
                    color: modelObj.color,
                    border: `1px solid ${modelObj.color}40`
                  }}
                >
                  <span>{modelObj.name}</span>
                  <span className="pulsing-dot">●</span>
                </span>
              );
            })}
          </div>
        </div>
      </div>
    );
  }

  const debate = message.debate;
  if (!debate) return null;

  const providerNames = {
    gemini: 'Gemini',
    groq: 'Groq',
    openrouter: 'OpenRouter'
  };

  const selectedKey = (debate.selectedModel || '').toLowerCase();
  let providerTitle = providerNames[selectedKey];

  if (!providerTitle && debate.responses && debate.responses.length > 0) {
    const firstSuccessful = debate.responses.find(r => r.status === 'SUCCESS' || r.response);
    if (firstSuccessful && firstSuccessful.provider) {
      providerTitle = providerNames[firstSuccessful.provider.toLowerCase()] || firstSuccessful.provider;
    }
  }

  if (!providerTitle) {
    providerTitle = 'Groq';
  }

  return (
    <div className="chat-message-assistant">
      <div className="chat-bubble-assistant">
        {/* Simple Provider Header */}
        <div className="ai-provider-header">
          <span className="ai-provider-title">{providerTitle}</span>
        </div>

        {/* Clean Markdown Output sitting directly on background */}
        <div className="markdown-body">
          <ReactMarkdown>{debate.finalAnswer}</ReactMarkdown>
        </div>
      </div>
    </div>
  );
}
