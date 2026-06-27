/** Small presentational helpers shared across pages. */

export function Spinner({ label = 'Loading…' }) {
  return (
    <div className="flex items-center justify-center gap-3 py-16 text-sage-500">
      <span className="h-5 w-5 animate-spin rounded-full border-2 border-sage-300 border-t-sage-600" />
      {label}
    </div>
  );
}

export function ErrorNote({ children }) {
  return (
    <div className="rounded-2xl border border-clay-200 bg-clay-50 px-4 py-3 text-sm text-clay-700">
      {children}
    </div>
  );
}

export function EmptyState({ title, hint }) {
  return (
    <div className="card flex flex-col items-center gap-2 px-6 py-16 text-center">
      <span className="text-3xl">🍃</span>
      <p className="font-semibold text-sage-800">{title}</p>
      {hint && <p className="text-sm text-sage-500">{hint}</p>}
    </div>
  );
}

export function Badge({ children, tone = 'sage' }) {
  const tones = {
    sage: 'bg-sage-100 text-sage-700',
    clay: 'bg-clay-100 text-clay-700',
    muted: 'bg-sage-50 text-sage-400',
  };
  return (
    <span className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${tones[tone]}`}>
      {children}
    </span>
  );
}
