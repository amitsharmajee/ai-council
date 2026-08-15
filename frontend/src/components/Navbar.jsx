import React from 'react';
import { Cpu, History, Plus } from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab, onNewChat }) {
  return (
    <header className="navbar">
      <div 
        className="brand" 
        onClick={onNewChat} 
        style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '0.75rem' }}
        title="Start a new chat"
      >
        <div className="brand-icon">
          <Cpu size={20} />
        </div>
        <span className="brand-title gradient-text">AI Council</span>
      </div>

      <nav className="nav-links">
        <button
          className={`nav-btn ${activeTab === 'history' ? 'active' : ''}`}
          onClick={() => setActiveTab('history')}
          title="History Log"
          style={{ width: '38px', height: '38px', padding: 0, justifyContent: 'center', borderRadius: '50%' }}
        >
          <History size={18} />
        </button>

        <button
          className="new-chat-btn"
          onClick={onNewChat}
        >
          <Plus size={16} />
          <span>New Debate</span>
        </button>
      </nav>
    </header>
  );
}
