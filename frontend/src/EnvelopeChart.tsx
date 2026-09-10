import { useState } from 'react'
import { motion } from 'motion/react'
import { money, type SummaryLine } from './api'
import { useElementWidth } from './useElementWidth'

type Props = { lines: SummaryLine[] }

type Hover = { line: SummaryLine; x: number; y: number }

const HEIGHT = 300
const PAD_TOP = 30
const PAD_BOTTOM = 46
const PAD_LEFT = 78 // gutter for the money axis, so ticks never sit under a bar
const PAD_RIGHT = 12
const MAX_BAR = 76
const TRACK_INSET = 8 // leaves a surface gap on each side between the track and the fill

/** Round tick values, so the axis reads in whole currency units. */
function ticks(max: number): number[] {
  if (max <= 0) return [0]
  const rough = max / 4
  const magnitude = 10 ** Math.floor(Math.log10(rough))
  const step = [1, 2, 2.5, 5, 10].map((m) => m * magnitude).find((s) => s >= rough) ?? magnitude * 10
  const result: number[] = []
  for (let value = 0; value <= max + step / 2; value += step) result.push(value)
  return result
}

export function EnvelopeChart({ lines }: Props) {
  const [hover, setHover] = useState<Hover | null>(null)
  const { ref, width: available } = useElementWidth<HTMLDivElement>()

  if (lines.length === 0) {
    return <p className="empty">No envelopes yet. Set a limit on a label to see it here.</p>
  }

  const width = Math.max(available || 640, PAD_LEFT + PAD_RIGHT + lines.length * 60)
  const plotWidth = width - PAD_LEFT - PAD_RIGHT
  const slot = plotWidth / lines.length
  const barWidth = Math.min(MAX_BAR, slot * 0.55)

  const ceiling = Math.max(...lines.flatMap((line) => [line.limitMinorUnits, line.consumed]), 1)
  const axis = ticks(ceiling)
  const top = axis[axis.length - 1]
  const plot = HEIGHT - PAD_TOP - PAD_BOTTOM
  const scale = (value: number) => (value / top) * plot
  const baseY = PAD_TOP + plot
  const overspent = lines.some((line) => line.remaining < 0)

  return (
    <div ref={ref}>
      <div className="legend">
        <span><i className="swatch rule" />Planned limit</span>
        <span><i className="swatch" style={{ background: 'var(--series-1)' }} />Consumed</span>
        {overspent && (
          <span style={{ color: 'var(--critical)' }}>
            <i className="swatch" style={{ background: 'var(--critical)' }} />⚠ Over the limit
          </span>
        )}
      </div>

      <div className="chart">
        <svg width={width} height={HEIGHT} role="img"
             aria-label="Consumed against the planned limit, one bar per label">
          {axis.map((value) => {
            const y = baseY - scale(value)
            return (
              <g key={value}>
                <line className={value === 0 ? 'baseline' : 'gridline'}
                      x1={PAD_LEFT - 8} x2={width - PAD_RIGHT} y1={y} y2={y} />
                <text className="axis-label" x={PAD_LEFT - 14} y={y + 4} textAnchor="end">
                  {money.format(value)}
                </text>
              </g>
            )
          })}

          {lines.map((line, index) => {
            const centre = PAD_LEFT + slot * (index + 0.5)
            const x = centre - barWidth / 2
            const limitHeight = scale(line.limitMinorUnits)
            const consumedHeight = scale(line.consumed)
            const over = line.remaining < 0
            const name = line.labelName ?? 'unnamed'
            const labelY = baseY - Math.max(consumedHeight, limitHeight) - 9

            return (
              <g key={line.labelId}
                 onMouseMove={(event) => setHover({ line, x: event.clientX, y: event.clientY })}
                 onMouseLeave={() => setHover(null)}>
                {/* Hit target wider than the mark itself. */}
                <rect x={centre - slot / 2} y={PAD_TOP} width={slot} height={plot} fill="transparent" />

                <rect x={x} y={baseY - limitHeight} width={barWidth} height={Math.max(limitHeight, 2)}
                      rx={4} fill="var(--series-1-soft)" />

                <motion.rect
                  x={x + TRACK_INSET / 2}
                  width={barWidth - TRACK_INSET}
                  rx={4}
                  fill={over ? 'var(--critical)' : 'var(--series-1)'}
                  initial={{ height: 0, y: baseY }}
                  animate={{
                    height: consumedHeight > 0 ? Math.max(consumedHeight, 3) : 0,
                    y: baseY - consumedHeight,
                  }}
                  transition={{ type: 'spring', stiffness: 130, damping: 20, delay: index * 0.06 }}
                />

                {/* The limit is a target, so it is always marked by a rule rather than
                    relying on the track fill being told apart from the bar. */}
                <line x1={x - 5} x2={x + barWidth + 5}
                      y1={baseY - limitHeight} y2={baseY - limitHeight}
                      stroke="var(--text-secondary)" strokeWidth={2} strokeDasharray="4 3" />

                <text className="bar-value" x={centre} y={labelY} textAnchor="middle">
                  {money.format(line.consumed)}
                </text>

                <text className="axis-label" x={centre} y={baseY + 18} textAnchor="middle">
                  {name.length > 11 ? `${name.slice(0, 10)}…` : name}
                </text>
                {over && (
                  <text className="axis-label" x={centre} y={baseY + 33}
                        textAnchor="middle" fill="var(--critical)">
                    ⚠ {money.format(-line.remaining)} over
                  </text>
                )}
              </g>
            )
          })}
        </svg>
      </div>

      {hover && (
        <div className="tooltip" style={{ left: hover.x + 14, top: hover.y + 14 }}>
          <div className="name">{hover.line.labelName ?? 'unnamed'}</div>
          <dl>
            <dt>Limit</dt><dd>{money.format(hover.line.limitMinorUnits)}</dd>
            <dt>Consumed</dt><dd>{money.format(hover.line.consumed)}</dd>
            <dt>Remaining</dt>
            <dd style={{ color: hover.line.remaining < 0 ? 'var(--critical)' : undefined }}>
              {money.format(hover.line.remaining)}
            </dd>
          </dl>
        </div>
      )}
    </div>
  )
}
