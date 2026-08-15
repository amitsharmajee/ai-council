import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api';

export const debateApi = {
  /**
   * Start a multi-model debate
   * @param {string} question 
   * @param {Array<string>} models 
   */
  startDebate: async (question, models) => {
    const response = await axios.post(`${API_BASE}/debate`, {
      question,
      models,
    });
    return response.data;
  },

  /**
   * Get specific debate details by ID
   * @param {number|string} id 
   */
  getDebateById: async (id) => {
    const response = await axios.get(`${API_BASE}/debate/${id}`);
    return response.data;
  },

  /**
   * Get list of historical debates
   */
  getDebatesHistory: async () => {
    const response = await axios.get(`${API_BASE}/debates`);
    return response.data;
  },

  /**
   * System health check
   */
  getHealth: async () => {
    const response = await axios.get(`${API_BASE}/health`);
    return response.data;
  }
};
