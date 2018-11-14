import * as React from 'react'
import { IWidgetProps } from '../Widget'
import Table from './Table'
import Scorecard from './Scorecard'
import Iframe from './Iframe'
import Chart from './Chart'
import ChartTypes from '../../config/chart/ChartTypes'

export interface IChartProps extends IWidgetProps {
  width: number
  height: number
}

export function CombinedChart (props: IChartProps) {
  const {
    width,
    height,
    data,
    selectedChart
  } = props

  switch (selectedChart) {
    case ChartTypes.Table:
      return (
        <Table
          data={data}
          width={width}
          height={height}
        />
      )
    case ChartTypes.Scorecard:
      return (
        <Scorecard {...props} />
      )
    case ChartTypes.Iframe:
        return (
          <Iframe {...props} />
        )
    default:
      return (
        <Chart {...props}/>
      )
  }
}

export default CombinedChart
