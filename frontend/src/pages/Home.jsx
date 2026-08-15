import React, { useState, useRef, useEffect } from 'react';
import QuestionInput from '../components/QuestionInput';
import ChatMessage from '../components/ChatMessage';
import { debateApi } from '../services/debateApi';

export default function Home({ newChatTrigger }) {
  const [messages, setMessages] = useState([]);
  const [selectedModels, setSelectedModels] = useState(() => {
    try {
      const saved = localStorage.getItem('selectedModels') || sessionStorage.getItem('selectedModels');
      if (saved) {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed) && parsed.length > 0) {
          const sanitized = parsed.map(m => m === 'cerebras' || m === 'openai' || m === 'claude' ? 'openrouter' : m).filter((m, i, self) => self.indexOf(m) === i);
          if (sanitized.length > 0) return sanitized;
        }
      }
    } catch (_) {}
    return ['gemini', 'groq', 'openrouter'];
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const messagesEndRef = useRef(null);

  // Clear chat thread when newChatTrigger is triggered
  useEffect(() => {
    if (newChatTrigger) {
      setMessages([]);
      setError(null);
    }
  }, [newChatTrigger]);

  // Sync persistence
  useEffect(() => {
    try {
      localStorage.setItem('selectedModels', JSON.stringify(selectedModels));
    } catch (_) {}
  }, [selectedModels]);

  // Auto-scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  const handleStartDebate = async ({ question, models }) => {
    const userMsg = { id: `user-${Date.now()}`, role: 'user', content: question };
    const loadingMsg = { id: `loading-${Date.now()}`, role: 'assistant', isLoading: true, selectedModels: models };

    setMessages(prev => [...prev, userMsg, loadingMsg]);
    setIsLoading(true);
    setError(null);

    try {
      const data = await debateApi.startDebate(question, models);
      const assistantMsg = { id: data.debateId || `debate-${Date.now()}`, role: 'assistant', debate: data };

      setMessages(prev => prev.map(m => m.isLoading ? assistantMsg : m));
    } catch (err) {
      console.error('Debate error:', err);
      const errText = err.response?.data?.message || err.message || 'Failed to process request.';
      setError(errText);
      setMessages(prev => prev.filter(m => !m.isLoading));
    } finally {
      setIsLoading(false);
    }
  };

  const isNewChat = messages.length === 0;

  return (
    <div className={`chat-layout-container ${isNewChat ? 'new-chat-state' : 'active-chat-state'}`}>
      {isNewChat ? (
        <div className="new-chat-container">
          <div className="chat-welcome-container">
            <h1 className="welcome-title">Good to see you.</h1>
            <p className="welcome-subtitle">What's on your mind?</p>
          </div>

          <QuestionInput
            onSubmit={handleStartDebate}
            isLoading={isLoading}
            selectedModels={selectedModels}
            setSelectedModels={setSelectedModels}
          />
        </div>
      ) : (
        <>
          <div className="chat-messages-scroll">
            <div className="chat-thread-container">
              {messages.map((msg) => (
                <ChatMessage key={msg.id} message={msg} />
              ))}
              <div ref={messagesEndRef} />
            </div>

            {error && (
              <div style={{ margin: '1rem auto', maxWidth: '800px', padding: '1rem', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.3)', borderRadius: '12px', color: '#f87171', fontSize: '0.9rem' }}>
                <strong>Execution Error:</strong> {error}
              </div>
            )}
          </div>

          <QuestionInput
            onSubmit={handleStartDebate}
            isLoading={isLoading}
            selectedModels={selectedModels}
            setSelectedModels={setSelectedModels}
          />
        </>
      )}
    </div>
  );
}
