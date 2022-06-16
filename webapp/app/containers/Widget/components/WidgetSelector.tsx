import * as React from 'react'
import * as classnames from 'classnames'
import { iconMapping } from './util'
import { message } from 'antd';
import { MAX_LAYER_COUNT } from 'app/globalConstants'

import { Icon, Row, Col, Checkbox, Pagination, Input } from 'antd'
const Search = Input.Search
const styles = require('../Widget.less')

interface IWidgetSelectorProps {
  className: any
  widgets: any[]
  layers: any[]
  multiple: boolean
  widgetsSelected: any[]
  onWidgetsSelect: (widgets) => void
}

interface IWidgetSelectorStates {
  screenWidth: number
  kwWidget: string
  pageSize: number
  currentPage: number
  showSelected: false
}

export class WidgetSelector extends React.Component<IWidgetSelectorProps, IWidgetSelectorStates> {
  constructor (props) {
    super(props)
    this.state = {
      screenWidth: 0,
      kwWidget: '',
      pageSize: 24,
      currentPage: 1,
      showSelected: false
    }
  }

  public componentWillMount () {
    this.getScreenWidth()
    window.addEventListener('resize', this.getScreenWidth, false)
  }

  public componentWillUnmount () {
    window.removeEventListener('resize', this.getScreenWidth, false)
  }

  private getScreenWidth = () => {
    this.setState({ screenWidth: document.documentElement.clientWidth })
  }

  private onChange = (page) => {
    this.setState({
      currentPage: page
    })
  }

  private onSearchWidgetItem = (value) => {
    this.setState({
      kwWidget: value
    })
  }

  private getWidgets () {
    const {
      widgets,
      widgetsSelected
    } = this.props

    const {
      kwWidget,
      showSelected
    } = this.state

    if (!Array.isArray(widgets)) {
      return []
    }

    const reg = new RegExp(kwWidget, 'i')

    const filteredWidgets = widgets.filter((w) => {
      let valid = true
      if (showSelected) {
        valid = valid && widgetsSelected.findIndex((ws) => ws.id === w.id) >= 0
      }
      if (valid && kwWidget) {
        valid = valid && reg.test(w.name)
      }
      return valid
    })

    return filteredWidgets
  }

  private onShowSizeChange = (current, pageSize) => {
    this.setState({
      currentPage: current,
      pageSize
    })
  }

  private onWidgetSelect = (w) => (e) => {
    const tempLay = this.props.layers
    const {
      multiple,
      onWidgetsSelect
    } = this.props

    let newWidgetsSelected

    if (!multiple) {
      // 如果勾选框不是多选
      newWidgetsSelected = [w]
    } else {
      const {
        widgetsSelected
      } = this.props
      // 看当前点击的这个widget是否已经被选中了
      const idx = widgetsSelected.findIndex((ws) => ws.id === w.id)
      newWidgetsSelected = [...widgetsSelected]
      // 如果idx是-1，说明当前widget还未被选中
      // 再选多一个widget时，进行判断，当前已选widget数+总图层数，是否大于等于MAX_LAYER_COUNT了
      if (idx < 0 && tempLay && newWidgetsSelected && tempLay.length + newWidgetsSelected.length >= MAX_LAYER_COUNT) return message.warning(`当前最多只支持添加${MAX_LAYER_COUNT}个图层！`, 5);
      idx < 0 ? newWidgetsSelected.push(w) : newWidgetsSelected.splice(idx, 1)
      if (widgetsSelected.length <= 0 && this.state.showSelected) {
        this.setState({ showSelected: false })
      }
    }

    onWidgetsSelect(newWidgetsSelected)
  }

  private onShowTypeChange = (e) => {
    this.setState({
      showSelected: e.target.checked,
      currentPage: 1
    })
  }

  public render () {
    const {
      className,
      widgetsSelected
    } = this.props

    const {
      screenWidth,
      pageSize,
      currentPage,
      showSelected
    } = this.state

    const widgetsFiltered = this.getWidgets()

    const startCol = (currentPage - 1) * pageSize
    const endCol = Math.min(currentPage * pageSize, widgetsFiltered.length)
    const widgetsCurrent = widgetsFiltered.slice(startCol, endCol)

    const widgetsList = widgetsCurrent.map((w, idx) => {
      const widgetType = w.type
      const widgetClassName = classnames({
        [styles.widget]: true,
        [styles.selector]: true,
        [styles.selected]: w.id === 1
      })
      const checkmark = widgetsSelected.findIndex((ws) => ws.id === w.id) >= 0
        ? (
          <div className={styles.checkmark}>
            <Icon type="check" />
          </div>
        )
        : ''

      return (
        <Col lg={8} md={12} sm={24} key={w.id} onClick={this.onWidgetSelect(w)}>
          <div className={widgetClassName} style={{overflowY: 'auto'}}>
            <h3 className={styles.title}>{w.name}</h3>
            <p className={styles.content}>{w.description}</p>
            <i className={`${styles.pic} iconfont ${iconMapping[widgetType]}`} />
            {checkmark}
          </div>
        </Col>
      )
    })

    return (
      <div className={className}>
        <Row gutter={20} className={`${styles.searchRow}`}>
          <Col span={17}>
            <Checkbox checked={showSelected} onChange={this.onShowTypeChange}>已选</Checkbox>
          </Col>
          <Col span={7}>
            <Search
              placeholder="Widget 名称"
              onSearch={this.onSearchWidgetItem}
            />
          </Col>
        </Row>
        <Row gutter={20}>
          {widgetsList}
        </Row>
        <Row>
          <Pagination
            simple={screenWidth < 768 || screenWidth === 768}
            className={styles.paginationPosition}
            showSizeChanger
            onShowSizeChange={this.onShowSizeChange}
            onChange={this.onChange}
            total={widgetsFiltered.length}
            defaultPageSize={24}
            pageSizeOptions={['24', '48', '72', '96']}
            current={currentPage}
          />
        </Row>
      </div>
    )
  }
}

export default WidgetSelector
