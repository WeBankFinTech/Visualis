import React from 'react'
import classnames from 'classnames'
import produce from 'immer'
import set from 'lodash/set'
import { uuid } from 'utils/util'
import { fontWeightOptions, fontStyleOptions, fontFamilyOptions, fontSizeOptions } from '../constants'
import { defaultConditionStyle, AvailableTableConditionStyleTypes } from './constants'
import { TOTAL_COLUMN_WIDTH } from 'app/globalConstants'
import { getColumnIconByType } from './util'
import { ITableColumnConfig, ITableConditionStyle } from './types'
import ColorPicker from 'components/ColorPicker'
import ConditionStyleConfigModal from './ConditionStyleConfigModal'

import { Row, Col, Tooltip, Select, Button, Radio, Checkbox, Table, Modal, InputNumber } from 'antd'
const RadioGroup = Radio.Group
const RadioButton = Radio.Button

import styles from './styles.less'
import stylesConfig from '../styles.less'

interface IColumnStyleConfigProps {
  visible: boolean
  config: ITableColumnConfig[]
  onCancel: () => void
  onSave: (config: ITableColumnConfig[]) => void
}

interface IColumnStyleConfigStates {
  localConfig: ITableColumnConfig[]
  selectedColumnName: string
  conditionStyleConfigModalVisible: boolean
  currentConditionStyle: ITableConditionStyle
  checkedColumns: []
}

export class ColumnStyleConfig extends React.PureComponent<IColumnStyleConfigProps, IColumnStyleConfigStates> {

  public constructor (props: IColumnStyleConfigProps) {
    super(props)
    // config是从tableSection里面传进来的validColumnConfig
    const localConfig = props.config
    this.state = {
      localConfig,
      selectedColumnName: localConfig.length > 0 ? localConfig[0].columnName : '',
      conditionStyleConfigModalVisible: false,
      currentConditionStyle: null,
      // 当前选中的列
      checkedColumns: []
    }
  }

  public componentWillReceiveProps (nextProps: IColumnStyleConfigProps) {
    if (nextProps.config === this.props.config) { return }
    const localConfig = nextProps.config
    this.setState({
      localConfig,
      selectedColumnName: localConfig.length > 0 ? localConfig[0].columnName : '',
      conditionStyleConfigModalVisible: false,
      currentConditionStyle: null
    })
  }

  private renderColumn (item: ITableColumnConfig) {
    const { selectedColumnName } = this.state
    const { columnName, alias, visualType } = item
    const displayName = alias || columnName
    const itemCls = classnames({
      [styles.selected]: selectedColumnName === columnName
    })
    return (
      <li className={itemCls} key={columnName} onClick={this.selectColumn(columnName)}>
        <Checkbox style={{marginRight: '7px', paddingLeft: '0', position: 'relative', top: '1px'}} onChange={this.checkColumn(columnName)}></Checkbox>
        <i className={`iconfont ${getColumnIconByType(visualType)}`} />
        <Tooltip title={displayName} mouseEnterDelay={0.8}>
          <label>{displayName}</label>
        </Tooltip>
      </li>
    )
  }

  private checkColumn = (columnName: string) => (e) => {
    const { checkedColumns } = this.state
    if (e.target.checked) {
      // 把这一列加进数组中
      checkedColumns.push(columnName)
      this.setState({ checkedColumns })
    } else {
      // 把这一列从数组中删除
      checkedColumns.splice(checkedColumns.indexOf(columnName), 1)
    }
  }

  private selectColumn = (columnName: string) => () => {
    this.setState({
      selectedColumnName: columnName
    })
  }

  private propChange = (
    propPath: Exclude<keyof(ITableColumnConfig), 'style'> | ['style', keyof ITableColumnConfig['style']]
  ) => (e) => {
    // 如果是配置 列宽 时，必须要是输入的数字才有意义
    if (propPath === 'width' && typeof e !== 'number') return e = null

    const value = e.target ? (e.target.value || e.target.checked) : e
    const { localConfig, selectedColumnName, checkedColumns } = this.state
    let nextLocalConfig = []
    // 如果当前列未勾选，则只更改当前列
    // 开启列排序，暂不需要批量修改
    if (!checkedColumns.includes(selectedColumnName) || propPath === 'sort') {
      nextLocalConfig = produce(localConfig, (draft) => {
        const selectedColumn = draft.find(({ columnName }) => columnName === selectedColumnName)
        set(selectedColumn, propPath, value)
        // 如果是更改了列宽之后，要改这个widthChanged值为true
        if (propPath === 'width') set(selectedColumn, 'widthChanged', true)
        return draft
      })
    } else {
      // 如果当前列已勾选，则更改样式时要更改所有的勾选的列
      nextLocalConfig = produce(localConfig, (draft) => {
        checkedColumns.forEach((name) => {
          const selectedColumn = draft.find(({ columnName }) => columnName === name)
          set(selectedColumn, propPath, value)
          // 如果是更改了列宽之后，要改这个widthChanged值为true
          if (propPath === 'width') set(selectedColumn, 'widthChanged', true)
        })
        return draft
      })
    }
    this.setState({
      localConfig: nextLocalConfig
    })
  }

  private cancel = () => {
    this.props.onCancel()
  }

  // 点击 表格数据设置 弹框下方的保存按钮
  private save = () => {
    // 调用TableSection传来的onSave
    this.props.onSave(this.state.localConfig)
  }

  private columns = [{
    title: '',
    dataIndex: 'idx',
    width: 30,
    render: (_, __, index) => (index + 1)
  }, {
    title: '样式类型',
    dataIndex: 'type',
    width: 50,
    render: (type) => AvailableTableConditionStyleTypes[type]
  }, {
    title: '操作',
    dataIndex: 'operation',
    width: 60,
    render: (_, record) => (
      <div className={styles.btns}>
        <Button onClick={this.editConditionStyle(record)} icon="edit" shape="circle" size="small" />
        <Button onClick={this.deleteConditionStyle(record.key)} icon="delete" shape="circle" size="small" />
      </div>
    )
  }]

  private addConditionStyle = () => {
    this.setState({
      conditionStyleConfigModalVisible: true,
      currentConditionStyle: {
        ...defaultConditionStyle
      }
    })
  }

  private editConditionStyle = (record) => () => {
    this.setState({
      currentConditionStyle: record,
      conditionStyleConfigModalVisible: true
    })
  }

  private deleteConditionStyle = (deletedKey: string) => () => {
    const { localConfig, selectedColumnName } = this.state
    const nextLocalConfig = produce(localConfig, (draft) => {
      const selectedColumn = draft.find(({ columnName }) => columnName === selectedColumnName)
      const idx = selectedColumn.conditionStyles.findIndex(({ key }) => key === deletedKey)
      selectedColumn.conditionStyles.splice(idx, 1)
    })
    this.setState({ localConfig: nextLocalConfig })
  }

  private closeConditionStyleConfig = () => {
    this.setState({
      conditionStyleConfigModalVisible: false,
      currentConditionStyle: null
    })
  }

  private saveConditionStyleConfig = (conditionStyle: ITableConditionStyle) => {
    const { localConfig, selectedColumnName } = this.state
    const nextLocalConfig = produce(localConfig, (draft) => {
      const selectedColumn = draft.find(({ columnName }) => columnName === selectedColumnName)
      if (conditionStyle.key) {
        const idx = selectedColumn.conditionStyles.findIndex(({ key }) => key === conditionStyle.key)
        selectedColumn.conditionStyles.splice(idx, 1, conditionStyle)
      } else {
        selectedColumn.conditionStyles.push({ ...conditionStyle, key: uuid(5) })
      }
    })
    this.setState({
      localConfig: nextLocalConfig,
      conditionStyleConfigModalVisible: false,
      currentConditionStyle: null
    })
  }

  private modalFooter = [(
    <Button
      key="cancel"
      size="large"
      onClick={this.cancel}
    >
      取 消
    </Button>
  ), (
    <Button
      key="submit"
      size="large"
      type="primary"
      onClick={this.save}
    >
      保 存
    </Button>
  )]

  public render () {
    const { visible } = this.props
    const {
      localConfig, selectedColumnName,
      conditionStyleConfigModalVisible, currentConditionStyle } = this.state
    if (localConfig.length <= 0) {
      return (<div />)
    }
    let { style, visualType, sort, conditionStyles, showAsPercent, width } = localConfig.find((c) => c.columnName === selectedColumnName)
    const { fontSize, fontFamily, fontWeight, fontColor, fontStyle, backgroundColor, justifyContent } = style
    // 在配置框中，只显示整数的width值，所以这里的取整只是显示上取整，实际存的width值可能是有小数的
    if (typeof width === 'number') width = Math.ceil(width)

    return (
      <Modal
        title="表格数据设置"
        wrapClassName="ant-modal-large"
        maskClosable={false}
        footer={this.modalFooter}
        visible={visible}
        onCancel={this.cancel}
        onOk={this.save}
      >
        <div className={styles.columnStyleConfig}>
          <div className={styles.left}>
            <div className={styles.title}>
              <h2>字段列表</h2>
            </div>
            <div className={styles.list}>
              <ul>
                {localConfig.map((item) => this.renderColumn(item))}
              </ul>
            </div>
          </div>
          <div className={styles.right}>
              <div className={styles.title}><h2>排序与过滤</h2></div>
              <div className={stylesConfig.rows}>
                <Row gutter={8} type="flex" align="middle" className={stylesConfig.rowBlock}>
                  <Col span={12}>
                    <Checkbox checked={sort} onChange={this.propChange('sort')}>开启列排序</Checkbox>
                  </Col>
                </Row>
              </div>
              <div className={styles.title}><h2>基础</h2></div>
              <div className={stylesConfig.rows}>
                {
                  visualType !== 'number' ? null :
                  (
                    <Row gutter={8} type="flex" align="middle" className={stylesConfig.rowBlock}>
                      <Col span={12}>
                        <Checkbox checked={showAsPercent} onChange={this.propChange('showAsPercent')}>数值以百分数的形式显示</Checkbox>
                      </Col>
                    </Row>
                  )
                }
                <Row gutter={8} type="flex" align="middle" className={stylesConfig.rowBlock}>
                  <Col span={4}>列宽</Col>
                  <Col span={10}>
                      <InputNumber
                        placeholder="20-2000"
                        min={20}
                        max={2000}
                        className={styles.blockElm}
                        value={width}
                        onChange={this.propChange('width')}
                      />
                  </Col>
                </Row>
                <Row gutter={8} type="flex" align="middle" className={stylesConfig.rowBlock}>
                  <Col span={4}>背景色</Col>
                  <Col span={2}>
                    <ColorPicker
                      className={stylesConfig.color}
                      value={backgroundColor}
                      onChange={this.propChange(['style', 'backgroundColor'])}
                    />
                  </Col>
                </Row>
                <Row gutter={8} type="flex" align="middle" className={stylesConfig.rowBlock}>
                  <Col span={4}>对齐</Col>
                  <Col span={20}>
                    <RadioGroup size="small" value={justifyContent} onChange={this.propChange(['style', 'justifyContent'])}>
                      <RadioButton value="flex-start">左对齐</RadioButton>
                      <RadioButton value="center">居中</RadioButton>
                      <RadioButton value="flex-end">右对齐</RadioButton>
                    </RadioGroup>
                  </Col>
                </Row>
                <Row gutter={8} type="flex" align="middle" className={stylesConfig.rowBlock}>
                  <Col span={4}>字体</Col>
                  <Col span={12}>
                    <Select
                      size="small"
                      className={stylesConfig.colControl}
                      placeholder="字体"
                      value={fontFamily}
                      onChange={this.propChange(['style', 'fontFamily'])}
                    >
                      {fontFamilyOptions}
                    </Select>
                  </Col>
                  <Col span={5}>
                    <Select
                      size="small"
                      className={stylesConfig.colControl}
                      placeholder="文字大小"
                      value={fontSize}
                      onChange={this.propChange(['style', 'fontSize'])}
                    >
                      {fontSizeOptions}
                    </Select>
                  </Col>
                  <Col span={3}>
                    <ColorPicker
                      className={stylesConfig.color}
                      value={fontColor}
                      onChange={this.propChange(['style', 'fontColor'])}
                    />
                  </Col>
                </Row>
                <Row gutter={8} type="flex" align="middle" className={stylesConfig.rowBlock}>
                  <Col span={4}>样式</Col>
                  <Col span={6}>
                    <Select
                      size="small"
                      className={stylesConfig.colControl}
                      value={fontStyle}
                      onChange={this.propChange(['style', 'fontStyle'])}
                    >
                      {fontStyleOptions}
                    </Select>
                  </Col>
                  <Col span={13}>
                    <Select
                      size="small"
                      className={stylesConfig.colControl}
                      value={fontWeight}
                      onChange={this.propChange(['style', 'fontWeight'])}
                    >
                      {fontWeightOptions}
                    </Select>
                  </Col>
                </Row>
              </div>
              <div className={styles.title}>
                <h2>条件样式</h2>
                <Button type="primary" onClick={this.addConditionStyle} shape="circle" icon="plus" size="small" />
              </div>
              <div className={styles.table}>
                <Table
                  bordered={true}
                  pagination={false}
                  columns={this.columns}
                  dataSource={conditionStyles}
                />
              </div>
            </div>
        </div>
        <ConditionStyleConfigModal
          visible={conditionStyleConfigModalVisible}
          visualType={visualType}
          style={currentConditionStyle}
          onCancel={this.closeConditionStyleConfig}
          onSave={this.saveConditionStyleConfig}
        />
      </Modal>
    )
  }
}

export default ColumnStyleConfig
