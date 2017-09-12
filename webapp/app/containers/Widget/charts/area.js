/*-
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

/*
 * Area chart options generator
 */
export default function (dataSource, flatInfo, chartParams) {
  const hasGroups = flatInfo.groups

  const {
    xAxis,
    metrics,
    groups,
    xAxisInterval,
    xAxisRotate,
    dataZoomThreshold,
    smooth,
    step,
    stack,
    symbol,
    tooltip,
    legend,
    toolbox,
    top,
    bottom,
    left,
    right
  } = chartParams

  let grouped,
    metricOptions,
    xAxisOptions,
    smoothOption,
    stepOption,
    stackOption,
    symbolOption,
    tooltipOptions,
    legendOptions,
    toolboxOptions,
    gridOptions,
    dataZoomOptions

  // symbol
  symbolOption = symbol && symbol.length
    ? { symbol: 'emptyCircle' }
    : { symbol: 'none' }
  // smooth
  smoothOption = smooth && smooth.length ? { smooth: true } : null
  // step
  stepOption = step && step.length ? { step: true } : null
  // stack
  stackOption = stack && stack.length ? { stack: 'stack' } : null

  // 数据分组
  if (hasGroups && groups && groups.length) {
    grouped = makeGourped(dataSource, [].concat(groups).filter(i => !!i))
  }

  // series 数据项； series = metrics * groups
  let metricArr = []

  if (metrics) {
    metrics.forEach(m => {
      if (hasGroups && groups && groups.length) {
        Object
          .keys(grouped)
          .forEach(k => {
            let serieObj = Object.assign({},
              {
                name: `${k} ${m}`,
                type: 'line',
                areaStyle: {normal: {}},
                sampling: 'average',
                data: grouped[k].map(g => g[m])
              },
              symbolOption,
              smoothOption,
              stepOption,
              stackOption
            )
            metricArr.push(serieObj)
          })
      } else {
        let serieObj = Object.assign({},
          {
            name: m,
            type: 'line',
            areaStyle: {normal: {}},
            sampling: 'average',
            symbol: symbolOption,
            data: dataSource.map(d => d[m])
          },
          symbolOption,
          smoothOption,
          stepOption
        )
        metricArr.push(serieObj)
      }
    })
    metricOptions = {
      series: metricArr
    }
  }

  // x轴数据
  xAxisOptions = xAxis && {
    xAxis: {
      data: hasGroups && groups && groups.length
        ? Object.keys(grouped)
          .map(k => grouped[k])
          .reduce((longest, g) => longest.length > g.length ? longest : g, [])
          .map(item => item[xAxis])
        : dataSource.map(d => d[xAxis]),
      axisLabel: {
        interval: xAxisInterval,
        rotate: xAxisRotate
      }
    }
  }

  // tooltip
  tooltipOptions = tooltip && tooltip.length
    ? {
      tooltip: {
        trigger: 'axis'
      }
    } : null

  // legend
  legendOptions = legend && legend.length
    ? {
      legend: {
        data: metricArr.map(m => m.name),
        align: 'left',
        right: 200
      }
    } : null

  // toolbox
  toolboxOptions = toolbox && toolbox.length
    ? {
      toolbox: {
        feature: {
          dataZoom: {
            yAxisIndex: 'none'
          },
          restore: {},
          saveAsImage: {
            pixelRatio: 2
          }
        }
      }
    } : null

  // grid
  gridOptions = {
    grid: {
      top: top,
      left: left,
      right: right,
      bottom: bottom
    }
  }

  dataZoomOptions = dataZoomThreshold > 0 && dataZoomThreshold < dataSource.length && {
    dataZoom: [{
      type: 'inside',
      start: Math.round((1 - dataZoomThreshold / dataSource.length) * 100),
      end: 100
    }, {
      start: Math.round((1 - dataZoomThreshold / dataSource.length) * 100),
      end: 100,
      handleIcon: 'M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
      handleSize: '80%',
      handleStyle: {
        color: '#fff',
        shadowBlur: 3,
        shadowColor: 'rgba(0, 0, 0, 0.6)',
        shadowOffsetX: 2,
        shadowOffsetY: 2
      }
    }]
  }

  return Object.assign({},
    {
      yAxis: {
        type: 'value'
      }
    },
    metricOptions,
    xAxisOptions,
    tooltipOptions,
    legendOptions,
    toolboxOptions,
    gridOptions,
    dataZoomOptions
  )
}

export function makeGourped (dataSource, groupColumns) {
  return dataSource.reduce((acc, val) => {
    let accColumn = groupColumns
      .reduce((arr, col) => {
        arr.push(val[col])
        return arr
      }, [])
      .join(' ')
    if (!acc[accColumn]) {
      acc[accColumn] = []
    }
    acc[accColumn].push(val)
    return acc
  }, {})
}
