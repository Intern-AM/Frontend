/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'pulse-blue': '#2563EB',
        'pulse-green': '#16A34A',
        'pulse-amber': '#F59E0B',
        'pulse-red': '#EF4444',
        'pulse-purple': '#7C3AED',
      },
    },
  },
  plugins: [],
}
