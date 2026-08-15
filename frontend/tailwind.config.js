/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        darkBg: '#090d16',
        cardBg: 'rgba(18, 25, 41, 0.7)',
        brandPrimary: '#6366f1',
        brandPurple: '#a855f7',
        brandCyan: '#06b6d4',
        brandEmerald: '#10b981',
      },
      fontFamily: {
        heading: ['Outfit', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      }
    },
  },
  plugins: [],
}
