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

import { IChartProps } from '../../components/Chart'
import {
  decodeMetricName,
  getChartTooltipLabel,
  getTextWidth,
  getAggregatorLocale,
  metricAxisLabelFormatter
} from '../../components/util'
import {
  getLegendOption,
  getGridPositions,
  getDimetionAxisOption
} from './util'
import { getFormattedValue } from '../../components/Config/Format'
import { getFieldAlias } from '../../components/Config/Field'

export default function (chartProps: IChartProps, drillOptions) {
  const {
    width,
    height,
    data,
    cols,
    metrics,
    chartStyles
    // color,
    // tip
  } = chartProps

  const {
    legend,
    spec,
    doubleYAxis,
    xAxis,
    splitLine
  } = chartStyles

  const {
    legendPosition,
    fontSize
  } = legend

  const {
    stack,
    smooth,
    step,
    symbol,
    label
  } = spec

  const {
    yAxisLeft,
    yAxisRight,
    leftMin,
    leftMax,
    rightMin,
    rightMax,
    yAxisSplitNumber,
    dataZoomThreshold
  } = doubleYAxis

  const {
    labelColor,
    labelFontFamily,
    labelFontSize,
    lineColor,
    lineSize,
    lineStyle,
    showLabel,
    showLine,
    xAxisInterval,
    xAxisRotate
  } = xAxis

  const {
    showVerticalLine,
    verticalLineColor,
    verticalLineSize,
    verticalLineStyle,
    showHorizontalLine,
    horizontalLineColor,
    horizontalLineSize,
    horizontalLineStyle
  } = splitLine

  const labelOption = {
    label: {
      normal: {
        show: label,
        position: 'top'
      }
    }
  }

  const { selectedItems } = drillOptions
  const { secondaryMetrics } = chartProps

  const xAxisData = showLabel ? data.map((d) => d[cols[0].name]) : []
  const seriesData = secondaryMetrics
    ? getAixsMetrics('metrics', metrics, data, stack, labelOption, selectedItems, {key: 'yAxisLeft', type: yAxisLeft})
      .concat(getAixsMetrics('secondaryMetrics', secondaryMetrics, data, stack, labelOption, selectedItems, {key: 'yAxisRight', type: yAxisRight}))
    : getAixsMetrics('metrics', metrics, data, stack, labelOption, selectedItems, {key: 'yAxisLeft', type: yAxisLeft})
  const seriesObj = {
    series: seriesData.map((series) => {
      if (series.type === 'line') {
        return {
          ...series,
          symbol: symbol ? 'emptyCircle' : 'none',
          smooth,
          step
        }
      } else {
        return series
      }
    })
  }

  let legendOption
  let gridOptions
  if (seriesData.length > 1) {
    const seriesNames = seriesData.map((s) => s.name)
    legendOption = {
      legend: getLegendOption(legend, seriesNames)
    }
    gridOptions = {
      grid: getGridPositions(legend, seriesNames, 'doubleYAxis', false, null, xAxis, xAxisData)
    }
  }

  let leftMaxValue
  let rightMaxValue

  if (leftMax) {
    leftMaxValue = leftMax
  } else {
    if (stack) {
      leftMaxValue = metrics.reduce((num, m) => num + Math.max(...data.map((d) => d[`${m.agg}(${decodeMetricName(m.name)})`])), 0)
    } else {
      leftMaxValue = Math.max(...metrics.map((m) => {
          return Math.max(...data.map((d) => {
            return typeof d[`${m.agg}(${decodeMetricName(m.name)})`] === 'number' ? d[`${m.agg}(${decodeMetricName(m.name)})`] : 0
          }
        ))}
      ))
    }
  }
  if (rightMax) {
    rightMaxValue = rightMax
  } else {
    if (stack) {
      rightMaxValue = secondaryMetrics.reduce((num, m) => num + Math.max(...data.map((d) => d[`${m.agg}(${decodeMetricName(m.name)})`])), 0)
    } else {
      rightMaxValue = Math.max(...secondaryMetrics.map((m) => {
        return Math.max(...data.map((d) => {
            return typeof d[`${m.agg}(${decodeMetricName(m.name)})`] === 'number' ? d[`${m.agg}(${decodeMetricName(m.name)})`] : 0
          }
        ))
      }))
    }
  }

  const leftInterval = getYaxisInterval(leftMaxValue, (yAxisSplitNumber - 1))
  // 右边根据左边的比例来
  const rightInterval = rightMaxValue * leftInterval / leftMaxValue
  // const rightInterval = rightMaxValue > 0 ? getYaxisInterval(rightMaxValue, (yAxisSplitNumber - 1)) : leftInterval

  const inverseOption = xAxis.inverse ? { inverse: true } : null

  const xAxisSplitLineConfig = {
    showLine: showVerticalLine,
    lineColor: verticalLineColor,
    lineSize: verticalLineSize,
    lineStyle: verticalLineStyle
  }

  const allMetrics = secondaryMetrics ? [].concat(metrics).concat(secondaryMetrics) : metrics
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {type: 'cross'},
      formatter (params) {
        const tooltipLabels = [getFormattedValue(params[0].name, cols[0].format), '<br/>']
        params.reduce((acc, param) => {
          const { color, value, seriesIndex } = param
          if (color) {
            acc.push(`<span class="widget-tooltip-circle" style="background: ${color}"></span>`)
          }
          acc.push(getFieldAlias(allMetrics[seriesIndex].field, {}) || decodeMetricName(allMetrics[seriesIndex].name))
          acc.push(': ', getFormattedValue(value, allMetrics[seriesIndex].format), '<br/>')
          return acc
        }, tooltipLabels)
        return tooltipLabels.join('')
      }
    },
    xAxis: getDimetionAxisOption(xAxis, xAxisSplitLineConfig, xAxisData),
    yAxis: [
      {
        type: 'value',
        key: 'yAxisIndex0',
        min: rightMin ? rightMin : 0,
        max: rightMaxValue,
        // 不能直接用这个splitNumber，因为根据echarts的机制，这个分割段数只是个预估值，最后实际显示的段数会在这个基础上根据分割后坐标轴刻度显示的易读程度作调整。
        // splitNumber: yAxisSplitNumber,
        interval: rightInterval,
        position: 'right',
        ...getDoubleYAxis(doubleYAxis)
      },
      {
        type: 'value',
        key: 'yAxisIndex1',
        min: leftMin ? leftMin : 0,
        max: leftMaxValue,
        interval: leftInterval,
        position: 'left',
        ...getDoubleYAxis(doubleYAxis)
      }
    ],
    ...seriesObj,
    ...gridOptions,
    ...legendOption
  }
  return option
}

function getDefaultValue (value, defaultValue) {
  if (typeof value === 'number' && !isNaN(value) && value !== Infinity && value !== -Infinity) return value
  return defaultValue
}

export function getAixsMetrics (type, axisMetrics, data, stack, labelOption, selectedItems, axisPosition?: {key: string, type: string}) {
  const seriesNames = []
  const seriesAxis = []
  axisMetrics.forEach((m) => {
    const decodedMetricName = decodeMetricName(m.name)
    const localeMetricName = `[${getAggregatorLocale(m.agg)}] ${decodedMetricName}`
    seriesNames.push(decodedMetricName)
    const stackOption = stack && axisPosition.type === 'bar' && axisMetrics.length > 1 ? { stack: axisPosition.key } : null
    const itemData = data.map((g, index) => {
      const itemStyle = selectedItems && selectedItems.length && selectedItems.some((item) => item === index) ? {itemStyle: {normal: {opacity: 1, borderWidth: 6}}} : null
      return {
        value: g[`${m.agg}(${decodedMetricName})`],
        ...itemStyle
      }
    })

    seriesAxis.push({
      name: decodedMetricName,
      type: axisPosition && axisPosition.type ? axisPosition.type : type === 'metrics' ? 'line' : 'bar',
      ...stackOption,
      yAxisIndex: type === 'metrics' ? 1 : 0,
      data: itemData,
      ...labelOption,
      itemStyle: {
        normal: {
          opacity: selectedItems && selectedItems.length > 0 ? 0.25 : 1
        }
      }
    })
  })
  return seriesAxis
}

export function getYaxisInterval (max, splitNumber) {
  const roughInterval = parseInt(`${max / splitNumber}`, 10)
  const divisor = Math.pow(10, (`${roughInterval}`.length - 1))
  return (parseInt(`${roughInterval / divisor}`, 10) + 1) * divisor
}

export function getDoubleYAxis (doubleYAxis) {
  const {
    inverse,
    showLine,
    lineStyle,
    lineSize,
    lineColor,
    showLabel,
    labelFontFamily,
    labelFontSize,
    labelColor
  } = doubleYAxis

  return {
    inverse,
    axisLine: {
      show: showLine,
      lineStyle: {
        color: lineColor,
        width: Number(lineSize),
        type: lineStyle
      }
    },
    axisLabel: {
      show: showLabel,
      color: labelColor,
      fontFamily: labelFontFamily,
      fontSize: Number(labelFontSize),
      formatter: metricAxisLabelFormatter
    }
  }
}
