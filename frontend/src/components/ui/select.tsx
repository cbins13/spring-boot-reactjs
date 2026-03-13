import * as React from 'react'

type SelectProps = React.SelectHTMLAttributes<HTMLSelectElement>

export function Select(props: SelectProps) {
  return (
    <select
      {...props}
      className={
        'flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm transition-colors ' +
        'focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50 ' +
        (props.className ?? '')
      }
    />
  )
}

