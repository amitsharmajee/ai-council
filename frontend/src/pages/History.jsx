import React, { useState, useEffect } from 'react';
import { debateApi } from '../services/debateApi';
import DebateView from '../components/DebateView';
import { History as HistoryIcon, Clock, ChevronRight, Search, RefreshCw, Moon, Sun, Eye, ChevronDown } from 'lucide-react';

export default function History({ currentTheme = 'dark', onSelectTheme }) {
  const [debates, setDebates] = useState([]);
  const [selectedDebate, setSelectedDebate] = useState(null);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [showThemeMenu, setShowThemeMenu] = useState(false);

  const fetchHistory = async () => {
    setLoading(true);
    try {
      const data = await debateApi.getDebatesHistory();
      setDebates(data);
      if (data.length > 0 && !selectedDebate) {
        setSelectedDebate(data[0]);
      }
    } catch (err) {
      console.error('Failed to fetch history:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  const filteredDebates = debates.filter(d =>
    d.question.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem', color: 'var(--text-main)', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <HistoryIcon size={24} color="var(--primary)" />
            AI Council History Log
          </h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            Review past multi-model debates and synthesized decisions
          </p>
        </div>

        <button
          onClick={fetchHistory}
          style={{
            background: 'var(--bg-surface)',
            border: '1px solid var(--border-color)',
            color: 'var(--text-main)',
            padding: '0.5rem 1rem',
            borderRadius: 'var(--radius-sm)',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            fontSize: '0.85rem'
          }}
        >
          <RefreshCw size={16} />
          Refresh
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '320px 1fr', gap: '1.5rem', alignItems: 'start' }}>
        {/* Left Sidebar List */}
        <div className="glass-panel" style={{ padding: '1rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          {/* Appearance / Theme Button & Compact Popover Menu */}
          <div style={{ position: 'relative' }}>
            <button
              type="button"
              onClick={() => setShowThemeMenu(!showThemeMenu)}
              className="theme-toggle-btn"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.45rem',
                padding: '0.45rem 0.75rem',
                borderRadius: '8px',
                fontSize: '0.83rem',
                fontWeight: 600,
                border: '1px solid var(--border-color)',
                background: 'var(--bg-secondary)',
                color: 'var(--text-main)',
                cursor: 'pointer',
                width: '100%',
                justifyContent: 'space-between'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                <span style={{ fontSize: '1rem', lineHeight: 1 }}>◐</span>
                <span>Appearance</span>
              </div>
              <ChevronDown size={14} color="var(--text-muted)" />
            </button>

            {showThemeMenu && (
              <div
                className="theme-dropdown-menu"
                style={{
                  position: 'absolute',
                  top: 'calc(100% + 6px)',
                  left: 0,
                  right: 0,
                  zIndex: 50,
                  background: 'var(--bg-surface)',
                  border: '1px solid var(--border-color)',
                  borderRadius: '10px',
                  padding: '0.4rem',
                  boxShadow: '0 8px 24px rgba(0,0,0,0.2)',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.2rem'
                }}
              >
                <div style={{ fontSize: '0.7rem', fontWeight: 700, color: 'var(--text-muted)', padding: '0.2rem 0.5rem', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Appearance
                </div>

                {[
                  { id: 'dark', label: 'Dark', icon: Moon },
                  { id: 'light', label: 'Light', icon: Sun },
                  { id: 'eye-comfort', label: 'Eye Comfort', icon: Eye }
                ].map((item) => {
                  const isSelected = currentTheme === item.id;
                  const Icon = item.icon;
                  return (
                    <button
                      key={item.id}
                      type="button"
                      onClick={() => {
                        if (onSelectTheme) onSelectTheme(item.id);
                        setShowThemeMenu(false);
                      }}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '0.45rem 0.65rem',
                        borderRadius: '6px',
                        fontSize: '0.83rem',
                        fontWeight: isSelected ? 600 : 400,
                        border: 'none',
                        background: isSelected ? 'var(--bg-secondary)' : 'transparent',
                        color: isSelected ? 'var(--primary)' : 'var(--text-main)',
                        cursor: 'pointer',
                        textAlign: 'left'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span style={{ fontSize: '0.75rem', color: isSelected ? 'var(--primary)' : 'var(--text-muted)' }}>
                          {isSelected ? '●' : '○'}
                        </span>
                        <span>{item.label}</span>
                      </div>
                      <Icon size={14} color={isSelected ? 'var(--primary)' : 'var(--text-muted)'} />
                    </button>
                  );
                })}
              </div>
            )}
          </div>

          <div style={{ height: '1px', background: 'var(--border-color)', margin: '0.1rem 0' }} />

          <h3 style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--text-main)', margin: '0' }}>
            History
          </h3>

          {/* Search bar */}
          <div style={{ position: 'relative', marginBottom: '1rem' }}>
            <Search size={16} color="var(--text-muted)" style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)' }} />
            <input
              type="text"
              placeholder="Filter past debates..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{
                width: '100%',
                background: 'var(--bg-secondary)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-sm)',
                padding: '0.5rem 0.5rem 0.5rem 2.2rem',
                color: 'var(--text-main)',
                fontSize: '0.85rem',
                outline: 'none'
              }}
            />
          </div>

          {loading ? (
            <div style={{ padding: '2rem 0', textAlign: 'center', color: 'var(--text-muted)' }}>
              Loading history...
            </div>
          ) : filteredDebates.length === 0 ? (
            <div style={{ padding: '2rem 0', textAlign: 'center', color: 'var(--text-dim)', fontSize: '0.9rem' }}>
              No debates found.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', maxHeight: '600px', overflowY: 'auto' }}>
              {filteredDebates.map((item) => {
                const isSelected = selectedDebate?.debateId === item.debateId;
                return (
                  <div
                    key={item.debateId}
                    onClick={() => setSelectedDebate(item)}
                    style={{
                      padding: '0.75rem',
                      borderRadius: 'var(--radius-sm)',
                      background: isSelected ? 'var(--bg-secondary)' : 'transparent',
                      border: isSelected ? '1px solid var(--primary)' : '1px solid transparent',
                      cursor: 'pointer',
                      transition: 'all 0.2s ease'
                    }}
                  >
                    <div style={{ fontWeight: 600, fontSize: '0.88rem', color: isSelected ? 'var(--primary)' : 'var(--text-main)', marginBottom: '0.3rem', lineClamp: 2, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                      {item.question}
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                        <Clock size={12} />
                        {new Date(item.createdAt).toLocaleDateString()}
                      </span>
                      <ChevronRight size={14} />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Right Main Content */}
        <div>
          {selectedDebate ? (
            <DebateView debate={selectedDebate} />
          ) : (
            <div className="glass-panel" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              Select a debate from the left log to view detailed consensus and model critiques.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
