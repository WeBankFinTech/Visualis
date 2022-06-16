import * as React from 'react'
import { IChartProps } from './index'
import chartlibs from '../../config/chart'
import * as echarts from 'echarts/lib/echarts'
import { ECharts } from 'echarts'
import chartOptionGenerator from '../../render/chart'
import { getTriggeringRecord } from '../util'
const styles = require('./Chart.less')

export class Chart extends React.PureComponent<IChartProps> {
  private container: HTMLDivElement = null
  private instance: ECharts
  constructor (props) {
    super(props)
  }
  public componentDidMount () {
    this.renderChart(this.props)
  }

  public componentDidUpdate () {
    this.renderChart(this.props)
  }

  private renderChart = (props: IChartProps) => {
    const { selectedChart, renderType, getDataDrillDetail, isDrilling, onSelectChartsItems, onDoInteract, onCheckTableInteract } = props

    if (renderType === 'loading') return

    // excel类型，不需要加载echarts相关内容
    if (selectedChart === 19) return

    if (!this.instance) {
      this.instance = echarts.init(this.container, 'default')
    } else {
      if (renderType === 'rerender') {
        this.instance.dispose()
        this.instance = echarts.init(this.container, 'default')
      }
      if (renderType === 'clear') {
        this.instance.clear()
      }
    }

    this.instance.setOption(
      chartOptionGenerator(
        chartlibs.find((cl) => cl.id === selectedChart).name,
        props,
        {
          instance: this.instance,
          isDrilling,
          getDataDrillDetail,
          selectedItems: this.props.selectedItems
        }
      )
    )


    // if (onDoInteract) {
    //   this.instance.off('click')
    //   this.instance.on('click', (params) => {
    //     const isInteractiveChart = onCheckTableInteract()
    //     if (isInteractiveChart) {
    //       const triggerData = getTriggeringRecord(params, seriesData)
    //       onDoInteract(triggerData)
    //     }
    //   })
    // }

    this.instance.off('click')
    // echarts图的点击事件
    this.instance.on('click', (params) => {
      if (params.componentSubType === 'graph') {
        // 关系图，执行其他的逻辑
        this.handleGraphClick(params)
      } else {
        this.collectSelectedItems(params)
      }
    })
    this.instance.resize()
  }

  // 关系图的点击事件
  // 点击某个节点后，不请求数据（因为都是用全量数据来生成图），只切换顶层节点
  // 点击某个节点后，顶层节点只有一个，即点击的这个节点
  public handleGraphClick = (params) => {
    const { dataType, name } = params
    const { selectedChart, getDataDrillDetail, isDrilling, onSetWidgetProps } = this.props
    if (dataType === 'node') {
      // 说明点击的是关系图中的节点，要更新数据，rootNodeCount为1并且rootNodeName有值
      this.props.chartStyles.spec.rootNodeCount = 1
      this.props.chartStyles.spec.rootNodeName = name
      this.props.tempWidgetProps.chartStyles.spec.rootNodeCount = 1
      this.props.tempWidgetProps.chartStyles.spec.rootNodeName = name

      this.instance.setOption(
        chartOptionGenerator(
          chartlibs.find((cl) => cl.id === selectedChart).name,
          this.props,
          {
            instance: this.instance,
            isDrilling,
            getDataDrillDetail,
            selectedItems: this.props.selectedItems
          }
        )
      )

      // 保存某个节点的点击操作，再次打开页面时依然是操作后的结果
      onSetWidgetProps(this.props.tempWidgetProps)
    }
  }

  public collectSelectedItems = (params) => {
    const { data, onSelectChartsItems, selectedChart, onDoInteract, onCheckTableInteract } = this.props
    let selectedItems = []
    if (this.props.selectedItems && this.props.selectedItems.length) {
      selectedItems = [...this.props.selectedItems]
    }
    const { getDataDrillDetail } = this.props
    let dataIndex = params.dataIndex
    if (selectedChart === 4) {
      dataIndex = params.seriesIndex
    }
    if (selectedItems.length === 0) {
      selectedItems.push(dataIndex)
    } else {
      const isb = selectedItems.some((item) => item === dataIndex)
      if (isb) {
        for (let index = 0, l = selectedItems.length; index < l; index++) {
          if (selectedItems[index] === dataIndex) {
            selectedItems.splice(index, 1)
            break
          }
        }
      } else {
        selectedItems.push(dataIndex)
      }
    }

    const resultData = selectedItems.map((item) => {
      return data[item]
    })
    const brushed = [{0: Object.values(resultData)}]
    const sourceData = Object.values(resultData)
    const isInteractiveChart = onCheckTableInteract && onCheckTableInteract()
    if (isInteractiveChart && onDoInteract) {
      const triggerData = sourceData
      onDoInteract(triggerData)
    }
    setTimeout(() => {
      if (getDataDrillDetail) {
        getDataDrillDetail(JSON.stringify({range: null, brushed, sourceData}))
      }
    }, 500)
    if (onSelectChartsItems) {
      onSelectChartsItems(selectedItems)
    }
  }
  public render () {
    return (
      <div
        className={styles.chartContainer}
        ref={(f) => this.container = f}
      />
    )
  }
}

export default Chart
