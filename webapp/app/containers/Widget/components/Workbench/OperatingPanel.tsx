import React from 'react'
import classnames from 'classnames'
import set from 'lodash/set'
import _ from 'lodash'

import widgetlibs from '../../config'
import { IDataRequestParams } from 'app/containers/Dashboard/Grid'
import { IView, IViewBase, IFormedView } from 'containers/View/types'
import { ViewModelVisualTypes } from 'containers/View/constants'
import Dropbox, { DropboxType, DropType, AggregatorType, IDataParamSource, IDataParamConfig, DragType, IDragItem} from './Dropbox'
import DropArea from './DropArea'
import { IWidgetProps, IChartStyles, IChartInfo, IPaginationParams, WidgetMode, RenderType, DimetionType } from '../Widget'
import { IFieldConfig, getDefaultFieldConfig, FieldConfigModal } from '../Config/Field'
import { IFieldFormatConfig, getDefaultFieldFormatConfig, FormatConfigModal } from '../Config/Format'
import { IFieldSortConfig, FieldSortTypes, SortConfigModal } from '../Config/Sort'
import ColorSettingForm from './ColorSettingForm'
import ActOnSettingForm from './ActOnSettingForm'
import FilterSettingForm from './FilterSettingForm'
import VariableConfigForm from '../VariableConfigForm'
import ControlConfig from './ControlConfig'
import ComputedConfigForm from '../ComputedConfigForm'
import ChartIndicator from './ChartIndicator'
import AxisSection, { IAxisConfig } from './ConfigSections/AxisSection'
import SplitLineSection, { ISplitLineConfig } from './ConfigSections/SplitLineSection'
import PivotSection, { IPivotConfig } from './ConfigSections/PivotSection'
import SpecSection, { ISpecConfig } from './ConfigSections/SpecSection'
import LabelSection, { ILabelConfig } from './ConfigSections/LabelSection'
import LegendSection, { ILegendConfig } from './ConfigSections/LegendSection'
import VisualMapSection, { IVisualMapConfig } from './ConfigSections/VisualMapSection'
import ToolboxSection, { IToolboxConfig } from './ConfigSections/ToolboxSection'
import DoubleYAxisSection, { IDoubleYAxisConfig } from './ConfigSections/DoubleYAxisSection'
import GapSection, { IGapConfig } from './ConfigSections/GapSection'
import AreaSelectSection, { IAreaSelectConfig } from './ConfigSections/AreaSelectSection'
import ScorecardSection, { IScorecardConfig } from './ConfigSections/ScorecardSection'
import IframeSection, { IframeConfig } from './ConfigSections/IframeSection'
import TableSection from './ConfigSections/TableSection'
import GaugeSection from './ConfigSections/GaugeSection'
import { ITableConfig } from '../Config/Table'
import BarSection from './ConfigSections/BarSection'
import RadarSection from './ConfigSections/RadarSection'
import { encodeMetricName, decodeMetricName, getPivot, getTable, getPivotModeSelectedCharts, checkChartEnable } from '../util'
import { PIVOT_DEFAULT_SCATTER_SIZE_TIMES } from 'app/globalConstants'
import PivotTypes from '../../config/pivot/PivotTypes'
import { uuid } from 'utils/util'

import { RadioChangeEvent } from 'antd/lib/radio'
import { Row, Col, Icon, Menu, Radio, InputNumber, Dropdown, Modal, Popconfirm, Checkbox, notification, Tooltip, Select, message, Button } from 'antd'
import { IDistinctValueReqeustParams } from 'app/components/Filters/types'
import { WorkbenchQueryMode } from './types'
import { CheckboxChangeEvent } from 'antd/lib/checkbox'
import { SelectProps } from 'antd/lib/select'
const MenuItem = Menu.Item
const RadioButton = Radio.Button
const RadioGroup = Radio.Group
const confirm = Modal.confirm
const Option = Select.Option
const styles = require('./Workbench.less')
const defaultTheme = require('assets/json/echartsThemes/default.project.json')
const defaultThemeColors = defaultTheme.theme.color
const utilStyles = require('assets/less/util.less')

export interface IDataParamProperty {
  title: string
  type: DropboxType
  value?: {all?: any}
  items: IDataParamSource[]
}

export interface IDataParams {
  [key: string]: IDataParamProperty
}

interface IOperatingPanelProps {
  id: number
  views: IViewBase[]
  widgetProps: IWidgetProps
  originalWidgetProps: IWidgetProps
  selectedView: IFormedView
  distinctColumnValues: any[]
  columnValueLoading: boolean
  controls: any[]
  cache: boolean
  autoLoadData: boolean
  expired: number
  queryMode: WorkbenchQueryMode
  multiDrag: boolean
  computed: any[]
  originalComputed: any[]
  view: object
  isFold: boolean
  collapsed: boolean
  onChangeIsFold: () => void
  onSetView: (view: object) => void
  onViewSelect: (view: object) => void
  onSetControls: (controls: any[]) => void
  onCacheChange: (e: RadioChangeEvent) => void
  onChangeAutoLoadData: (e: RadioChangeEvent) => void
  onExpiredChange: (expired: number) => void
  onSetComputed: (computesField: any[]) => void
  onDeleteComputed: (computesField: any[]) => void
  onSetWidgetProps: (widgetProps: IWidgetProps) => void
  onLoadData: (
    viewId: number,
    requestParams: IDataRequestParams,
    resolve: (data) => void,
    reject: (error) => void
  ) => void
  onLoadEngines: (
    viewId: number,
    resolve: (data) => void,
  ) => void
  // widget页面 提交查询数据接口
  onExecuteQuery: (
    viewId: number,
    requestParams: IDataRequestParams,
    resolve: (data) => void,
    reject: (error) => void
  ) => void
  // widget页面 进度查询接口
  onGetProgress: (execId: string, resolve: (data) => void, reject: (error) => void) => void
  // widget页面 获取结果集接口
  onGetResult: (execId: string, pageNo: number, pageSize: number, resolve: (data) => void, reject: (error) => void) => void
  // widget页面 进度查询接口
  onKillExecute: (execId: string, resolve: (data) => void, reject: (error) => void) => void
  onSetQueryData: (data: object) => void
  onLoadDistinctValue: (viewId: number, params: Partial<IDistinctValueReqeustParams>) => void,
  onBeofreDropColunm: (view: IView, resolve: () => void) => void
  changeGetProgressPercent: (percent: number) => void
  setEngine: (val: string) => void
  engine: string
}

interface IOperatingPanelStates {
  dragged: IDataParamSource
  showColsAndRows: boolean
  mode: WidgetMode
  currentWidgetlibs: IChartInfo[]
  chartModeSelectedChart: IChartInfo
  // selectedTab: 'data' | 'style' | 'variable' | 'cache'
  selectedTab: 'data' | 'style' | 'setting'
  dataParams: IDataParams
  styleParams: IChartStyles
  pagination: IPaginationParams
  modalCachedData: IDataParamSource
  modalCallback: (data: boolean | IDataParamConfig) => void
  modalDataFrom: string

  currentEditingCommonParamKey: string
  currentEditingItem: IDataParamSource
  fieldModalVisible: boolean

  formatModalVisible: boolean
  sortModalVisible: boolean

  colorModalVisible: boolean
  actOnModalVisible: boolean
  actOnModalList: IDataParamSource[]
  filterModalVisible: boolean
  controlConfigVisible: boolean

  categoryDragItems: IDragItem[],
  valueDragItems: IDragItem[],

  computedConfigModalVisible: boolean
  selectedComputed: object
  engines: []
}

export class OperatingPanel extends React.Component<IOperatingPanelProps, IOperatingPanelStates> {
  constructor (props) {
    super(props)
    this.state = {
      dragged: null,
      showColsAndRows: false,
      mode: 'chart',
      currentWidgetlibs: widgetlibs['chart'],
      chartModeSelectedChart: getTable(),
      selectedTab: 'data',
      dataParams: Object.entries(getTable().data)
        .reduce((params: IDataParams, [key, value]) => {
          params[key] = { ...value, items: []}
          return params
        }, {}),
      styleParams: {},
      pagination: { pageNo: 0, pageSize: 0, withPaging: false, totalCount: 0 },
      modalCachedData: null,
      modalCallback: null,
      modalDataFrom: '',
      currentEditingCommonParamKey: '',
      currentEditingItem: null,
      fieldModalVisible: false,
      formatModalVisible: false,
      sortModalVisible: false,
      colorModalVisible: false,
      actOnModalVisible: false,
      actOnModalList: null,
      filterModalVisible: false,
      controlConfigVisible: false,
      categoryDragItems: [],
      valueDragItems: [],
      computedConfigModalVisible: false,
      selectedComputed: null,
      engines: []
    }
  }

  private lastRequestParamString: string = ''

  private tabKeys = [
    { key: 'data', title: '数据' },
    { key: 'style', title: '样式' },
    { key: 'setting', title: '配置' }
  ]

  private colorSettingForm = null
  private actOnSettingForm = null
  private filterSettingForm = null

  private variableConfigForm = null
  private computedConfigForm = null
  private refHandlers = {
    variableConfigForm: (ref) => this.variableConfigForm = ref,
    computedConfigForm: (ref) => this.computedConfigForm = ref
  }

  public componentWillMount () {
    this.setState({
      ...this.getChartDataConfig([getTable()])
    })
  }

  public componentDidMount () {
    const { collapsed, onChangeIsFold } = this.props
    if (collapsed) {
      onChangeIsFold()
    }
  }

  public componentWillReceiveProps (nextProps: IOperatingPanelProps) {
    const { selectedView, originalWidgetProps, widgetProps } = nextProps
    const { dataParams } = this.state
    if (selectedView && selectedView !== this.props.selectedView) {
      const model = selectedView.model
      const categoryDragItems = []
      const valueDragItems = []

      Object.entries(model).forEach(([key, m]) => {
        if (m.modelType === 'category') {
          categoryDragItems.push({
            name: key,
            type: 'category',
            visualType: m.visualType,
            checked: false
          })
        } else {
          valueDragItems.push({
            name: key,
            type: 'value',
            visualType: m.visualType,
            checked: false
          })
        }
      })

      this.setState({
        categoryDragItems,
        valueDragItems
      })
    }

    // 只要widgetProps.cols和widgetProps.metrics里有任何一列的width,widthChanged,alreadySetWidth,oldColumnCounts这四个属性中的任意一个变化了之后，就要更新dataParams
    // needUpdate为true的情况是指，在 图表驱动表格 下，有任何一个维度列或者指标列的width,widthChanged,alreadySetWidth,oldColumnCounts四个值中的至少一个改变了
    let needUpdate = false
    // 判断当前是在 图表驱动-表格 下 this.state.styleParams && this.state.styleParams.table
    if (this.state.styleParams && this.state.styleParams.table) {
      // 如果widgetProps（下一个状态）和dataParams（当前状态）中的cols和metrics在列宽相关的属性上有区别，在下面就要更新（通过将needUpdate设为true）
      if (widgetProps.cols && dataParams && dataParams.cols && dataParams.cols.items && typeof dataParams.cols.items.length === 'number') {
        widgetProps.cols.forEach((col) => {
          for (let i = 0; i < dataParams.cols.items.length; i++) {
            const tempCol = dataParams.cols.items[i]
            if (col.name === tempCol.name) {
              if (col.width !== tempCol.width || col.widthChanged !== tempCol.widthChanged || col.alreadySetWidth !== tempCol.alreadySetWidth || col.oldColumnCounts !== tempCol.oldColumnCounts) {
                needUpdate = true
                break
              }
            }
          }
        })
      }
      if (widgetProps.metrics && dataParams && dataParams.metrics && dataParams.metrics.items && typeof dataParams.metrics.items.length === 'number') {
        widgetProps.metrics.forEach((col) => {
          for (let i = 0; i < dataParams.metrics.items.length; i++) {
            const tempMetric = dataParams.metrics.items[i]
            if (col.name === tempMetric.name) {
              if (col.width !== tempMetric.width || col.widthChanged !== tempMetric.widthChanged || col.alreadySetWidth !== tempMetric.alreadySetWidth || col.oldColumnCounts !== tempMetric.oldColumnCounts) {
                needUpdate = true
                break
              }
            }
          }
        })
      }
    }

      // 此时可能是新建widget页面，originalWidgetProps为null；也可能是从其他图表切换到图表驱动表格时，Object.keys(this.state.styleParams)[0] !== Object.keys(originalWidgetProps.chartStyles)[0]。需要改动dataParams里的cols和metrics属性，不然在新建widget页面中，表格数据设置弹框中的width这些为undefined，因为dataParams里不设置的话，dataParams里就没有cols和metrics的width值，传到ColumnConfigModal.tsx中的localConfig里也没有width值
    if ((!originalWidgetProps || originalWidgetProps && this.state.styleParams && Object.keys(this.state.styleParams)[0] !== Object.keys(originalWidgetProps.chartStyles)[0]) && selectedView && needUpdate) {
      // needUpdate如果为true是说在图表驱动的表格下，有任何一个维度列或者指标列的width,widthChanged,alreadySetWidth,oldColumnCounts四个值中的至少一个改变了
      const { dataParams } = this.state
      const { cols, metrics } = widgetProps

      dataParams.cols.items = cols
      dataParams.metrics.items = metrics
      this.setState({dataParams})
    }

    if ((originalWidgetProps && selectedView && this.state.styleParams) && (originalWidgetProps !== this.props.originalWidgetProps || selectedView !== this.props.selectedView || needUpdate)) {
      // 初始的时候有一次默认值的设置，那时候originalWidgetProps不为空但this.props.originalWidgetProps为null，所以在第一次this.props.originalWidgetProps为null时不进行Object.keys(this.state.styleParams)[0] !== Object.keys(originalWidgetProps.chartStyles)[0]的判断
      if (this.props.originalWidgetProps && Object.keys(this.state.styleParams)[0] !== Object.keys(originalWidgetProps.chartStyles)[0]) return
      // Object.keys(this.state.styleParams)[0] === Object.keys(originalWidgetProps.chartStyles)[0] 是保证当前操作的图表的类型和originalWidgetProps里对应的图表类型是一致的，比如从透视驱动表格切到图表驱动表格后，originalWidgetProps.chartStyles里是pivot，但是this.state.styleParams里是table，这个时候就不需要执行这个if里的逻辑了，因为下面是用originalWidgetProps来更新的，执行的话就会出现透视驱动表格切换到图表驱动表格然后拖拽字段后自动切回透视驱动的bug
      const { rows, secondaryMetrics, filters, color, label, size, xAxis, tip, chartStyles, mode, selectedChart } = originalWidgetProps
      const { cols, metrics, engine } = widgetProps

      const { dataParams } = this.state
      const model = selectedView.model
      const currentWidgetlibs = widgetlibs[mode || 'chart'] // FIXME 兼容 0.3.0-beta.1 之前版本
      dataParams.cols.items = []
      cols.forEach((c) => {
        const modelColumn = model[c.name]
        if (modelColumn) {
          dataParams.cols.items = dataParams.cols.items.concat({
            ...c,
            from: 'cols',
            type: 'category' as DragType,
            visualType: c.name === '指标名称' ? ViewModelVisualTypes.String : modelColumn.visualType
          })
        }
      })

      dataParams.rows.items = []
      rows.forEach((r) => {
        const modelColumn = model[r.name]
        if (modelColumn) {
          dataParams.rows.items = dataParams.rows.items.concat({
            ...r,
            from: 'rows',
            type: 'category' as DragType,
            visualType: r.name === '指标名称' ? ViewModelVisualTypes.String :  modelColumn.visualType
          })
        }
      })

      if (secondaryMetrics) {
        dataParams.metrics = {
          title: '左轴指标',
          type: 'value',
          items: []
        }
      }

      dataParams.metrics.items = []
      metrics.forEach((m) => {
        const modelColumn = model[decodeMetricName(m.name)]
        if (modelColumn) {
          dataParams.metrics.items = dataParams.metrics.items.concat({
            ...m,
            from: 'metrics',
            type: 'value' as DragType,
            visualType: modelColumn.visualType,
            chart: currentWidgetlibs.find((wl) => wl.id === m.chart.id) // FIXME 兼容 0.3.0-beta.1 之前版本，widgetlib requireDimetions requireMetrics 有发生变更
          })
        }
      })

      if (secondaryMetrics) {
        dataParams.secondaryMetrics = {
          title: '右轴指标',
          type: 'value',
          items: []
        }
        dataParams.secondaryMetrics.items = []
        secondaryMetrics.forEach((m) => {
          const modelColumn = model[decodeMetricName(m.name)]
          if (modelColumn) {
            dataParams.secondaryMetrics.items = dataParams.secondaryMetrics.items.concat({
              ...m,
              from: 'secondaryMetrics',
              type: 'value' as DragType,
              visualType: modelColumn.visualType
            })
          }
        })
      }

      dataParams.filters.items = []
      filters.forEach((f) => {
        const modelColumn = model[f.name] ? model[f.name] : model[f.name.split('@')[0]]
        if (modelColumn) {
          dataParams.filters.items = dataParams.filters.items.concat({
            ...f,
            visualType: modelColumn.visualType
          })
        }
      })

      const mergedDataParams = {
        ...dataParams,
        ...color && {color},
        ...label && {label},
        ...size && {size},
        ...xAxis && {xAxis},
        ...tip && {tip}
      }
      this.setState({
        // 要用widgetProps而不是originalProps里的数据，不然在还未查询出数据时就切换图表驱动和透视驱动就会报错
        mode: widgetProps.mode || 'chart', // FIXME 兼容 0.3.0-beta.1 之前版本
        currentWidgetlibs,
        ...selectedChart && {chartModeSelectedChart: widgetlibs['chart'].find((wl) => wl.id === selectedChart)},
        dataParams: mergedDataParams,
        // 要用widgetProps而不是originalProps里的数据，不然在还未查询出数据时就切换图表驱动和透视驱动就会报错
        styleParams: widgetProps.chartStyles,
        showColsAndRows: !!rows.length
      }, () => {
        // 这里需要widgetProps.chartStyles.table.headerConfig而不是originalWidgetProps.chartStyles.table.headerCon而不是
        if (chartStyles.table && widgetProps && widgetProps.chartStyles.table) chartStyles.table.headerConfig = widgetProps.chartStyles.table.headerConfig
        // 要用widgetProps而不是originalProps里的数据，不然在还未查询出数据时就切换图表驱动和透视驱动就会报错
        this.setWidgetProps(mergedDataParams, widgetProps.chartStyles, {engine})
      })
    }

    if (selectedView) {
      const tempId = selectedView.id ? selectedView.id : 0
      this.props.onLoadEngines(tempId, (data) => {
        // 切换view时不用清空this.props.engine
        this.setState({
          engines: data.engineTypes ? data.engineTypes : []
        })
      })
    }
  }

  private execIds = []

  private deleteExecId = (execId) => {
    const index = this.execIds.indexOf(execId);
    if (index > -1) this.execIds.splice(index, 1)
  }

  public componentWillUnmount () {
    this.timeout.forEach(item => clearTimeout(item))
    this.execIds.forEach((execId) => {
      this.props.onKillExecute(execId, () => {}, () => {})
    })
    notification.destroy()
    window.removeEventListener('message', this.listenerHanlder)
  }

  // 获取各种图表类型的数据 dataParams 和 styleParams
  private getChartDataConfig = (selectedCharts: IChartInfo[]) => {
    const { mode } = this.state
    const { dataParams, styleParams } = this.state
    const { metrics, color, size } = dataParams
    const dataConfig = {}
    const styleConfig = {}
    let specSign = false
    selectedCharts.forEach((chartInfo) => {
      Object.entries(chartInfo.data).forEach(([key, prop]: [string, IDataParamProperty]) => {
        if (!dataConfig[key]) {
          let value = null
          switch (key) {
            case 'color':
              value = color && color.value
                ? {
                  all: color.value.all,
                  ...metrics.items.reduce((props, item, i) => {
                    props[item.name] = mode === 'pivot'
                      ? color.value[item.name] || color.value['all']
                      : color.value[item.name] || defaultThemeColors[i]
                    return props
                  }, {})
                }
                : { all: defaultThemeColors[0] }
              break
            case 'size':
              value = size && size.value ? size.value : { all: PIVOT_DEFAULT_SCATTER_SIZE_TIMES }
              break
          }
          dataConfig[key] = {
            ...prop,
            ...value && {value},
            items: dataParams[key] ? dataParams[key].items : []
          }
        }
      })
      Object.entries(chartInfo.style).forEach(([key, prop]: [string, object]) => {
        if (key !== 'spec') {
          styleConfig[key] = {
            ...prop,
            ...styleParams[key]
          }
        } else {
          specSign = true
        }
      })
    })
    if (specSign) {
      styleConfig['spec'] = selectedCharts.reduce((spec, chartInfo) => {
        const specConfig = chartInfo.style['spec'] || {}
        return {
          ...spec,
          ...Object.entries(specConfig).reduce((obj, [key, value]) => {
            const settledValue = styleParams.spec && styleParams.spec[key]
            obj[key] = settledValue !== void 0 ? settledValue : value
            return obj
          }, {})
        }
      }, {})
    }
    return {
      dataParams: dataConfig,
      styleParams: styleConfig
    }
  }

  private getDragItemIconClass = (type: ViewModelVisualTypes) => {
    switch (type) {
      case ViewModelVisualTypes.Number: return 'icon-values'
      case ViewModelVisualTypes.Date: return `icon-calendar ${styles.iconDate}`
      case ViewModelVisualTypes.GeoCountry:
      case ViewModelVisualTypes.GeoProvince:
      case ViewModelVisualTypes.GeoCity: return 'icon-map'
      default: return 'icon-categories'
    }
  }

  private dragStart = (item) =>
    (e: React.DragEvent<HTMLLIElement | HTMLParagraphElement>) => {
      // hack firefox trigger dragEnd
      e.dataTransfer.setData('text/plain', '')
      e.dataTransfer.effectAllowed = "move";
      this.setState({
        dragged: {...item}
      })
    }

  private dragEnd = () => {
    if (this.state.dragged) {
      this.setState({
        dragged: null
      })
    }
  }

  private insideDragStart = (from: string) =>
    (item: IDataParamSource, e: React.DragEvent<HTMLLIElement | HTMLParagraphElement>) => {
      this.dragStart({ ...item, from })(e)
    }

  // 已经在维度、指标、筛选里的项进行拖拽放下时就会触发，但是仅是维度或指标里的拖拽排序时，dropType为'inside'（也可能是unmoved），如果是从指标或维度里拖拽某一项放到筛选中时，dropType才为undefined，才可能会执行下面getVisualData的逻辑
  private insideDragEnd = (dropType: DropType) => {
    if (!dropType) {
      const { dragged: { name, from }, dataParams, styleParams } = this.state
      const prop = dataParams[from]
      prop.items = prop.items.filter((i) => i.name !== name)
      this.setWidgetProps(dataParams, styleParams)
    }

    this.setState({
      dragged: null
    })
  }

  private beforeDrop = (name, cachedItem, resolve) => {
    const { selectedView, onLoadDistinctValue } = this.props
    const { mode, dataParams } = this.state
    const { metrics } = dataParams

    if (mode === 'pivot'
        && cachedItem.name === '指标名称'
        && !['cols', 'rows'].includes(name)) {
      resolve(false)
      this.setState({ dragged: null })
      return
    }

    switch (name) {
      case 'filters':
        if (cachedItem.visualType !== 'number' && cachedItem.visualType !== 'date') {
          const tempParams = {
            columns: cachedItem.name.split('@').length > 0 ? [cachedItem.name.split('@')[0]] : [cachedItem.name]
          }
          if (typeof this.props.view === 'object' && Object.keys(this.props.view).length > 0) tempParams.view = this.props.view
          onLoadDistinctValue(selectedView.id, tempParams)
        }
        this.setState({
          modalCachedData: cachedItem,
          modalCallback: resolve,
          modalDataFrom: 'filters',
          filterModalVisible: true
        })
        break
      case 'color':
        const tempParams = {
          columns: [cachedItem.name]
        }
        if (typeof this.props.view === 'object' && Object.keys(this.props.view).length > 0) tempParams.view = this.props.view
        onLoadDistinctValue(selectedView.id, tempParams)
        this.setState({
          modalCachedData: cachedItem,
          modalCallback: resolve,
          modalDataFrom: 'color',
          colorModalVisible: true
        })
        break
      case 'label':
        this.setState({
          modalCachedData: cachedItem,
          modalCallback: resolve,
          modalDataFrom: 'label',
          actOnModalVisible: true,
          actOnModalList: metrics.items.slice()
        })
        break
      case 'size':
        if (mode === 'pivot') {
          this.setState({
            modalCachedData: cachedItem,
            modalCallback: resolve,
            modalDataFrom: 'size',
            actOnModalVisible: true,
            actOnModalList: metrics.items.filter((m) => m.chart.id === PivotTypes.Scatter)
          })
        } else {
          resolve(true)
        }
        break
      default:
        resolve(true)
        break
    }
  }

  // 拖拽一个或多个维度或指标或筛选之后在盒子里放下时
  private drop = (name: string, dropIndex: number, dropType: DropType, changedItems: IDataParamSource[], config?: IDataParamConfig) => {
    const { multiDrag } = this.props
    const {
      dragged: stateDragged,
      dataParams,
      styleParams,
      modalCachedData,
      categoryDragItems,
      valueDragItems
    } = this.state

    const dragged = stateDragged || modalCachedData
    const from = dragged.from && dragged.from !== name && dataParams[dragged.from]
    const destination = dataParams[name]
    const { items } = destination

    const multiDragCategoryDropboxNames = ['cols', 'rows']
    const multiDragValueDropboxNames = ['metrics', 'secondaryMetrics']
    if (multiDrag
        && dropType === 'outside'
        && multiDragCategoryDropboxNames.concat(multiDragValueDropboxNames).includes(name)) {
      let selectedItems = []
      if (multiDragCategoryDropboxNames.includes(name)) {
        selectedItems = selectedItems.concat(
          categoryDragItems
            .filter((item) => item.checked && !items.find((i) => i.name === item.name))
            .map(({ checked, ...rest }) => ({...rest}))
        )
        const tempNames = []
        selectedItems.forEach((obj) => tempNames.push(obj.name))
        // 多选时，dragged会重复一次，所以要先判断
        if (!tempNames.includes(dragged.name)) selectedItems = selectedItems.concat(dragged)
        this.setState({
          categoryDragItems: categoryDragItems.map((item) => ({ ...item, checked: false }))
        })
      } else if (multiDragValueDropboxNames.includes(name)) {
        selectedItems = selectedItems.concat(
          valueDragItems
            .filter((item) => item.checked)
            .map(({ checked, ...rest }): IDataParamSource => ({...rest, name: encodeMetricName(rest.name), agg: 'sum', chart: getPivot()}))
          )
        const tempNames = []
        selectedItems.forEach((obj) => tempNames.push(decodeMetricName(obj.name)))
        // 多选时，dragged会重复一次，所以要先判断
        if (!tempNames.includes(decodeMetricName(dragged.name))) selectedItems = selectedItems.concat({...dragged, chart: getPivot()})
        this.setState({
          valueDragItems: valueDragItems.map((item) => ({ ...item, checked: false }))
        })
      }
      destination.items = [...items.slice(0, dropIndex), ...selectedItems, ...items.slice(dropIndex)]
    } else {
      if (config) {
        dragged.config = config
        if (['color', 'label', 'size'].includes(name)) {
          const actingOnItemIndex = items.findIndex((i) => i.config.actOn === config.actOn)
          if (actingOnItemIndex >= 0) {
            items.splice(actingOnItemIndex, 1)
            dropIndex = dropIndex <= actingOnItemIndex ? dropIndex : dropIndex - 1
          }
        }
        if (name === 'xAxis') {
          items.splice(0, 1)
          dropIndex = 0
        }
      }

      if (dropType === 'outside') {
        let combinedItem = dragged
        if (name === 'metrics') {
          combinedItem = {...dragged, chart: dataParams.metrics.items.length ? dataParams.metrics.items[0].chart : getPivot()}
        }
        if (name === 'secondaryMetrics') {
          combinedItem = {...dragged, chart: dataParams.secondaryMetrics.items.length ? dataParams.secondaryMetrics.items[0].chart : getPivot()}
        }
        destination.items = [...items.slice(0, dropIndex), combinedItem, ...items.slice(dropIndex)]
      } else {
        destination.items = [...changedItems]
      }
    }

    if (from) {
      from.items = from.items.filter((i) => i.name !== dragged.name)
    }

    this.setState({
      dragged: null,
      modalCachedData: null
    })
    this.setWidgetProps(dataParams, styleParams)
  }

  // 透视驱动中 使用维度和使用行列的切换
  private toggleRowsAndCols = () => {
    const { dataParams, styleParams } = this.state
    const { cols, rows } = dataParams

    if (this.state.showColsAndRows && rows.items.length) {
      cols.items = cols.items.concat(rows.items)
      rows.items = []
      this.setWidgetProps(dataParams, styleParams)
    }

    this.setState({
      showColsAndRows: !this.state.showColsAndRows
    })
  }

  // 透视驱动中 行列切换
  private switchRowsAndCols = () => {
    const { dataParams, styleParams } = this.state
    const { cols, rows } = dataParams

    let temp = cols.items.slice()
    cols.items = rows.items.slice()
    rows.items = temp
    temp = null

    this.setWidgetProps(dataParams, styleParams)
  }

  // 清除某一个指标或维度或筛选
  private removeDropboxItem = (from: string) => (name: string) => () => {
    const { dataParams, styleParams, chartModeSelectedChart } = this.state
    if (chartModeSelectedChart && chartModeSelectedChart.name === 'relationGraph') {
      // 关系图下，删除掉第一个维度的筛选条件时，顶层节点数变回为五个
      const firstCol = dataParams.cols.items[0].name
      if (firstCol === name) {
        styleParams.spec.rootNodeCount = 5
        styleParams.spec.rootNodeName = ''
      }
    }
    const prop = dataParams[from]
    prop.items = prop.items.filter((i) => i.name !== name)
    this.setWidgetProps(dataParams, styleParams)
  }

  // 某一个配置项点开下拉菜单，进行升序、降序、默认顺序的配置
  private getDropboxItemSortDirection = (from: string) => (item: IDataParamSource, sortType: FieldSortTypes) => {
    const { dataParams, styleParams } = this.state
    const prop = dataParams[from]
    if (sortType !== FieldSortTypes.Custom) {
      item.sort = { sortType }
      prop.items = [...prop.items]
      this.setWidgetProps(dataParams, styleParams)
    } else {
      const { selectedView, onLoadDistinctValue } = this.props
      const tempParams = {
        columns: [item.name]
      }
      if (typeof this.props.view === 'object' && Object.keys(this.props.view).length > 0) tempParams.view = this.props.view
      onLoadDistinctValue(selectedView.id, tempParams)
      this.setState({
        currentEditingCommonParamKey: from,
        currentEditingItem: item,
        sortModalVisible: true
      })
    }
  }

  // 指标中的某一项点开下拉菜单，选择 总计、平均数等值
  private getDropboxItemAggregator = (from: string) => (item: IDataParamSource, agg: AggregatorType) => {
    const { dataParams, styleParams } = this.state
    const prop = dataParams[from]
    item.agg = agg
    prop.items = [...prop.items]
    this.setWidgetProps(dataParams, styleParams)
  }

  // 筛选中的某一项点开下拉菜单，选择配置筛选
  private dropboxItemChangeFieldConfig = (from: string) => (item: IDataParamSource) => {
    this.setState({
      currentEditingCommonParamKey: from,
      currentEditingItem: item,
      fieldModalVisible: true
    })
  }

  private saveFieldConfig = (fieldConfig: IFieldConfig) => {
    const {
      currentEditingCommonParamKey,
      currentEditingItem,
      dataParams,
      styleParams
    } = this.state
    const item = dataParams[currentEditingCommonParamKey].items.find((i) => i.name === currentEditingItem.name)
    item.field = fieldConfig
    this.setWidgetProps(dataParams, styleParams)
    this.setState({
      fieldModalVisible: false
    })
  }

  private cancelFieldConfig = () => {
    this.setState({
      fieldModalVisible: false
    })
  }

  private dropboxItemChangeFormatConfig = (from: string) => (item: IDataParamSource) => {
    this.setState({
      currentEditingCommonParamKey: from,
      currentEditingItem: item,
      formatModalVisible: true
    })
  }

  private saveFormatConfig = (formatConfig: IFieldFormatConfig) => {
    const {
      currentEditingCommonParamKey,
      currentEditingItem,
      dataParams,
      styleParams
    } = this.state
    const item = dataParams[currentEditingCommonParamKey].items.find((i) => i.name === currentEditingItem.name)
    item.format = formatConfig
    this.setWidgetProps(dataParams, styleParams)
    this.setState({
      formatModalVisible: false
    })
  }

  private cancelFormatConfig = () => {
    this.setState({
      formatModalVisible: false
    })
  }

  private saveSortConfig = (sortConfig: IFieldSortConfig) => {
    const {
      currentEditingCommonParamKey,
      currentEditingItem,
      dataParams,
      styleParams
    } = this.state
    const item = dataParams[currentEditingCommonParamKey].items.find((i) => i.name === currentEditingItem.name)
    item.sort = sortConfig
    this.setWidgetProps(dataParams, styleParams)
    this.setState({
      sortModalVisible: false
    })
  }

  private cancelSortConfig = () => {
    this.setState({ sortModalVisible: false })
  }

  private dropboxItemChangeColorConfig = (item: IDataParamSource) => {
    const { selectedView, onLoadDistinctValue } = this.props
    const { dataParams, styleParams } = this.state
    const tempParams = {
      columns: [item.name]
    }
    if (typeof this.props.view === 'object' && Object.keys(this.props.view).length > 0) tempParams.view = this.props.view

    onLoadDistinctValue(selectedView.id, tempParams)
    this.setState({
      modalCachedData: item,
      modalDataFrom: 'color',
      modalCallback: (config) => {
        if (config) {
          const colorItems = dataParams.color.items
          const actingOnItemIndex = colorItems.findIndex((i) => i.config.actOn === config['actOn'] && i.name !== item.name)
          if (actingOnItemIndex >= 0) {
            dataParams.color.items = [
              ...colorItems.slice(0, actingOnItemIndex),
              ...colorItems.slice(actingOnItemIndex + 1)
            ]
          }
          item.config = config as IDataParamConfig
          this.setWidgetProps(dataParams, styleParams)
        }
      },
      colorModalVisible: true
    })
  }

  private dropboxItemChangeFilterConfig = (item: IDataParamSource) => {
    const { selectedView, onLoadDistinctValue } = this.props
    const { dataParams, styleParams } = this.state
    if (item.type === 'category') {
      const tempParams = {
        columns: [item.name]
      }
      if (typeof this.props.view === 'object' && Object.keys(this.props.view).length > 0) tempParams.view = this.props.view
      onLoadDistinctValue(selectedView.id, tempParams)
    }
    this.setState({
      modalCachedData: item,
      modalDataFrom: 'filters',
      modalCallback: (config) => {
        if (config) {
          item.config = config as IDataParamConfig
          // 保持dataParams.filters.items[i]和item一致，这样才能更换config
          if (dataParams && dataParams.filters && dataParams.filters.items) {
            for (let i = 0; i < dataParams.filters.items.length; i++) {
              if (dataParams.filters.items[i].name === item.name) {
                dataParams.filters.items[i] = item
                break
              }
            }
          }
          this.setWidgetProps(dataParams, styleParams)
        }
      },
      filterModalVisible: true
    })
  }

  private getDropboxItemChart = (item: IDataParamSource) => (chart: IChartInfo) => {
    const { dataParams } = this.state
    item.chart = chart
    dataParams.metrics.items = [...dataParams.metrics.items]
    const selectedParams = this.getChartDataConfig(getPivotModeSelectedCharts(dataParams.metrics.items))
    this.setWidgetProps(selectedParams.dataParams, selectedParams.styleParams)
  }

  private getDimetionsAndMetricsCount = () => {
    const { dataParams } = this.state
    const { cols, rows, metrics, secondaryMetrics } = dataParams
    const dcount = cols.items.length + rows.items.length
    const mcount = secondaryMetrics ? secondaryMetrics.items.length + metrics.items.length : metrics.items.length
    return [dcount, mcount]
  }

  public flipPage = (pageNo: number, pageSize: number, orders) => {
    const { dataParams, styleParams, pagination } = this.state
    this.setWidgetProps(dataParams, styleParams, {
      renderType: 'rerender',
      // 翻页的时候，更新updatedPagination
      updatedPagination: {
        ...pagination,
        pageNo,
        pageSize
      },
      queryMode: WorkbenchQueryMode.Immediately,
      orders
    })
  }

  private timeout = []

  private executeQuery(dataParams, execId, updatedPagination, selectedCharts, renderType, orders, that) {
    const { cols, rows, metrics, secondaryMetrics, filters, color, label, size, xAxis, tip, yAxis } = dataParams
    const { onSetWidgetProps, onGetProgress, onGetResult, selectedView } = that.props
    const { mode, chartModeSelectedChart } = that.state
    onGetProgress(execId, (result) => {
      const { progress, status } = result
      if (status === 'Failed') {
        // 提示 查询失败（显示表格头，就和现在的暂无数据保持一致的交互，只是提示换成“查询失败”）
        // -2表示查询失败
        that.props.changeGetProgressPercent(-2)
        that.deleteExecId(execId)
        return message.error('查询失败！')
      } else if (status === 'Succeed' && progress === 1) {
        // 查询成功，调用 结果集接口，status为success时，progress一定为1
        const pageNoReal = updatedPagination && updatedPagination.pageNo ? updatedPagination.pageNo : 0
        const pageSizeReal = updatedPagination && updatedPagination.pageSize ? updatedPagination.pageSize : 0
        onGetResult(execId, pageNoReal, pageSizeReal, (result) => {
          // 后续一样，执行数据显示的逻辑
          const { resultList: data, pageNo, pageSize, totalCount } = result
          updatedPagination = !updatedPagination.withPaging ? updatedPagination : {
            ...updatedPagination,
            pageNo,
            pageSize,
            totalCount
          }
          const mergedParams = that.getChartDataConfig(selectedCharts)
          const mergedDataParams = mergedParams.dataParams
          const mergedStyleParams = mergedParams.styleParams
          const requestParamsFilters = filters.items.reduce((a, b) => {
            return a.concat(b.config.sqlModel)
          }, [])

          // 关系图下
          if (selectedCharts[0].name === 'relationGraph' && mergedStyleParams.spec) {
            if (Array.isArray(requestParamsFilters) && requestParamsFilters[0] && cols.items && cols.items[0]) {
              // 值筛选 && 选的是第一个维度 && 只选了一个值
              if (requestParamsFilters[0].operator === 'in' && requestParamsFilters[0].name === cols.items[0].name && Array.isArray(requestParamsFilters[0].value) && requestParamsFilters[0].value.length === 1) {
                // 值筛选一个值，后端返回全量数据，前端根据全量数据，顶层节点变为只有一个，为筛选的这个值
                mergedStyleParams.spec.rootNodeCount = 1
                // requestParamsFilters[0].value[0]的值是带了单引号的字符串
                mergedStyleParams.spec.rootNodeName = requestParamsFilters[0].value[0].replace(/'/g, '')
              } else if (requestParamsFilters[0].operator === '=' && requestParamsFilters[0].name === cols.items[0].name) {
                // 条件筛选选的等于操作 && 选的是第一个维度 && 值是data里有的值
                for (let i = 0; i < data.length; i++) {
                  if (requestParamsFilters[0].value.replace(/'/g, '') === data[i][requestParamsFilters[0].name]) {
                    mergedStyleParams.spec.rootNodeCount = 1
                    // requestParamsFilters[0].value的值是带了单引号的字符串
                    mergedStyleParams.spec.rootNodeName = requestParamsFilters[0].value.replace(/'/g, '')
                    break
                  }
                }
              }
            }
          }

          onSetWidgetProps({
            cols: cols.items.map((item) => ({
              ...item,
              field: item.field || getDefaultFieldConfig(),
              format: item.format || getDefaultFieldFormatConfig(),
              sort: item.sort
            })),
            rows: rows.items.map((item) => ({
              ...item,
              field: item.field || getDefaultFieldConfig(),
              format: item.format || getDefaultFieldFormatConfig(),
              sort: item.sort
            })),
            metrics: metrics.items.map((item) => ({
              ...item,
              agg: item.agg || 'sum',
              chart: item.chart || getPivot(),
              field: item.field || getDefaultFieldConfig(),
              format: item.format || getDefaultFieldFormatConfig()
            })),
            ...secondaryMetrics && {
              secondaryMetrics: secondaryMetrics.items.map((item) => ({
                ...item,
                agg: item.agg || 'sum',
                chart: item.chart || getPivot(),
                field: item.field || getDefaultFieldConfig(),
                format: item.format || getDefaultFieldFormatConfig()
              }))
            },
            filters: filters.items.map(({name, type, config}) => ({ name, type, config })),
            ...color && {color},
            ...label && {label},
            ...size && {size},
            ...xAxis && {xAxis},
            ...tip && {tip},
            ...yAxis && {yAxis},
            chartStyles: mergedStyleParams,
            selectedChart: mode === 'pivot' ? chartModeSelectedChart.id : selectedCharts[0].id,
            data,
            pagination: updatedPagination,
            dimetionAxis: that.getDimetionAxis(selectedCharts),
            renderType: renderType || 'rerender',
            orders,
            mode,
            model: selectedView.model
          })
          that.setState({
            chartModeSelectedChart: mode === 'pivot' ? chartModeSelectedChart : selectedCharts[0],
            pagination: updatedPagination,
            styleParams: mergedStyleParams
          }, () => {
            const mergedParams = that.getChartDataConfig(selectedCharts)
            const mergedDataParams = mergedParams.dataParams
            that.setState({
              dataParams: mergedDataParams,
            })
          })
          that.props.changeGetProgressPercent(progress)
          that.deleteExecId(execId)
        }, (err) => {
          // -2表示查询失败
          that.props.changeGetProgressPercent(-2)
          that.deleteExecId(execId)
          return message.error('查询失败！')
        })
      } else {
        // 说明还在运行中
        // 更新进度条
        that.props.changeGetProgressPercent(progress)
        // 三秒后再请求一次进度查询接口
        const t = setTimeout(that.executeQuery, 3000, dataParams, execId, updatedPagination, selectedCharts, renderType, orders, that)
        that.timeout.push(t)
      }
    }, (err) => {
      // -2表示查询失败
      that.props.changeGetProgressPercent(-2)
      that.deleteExecId(execId)
      return message.error('查询失败！')
    })
  }

  private manuallyQuery = false

  // 点击手动查询按钮
  private forceSetWidgetProps = () => {
    const { dataParams, styleParams, pagination } = this.state
    this.manuallyQuery = true
    this.setWidgetProps(dataParams, styleParams, {
      renderType: 'rerender',
      updatedPagination: pagination,
      queryMode: WorkbenchQueryMode.Immediately
    })
  }

  private setWidgetProps = (
    dataParams: IDataParams,
    styleParams: IChartStyles,
    options?: {
      renderType?: RenderType,
      updatedPagination?: IPaginationParams,
      queryMode?: WorkbenchQueryMode,
      engine?: string,
      orders?
    }
  ) => {
    const { cols, rows, metrics, secondaryMetrics, filters, color, label, size, xAxis, tip, yAxis } = dataParams
    const { selectedView, onLoadData, onExecuteQuery, onSetWidgetProps, onSetQueryData, view, cache, expired, widgetProps } = this.props
    const { mode, chartModeSelectedChart, pagination } = this.state
    let renderType
    let updatedPagination
    let queryMode = this.props.queryMode

    if (options) {
      renderType = options.renderType
      updatedPagination = options.updatedPagination
      queryMode = WorkbenchQueryMode[options.queryMode] ? options.queryMode : queryMode
    }

    const fromPagination = !!updatedPagination
    updatedPagination = { ...pagination, ...updatedPagination }

    let groups = cols.items.map((c) => c.name)
      .concat(rows.items.map((r) => r.name))
      .filter((g) => g !== '指标名称')
    let aggregators = metrics.items.map((m) => ({
      column: decodeMetricName(m.name),
      func: m.agg
    }))
    if (secondaryMetrics) {
      aggregators = aggregators.concat(secondaryMetrics.items
        .map((m) => ({
          column: decodeMetricName(m.name),
          func: m.agg
        })))
    }
    if (color) {
      groups = groups.concat(color.items.map((c) => c.name))
      // 去重
      groups = Array.from(new Set(groups))
    }
    if (label) {
      groups = groups.concat(label.items
        .filter((l) => l.type === 'category')
        .map((l) => l.name))
      aggregators = aggregators.concat(label.items
        .filter((l) => l.type === 'value')
        .map((l) => ({
          column: decodeMetricName(l.name),
          func: l.agg
        })))
    }
    if (size) {
      aggregators = aggregators.concat(size.items
        .map((l) => ({
          column: decodeMetricName(l.name),
          func: l.agg
        })))
    }
    if (xAxis) {
      aggregators = aggregators.concat(xAxis.items
        .map((l) => ({
          column: decodeMetricName(l.name),
          func: l.agg
        })))
    }
    if (tip) {
      aggregators = aggregators.concat(tip.items
        .map((l) => ({
          column: decodeMetricName(l.name),
          func: l.agg
        })))
    }
    if (yAxis) {
      aggregators = aggregators.concat(yAxis.items
        .map((l) => ({
          column: decodeMetricName(l.name),
          func: l.agg
        })))
    }

    const orders = []
    Object.values(dataParams)
      .reduce<IDataParamSource[]>((items, param: IDataParamProperty) => items.concat(param.items), [])
      .forEach((item) => {
        let column = item.name
        if (item.type === 'value') {
          column = decodeMetricName(item.name)
          if (!styleParams.table || !styleParams.table.withNoAggregators) {
            column = `${item.agg}(${column})`
          }
        }
        if (item.sort && [FieldSortTypes.Asc, FieldSortTypes.Desc].includes(item.sort.sortType)) {
          orders.push({
            column,
            direction: item.sort.sortType
          })
        }
      })

    let selectedCharts
    let dimetionsCount

    if (mode === 'pivot') {
      selectedCharts = getPivotModeSelectedCharts(metrics.items)
      dimetionsCount = groups.length
    } else {
      selectedCharts = [chartModeSelectedChart]
      dimetionsCount = cols.items.length
    }

    const metricsLength = secondaryMetrics
      ? metrics.items.length + secondaryMetrics.items.length
      : metrics.items.length
    if (!checkChartEnable(dimetionsCount, metricsLength, selectedCharts)) {
      selectedCharts = mode === 'pivot'
        ? getPivotModeSelectedCharts([])
        : [getTable()]
    }

    let noAggregators = false
    if (styleParams.table) { // @FIXME pagination in table style config
      const { withPaging, pageSize, withNoAggregators } = styleParams.table
      noAggregators = withNoAggregators
      if (!fromPagination) {
        if (withPaging) {
          updatedPagination.pageNo = widgetProps && widgetProps.pagination && widgetProps.pagination.pageNo ? widgetProps.pagination.pageNo : 1
          updatedPagination.pageSize = widgetProps && widgetProps.pagination && widgetProps.pagination.pageSize ? widgetProps.pagination.pageSize : +pageSize
        } else {
          updatedPagination.pageNo = 0
          updatedPagination.pageSize = 0
        }
      }
      updatedPagination.withPaging = withPaging
    }

    const requestParamsFilters = filters.items.reduce((a, b) => {
      return a.concat(b.config.sqlModel)
    }, [])

    const requestParams = {
      groups,
      aggregators,
     // filters: filters.items.map((i) => [].concat(i.config.sql)),
      filters: requestParamsFilters,
      orders,
      pageNo: updatedPagination.pageNo,
      pageSize: updatedPagination.pageSize,
      nativeQuery: noAggregators,
      cache,
      expired,
      // 只有清除缓存时flush为true，其他所有时候都为false
      flush: this.clearCacheStatus
    }

    // 关系图下的请求都要加上这个参数
    if (selectedCharts[0].name === 'relationGraph') requestParams.chartType = 'relation_graph'

    // 如果有view，就把view放进requestParams才能正常请求
    if (Object.keys(view).length > 0) requestParams.view = view

    // 第一次进入编辑页面时，如果本身选了引擎，但这时候可能因为this.props.engine还没更新，所以this.props.engine为''，但this.props.originalWidgetProps.engine不为''
    // 如果this.props.engine和this.props.originalWidgetProps.engine都为''，说明没有选过引擎，用this.props.engine就行了
    // 如果this.props.engine不为''，则使用this.props.engine的值
    if (this.props.engine === '' && this.props.originalWidgetProps && this.props.originalWidgetProps.engine !== '') {
      requestParams.engineType = this.props.originalWidgetProps.engine
    } else {
      if (this.props.engine) requestParams.engineType = this.props.engine
    }

    if (options) {
      if (options.orders) {
        requestParams.orders = requestParams.orders.concat(options.orders)
      }
    }

    const requestParamString = JSON.stringify(requestParams)
    const needRequest = (groups.length > 0 || aggregators.length > 0)
                       && selectedView
                       && requestParamString !== this.lastRequestParamString
                       && queryMode === WorkbenchQueryMode.Immediately
                       || this.manuallyQuery
    this.manuallyQuery = false

    if (needRequest) {
      this.lastRequestParamString = requestParamString
      onSetQueryData(requestParams)

      // 在查询数据之前，清空所有之前的请求
      if (!this.clearCacheStatus) {
        // 清理缓存请求不影响正常查询
        this.timeout.forEach(item => clearTimeout(item))
        this.execIds.forEach((execId) => {
          this.props.onKillExecute(execId, () => {}, () => {})
        })
      }

      const mergedParams = this.getChartDataConfig(selectedCharts)
      const mergedDataParams = mergedParams.dataParams
      const mergedStyleParams = mergedParams.styleParams

      onSetWidgetProps({
        data: null,
        cols: cols.items.map((item) => ({
          ...item,
          field: item.field || getDefaultFieldConfig(),
          format: item.format || getDefaultFieldFormatConfig(),
          sort: item.sort
        })),
        rows: rows.items.map((item) => ({
          ...item,
          field: item.field || getDefaultFieldConfig(),
          format: item.format || getDefaultFieldFormatConfig(),
          sort: item.sort
        })),
        metrics: metrics.items.map((item) => ({
          ...item,
          agg: item.agg || 'sum',
          chart: item.chart || getPivot(),
          field: item.field || getDefaultFieldConfig(),
          format: item.format || getDefaultFieldFormatConfig()
        })),
        ...secondaryMetrics && {
          secondaryMetrics: secondaryMetrics.items.map((item) => ({
            ...item,
            agg: item.agg || 'sum',
            chart: item.chart || getPivot(),
            field: item.field || getDefaultFieldConfig(),
            format: item.format || getDefaultFieldFormatConfig()
          }))
        },
        filters: filters.items.map(({name, type, config}) => ({ name, type, config })),
        ...color && {color},
        ...label && {label},
        ...size && {size},
        ...xAxis && {xAxis},
        ...tip && {tip},
        ...yAxis && {yAxis},
        chartStyles: mergedStyleParams,
        selectedChart: mode === 'pivot' ? chartModeSelectedChart.id : selectedCharts[0].id,
        pagination: updatedPagination,
        dimetionAxis: this.getDimetionAxis(selectedCharts),
        renderType: renderType || 'clear',
        orders,
        mode,
        model: selectedView ? selectedView.model : {}
      })
      this.setState({
        chartModeSelectedChart: mode === 'pivot' ? chartModeSelectedChart : selectedCharts[0],
        pagination: updatedPagination,
        dataParams: mergedDataParams,
        styleParams: mergedStyleParams
      })

      // 执行查询数据接口
      // 虚拟view切换分类型和数值型时，会执行到这里，要加上判断，切换操作不调用查询数据的接口
      if (!this.changeValueCategory) {
        // 图表驱动里的excel类型，不走visualis里的查询数据逻辑，而是改动接dataWrangler的iframe的url
        if (mode === 'chart' && selectedCharts[0].id === 19) return this.lastRequestParamString = ''
        onExecuteQuery(selectedView.id, requestParams, (result) => {
          const { execId } = result
          this.execIds.push(execId)
          if (this.clearCacheStatus) {
            // 说明此时是清理缓存调用的getdata接口，并且不调用progress和result接口
            this.clearCacheStatus = false
            return message.success('清理缓存成功！')
          } else {
            // 此时不是清理缓存，是正常查询数据
            // 每隔三秒执行一次查询进度接口
            this.executeQuery(dataParams, execId, updatedPagination, selectedCharts, renderType, orders, this)
          }
        }, () => {
          if (this.clearCacheStatus) {
            // 说明此时是清理缓存调用的getdata接口
            this.clearCacheStatus = false
            this.props.changeGetProgressPercent(-2)
            return message.error('清理缓存失败！')
          } else {
            this.props.changeGetProgressPercent(-2)
            return message.error('查询失败！')
          }
        })
      } else {
        this.changeValueCategory = false
      }
    } else {
      const mergedParams = this.getChartDataConfig(selectedCharts)
      const mergedDataParams = mergedParams.dataParams
      const mergedStyleParams = mergedParams.styleParams
      onSetWidgetProps({
        data: null,
        cols: cols.items.map((item) => ({
          ...item,
          field: item.field || getDefaultFieldConfig(),
          format: item.format || getDefaultFieldFormatConfig(),
          sort: item.sort
        })),
        rows: rows.items.map((item) => ({
          ...item,
          field: item.field || getDefaultFieldConfig(),
          format: item.format || getDefaultFieldFormatConfig(),
          sort: item.sort
        })),
        metrics: metrics.items.map((item) => ({
          ...item,
          agg: item.agg || 'sum',
          chart: item.chart || getPivot(),
          field: item.field || getDefaultFieldConfig(),
          format: item.format || getDefaultFieldFormatConfig()
        })),
        ...secondaryMetrics && {
          secondaryMetrics: secondaryMetrics.items.map((item) => ({
            ...item,
            agg: item.agg || 'sum',
            chart: item.chart || getPivot(),
            field: item.field || getDefaultFieldConfig(),
            format: item.format || getDefaultFieldFormatConfig()
          }))
        },
        filters: filters.items.map(({name, type, config}) => ({ name, type, config })),
        ...color && {color},
        ...label && {label},
        ...size && {size},
        ...xAxis && {xAxis},
        ...tip && {tip},
        ...yAxis && {yAxis},
        chartStyles: mergedStyleParams,
        selectedChart: mode === 'pivot' ? chartModeSelectedChart.id : selectedCharts[0].id,
        pagination: updatedPagination,
        dimetionAxis: this.getDimetionAxis(selectedCharts),
        renderType: renderType || 'clear',
        orders,
        mode,
        model: selectedView ? selectedView.model : {}
      })
      this.setState({
        chartModeSelectedChart: mode === 'pivot' ? chartModeSelectedChart : selectedCharts[0],
        pagination: updatedPagination,
        dataParams: mergedDataParams,
        styleParams: mergedStyleParams
      })
    }
  }

  // 虚拟view切换分类型和数值型时，会执行到这里，要加上判断，切换操作不调用查询数据的接口
  private changeValueCategory = false

  // 清除缓存时，用来控制flush=true和只请求getdata不请求progress和结果集
  private clearCacheStatus = false

  // 清理缓存
  private clearCache = () => {
    const { dataParams, styleParams } = this.state
    this.clearCacheStatus = true
    this.setWidgetProps(dataParams, styleParams)
  }

  private getDimetionAxis = (selectedCharts): DimetionType => {
    const pivotChart = getPivot()
    const onlyPivot = !selectedCharts.filter((sc) => sc.id !== pivotChart.id).length
    if (!onlyPivot) {
      return 'col'
    }
  }

  // 选择 透视驱动和图表驱动下面的各个图标
  private chartSelect = (chart: IChartInfo) => {
    const { mode, dataParams } = this.state
    const { cols, rows, metrics } = dataParams
    if (mode === 'pivot') {
      if (!(metrics.items.length === 1 && metrics.items[0].chart.id === chart.id)) {
        metrics.items.forEach((i) => {
          i.chart = chart
        })
        if (chart.id !== PivotTypes.PivotTable) {
          cols.items = cols.items.filter((c) => c.name !== '指标名称')
          rows.items = rows.items.filter((r) => r.name !== '指标名称')
        }
        const selectedParams = this.getChartDataConfig(getPivotModeSelectedCharts(metrics.items))
        this.setWidgetProps(selectedParams.dataParams, selectedParams.styleParams)
      }
    } else {
      // 编辑widget页面里，每次从excel类型切到其他类型时，要删掉DataWrangler里面名为visualis_widget_{widgetId}的表格
      if (chart.id !== 19 && document.getElementById('dataWrangler') && this.props.id !== 0) {
        // this.props.id === 0是新建widget的时候，新建widget的时候只有保存widget时，如果是excel类型就要在DataWrangler里面保存表格
        // 从excel类型切换到另外的类型时，要删除DataWrangler里的表格
        document.getElementById('dataWrangler').contentWindow.postMessage({type: 'delete', id: this.props.id},'*')
        this.chart = chart
        // 那边删除成功之后调用这个deleteVisualisWidgetListener再切换类型
        window.addEventListener('message', this.listenerHanlder)
      } else {
        this.setState({
          chartModeSelectedChart: chart,
          pagination: { pageNo: 0, pageSize: 0, withPaging: false, totalCount: 0 }
        }, () => {
          const selectedParams = this.getChartDataConfig([chart])
          this.setWidgetProps(selectedParams.dataParams, selectedParams.styleParams)
        })
      }
    }
  }

  private chart = null

  private listenerHanlder = (event) => {
    if (event.data.type === 'delete') {
      this.setState({
        chartModeSelectedChart: this.chart,
        pagination: { pageNo: 0, pageSize: 0, withPaging: false, totalCount: 0 }
      }, () => {
        const selectedParams = this.getChartDataConfig([this.chart])
        this.setWidgetProps(selectedParams.dataParams, selectedParams.styleParams)
      })
    }
  }

  private viewSelect = (name: string) => {
    const { views } = this.props
    let view = null
    for (let i = 0; i < views.length; i++) {
      if (name === views[i].name) {
        view = views[i]
        break
      }
    }
    const viewId = view.id
    const { mode, dataParams } = this.state
    const hasItems = Object.values(dataParams)
      .filter((param) => !!param.items.length)
    if (hasItems.length) {
      confirm({
        title: '切换 View 会清空所有配置项，是否继续？',
        onOk: () => {
          this.resetWorkbench(mode)
          this.props.onViewSelect(view)
        }
      })
    } else {
      this.props.onViewSelect(view)
    }
    sessionStorage.setItem('viewId', viewId.toString());
  }

  private filterView: SelectProps['filterOption'] = (input, option) =>
    (option.props.children as string).toLowerCase().includes(input.toLowerCase())

  private changeMode = (e) => {
    const mode = e.target.value
    const { dataParams } = this.state
    const hasItems = Object.values(dataParams)
      .filter((param) => !!param.items.length)
    if (hasItems.length) {
      confirm({
        title: '切换图表模式会清空所有配置项，是否继续？',
        onOk: () => {
          this.setState({
            mode,
            currentWidgetlibs: widgetlibs[mode]
          }, () => {
            this.resetWorkbench(mode)
          })
        }
      })
    } else {
      this.setState({
        mode,
        currentWidgetlibs: widgetlibs[mode]
      }, () => {
        this.resetWorkbench(mode)
      })
    }
  }

  // 如切换 透视驱动和图表驱动 时，重置各配置
  private resetWorkbench = (mode) => {
    // 重置时，清空所有之前的请求
    this.timeout.forEach(item => clearTimeout(item))
    this.execIds.forEach((execId) => {
      this.props.onKillExecute(execId, () => {}, () => {})
    })
    // 让进度条消失，如果设成-2，会显示查询失败，所以这里设成-3，进度条只是消失
    this.props.changeGetProgressPercent(-3)

    const { dataParams } = this.state
    Object.values(dataParams).forEach((param) => {
      param.items = []
      if (param.value) {
        param.value = {}
      }
    })
    this.setState({
      showColsAndRows: false,
      chartModeSelectedChart: getTable()
    })
    const selectedCharts = mode === 'pivot'
      ? getPivotModeSelectedCharts([])
      : [getTable()]
    const resetedParams = this.getChartDataConfig(selectedCharts)
    // 这样清空了选项之后，重新切回到excel类型，才不会自动请求
    this.props.onSetQueryData(null)
    this.setWidgetProps(resetedParams.dataParams, resetedParams.styleParams)
  }

  // 某个dropbox的设置里面更改值之后触发
  private dropboxValueChange = (name) => (key: string, value: string | number) => {
    const { mode, dataParams, styleParams } = this.state
    const { color, size } = dataParams
    switch (name) {
      case 'color':
        if (key === 'all' && mode === 'pivot') {
          Object.keys(color.value).forEach((k) => {
            color.value[k] = value
          })
        } else {
          color.value[key] = value
        }
        break
      case 'size':
        if (key === 'all') {
          Object.keys(size.value).forEach((k) => {
            size.value[k] = value
          })
        } else {
          size.value[key] = value
        }
    }
  }

  // 更改 样式 配置里的内容
  private styleChange = (name) => (prop, value, propPath?: string[]) => {
    const { dataParams, styleParams } = this.state
    if (!propPath || !propPath.length) {
      styleParams[name][prop] = value
    } else {
      propPath.reduce((subStyle, currentPathName, idx) => {
        const childStyle = subStyle[currentPathName]
        if (idx === propPath.length - 1) {
          childStyle[prop] = value
        }
        return childStyle
      }, styleParams[name])
    }
    let renderType: RenderType = 'clear'
    switch (prop) {
      case 'layerType':
        renderType = 'rerender'
        break
      case 'smooth':
        renderType = 'clear'
        break
    }
    if (name === 'table') {
      // 如果是图表驱动表格，就要更新dataParams里的cols和metrics的width, alreadySetWidth, oldColumnCounts, widthChanged属性
      if (dataParams.cols && dataParams.cols.items) {
        dataParams.cols.items.forEach((item, index) => {
          for (let i = 0; i < value.length; i++) {
            if (item.name === value[i].columnName) {
              dataParams.cols.items[index].width = value[i].width
              dataParams.cols.items[index].alreadySetWidth = value[i].alreadySetWidth
              dataParams.cols.items[index].oldColumnCounts = value[i].oldColumnCounts
              dataParams.cols.items[index].widthChanged = value[i].widthChanged
              break
            }
          }
        })
      }
      if (dataParams.metrics && dataParams.metrics.items) {
        dataParams.metrics.items.forEach((item, index) => {
          for (let i = 0; i < value.length; i++) {
            if (item.name === value[i].columnName) {
              dataParams.metrics.items[index].width = value[i].width
              dataParams.metrics.items[index].alreadySetWidth = value[i].alreadySetWidth
              dataParams.metrics.items[index].oldColumnCounts = value[i].oldColumnCounts
              dataParams.metrics.items[index].widthChanged = value[i].widthChanged
              break
            }
          }
        })
      }
      this.setState({dataParams})
    }
    this.setWidgetProps(dataParams, styleParams, { renderType })
    // const { layerType } = styleParams.spec
    // chartModeSelectedChart.style.spec.layerType = layerType
  }

  // @FIXME refactor function styleChange2
  private styleChange2 = (value: string | number, propPath: string[]) => {
    const { dataParams, styleParams } = this.state
    set(styleParams, propPath, value)
    let renderType: RenderType = 'clear'
    if (propPath.includes('layerType')) {
      renderType = 'rerender'
    } else if (propPath.includes('smooth')) {
      renderType = 'clear'
    }
    this.setWidgetProps(dataParams, styleParams, { renderType })
  }

  private confirmColorModal = (config) => {
    this.state.modalCallback(config)
    this.closeColorModal()
  }

  private cancelColorModal = () => {
    this.state.modalCallback(false)
    this.closeColorModal()
  }

  private closeColorModal = () => {
    this.setState({
      colorModalVisible: false,
      modalCachedData: null,
      modalCallback: null,
      modalDataFrom: ''
    })
  }

  private confirmActOnModal = (config) => {
    this.state.modalCallback(config)
    this.closeActOnModal()
  }

  private cancelActOnModal = () => {
    this.state.modalCallback(false)
    this.closeActOnModal()
  }

  private closeActOnModal = () => {
    this.setState({
      actOnModalVisible: false,
      actOnModalList: null,
      modalCachedData: null,
      modalCallback: null,
      modalDataFrom: ''
    })
  }

  // 筛选框的保存
  private confirmFilterModal = (config) => {
    this.state.modalCallback(config)
    this.closeFilterModal()
  }

  private cancelFilterModal = () => {
    this.state.modalCallback(false)
    this.closeFilterModal()
  }

  private closeFilterModal = () => {
    this.setState({
      filterModalVisible: false,
      modalCachedData: null,
      modalCallback: null,
      modalDataFrom: ''
    })
  }

  private afterColorModalClose = () => {
    this.colorSettingForm.reset()
  }

  private afterActOnModalClose = () => {
    this.actOnSettingForm.reset()
  }

  private afterFilterModalClose = () => {
    this.filterSettingForm.reset()
  }

  private tabSelect = (key) => () => {
    this.setState({
      selectedTab: key
    })
  }

  private showControlConfig = () => {
    this.setState({
      controlConfigVisible: true
    })
  }

  private closeControlConfig = () => {
    this.setState({
      controlConfigVisible: false
    })
  }

  private saveControls = (controls) => {
    this.props.onSetControls(controls)
    this.closeControlConfig()
  }

  private checkAllDragItem = (type: DragType) => (e: CheckboxChangeEvent) => {
    const { categoryDragItems, valueDragItems } = this.state
    const checked = e.target.checked
    if (type === 'category') {
      this.setState({
        categoryDragItems: categoryDragItems.map((item) => ({ ...item, checked }))
      })
    } else {
      this.setState({
        valueDragItems: valueDragItems.map((item) => ({ ...item, checked }))
      })
    }
  }

  private checkDragItem = (type: DragType, name: string) => (e: CheckboxChangeEvent) => {
    const { categoryDragItems, valueDragItems } = this.state
    const checked = e.target.checked
    if (type === 'category') {
      this.setState({
        categoryDragItems: categoryDragItems.map((item) => {
          if (item.name === name) {
            return { ...item, checked }
          } else {
            return item
          }
        })
      })
    } else {
      this.setState({
        valueDragItems: valueDragItems.map((item) => {
          if (item.name === name) {
            return { ...item, checked }
          } else {
            return item
          }
        })
      })
    }
  }

  private coustomFieldSelect = (event) => {
    const {key} = event
    switch (key) {
      case 'computed':
        this.setState({
          computedConfigModalVisible: true
        })
        break
      default:
        break
    }
  }

  private hideComputedConfigModal = () => {
    this.setState({computedConfigModalVisible: false, selectedComputed: null})
  }

  private saveComputedConfig = (config) => {
    const {onSetComputed} = this.props
    if (config) {
      onSetComputed(config)
    }
  }

  private onShowEditComputed = (tag) => () => {
    this.setState({
      computedConfigModalVisible: true,
      selectedComputed: tag
    }, () => {
      const {id, name, visualType, sqlExpression} = tag
      this.forceUpdate(() => {
        this.computedConfigForm.props.form.setFieldsValue({id, name, visualType})
      })
    })
  }

  private onDeleteComputed = (tag) => () => {
    const { onDeleteComputed } = this.props
    if (onDeleteComputed) {
      onDeleteComputed(tag)
    }
  }

  private bootstrapMorePanel = (tag) => {
    const columnMenu = (
      <Menu>
        <Menu.Item className={styles.menuItem}>
          <span
            className={styles.menuText}
            onClick={this.onShowEditComputed(tag)}
          >
          字段信息
          </span>
        </Menu.Item>
        <Menu.Item className={styles.menuItem}>
          <Popconfirm
            title={`确定删除 ${tag.name}?`}
            placement="bottom"
            onConfirm={this.onDeleteComputed(tag)}
          >
            <span className={styles.menuText}>删除</span>
          </Popconfirm>
        </Menu.Item>
      </Menu>
    )

    return (
      <span className={styles.more}>
        <Dropdown overlay={columnMenu} placement="bottomRight" trigger={['click']}>
          <Icon type="ellipsis" />
        </Dropdown>
      </span>
    )
  }

  private changeEngine = (value) => {
    this.props.setEngine(value)
  }

  public render () {
    const {
      views,
      selectedView,
      distinctColumnValues,
      columnValueLoading,
      controls,
      cache,
      autoLoadData,
      expired,
      queryMode,
      multiDrag,
      computed,
      onCacheChange,
      onChangeAutoLoadData,
      onExpiredChange,
      originalWidgetProps,
      originalComputed,
      isFold,
      onChangeIsFold,
      view,
      engine
    } = this.props
    const {
      dragged,
      showColsAndRows,
      mode,
      currentWidgetlibs,
      chartModeSelectedChart,
      selectedTab,
      dataParams,
      styleParams,
      modalCachedData,
      modalDataFrom,
      fieldModalVisible,
      formatModalVisible,
      sortModalVisible,
      currentEditingItem,
      colorModalVisible,
      actOnModalVisible,
      actOnModalList,
      filterModalVisible,
      controlConfigVisible,
      valueDragItems,
      computedConfigModalVisible,
      selectedComputed,
      engines
    } = this.state
    const widgetPropsModel = selectedView && selectedView.model ? selectedView.model : {}

    const { metrics } = dataParams
    const [dimetionsCount, metricsCount] = this.getDimetionsAndMetricsCount()
    const {
      spec, xAxis, yAxis, axis, splitLine, pivot: pivotConfig, label, legend,
      visualMap, toolbox, areaSelect, scorecard, gauge, iframe, table, bar, radar, doubleYAxis } = styleParams

    let categoryDragItems = this.state.categoryDragItems
    if (mode === 'pivot'
      && valueDragItems.length
      && dataParams.metrics.items.every((item) => item.chart.id === getPivot().id)) {
      // categoryDragItems = categoryDragItems.concat({
      //   name: '指标名称',
      //   type: 'category',
      //   visualType: ViewModelVisualTypes.String,
      //   checked: false
      // })
    }

    const coustomFieldSelectMenu = (
      <Menu onClick={this.coustomFieldSelect}>
        <MenuItem key="computed">计算字段</MenuItem>
      </Menu>
    )

    // 拖拽框
    const dropboxes = Object.entries(dataParams)
      .map(([k, v]) => {
        if (k === 'rows' && !showColsAndRows) {
          return
        }
        if (k === 'cols') {
          v.title = showColsAndRows ? '列' : '维度'
        }
        let panelList = []
        if (k === 'color') {
          panelList = metrics.items
        }
        if (k === 'size') {
          panelList = v.items
        }
        return (
          <Dropbox
            key={k}
            name={k}
            title={v.title}
            type={v.type}
            value={v.value}
            items={v.items}
            mode={mode}
            selectedChartId={chartModeSelectedChart.id}
            dragged={dragged}
            panelList={panelList}
            dimetionsCount={dimetionsCount}
            metricsCount={metricsCount}
            onValueChange={this.dropboxValueChange(k)}
            onItemDragStart={this.insideDragStart(k)}
            onItemDragEnd={this.insideDragEnd}
            onItemRemove={this.removeDropboxItem(k)}
            onItemSort={this.getDropboxItemSortDirection(k)}
            onItemChangeAgg={this.getDropboxItemAggregator(k)}
            onItemChangeFieldConfig={this.dropboxItemChangeFieldConfig(k)}
            onItemChangeFormatConfig={this.dropboxItemChangeFormatConfig(k)}
            onItemChangeColorConfig={this.dropboxItemChangeColorConfig}
            onItemChangeFilterConfig={this.dropboxItemChangeFilterConfig}
            onItemChangeChart={this.getDropboxItemChart}
            beforeDrop={this.beforeDrop}
            onDrop={this.drop}
          />
        )
      })

    const rowsColsToggleClass = classnames({
      [styles.toggleRowsAndCols]: true,
      [utilStyles.hide]: mode === 'chart'
    })
    const rowsColsSwitchClass = classnames({
      [styles.switchRowsAndCols]: true,
      [utilStyles.hide]: !showColsAndRows
    })

    const tabs = this.tabKeys.map(({key, title}) => {
      const tabClass = classnames({
        [styles.selected]: key === selectedTab
      })
      return (
        <li
          key={key}
          className={tabClass}
          onClick={this.tabSelect(key)}
        >
          {title}
        </li>
      )
    })

    let queryInfo: string[] = []
    if (selectedView) {
      if (!selectedView.variable) selectedView.variable = []
      if (typeof selectedView.variable !== 'object') selectedView.variable = JSON.parse(selectedView.variable)
      queryInfo = selectedView.variable.map((v) => v.name)
    }

    let mapLegendLayerType
    let mapLabelLayerType
    if (spec) {
      const { layerType } = spec
      mapLabelLayerType = !(layerType && layerType === 'heatmap')
      mapLegendLayerType = !(layerType && (layerType === 'heatmap' || layerType === 'map' || layerType === 'scatter'))
    }
    // 中间栏的数据/样式/配置里的内容
    let tabPane
    switch (selectedTab) {
      case 'data':
        tabPane = (
          <div className={`${styles.paramsPane} ${styles.dropPane}`}>
            <div className={rowsColsToggleClass} onClick={this.toggleRowsAndCols}>
              <Icon type="swap" />
              {showColsAndRows ? ' 使用维度' : ' 使用行列'}
            </div>
            <div className={rowsColsSwitchClass} onClick={this.switchRowsAndCols}>
              <Icon type="retweet" /> 行列切换
            </div>
            {dropboxes}
          </div>
        )
        break
      case 'style':
        tabPane = (
          <div className={styles.paramsPane}>
            {spec && <SpecSection
              name={chartModeSelectedChart.name}
              title={chartModeSelectedChart.title}
              config={spec}
              onChange={this.styleChange2}
              isLegendSection={mapLegendLayerType}
            />}
            {bar && <BarSection
              onChange={this.styleChange('bar')}
              config={bar}
              dataParams={dataParams}
            />}
            {radar && <RadarSection config={radar} onChange={this.styleChange2} />}
            { mapLabelLayerType
                ? label && <LabelSection
                  title="标签"
                  config={label}
                  onChange={this.styleChange('label')}
                  name={chartModeSelectedChart.name}
                />
                : null
            }
            { mapLegendLayerType
                ? legend && <LegendSection
                  title="图例"
                  config={legend}
                  onChange={this.styleChange('legend')}
                />
                : null
            }
            { mapLegendLayerType
                ? null
                : visualMap && <VisualMapSection
                  title="视觉映射"
                  config={visualMap}
                  onChange={this.styleChange('visualMap')}
                />
            }
            {toolbox && <ToolboxSection
              title="工具"
              config={toolbox}
              onChange={this.styleChange('toolbox')}
            />}
            {doubleYAxis && <DoubleYAxisSection
              title="双Y轴"
              config={doubleYAxis}
              onChange={this.styleChange('doubleYAxis')}
            />}
            {xAxis && <AxisSection
              title="X轴"
              config={xAxis}
              onChange={this.styleChange('xAxis')}
            />}
            {yAxis && <AxisSection
              title="Y轴"
              config={yAxis}
              onChange={this.styleChange('yAxis')}
            />}
            {axis && <AxisSection
              title="轴"
              config={axis}
              onChange={this.styleChange('axis')}
            />}
            {splitLine && <SplitLineSection
              title="分隔线"
              config={splitLine}
              onChange={this.styleChange('splitLine')}
            />}
            {areaSelect && <AreaSelectSection
              title="坐标轴框选"
              config={areaSelect}
              onChange={this.styleChange('areaSelect')}
            />}
            {scorecard && <ScorecardSection
              title="翻牌器"
              config={scorecard}
              onChange={this.styleChange('scorecard')}
            />}
            {gauge && <GaugeSection
              title="仪表盘"
              config={gauge}
              onChange={this.styleChange('gauge')}
            />}
            {iframe && <IframeSection
              title="内嵌网页"
              config={iframe}
              onChange={this.styleChange('iframe')}
            />}
            {table && <TableSection
              dataParams={dataParams}
              config={table}
              onChange={this.styleChange('table')}
            />}
            {pivotConfig && <PivotSection
              title="透视表"
              config={pivotConfig}
              onChange={this.styleChange('pivot')}
            />}
          </div>
        )
        break
      case 'setting':
        tabPane = (
          <div className={styles.paramsPane}>
            {
              queryInfo.length
                ? <div className={styles.paneBlock}>
                    <h4>
                      <span>控制器</span>
                      <span
                        className={styles.addVariable}
                        onClick={this.showControlConfig}
                      >
                        <Icon type="edit" /> 点击配置
                      </span>
                    </h4>
                  </div>
                : <div className={styles.paneBlock}>
                    <h4>控制器</h4>
                    <Row
                      gutter={8}
                      type="flex"
                      justify="center"
                      align="middle"
                      className={`${styles.blockRow} ${styles.noVariable}`}
                    >
                      <Icon type="stop" /> 没有变量可以设置
                    </Row>
                  </div>
            }
            <div className={styles.paneBlock}>
              <h4>开启缓存</h4>
              <div className={styles.blockBody}>
                <Row gutter={8} type="flex" align="middle" className={styles.blockRow}>
                  <Col span={24}>
                    <RadioGroup size="small" value={cache} onChange={onCacheChange} disabled>
                      <RadioButton value={false}>关闭</RadioButton>
                      <RadioButton value={true}>开启</RadioButton>
                    </RadioGroup>
                  </Col>
                </Row>
              </div>
            </div>
            <div className={styles.paneBlock}>
              <h4>缓存有效期（秒）</h4>
              <div className={styles.blockBody}>
                <Row gutter={8} type="flex" align="middle" className={styles.blockRow}>
                  <Col span={24}>
                    <InputNumber value={expired} disabled={!cache} onChange={onExpiredChange} />
                  </Col>
                </Row>
              </div>
            </div>
            <div className={styles.paneBlock}>
              <div className={styles.blockBody}>
                <Row gutter={8} type="flex" align="middle" className={styles.blockRow}>
                  <Col span={24}>
                    <Button size="small" onClick={this.clearCache} disabled>清理缓存</Button>
                  </Col>
                </Row>
              </div>
            </div>
            <div className={styles.paneBlock}>
              <h4>查询引擎</h4>
              <div className={styles.blockBody}>
                <Row gutter={8} type="flex" align="middle" className={styles.blockRow}>
                  <Col span={24}>
                    <Select size="small" style={{ width: 150 }} defaultValue={engine} placeholder="请选择引擎" onChange={this.changeEngine}>
                      {engines.map((o) => {
                        return <Option key={o} value={o}>{o}</Option>
                      })}
                    </Select>
                  </Col>
                </Row>
              </div>
            </div>
            {/* <div className={styles.paneBlock}>
              <h4>自动加载数据</h4>
              <div className={styles.blockBody}>
                <Row gutter={8} type="flex" align="middle" className={styles.blockRow}>
                  <Col span={24}>
                    <RadioGroup size="small" value={autoLoadData} onChange={onChangeAutoLoadData}>
                      <RadioButton value={true}>是</RadioButton>
                      <RadioButton value={false}>否</RadioButton>
                    </RadioGroup>
                  </Col>
                </Row>
              </div>
            </div> */}
          </div>
        )
        break
    }

    let colorSettingConfig
    let actOnSettingConfig
    let filterSettingConfig
    if (modalCachedData) {
      const selectedItem = dataParams[modalDataFrom]
        .items.find((i) => i.name === modalCachedData.name)
      switch (modalDataFrom) {
        case 'color':
          colorSettingConfig = selectedItem ? selectedItem.config : {}
          break
        case 'filters':
          filterSettingConfig = selectedItem ? selectedItem.config : {}
          break
        default:
          actOnSettingConfig = selectedItem ? selectedItem.config : {}
          break
      }
    }

    const categoryAreaProps = {
      dragged: this.state.dragged,
      type: 'category' as DragType,
      beforeDrop: (draggedItem: IDataParamSource) => {
        const draggedItemName = decodeMetricName(draggedItem.name)
        const params = this.state.dataParams
        const keys = Object.keys(params)
        // 判断该字段是否已经被拖拽进了维度、指标等任意地方，已经被拖进去了就不能再切换分类型和数值型了
        let isChoosed = false
        keys.forEach((key) => {
          if (params[key] && Array.isArray(params[key].items)) {
            params[key].items.forEach((item) => {
              if (decodeMetricName(item.name) === draggedItemName) isChoosed = true
            })
          }
        })

        if (isChoosed) return message.warning('该字段已被使用，无法切换分类型/数值型')

        // 如果该字段没被使用，执行下面的逻辑
        let selectedView = this.props.selectedView;
        const propName = draggedItem.name;
        draggedItem.modelType = 'value';
        draggedItem.name = propName;
        // selectedView.id小于等于0或者为null，就是虚拟view
        if (selectedView && selectedView.id) {
          // 实体view，调用更新view的接口
          if (typeof selectedView.model !== 'object') selectedView.model = JSON.parse(selectedView.model)
          selectedView.model[propName] = draggedItem;
          this.props.onBeofreDropColunm(selectedView, () => {})
        } else {
          // 虚拟view，不调用更新view的接口，直接更新widget的view
          const tempView = this.props.view
          if (typeof tempView.model !== 'object') tempView.model = JSON.parse(tempView.model)
          // 从category变为value
          tempView.model[propName].modelType = 'value'
          this.changeValueCategory = true
          this.props.onSetView(tempView)
          // 取消掉拖拽时的样式
          this.setState({dragged: null})
        }
      }
    }
    const valueAreaProps = {
      dragged: this.state.dragged,
      type: 'value' as DragType,
      beforeDrop: (draggedItem: IDataParamSource) => {
        const draggedItemName = decodeMetricName(draggedItem.name)
        const params = this.state.dataParams
        const keys = Object.keys(params)
        // 判断该字段是否已经被拖拽进了维度、指标等任意地方，已经被拖进去了就不能再切换分类型和数值型了
        let isChoosed = false
        keys.forEach((key) => {
          if (params[key] && Array.isArray(params[key].items)) {
            params[key].items.forEach((item) => {
              if (decodeMetricName(item.name) === draggedItemName) isChoosed = true
            })
          }
        })

        if (isChoosed) return message.warning('该字段已被使用，无法切换分类型/数值型')

        // 如果该字段没被使用，再执行下面的逻辑
        let selectedView = this.props.selectedView;
        const propName = decodeMetricName(draggedItem.name);
        draggedItem.modelType = 'category';
        draggedItem.name = propName;
        // selectedView.id小于等于0或者为null，就是虚拟view
        if (selectedView && selectedView.id) {
          // 实体view，调用更新view的接口
          if (typeof selectedView.model !== 'object') selectedView.model = JSON.parse(selectedView.model)
          selectedView.model[propName] = draggedItem;
          this.props.onBeofreDropColunm(selectedView, () => {})
        } else {
          // 虚拟view，不调用更新view的接口，直接更新widget的view
          const tempView = this.props.view
          if (typeof tempView.model !== 'object') tempView.model = JSON.parse(tempView.model)
          // 从value变为category
          tempView.model[propName].modelType = 'category'
          this.changeValueCategory = true
          this.props.onSetView(tempView)
          // 取消掉拖拽时的样式
          this.setState({dragged: null})
        }
      }
    }

    const selectedCharts = mode === 'pivot'
      ? getPivotModeSelectedCharts(metrics.items)
      : [chartModeSelectedChart]
    const computedAddFrom = computed.map((c) => ({...c, from: 'computed'}))
    const originalWidgetPropsAddFrom = originalComputed ? originalComputed.map((c) => ({...c, from: 'originalComputed'})) : []
    const combineComputedFields = originalComputed
    ? [...computedAddFrom, ...originalWidgetPropsAddFrom]
    : [...computedAddFrom]

    // combineComputedFields.forEach((compute) => {
    //   if (compute.visualType === 'number') {
    //     values.push(compute)
    //   } else if (compute.visualType === 'string') {
    //     categories.push(compute)
    //   }
    // })

    // widget编辑页的左、中两栏
    return (
      <div className={styles.operatingPanel}>
        {/* 最左栏 */}
        <div className={styles.model} style={{display: isFold ? 'none' : ''}}>
          {/* view选择下拉框 */}
          <div className={styles.viewSelect}>
            {
              Object.keys(view).length > 0 ?
              <Select
                size="small"
                value={view.name}
                disabled
              >
              </Select>
              :
              <Select
                size="small"
                placeholder="选择一个View"
                showSearch
                dropdownMatchSelectWidth={false}
                value={selectedView && selectedView.name}
                onChange={this.viewSelect}
                filterOption={this.filterView}
              >
                {(views || []).map(({ id, name }) => <Option key={name} value={name}>{name}</Option>)}
              </Select>
            }
            {/* <Dropdown overlay={coustomFieldSelectMenu} trigger={['click']} placement="bottomRight">
              <Icon type="plus" />
            </Dropdown> */}
          </div>
          {/* 分类型 */}
          <div className={styles.columnContainer}>
            <div className={styles.title}>
              <h4>分类型</h4>
              {
                multiDrag && (
                  <Checkbox
                    checked={categoryDragItems.length && categoryDragItems.every((item) => item.checked)}
                    onChange={this.checkAllDragItem('category')}
                  />
                )
              }
            </div>
            <ul className={`${styles.columnList} ${styles.categories}`}>
              {categoryDragItems.map((item) => {
                const { name, title, visualType, checked, ...rest } = item
                const data = { name, title, visualType, ...rest }
                return (
                  <li
                    className={`${title === 'computedField' ? styles.computed : ''}`}
                    key={name}
                    onDragStart={this.dragStart(data)}
                    onDragEnd={this.dragEnd}
                    draggable={item.name !== "指标名称"}
                  >
                    <i className={`iconfont ${this.getDragItemIconClass(visualType)}`} />
                    <p>{name}</p>
                    {title === 'computedField' ? this.bootstrapMorePanel(data) : null}
                    {
                      multiDrag && (
                        <Checkbox
                          checked={checked}
                          onChange={this.checkDragItem('category', name)}
                        />
                      )
                    }
                  </li>
                )
              })}
            </ul>
            <DropArea {...valueAreaProps}/>
          </div>
          {/* 数值型 */}
          <div className={styles.columnContainer}>
            <div className={styles.title}>
              <h4>数值型</h4>
              {
                multiDrag && (
                  <Checkbox
                    checked={valueDragItems.length && valueDragItems.every((item) => item.checked)}
                    onChange={this.checkAllDragItem('value')}
                  />
                )
              }
            </div>
            <ul className={`${styles.columnList} ${styles.values}`}>
              {valueDragItems.map((item) => {
                const { name, title, visualType, checked, ...rest } = item
                const data = { name, title, visualType, ...rest }
                return (
                  <li
                    className={`${title === 'computedField' ? styles.computed : ''}`}
                    key={name}
                    onDragStart={this.dragStart({...data, name: encodeMetricName(name), agg: 'sum'})}
                    onDragEnd={this.dragEnd}
                    draggable
                  >
                    <i className={`iconfont ${this.getDragItemIconClass(visualType)}`} />
                    <p>{name}</p>
                    {title === 'computedField' ? this.bootstrapMorePanel(data) : null}
                    {
                      multiDrag && (
                        <Checkbox
                          checked={checked}
                          onChange={this.checkDragItem('value', name)}
                        />
                      )
                    }
                  </li>
                )
              })}
            </ul>
            <DropArea {...categoryAreaProps}/>
          </div>
        </div>
        {/* 中间栏 */}
        <div className={styles.config} style={{display: isFold ? 'none' : ''}}>
          <div className={styles.mode}>
            <RadioGroup
              size="small"
              className={styles.radio}
              value={mode}
              onChange={this.changeMode}
            >
              <RadioButton
                className={classnames({
                  [styles.selected]: mode !== 'pivot'
                })}
                value="pivot"
              >
                透视驱动
              </RadioButton>
              <RadioButton
                className={classnames({
                  [styles.selected]: mode !== 'chart'
                })}
                value="chart"
              >
                图表驱动
              </RadioButton>
            </RadioGroup>
          </div>


          {/* 各类图表的图标 */}
          <div className={styles.charts}>
            {/* currentWidgetlibs是指在“透视驱动”或者“图表驱动”下有哪些图表类型 */}
            {/* ChartIndicator是增加了tooltip之后的图标 */}
            {currentWidgetlibs.map((c) => (
              <ChartIndicator
                key={c.id}
                chartInfo={c}
                dimetionsCount={dimetionsCount}
                metricsCount={metricsCount}
                selectedCharts={selectedCharts}
                onSelect={this.chartSelect}
              />
            ))}
          </div>
          {
            queryMode === WorkbenchQueryMode.Manually && (
              // widget编辑页面里，中间的配置栏，手动查询时会显示的查询按钮
              <div className={styles.manualQuery} onClick={this.forceSetWidgetProps}>
                <Icon type="caret-right" />查询
              </div>
            )
          }
          <div className={styles.params}>
            <ul className={styles.paramsTab}>{tabs}</ul>
            {tabPane}
          </div>
        </div>
        <div className={styles.toggleContainer}><div className={styles.toggle} onClick={onChangeIsFold}><Icon type={isFold ? "double-right" : "double-left"} /></div></div>
        <Modal
          visible={colorModalVisible}
          onCancel={this.cancelColorModal}
          afterClose={this.afterColorModalClose}
          footer={null}
        >
          <ColorSettingForm
            mode={mode}
            list={distinctColumnValues}
            loading={columnValueLoading}
            metrics={metrics.items}
            config={colorSettingConfig}
            onSave={this.confirmColorModal}
            onCancel={this.cancelColorModal}
            ref={(f) => this.colorSettingForm = f}
          />
        </Modal>
        <Modal
          title="作用于"
          wrapClassName="ant-modal-small"
          visible={actOnModalVisible}
          onCancel={this.cancelActOnModal}
          afterClose={this.afterActOnModalClose}
          footer={null}
        >
          <ActOnSettingForm
            list={actOnModalList}
            config={actOnSettingConfig}
            onSave={this.confirmActOnModal}
            onCancel={this.cancelActOnModal}
            ref={(f) => this.actOnSettingForm = f}
          />
        </Modal>
        <Modal
          title="筛选配置"
          visible={filterModalVisible}
          onCancel={this.cancelFilterModal}
          afterClose={this.afterFilterModalClose}
          footer={null}
        >
          <FilterSettingForm
            item={modalCachedData}
            model={widgetPropsModel}
            list={distinctColumnValues}
            config={filterSettingConfig}
            onSave={this.confirmFilterModal}
            onCancel={this.cancelFilterModal}
            ref={(f) => this.filterSettingForm = f}
          />
        </Modal>
        <ControlConfig
          currentControls={controls}
          view={selectedView}
          visible={controlConfigVisible}
          onSave={this.saveControls}
          onCancel={this.closeControlConfig}
        />
        {/* <Modal
          title="控制器配置"
          wrapClassName="ant-modal-large"
          visible={variableConfigModalVisible}
          onCancel={this.hideVariableConfigTable}
          afterClose={this.resetVariableConfigForm}
          footer={false}
          maskClosable={false}
        >
          <VariableConfigForm
            queryInfo={queryInfo}
            control={variableConfigControl}
            onSave={this.saveControl}
            onClose={this.hideVariableConfigTable}
            wrappedComponentRef={this.refHandlers.variableConfigForm}
          />
        </Modal> */}
        {!currentEditingItem ? null : [(
          <FieldConfigModal
            key="fieldConfigModal"
            queryInfo={queryInfo}
            visible={fieldModalVisible}
            fieldConfig={currentEditingItem.field}
            onSave={this.saveFieldConfig}
            onCancel={this.cancelFieldConfig}
            currentEditingItemName={this.state.currentEditingItem ? this.state.currentEditingItem.name : ''}
          />
        ), (
          <FormatConfigModal
            key="formatConfigModal"
            visible={formatModalVisible}
            visualType={currentEditingItem.visualType}
            formatConfig={currentEditingItem.format}
            onSave={this.saveFormatConfig}
            onCancel={this.cancelFormatConfig}
          />
        ), (
          <SortConfigModal
            key="sortConfigModal"
            visible={sortModalVisible}
            config={currentEditingItem.sort}
            list={distinctColumnValues}
            onSave={this.saveSortConfig}
            onCancel={this.cancelSortConfig}
          />
        )
        ]}
        <Modal
          title="计算字段配置"
          wrapClassName="ant-modal-large"
          visible={computedConfigModalVisible}
          onCancel={this.hideComputedConfigModal}
          closable={false}
          footer={false}
          maskClosable={false}
        >
          <ComputedConfigForm
            queryInfo={queryInfo}
            categories={categoryDragItems}
            onSave={this.saveComputedConfig}
            onClose={this.hideComputedConfigModal}
            selectedComputed={selectedComputed}
            wrappedComponentRef={this.refHandlers.computedConfigForm}
          />
        </Modal>
      </div>
    )
  }
}

export default OperatingPanel
