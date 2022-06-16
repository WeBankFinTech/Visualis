import * as React from 'react'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'

import { compose } from 'redux'
import reducer from './reducer'
import reducerWidget from 'containers/Widget/reducer'
import saga from './sagas'
import sagaWidget from 'containers/Widget/sagas'
import reducerView from 'containers/View/reducer'
import sagaView from 'containers/View/sagas'
import injectReducer from 'utils/injectReducer'
import injectSaga from 'utils/injectSaga'

import { makeSelectWidgets } from 'containers/Widget/selectors'
import { makeSelectViews, makeSelectFormedViews } from 'containers/View/selectors'
import {
  makeSelectCurrentDisplay,
  makeSelectCurrentSlide,
  makeSelectDisplays,
  makeSelectCurrentLayers,
  makeSelectCurrentLayersInfo,
  makeSelectCurrentProject
} from './selectors'

import config, { env } from '../../globalConfig'

import { hideNavigator } from 'containers/App/actions'
import { ViewActions } from 'containers/View/actions'
const { loadViewDataFromVizItem } = ViewActions // @TODO global filter in Display Preview
import DisplayActions from './actions'

const styles = require('./Display.less')
import { Spin } from 'antd'
import { RenderType } from 'containers/Widget/components/Widget'
import { IQueryConditions, IDataRequestParams } from 'containers/Dashboard/Grid'
import { IFormedViews } from 'containers/View/types'
import { statistic } from 'utils/statistic/statistic.dv'
import {IProject} from 'containers/Projects'
interface IPreviewProps {
  params: any
  widgets: any[]
  views: any[]
  formedViews: IFormedViews
  currentDisplay: any
  currentSlide: any
  currentLayers: any[]
  currentLayersInfo: {
    [key: string]: {
      datasource: {
        pageNo: number
        pageSize: number
        resultList: any[]
        totalCount: number
      }
      loading: boolean
      queryConditions: IQueryConditions
      interactId: string
      rendered: boolean
      renderType: RenderType
    }
  }
  onHideNavigator: () => void
  onLoadDisplayDetail: (projectId: number, displayId: number) => void
  onLoadViewDataFromVizItem: (
    renderType: RenderType,
    layerItemId: number,
    viewId: number,
    requestParams: IDataRequestParams
  ) => void
  onMonitoredSyncDataAction: () => any
  onMonitoredSearchDataAction: () => any
  onMonitoredLinkageDataAction: () => any
  currentProject: IProject
  onloadProjectDetail: (pid) => any
}

interface IPreviewStates {
  scale: [number, number],
  spinning: boolean
}

export class Preview extends React.Component<IPreviewProps, IPreviewStates> {


  public constructor (props) {
    super(props)
    this.state = {
      scale: [1, 1],
      spinning: true
    }
  }

  private picOnload() {
    this.setState({
      spinning: false
    })
  }

  public componentWillMount () {
    const {
      params,
      onLoadDisplayDetail,
      onloadProjectDetail
    } = this.props
    const projectId = +params.pid
    const displayId = +params.displayId
    onLoadDisplayDetail(projectId, displayId)
    onloadProjectDetail(projectId)
  }

  public componentDidMount () {
    this.props.onHideNavigator()
    window.addEventListener('beforeunload', function (event) {
      statistic.setDurations({
        end_time: statistic.getCurrentDateTime()
      }, (data) => {
        statistic.setPrevDurationRecord(data, () => {
          statistic.setDurations({
            start_time: statistic.getCurrentDateTime(),
            end_time: ''
          })
        })
      })
    }, false)
    this.statisticFirstVisit = this.__once__(statistic.setOperations)
    statistic.setDurations({
      start_time: statistic.getCurrentDateTime()
    })
    statistic.startClock()
    window.addEventListener('mousemove', this.statisticTimeFuc, false)
    window.addEventListener('visibilitychange', this.onVisibilityChanged, false)
    window.addEventListener('keydown', this.statisticTimeFuc, false)
  }

  private statisticTimeFuc = () => {
    statistic.isTimeout()
  }

  public componentWillReceiveProps (nextProps: IPreviewProps) {
    const { currentSlide } = nextProps
    const { scale } = this.state
    const [scaleWidth, scaleHeight] = scale
    const { params: {pid, displayId}, currentDisplay, currentProject} = this.props
    if (this.props.currentSlide) {
      this.statisticFirstVisit({
        org_id: currentProject.orgId,
        project_name: currentProject.name,
        project_id: pid,
        viz_type: 'display',
        viz_id: displayId,
        viz_name: currentDisplay['name'],
        create_time:  statistic.getCurrentDateTime()
      }, (data) => {
        const visitRecord = {
          ...data,
          action: 'visit'
        }
        statistic.sendOperation(visitRecord)
      })
    }

    if (currentSlide && this.props.currentSlide !== currentSlide) {
      const { slideParams } = JSON.parse(currentSlide.config)
      const { scaleMode, width, height } = slideParams
      const { clientHeight, clientWidth } = document.body
      let nextScaleHeight = 1
      let nextScaleWidth = 1
      switch (scaleMode) {
        case 'scaleHeight':
          nextScaleWidth = nextScaleHeight = clientHeight / height
          break
        case 'scaleWidth':
          nextScaleHeight = nextScaleWidth = clientWidth / width
          break
        case 'scaleFull':
          nextScaleHeight = clientHeight / height
          nextScaleWidth = clientWidth / width
      }
      if (scaleHeight !== nextScaleHeight || scaleWidth !== nextScaleWidth) {
        this.setState({
          scale: [nextScaleWidth, nextScaleHeight]
        })
      }

    }
  }

  private __once__ (fn) {
    let tag = true
    return (...args) => {
      if (tag) {
        tag = !tag
        return fn.apply(this, args)
      } else {
        return void 0
      }
    }
  }

  private statisticFirstVisit: any

  private onVisibilityChanged (event) {
    const flag = event.target.webkitHidden
    if (flag) {
      statistic.setDurations({
        end_time: statistic.getCurrentDateTime()
      }, (data) => {
        statistic.sendDuration([data]).then((res) => {
          statistic.resetClock()
        })
      })
    } else {
      statistic.setDurations({
        start_time: statistic.getCurrentDateTime()
      }, (data) => {
        statistic.startClock()
      })
    }
  }

  public componentWillUnmount () {
    statistic.setDurations({
      end_time: statistic.getCurrentDateTime()
    }, (data) => {
      statistic.sendDuration([data])
    })
    window.removeEventListener('mousemove', this.statisticTimeFuc, false)
    window.removeEventListener('keydown', this.statisticTimeFuc, false)
    window.removeEventListener('visibilitychange', this.onVisibilityChanged, false)
    statistic.resetClock()
  }

// 备注：preview接口由前端发起的说明是开发中心所以加入labelsRoute=dev
// 如果是Display或者DashBoard在工作流执行时，发起的执行，是访问后台服务接口，
// 在请求时，由相关接口可以就可以带上环境标签。所以前端侧固定为Dev环境。
  public render () {
    const {spinning} = this.state;
    const {
      params } = this.props
    const displayId = +params.displayId
    const dashboardId = +params.dashboardId
    let host = `${config[env].host}`
    if (displayId) {
      host += `/displays/${displayId}/preview?labelsRoute=dev`
    } else {
      host += `/dashboard/${dashboardId}/preview?labelsRoute=dev`
    }
    return (
      <div className={styles.preview}>
        <Spin className={styles.preivewLoading} spinning={spinning} size="large"/>
        <div className={styles.previewImgWrapper}>
          <img src={host} onLoad={() => {this.picOnload()}}/>
        </div>
      </div>
    )
  }
}

const mapStateToProps = createStructuredSelector({
  widgets: makeSelectWidgets(),
  views: makeSelectViews(),
  formedViews: makeSelectFormedViews(),
  currentDisplay: makeSelectCurrentDisplay(),
  currentSlide: makeSelectCurrentSlide(),
  displays: makeSelectDisplays(),
  currentLayers: makeSelectCurrentLayers(),
  currentLayersInfo: makeSelectCurrentLayersInfo(),
  currentProject: makeSelectCurrentProject()
})

export function mapDispatchToProps (dispatch) {
  return {
    onHideNavigator: () => dispatch(hideNavigator()),
    onLoadDisplayDetail: (projectId, displayId) => dispatch(DisplayActions.loadDisplayDetail(projectId, displayId)),
    onLoadViewDataFromVizItem: (renderType, itemId, viewId, requestParams) => dispatch(loadViewDataFromVizItem(renderType, itemId, viewId, requestParams, 'display')),
    onMonitoredSyncDataAction: () => dispatch(DisplayActions.monitoredSyncDataAction()),
    onMonitoredSearchDataAction: () => dispatch(DisplayActions.monitoredSearchDataAction()),
    onMonitoredLinkageDataAction: () => dispatch(DisplayActions.monitoredLinkageDataAction()),
    onloadProjectDetail: (pid) => dispatch(DisplayActions.loadProjectDetail(pid))
  }
}

const withReducer = injectReducer({ key: 'display', reducer })
const withReducerWidget = injectReducer({ key: 'widget', reducer: reducerWidget })

const withSaga = injectSaga({ key: 'display', saga })
const withSagaWidget = injectSaga({ key: 'widget', saga: sagaWidget })

const withReducerView = injectReducer({ key: 'view', reducer: reducerView })
const withSagaView = injectSaga({ key: 'view', saga: sagaView })


const withConnect = connect<{}, {}, IPreviewProps>(mapStateToProps, mapDispatchToProps)

export default compose(
  withReducer,
  withReducerWidget,
  withReducerView,
  withSaga,
  withSagaWidget,
  withSagaView,
  withConnect)(Preview)
