import { Dialog, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'

type ResultDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  description?: string
}

export function ResultDialog({ open, onOpenChange, title, description }: ResultDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <div className="space-y-4">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        {description ? <p className="text-sm text-muted-foreground">{description}</p> : null}

        <DialogFooter>
          <Button type="button" onClick={() => onOpenChange(false)}>
            OK
          </Button>
        </DialogFooter>
      </div>
    </Dialog>
  )
}

