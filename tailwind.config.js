/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        surface: {
          50: '#fafafa',
          100: '#f5f5f5',
          200: '#e5e5e5',
          300: '#d4d4d4',
          400: '#a3a3a3',
          500: '#737373',
          600: '#525252',
          700: '#404040',
          800: '#262626',
          900: '#171717',
          950: '#0a0a0a',
        },
        border: {
          DEFAULT: '#e5e5e5',
          dark: '#404040',
        },
        accent: {
          mauve: {
            50: '#faf7fa',
            100: '#f2eef5',
            200: '#e4ddea',
            300: '#c9bdd6',
            400: '#a896bc',
            500: '#8873a3',
            600: '#765c94',
            700: '#634b7d',
            800: '#523f6a',
            900: '#453859',
            950: '#2b1e3f',
          },
          blue: {
            50: '#f5f9ff',
            100: '#e8f2ff',
            200: '#c4ddff',
            300: '#8fbfff',
            400: '#4d8ffa',
            500: '#1d6cf5',
            600: '#1554db',
            700: '#1345b8',
            800: '#153a90',
            900: '#153471',
            950: '#0d1f4a',
          }
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'Menlo', 'monospace'],
      },
      boxShadow: {
        'soft': '0 2px 8px -2px rgba(0, 0, 0, 0.08), 0 1px 2px -1px rgba(0, 0, 0, 0.04)',
        'medium': '0 4px 16px -4px rgba(0, 0, 0, 0.12), 0 2px 4px -2px rgba(0, 0, 0, 0.04)',
        'large': '0 8px 32px -8px rgba(0, 0, 0, 0.16), 0 4px 8px -4px rgba(0, 0, 0, 0.06)',
      },
      borderRadius: {
        'xl': '12px',
        '2xl': '16px',
        '3xl': '24px',
      },
      animation: {
        'fade-in': 'fadeIn 0.2s ease-out',
        'slide-up': 'slideUp 0.3s ease-out',
        'scale-in': 'scaleIn 0.2s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { opacity: '0', transform: 'translateY(10px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        scaleIn: {
          '0%': { opacity: '0', transform: 'scale(0.95)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
      },
    },
  },
  plugins: [],
}
