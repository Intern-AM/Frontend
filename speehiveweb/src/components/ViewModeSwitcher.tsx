import React from 'react';
import { LayoutGrid, List, AlignJustify } from 'lucide-react';

export type ViewMode = 'grid' | 'compact' | 'detailed';

interface ViewModeSwitcherProps {
  currentMode: ViewMode;
  onModeChange: (mode: ViewMode) => void;
}

export const ViewModeSwitcher: React.FC<ViewModeSwitcherProps> = ({ currentMode, onModeChange }) => {
  return (
    <div className="inline-flex items-center p-1.5 rounded-xl bg-slate-200/80 border border-slate-300 shadow-inner">
      <button
        onClick={() => onModeChange('grid')}
        className={`deep-3d-press flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
          currentMode === 'grid'
            ? 'bg-white text-blue-600 shadow-md border border-slate-200'
            : 'text-slate-600 hover:text-slate-900'
        }`}
        title="Grid View"
      >
        <LayoutGrid className="w-4 h-4" />
        <span className="hidden sm:inline">Grid</span>
      </button>

      <button
        onClick={() => onModeChange('compact')}
        className={`deep-3d-press flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
          currentMode === 'compact'
            ? 'bg-white text-blue-600 shadow-md border border-slate-200'
            : 'text-slate-600 hover:text-slate-900'
        }`}
        title="Compact List View"
      >
        <List className="w-4 h-4" />
        <span className="hidden sm:inline">Compact</span>
      </button>

      <button
        onClick={() => onModeChange('detailed')}
        className={`deep-3d-press flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
          currentMode === 'detailed'
            ? 'bg-white text-blue-600 shadow-md border border-slate-200'
            : 'text-slate-600 hover:text-slate-900'
        }`}
        title="Detailed View"
      >
        <AlignJustify className="w-4 h-4" />
        <span className="hidden sm:inline">Detailed</span>
      </button>
    </div>
  );
};
