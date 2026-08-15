import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import History from './pages/History';

export default function App() {
  const [activeTab, setActiveTab] = useState('home');
  const [newChatTrigger, setNewChatTrigger] = useState(0);
  const [theme, setTheme] = useState(() => {
    try {
      return localStorage.getItem('theme') || 'dark';
    } catch (_) {
      return 'dark';
    }
  });

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    try {
      localStorage.setItem('theme', theme);
    } catch (_) {}
  }, [theme]);

  const handleNewChat = () => {
    setActiveTab('home');
    setNewChatTrigger(prev => prev + 1);
  };

  return (
    <div className="app-container">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} onNewChat={handleNewChat} />
      
      <main style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        {activeTab === 'home' && <Home newChatTrigger={newChatTrigger} />}
        {activeTab === 'history' && <History currentTheme={theme} onSelectTheme={setTheme} />}
      </main>
    </div>
  );
}
