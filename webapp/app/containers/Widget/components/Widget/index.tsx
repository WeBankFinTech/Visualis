import React, { createRef } from 'react'
import classnames from 'classnames'
import Pivot from '../Pivot'
import Chart from '../Chart'
import { Icon, Progress } from 'antd'
import widgetStyles from './style.less'
import {
  AggregatorType,
  DragType,
  IDataParamConfig
} from '../Workbench/Dropbox'
import { IDataParamProperty } from '../Workbench/OperatingPanel'
import { IFieldFormatConfig } from '../Config/Format'
import { IFieldConfig } from '../Config/Field'
import { IFieldSortConfig } from '../Config/Sort'
import { IAxisConfig } from '../Workbench/ConfigSections/AxisSection'
import { ISplitLineConfig } from '../Workbench/ConfigSections/SplitLineSection'
import { IPivotConfig } from '../Workbench/ConfigSections/PivotSection'
import { ILabelConfig } from '..//Workbench/ConfigSections/LabelSection'
import { ISpecConfig } from '../Workbench/ConfigSections/SpecSection'
import { ILegendConfig } from '../Workbench/ConfigSections/LegendSection'
import { IVisualMapConfig } from '../Workbench/ConfigSections/VisualMapSection'
import { IToolboxConfig } from '../Workbench/ConfigSections/ToolboxSection'
import { IAreaSelectConfig } from '../Workbench/ConfigSections/AreaSelectSection'
import { IScorecardConfig } from '../Workbench/ConfigSections/ScorecardSection'
import { IGaugeConfig } from '../Workbench/ConfigSections/GaugeSection'
import { IframeConfig } from '../Workbench/ConfigSections/IframeSection'
import { ITableConfig } from '../Config/Table'
import { IRichTextConfig, IBarConfig, IRadarConfig } from '../Workbench/ConfigSections'
import { IDoubleYAxisConfig } from '../Workbench/ConfigSections/DoubleYAxisSection'
import { IGapConfig } from '../Workbench/ConfigSections/GapSection'
import { IModel } from '../Workbench/index'
//import WaterMask from '../WaterMask/index'
import { IQueryVariableMap } from 'containers/Dashboard/Grid'
import { getStyleConfig } from '../util'
import ChartTypes from '../../config/chart/ChartTypes'
const styles = require('../Pivot/Pivot.less')

export type DimetionType = 'row' | 'col'
export type RenderType =
  | 'rerender'
  | 'clear'
  | 'refresh'
  | 'resize'
  | 'loading'
  | 'select'
  | 'flush'
export type WidgetMode = 'pivot' | 'chart'
export type Coordinate = 'cartesian' | 'polar' | 'other'

export interface IWidgetDimension {
  name: string
  field: IFieldConfig
  format: IFieldFormatConfig
  sort: IFieldSortConfig
  width?: number
  widthChanged?: boolean
  alreadySetWidth?: boolean
  oldColumnCounts?: number
}

export interface IWidgetMetric {
  name: string
  agg: AggregatorType
  chart: IChartInfo
  field: IFieldConfig
  format: IFieldFormatConfig
  width?: number
  widthChanged?: boolean
  alreadySetWidth?: boolean
  oldColumnCounts?: number
}

export interface IWidgetSecondaryMetric {
  name: string
  agg: AggregatorType
  field: IFieldConfig
  format: IFieldFormatConfig
  from?: string
  type?: any
  visualType?: any
}

export interface IWidgetFilter {
  name: string
  type: DragType
  config: IDataParamConfig
}

export interface IChartStyles {
  pivot?: IPivotConfig
  xAxis?: IAxisConfig
  yAxis?: IAxisConfig
  axis?: IAxisConfig
  splitLine?: ISplitLineConfig
  label?: ILabelConfig
  legend?: ILegendConfig
  toolbox?: IToolboxConfig
  areaSelect?: IAreaSelectConfig
  spec?: ISpecConfig
  visualMap?: IVisualMapConfig
  scorecard?: IScorecardConfig
  gauge?: IGaugeConfig
  iframe?: IframeConfig
  table?: ITableConfig
  richText?: IRichTextConfig
  bar?: IBarConfig
  radar?: IRadarConfig
  doubleYAxis?: IDoubleYAxisConfig
}

export interface IChartRule {
  dimension: number | [number, number]
  metric: number | [number, number]
}

export interface IChartInfo {
  id: number
  name: string
  title: string
  icon: string
  coordinate: Coordinate
  rules: IChartRule[]
  dimetionAxis?: DimetionType
  data: object
  style: object
}

export interface IPaginationParams {
  pageNo: number
  pageSize: number
  totalCount: number
  withPaging: boolean
}

export interface IWidgetProps {
  data: object[]
  cols: IWidgetDimension[]
  rows: IWidgetDimension[]
  metrics: IWidgetMetric[]
  secondaryMetrics?: IWidgetSecondaryMetric[]
  filters: IWidgetFilter[]
  chartStyles: IChartStyles
  selectedChart: number
  interacting?: boolean
  color?: IDataParamProperty
  label?: IDataParamProperty
  size?: IDataParamProperty
  xAxis?: IDataParamProperty
  tip?: IDataParamProperty
  yAxis?: IDataParamProperty
  dimetionAxis?: DimetionType
  renderType?: RenderType
  orders: Array<{ column: string, direction: string }>
  mode: WidgetMode
  model: IModel
  pagination?: IPaginationParams
  editing?: boolean
  queryVariables?: IQueryVariableMap
  onCheckTableInteract?: () => boolean
  onDoInteract?: (triggerData: object) => void
  getDataDrillDetail?: (position: string) => void
  onPaginationChange?: (pageNo: number, pageSize: number, order?: { column: string, direction: string }) => void
  onChartStylesChange?: (propPath: string[], value: string) => void
  isDrilling?: boolean
  whichDataDrillBrushed?: boolean | object[]
  computed?: any[]
  selectedItems?: number[]
  onSelectChartsItems?: (selectedItems: number[]) => void
  onSetWidgetProps: (widgetProps: IWidgetProps) => void
  // onHideDrillPanel?: (swtich: boolean) => void
  executeQueryFailed?: boolean
  visualisData: object
}

export interface IWidgetConfig extends IWidgetProps {
  controls: any[]
  cache: boolean
  expired: number
  autoLoadData: boolean
}

export interface IWidgetWrapperProps extends IWidgetProps {
  loading?: boolean | JSX.Element
  empty?: boolean | JSX.Element
}

export interface IWidgetWrapperStates {
  width: number
  height: number
  getProgressPercent: number
}

export class Widget extends React.Component<
  IWidgetWrapperProps,
  IWidgetWrapperStates
> {
  public static defaultProps = {
    editing: false
  }

  constructor (props) {
    super(props)
    this.state = {
      width: 0,
      height: 0,
      getProgressPercent: -1
    }
  }

  private changePercent = (percent) => {
    this.setState({getProgressPercent: percent})
  }

  private container = createRef<HTMLDivElement>()

  public componentDidMount () {
    if (typeof this.props.onRef === 'function') this.props.onRef(this)
    this.getContainerSize()
  }

  public componentWillReceiveProps (nextProps: IWidgetProps) {
    if (nextProps.renderType === 'resize') {
      this.getContainerSize()
    }
  }

  private getContainerSize = () => {
    const { offsetWidth, offsetHeight } = this.container
      .current as HTMLDivElement
    const { width, height } = this.state
    if ( offsetWidth && offsetHeight && (offsetWidth !== width || offsetHeight !== height) ) {
      this.setState({ width: offsetWidth, height: offsetHeight })
    }
  }

  public render () {
    const { loading, empty, mode, executeQueryFailed, selectedChart, visualisData } = this.props
    const { width, height, getProgressPercent } = this.state
    const username = localStorage.getItem('username');
    const widgetProps = { width, height, ...this.props }

    delete widgetProps.loading
    let widgetContent: JSX.Element
    if (width && height) {
      // FIXME
      widgetContent =
        widgetProps.mode === 'chart' ? (
          <Chart {...widgetProps} />
        ) : (
          <Pivot {...widgetProps} />
        )
    }

    let waterMaskWidth = 0
    if (this.container &&  this.container.current && this.container.current.childNodes[1] && this.container.current.childNodes[1].style.width) waterMaskWidth = parseInt(this.container.current.childNodes[1].style.width)
    const waterMaskProps = {
      text: `${username}`,
      waterMaskWidth
    }

    // visualis和datawrangler测试环境不一样，所以测试环境下要写死这个ip
    const dataWranglerUrl = `http://10.107.116.246:8315/#/sheet/add?simpleMode=true&showBottomBar=false&readOnly=true&showChangeModeButton=false&visualisData=${JSON.stringify(visualisData)}`
    return (
      <div className={styles.wrapper + ' widget-class'} ref={this.container} id="widget" style={{overflowX: 'auto', overflowY: 'hidden'}}>
        {/* <WaterMask {...waterMaskProps} /> */}
        { selectedChart === 19 && mode === 'chart' ? 
          <iframe src={dataWranglerUrl} width="100%" height="100%" frameBorder="0" id="dataWrangler"></iframe>
          :
          widgetContent
        }
        {loading}
        {/* 表格暂无数据时的提示，有了进度条就不需要了 */}
        {/* {empty} */}
        {
          getProgressPercent * 100 > -1 && getProgressPercent * 100 < 100 ? 
          <div className={widgetStyles.mask}>
            <Progress type='circle' percent={getProgressPercent * 100}></Progress>
          </div> : null
        }
        {
          // -2表示是查询失败后设置的getProgressPercent
          // executeQueryFailed是查询失败的另一个明显标时 dashboard和display的编辑、分享页里如果查询失败会传进来
          getProgressPercent === -2 || executeQueryFailed ? 
          <div className={widgetStyles.mask} style={{fontSize: '28px', fontWeight: 'bold'}}>
            查询失败
          </div> : null
        }
      </div>
    )
  }
}

export default Widget
