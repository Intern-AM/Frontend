import React, { useState } from 'react';
import { Lightbulb, Copy, Check, ChevronDown, ChevronUp } from 'lucide-react';

interface ImagePromptCardProps {
  promptText?: string;
}

export const ImagePromptCard: React.FC<ImagePromptCardProps> = ({ promptText }) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [isCopied, setIsCopied] = useState(false);

  const hasPrompt = Boolean(promptText && promptText.trim().length > 0);
  const textToDisplay = hasPrompt
    ? promptText!
    : 'No image prompt available for this campaign.';

  const handleCopy = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (hasPrompt) {
      navigator.clipboard.writeText(promptText!);
      setIsCopied(true);
      setTimeout(() => setIsCopied(false), 2000);
    }
  };

  return (
    <div className="deep-3d-card p-4 bg-amber-50/60 border border-amber-200/80 rounded-2xl mb-4">
      <div
        className="flex items-center justify-between cursor-pointer"
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <div className="flex items-center gap-2.5">
          <div className="p-2 rounded-xl bg-amber-500 text-white shadow-sm">
            <Lightbulb className="w-4 h-4" />
          </div>
          <div>
            <h4 className="font-extrabold text-xs uppercase tracking-wider text-amber-900">
              AI Creative Image Prompt
            </h4>
            <p className="text-[11px] text-amber-700 font-medium">
              Click to {isExpanded ? 'collapse' : 'view full prompt details'}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleCopy}
            disabled={!hasPrompt}
            className={`deep-3d-press px-2.5 py-1 rounded-lg border text-xs font-bold flex items-center gap-1 transition-colors ${
              !hasPrompt
                ? 'bg-amber-100/50 border-amber-200 text-amber-400 cursor-not-allowed'
                : 'bg-white border-amber-300 text-amber-800 hover:bg-amber-100 shadow-sm'
            }`}
          >
            {isCopied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
            <span>{isCopied ? 'Copied' : 'Copy Prompt'}</span>
          </button>
          <button className="p-1 text-amber-700">
            {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {isExpanded && (
        <div className="mt-3 pt-3 border-t border-amber-200/60 text-xs text-amber-950 font-mono leading-relaxed bg-white/80 p-3 rounded-xl border border-amber-200 shadow-inner">
          {textToDisplay}
        </div>
      )}
    </div>
  );
};

