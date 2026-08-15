import React, { useState, useRef } from 'react';
import { Send, AlertCircle } from 'lucide-react';
import ModelSelector from './ModelSelector';

export default function QuestionInput({ onSubmit, isLoading, selectedModels, setSelectedModels }) {
  const [question, setQuestion] = useState('');
  const [error, setError] = useState('');
  const textareaRef = useRef(null);

  const handleSubmit = (e) => {
    if (e) e.preventDefault();
    if (isLoading) return;
    if (!question.trim()) {
      setError('Please enter a prompt before sending.');
      return;
    }
    if (!selectedModels || selectedModels.length === 0) {
      setError('Please select at least one AI model.');
      return;
    }
    setError('');
    const prompt = question.trim();
    setQuestion('');
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
    }
    onSubmit({ question: prompt, models: selectedModels });
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit();
    }
  };

  const handleInput = (e) => {
    setQuestion(e.target.value);
    if (error) setError('');
    // Auto-resize textarea
    e.target.style.height = 'auto';
    e.target.style.height = `${Math.min(e.target.scrollHeight, 180)}px`;
  };

  return (
    <div className="chat-input-wrapper">
      <form onSubmit={handleSubmit} className="chat-input-form">
        <div className="chat-input-box">
          {/* Top Row: Textarea + Send Button */}
          <div className="chat-input-top-row">
            <textarea
              ref={textareaRef}
              value={question}
              onChange={handleInput}
              onKeyDown={handleKeyDown}
              disabled={isLoading}
              rows={1}
              maxLength={4000}
              placeholder="Type your message..."
              className="chat-textarea"
            />

            <button
              type="submit"
              disabled={isLoading || !question.trim()}
              className={`chat-send-btn ${question.trim() && !isLoading ? 'active' : ''}`}
            >
              <Send size={16} />
            </button>
          </div>

          {/* Bottom Row: Model Selector Chips at bottom-left */}
          <div className="chat-input-bottom-row">
            <ModelSelector
              selectedModels={selectedModels}
              setSelectedModels={setSelectedModels}
              disabled={isLoading}
            />
          </div>
        </div>

        {error && (
          <div className="chat-input-error">
            <AlertCircle size={14} />
            <span>{error}</span>
          </div>
        )}
      </form>
    </div>
  );
}
