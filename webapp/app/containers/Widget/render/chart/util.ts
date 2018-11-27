/*
 * <<
 * Davinci
 * ==
 * Copyright (C) 2016 - 2017 EDP
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

import { IAxisConfig } from '../../components/Workbench/ConfigSections/AxisSection'
import { ILabelConfig } from '../../components/Workbench/ConfigSections/LabelSection'
import { ILegendConfig } from '../../components/Workbench/ConfigSections/LegendSection'
import { metricAxisLabelFormatter, decodeMetricName, getTextWidth } from '../../components/util'
import { CHART_LEGEND_POSITIONS } from '../../../../globalConstants'
import { dataFromItemLoaded } from 'containers/Bizlogic/actions'

interface ISplitLineConfig {
  showLine: boolean
  lineStyle: string
  lineSize: string
  lineColor: string
}

export function getDimetionAxisOption (
  dimetionAxisConfig: IAxisConfig,
  splitLineConfig: ISplitLineConfig,
  data: string[]
) {
  const {
    inverse,
    showLine: showLineX,
    lineStyle: lineStyleX,
    lineSize: lineSizeX,
    lineColor: lineColorX,
    showLabel: showLabelX,
    labelFontFamily: labelFontFamilyX,
    labelFontSize: labelFontSizeX,
    labelColor: labelColorX,
    nameLocation,
    nameGap,
    nameRotate,
    showInterval,
    xAxisInterval,
    xAxisRotate
  } = dimetionAxisConfig

  const {
    showLine,
    lineStyle,
    lineSize,
    lineColor
  } = splitLineConfig

  const intervalOption = showInterval
    ? { interval: xAxisInterval }
    : null

  return {
    data,
    inverse,
    axisLabel: {
      show: showLabelX,
      color: labelColorX,
      fontFamily: labelFontFamilyX,
      fontSize: labelFontSizeX,
      rotate: xAxisRotate,
      ...intervalOption
    },
    axisLine: {
      show: showLineX,
      lineStyle: {
        color: lineColorX,
        width: lineSizeX,
        type: lineStyleX
      }
    },
    axisTick: {
      show: showLabelX,
      lineStyle: {
        color: lineColorX
      }
    },
    splitLine: {
      show: showLine,
      lineStyle: {
        color: lineColor,
        width: lineSize,
        type: lineStyle
      }
    },
    nameLocation,
    nameRotate,
    nameGap
  }
}

export function getMetricAxisOption (
  metricAxisConfig: IAxisConfig,
  splitLineConfig: ISplitLineConfig,
  title: string,
  axis: 'x' | 'y' = 'y'
) {
  const {
    inverse,
    showLine: showLineY,
    lineStyle: lineStyleY,
    lineSize: lineSizeY,
    lineColor: lineColorY,
    showLabel: showLabelY,
    labelFontFamily: labelFontFamilyY,
    labelFontSize: labelFontSizeY,
    labelColor: labelColorY,
    showTitleAndUnit,
    titleFontFamily,
    titleFontSize,
    titleColor,
    nameLocation,
    nameRotate,
    nameGap
  } = metricAxisConfig

  const {
    showLine,
    lineStyle,
    lineSize,
    lineColor
  } = splitLineConfig

  return {
    type: 'value',
    inverse,
    axisLabel: {
      show: showLabelY,
      color: labelColorY,
      fontFamily: labelFontFamilyY,
      fontSize: labelFontSizeY,
      formatter: metricAxisLabelFormatter
    },
    axisLine: {
      show: showLineY,
      lineStyle: {
        color: lineColorY,
        width: lineSizeY,
        type: lineStyleY
      }
    },
    axisTick: {
      show: showLabelY,
      lineStyle: {
        color: lineColorY
      }
    },
    name: showTitleAndUnit ? title : '',
    nameLocation,
    nameGap,
    nameRotate,
    nameTextStyle: {
      color: titleColor,
      fontFamily: titleFontFamily,
      fontSize: titleFontSize
    },
    splitLine: {
      show: showLine,
      lineStyle: {
        color: lineColor,
        width: lineSize,
        type: lineStyle
      }
    }
  }
}

export function getLabelOption (type: string, labelConfig: ILabelConfig, emphasis?: boolean, options?: object) {
  const {
    showLabel,
    labelPosition,
    labelFontFamily,
    labelFontSize,
    labelColor,
    pieLabelPosition,
    funnelLabelPosition
  } = labelConfig

  let positionVale
  switch (type) {
    case 'pie':
      positionVale = pieLabelPosition
      break
    case 'funnel':
      positionVale = funnelLabelPosition
      break
    default:
      positionVale = labelPosition
      break
  }

  return {
    normal: {
      show: type === 'pie' && pieLabelPosition === 'center' ? false : showLabel,
      position: positionVale,
      color: labelColor,
      fontFamily: labelFontFamily,
      fontSize: labelFontSize,
      ...options
    },
    ...emphasis && {
      emphasis: {
        show: showLabel,
        position: positionVale,
        color: labelColor,
        fontFamily: labelFontFamily,
        fontSize: labelFontSize,
        ...options
      }
    }
  }
}

export function getLegendOption (legendConfig: ILegendConfig, seriesNames: string[]) {
  const {
    showLegend,
    legendPosition,
    selectAll,
    fontFamily,
    fontSize,
    color
  } = legendConfig

  let orient
  let positions

  switch (legendPosition) {
    case 'top':
      orient = { orient: 'horizontal' }
      positions = { top: 8, left: 8, right: 8, height: 32 }
      break
    case 'bottom':
      orient = { orient: 'horizontal' }
      positions = { bottom: 8, left: 8, right: 8, height: 32 }
      break
    case 'left':
      orient = { orient: 'vertical' }
      positions = { left: 8, top: 16, bottom: 24, width: 96 }
      break
    default:
      orient = { orient: 'vertical' }
      positions = { right: 8, top: 16, bottom: 24, width: 96 }
      break
  }

  const selected = {
    selected: seriesNames.reduce((obj, name) => ({
      ...obj,
      [name]: selectAll
    }), {})
  }

  return {
    show: showLegend,
    data: seriesNames,
    type: 'scroll',
    textStyle: {
      fontFamily,
      fontSize,
      color
    },
    ...orient,
    ...positions,
    ...selected
  }
}

export function getGridPositions (legendConfig: Partial<ILegendConfig>, seriesNames, dimetionAxisConfig?: IAxisConfig, xAxisData?: string[]) {
  const { showLegend, legendPosition, fontSize } = legendConfig
  return CHART_LEGEND_POSITIONS.reduce((grid, pos) => {
    const val = pos.value
    grid[val] = getGridBase(val, dimetionAxisConfig, xAxisData)
    if (showLegend) {
      grid[val] += legendPosition === val
        ? ['top', 'bottom'].includes(val)
          ? 32
          : 32 + Math.max(...seriesNames.map((s) => getTextWidth(s, '', `${fontSize}px`)))
        : 0
    }
    return grid
  }, {})
}

function getGridBase (pos, dimetionAxisConfig?: IAxisConfig, xAxisData?: string[]) {
  const labelFontSize = dimetionAxisConfig ? dimetionAxisConfig.labelFontSize : 12
  const xAxisRotate = dimetionAxisConfig ? dimetionAxisConfig.xAxisRotate : 0
  const maxWidth = Math.max(...(xAxisData || []).map((s) => getTextWidth(s, '', `${labelFontSize}px`)))

  switch (pos) {
    case 'top': return 24
    case 'left': return 64
    case 'right': return 24
    case 'bottom': return xAxisRotate ? 50 + Math.sin(xAxisRotate * Math.PI / 180) * maxWidth : 50
  }
}

export function makeGrouped (data, groupColumns, xAxisColumn, metrics, xAxisData) {
  const grouped = {}

  data.forEach((d) => {
    const groupingKey = groupColumns.map((col) => d[col]).join(' ')
    const colKey = d[xAxisColumn]
    if (!grouped[groupingKey]) {
      grouped[groupingKey] = {}
    }
    if (!grouped[groupingKey][colKey]) {
      grouped[groupingKey][colKey] = []
    }
    grouped[groupingKey][colKey].push(d)
  })

  Object.keys(grouped).map((groupingKey) => {
    const currentGroupValues = grouped[groupingKey]

    grouped[groupingKey] = xAxisData.map((xd) => {
      if (currentGroupValues[xd]) {
        return currentGroupValues[xd][0]
      } else {
        return metrics.reduce((obj, m) => ({ ...obj, [`${m.agg}(${decodeMetricName(m.name)})`]: 0 }), {})
      }
    })
  })

  return grouped
}

export function distinctXaxis (data, xAxisColumn) {
  return xAxisColumn
    ? Object.keys(data.reduce((distinct, ds) => {
      if (!distinct[ds[xAxisColumn]]) {
        distinct[ds[xAxisColumn]] = true
      }
      return distinct
    }, {}))
    : []
}

export function getSymbolSize (sizeRate, size) {
  return sizeRate ? Math.ceil(size / sizeRate) : size
}
