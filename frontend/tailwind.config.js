/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        // Spa / zen earthy palette - sage, clay, sand, deep forest.
        sage: {
          50: '#f4f7f2', 100: '#e5ede0', 200: '#cbdcc2', 300: '#a7c399',
          400: '#7fa56d', 500: '#5f874d', 600: '#4a6b3c', 700: '#3c5531',
          800: '#32462a', 900: '#2a3a24',
        },
        clay: {
          50: '#faf5f0', 100: '#f2e6d8', 200: '#e4cab0', 300: '#d4a883',
          400: '#c4895c', 500: '#b67144', 600: '#a85c39', 700: '#8c4831',
          800: '#723c2e', 900: '#5e3328',
        },
        sand: '#f7f3ec',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
