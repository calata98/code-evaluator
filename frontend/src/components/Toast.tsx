export default function Toast({
  title,
  children,
  onClose,
  variant = "success",
}: {
  title: string;
  children?: React.ReactNode;
  onClose: () => void;
  variant?: "success" | "warning" | "error";
}) {
  const styles = {
    success: {
      border: "border-emerald-300/60 dark:border-emerald-800/60",
      iconColor: "text-emerald-600 dark:text-emerald-300",
      icon: (
        <path
          fill="currentColor"
          d="M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"
        />
      ),
    },
    warning: {
      border: "border-amber-300/60 dark:border-amber-700/60",
      iconColor: "text-amber-600 dark:text-amber-300",
      icon: (
        <path
          fill="currentColor"
          d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-2h2v2z"
        />
      ),
    },
    error: {
      border: "border-red-300/60 dark:border-red-800/60",
      iconColor: "text-red-600 dark:text-red-300",
      icon: (
        <path
          fill="currentColor"
          d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 
             10-4.48 10-10S17.52 2 12 2zm5 13.59L15.59 
             17 12 13.41 8.41 17 7 15.59 10.59 12 7 
             8.41 8.41 7 12 10.59 15.59 7 17 
             8.41 13.41 12 17 15.59z"
        />
      ),
    },
  }[variant];

  return (
    <div
      role="status"
      className={`fixed top-4 right-4 z-[60] rounded-lg shadow-lg 
      bg-white dark:bg-gray-900 px-4 py-3 flex items-start gap-3 
      border ${styles.border}`}
    >
      <svg viewBox="0 0 24 24" className={`h-5 w-5 mt-[2px] ${styles.iconColor}`}>
        {styles.icon}
      </svg>

      <div className="text-sm">
        <div className="font-medium">{title}</div>
        {children && (
          <div className="text-gray-600 dark:text-gray-300">{children}</div>
        )}
      </div>

      <button
        onClick={onClose}
        className="ml-2 text-xs text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
      >
        Close
      </button>
    </div>
  );
}
