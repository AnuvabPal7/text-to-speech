import React from 'react';
import { Download, CheckCircle2 } from 'lucide-react';

interface DownloadButtonProps {
  audioUrl: string;
  filename?: string;
  format?: string;
  fileSizeBytes?: number;
  disabled?: boolean;
}

export const DownloadButton: React.FC<DownloadButtonProps> = ({
  audioUrl,
  filename = 'speech.mp3',
  format = 'mp3',
  fileSizeBytes,
  disabled = false,
}) => {
  const [downloaded, setDownloaded] = React.useState(false);

  const handleDownload = () => {
    setDownloaded(true);
    setTimeout(() => setDownloaded(false), 2500);
  };

  const formattedSize = fileSizeBytes
    ? `${(fileSizeBytes / 1024).toFixed(1)} KB`
    : null;

  return (
    <a
      href={audioUrl}
      download={filename}
      onClick={handleDownload}
      className={`inline-flex items-center justify-center gap-2 h-11 px-5 rounded-xl text-sm font-semibold transition-all shadow-xs cursor-pointer ${
        disabled
          ? 'bg-slate-100 text-slate-400 pointer-events-none'
          : downloaded
          ? 'bg-emerald-600 text-white shadow-emerald-200'
          : 'bg-slate-900 hover:bg-slate-800 text-white shadow-slate-200 hover:shadow-slate-300'
      }`}
    >
      {downloaded ? (
        <>
          <CheckCircle2 className="w-4 h-4 text-emerald-200" />
          <span>Downloaded!</span>
        </>
      ) : (
        <>
          <Download className="w-4 h-4" />
          <span>Download Audio ({format.toUpperCase()})</span>
          {formattedSize && (
            <span className="text-xs font-normal opacity-80 pl-1 border-l border-white/20">
              {formattedSize}
            </span>
          )}
        </>
      )}
    </a>
  );
};
