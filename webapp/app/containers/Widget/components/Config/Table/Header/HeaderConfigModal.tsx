import React from 'react'
import classnames from 'classnames'
import { uuid } from 'utils/util'
import { fontWeightOptions, fontStyleOptions, fontFamilyOptions, fontSizeOptions, DefaultTableCellStyle } from '../constants'
import { ITableHeaderConfig } from './types'

import { Icon, Row, Col, Modal, Input, Button, Radio, Select, Table, message, Tooltip, Checkbox } from 'antd'
const ButtonGroup = Button.Group
const RadioGroup = Radio.Group
const RadioButton = Radio.Button
import { TableRowSelection, ColumnProps } from 'antd/lib/table'

import ColorPicker from 'components/ColorPicker'
import { fromJS } from 'immutable'

import styles from './styles.less'
import stylesConfig from '../styles.less'

interface IHeaderConfigModalProps {
  visible: boolean
  config: ITableHeaderConfig[]
  validColumns: IDataParamSource[]
  onCancel: () => void
  onSave: (config: ITableHeaderConfig[]) => void
}

interface IHeaderConfigModalStates {
  localConfig: ITableHeaderConfig[]
  currentEditingConfig: ITableHeaderConfig
  currentSelectedKeys: string[]
  mapHeader: { [key: string]: ITableHeaderConfig }
  mapHeaderParent: { [key: string]: ITableHeaderConfig }
  styleChangeModalVisible: boolean
  styleChangeDefaultConfig: Object
}

class HeaderConfigModal extends React.PureComponent<IHeaderConfigModalProps, IHeaderConfigModalStates> {

  private headerNameInput = React.createRef<Input>()

  public constructor (props: IHeaderConfigModalProps) {
    super(props)
    let localConfig = fromJS(props.config).toJS()
    const { validColumns } = props
    // 如果是第一次进来的时候，localConfig里的每一个元素的seq属性都是undefined，这时候采用默认的排序
    // 如果是新增了维度或指标，说明loaclConfig里至少有一项是undefined，这时候采用默认的排序
    // 如果是删除了维度或指标，说明有seq属性的字段数量和seq的最大值很可能不同（除非删除最后一项，删除最后一项时保持seq的顺序也没uanxu）
    // 所以，只有当localConfig里的所有项都有seq属性，并且有seq属性的字段数量和seq最大值相等，才采用seq的这个排序
    let seqCount = 0
    let maxSeq = 0
    localConfig.forEach((item) => {
      if (item.seq) seqCount++
      if (item.seq > maxSeq) maxSeq = item.seq
    })
    if (seqCount === localConfig.length && seqCount === maxSeq) {
      // localConfig里的各项按照seq的值来排序
      localConfig.sort(function (x, y) {return x.seq - y.seq})
    } else {
      // 否则清空所有seq值
      localConfig.forEach((item, index) => {
        delete localConfig[index].seq
      })
      // 并且按照validColumns里的顺序进行排序(即原始的按照指标维度拖拽框里的顺序排序)
      const tempArr = []
      validColumns.forEach((item) => {
        for (let i = 0; i < localConfig.length; i++) {
          if (item.name === localConfig[i].headerName) {
            tempArr.push(localConfig[i])
            break
          }
        }
      })
      for (let i = 0; i < localConfig.length; i++) {
        if (localConfig[i].isGroup) {
          tempArr.push(localConfig[i])
        }
      }
      localConfig = tempArr
    }
    // 保存一个初始的localConfig，如果后面改变了顺序但是点击取消按钮，应该还原localConfig的顺序
    if (localConfig && localConfig.length) this.initLocalConfig = JSON.parse(JSON.stringify(localConfig))
    const [mapHeader, mapHeaderParent] = this.getMapHeaderKeyAndConfig(localConfig)
    this.state = {
      localConfig,
      currentEditingConfig: null,
      mapHeader,
      mapHeaderParent,
      currentSelectedKeys: [],
      styleChangeModalVisible: false,
      // 批量修改样式 弹框里的初始数据
      styleChangeDefaultConfig: [{
        style: {
          backgroundColor: "transparent",
          fontColor: "#666",
          fontFamily: "PingFang SC",
          fontSize: "12",
          fontStyle: "normal",
          fontWeight: "normal",
          justifyContent: "flex-start"
        }
      }]
    }
  }

  private initLocalConfig = []

  public componentWillReceiveProps (nextProps: IHeaderConfigModalProps) {
    if (nextProps.config === this.props.config) { return }
    let localConfig = fromJS(nextProps.config).toJS()
    const { validColumns } = nextProps
    // 如果是第一次进来的时候，localConfig里的每一个元素的seq属性都是undefined，这时候采用默认的排序
    // 如果是新增了维度或指标，说明loaclConfig里至少有一项是undefined，这时候采用默认的排序
    // 如果是删除了维度或指标，说明有seq属性的字段数量和seq的最大值很可能不同（除非删除最后一项，删除最后一项时保持seq的顺序也没uanxu）
    // 所以，只有当localConfig里的所有项都有seq属性，并且有seq属性的字段数量和seq最大值相等，才采用seq的这个排序
    let seqCount = 0
    let maxSeq = 0
    localConfig.forEach((item) => {
      if (item.seq) seqCount++
      if (item.seq > maxSeq) maxSeq = item.seq
    })
    if (seqCount === localConfig.length && seqCount === maxSeq) {
      // localConfig里的各项按照seq的值来排序
      localConfig.sort(function (x, y) {return x.seq - y.seq})
    } else {
      // 否则清空所有seq值
      localConfig.forEach((item, index) => {
        delete localConfig[index].seq
      })
      // 并且按照validColumns里的顺序进行排序(即原始的按照指标维度拖拽框里的顺序排序)
      const tempArr = []
      validColumns.forEach((item) => {
        for (let i = 0; i < localConfig.length; i++) {
          if (item.name === localConfig[i].headerName) {
            tempArr.push(localConfig[i])
            break
          }
        }
      })
      for (let i = 0; i < localConfig.length; i++) {
        if (localConfig[i].isGroup) {
          tempArr.push(localConfig[i])
        }
      }
      localConfig = tempArr
    }
    // 点击弹框的保存后，会触发一次compontWillReceiveProps，可能是更新了顺序了，所以这里也要更新initLocalConfig
    if (localConfig && localConfig.length) this.initLocalConfig = JSON.parse(JSON.stringify(localConfig))
    const [mapHeader, mapHeaderParent] = this.getMapHeaderKeyAndConfig(localConfig)
    this.setState({
      localConfig,
      mapHeader,
      mapHeaderParent,
      currentSelectedKeys: [],
      styleChangeModalVisible: false,
      styleChangeDefaultConfig: [{
        style: {
          backgroundColor: "transparent",
          fontColor: "#666",
          fontFamily: "PingFang SC",
          fontSize: "12",
          fontStyle: "normal",
          fontWeight: "normal",
          justifyContent: "flex-start"
        }
      }]
    })
  }

  private getMapHeaderKeyAndConfig (config: ITableHeaderConfig[]): [{ [key: string]: ITableHeaderConfig }, { [key: string]: ITableHeaderConfig }] {
    const map: { [key: string]: ITableHeaderConfig } = {}
    const mapParent: { [key: string]: ITableHeaderConfig } = {}
    config.forEach((c) => this.traverseHeaderConfig(c, null, (cursorConfig, parentConfig) => {
      map[cursorConfig.key] = cursorConfig
      mapParent[cursorConfig.key] = parentConfig
      return false
    }))
    return [map, mapParent]
  }

  private moveUp = () => {
    const { localConfig, mapHeaderParent, currentSelectedKeys } = this.state
    if (currentSelectedKeys.length <= 0) {
      message.warning('请勾选要上移的列')
      return
    }
    currentSelectedKeys.forEach((key) => {
      const parent = mapHeaderParent[key]
      const siblings = parent ? parent.children : localConfig
      const idx = siblings.findIndex((s) => s.key === key)
      if (idx < 1) { return }
      const temp = siblings[idx - 1]
      siblings[idx - 1] = siblings[idx]
      siblings[idx] = temp
    })
    this.setState({
      localConfig: [...localConfig]
    })
  }

  private moveDown = () => {
    const { localConfig, mapHeaderParent, currentSelectedKeys } = this.state
    if (currentSelectedKeys.length <= 0) {
      message.warning('请勾选要下移的列')
      return
    }
    currentSelectedKeys.forEach((key) => {
      const parent = mapHeaderParent[key]
      const siblings = parent ? parent.children : localConfig
      const idx = siblings.findIndex((s) => s.key === key)
      if (idx >= siblings.length - 1) { return }
      const temp = siblings[idx]
      siblings[idx] = siblings[idx + 1]
      siblings[idx + 1] = temp
    })
    this.setState({
      localConfig: [...localConfig]
    })
  }

  private mergeColumns = () => {
    const { localConfig, mapHeader, mapHeaderParent, currentSelectedKeys } = this.state
    if (currentSelectedKeys.length <= 0) {
      message.warning('请勾选要合并的列')
      return
    }
    const ancestors = []
    currentSelectedKeys.forEach((key) => {
      let cursorConfig = mapHeader[key]
      while (true) {
        if (currentSelectedKeys.includes(cursorConfig.key)) {
          const parent = mapHeaderParent[cursorConfig.key]
          if (!parent) { break }
          cursorConfig = parent
        } else {
          break
        }
      }
      if (ancestors.findIndex((c) => c.key === cursorConfig.key) < 0) {
        ancestors.push(cursorConfig)
      }
    })

    const isTop = ancestors.every((config) => !mapHeaderParent[config.key])
    if (!isTop) {
      message.warning('勾选的列应是当前最上级列')
      return
    }

    const insertConfig: ITableHeaderConfig = {
      key: uuid(5),
      headerName: `新建合并列`,
      alias: null,
      visualType: null,
      isGroup: true,
      style: {
        ...DefaultTableCellStyle,
        justifyContent: 'center'
      },
      children: ancestors
    }

    let minIdx = localConfig.length - ancestors.length
    minIdx = ancestors.reduce((min, config) => Math.min(min,
      localConfig.findIndex((c) => c.key === config.key)), minIdx)
    const ancestorKeys = ancestors.map((c) => c.key)
    const newLocalConfig = localConfig.filter((c) => !ancestorKeys.includes(c.key))
    newLocalConfig.splice(minIdx, 0, insertConfig)
    const [newMapHeader, newMapHeaderParent] = this.getMapHeaderKeyAndConfig(newLocalConfig)

    this.setState({
      localConfig: newLocalConfig,
      mapHeader: newMapHeader,
      mapHeaderParent: newMapHeaderParent,
      currentEditingConfig: insertConfig,
      currentSelectedKeys: []
    }, () => {
      this.headerNameInput.current.focus()
      this.headerNameInput.current.select()
    })
  }

  private cancel = () => {
    const { localConfig } = this.state
    // 因为可能改变了顺序，所以取消时要还原顺序(在表头设置中，localConfig的长度应该不会变)
    const temp = []
    this.initLocalConfig.forEach((item) => {
      for (let i = 0; i < localConfig.length; i++) {
        if (item.key === localConfig[i].key) {
          temp.push(localConfig[i])
          break
        }
      }
    })
    this.setState({
      localConfig: temp
    }, () => this.props.onCancel())
  }

  private save = () => {
    this.props.onSave(this.state.localConfig)
  }

  private traverseHeaderConfig (
    config: ITableHeaderConfig,
    parentConfig: ITableHeaderConfig,
    cb: (cursorConfig: ITableHeaderConfig, parentConfig?: ITableHeaderConfig) => boolean
  ) {
    let hasFound = cb(config, parentConfig)
    if (hasFound) { return hasFound }
    hasFound = Array.isArray(config.children) &&
      config.children.some((c) => this.traverseHeaderConfig(c, config, cb))
    return hasFound
  }

  private propChange = (record: ITableHeaderConfig, propName) => (e) => {
    const value = e.target ? e.target.value : e
    const { localConfig } = this.state
    const { key } = record
    const cb = (cursorConfig: ITableHeaderConfig) => {
      const isTarget = key === cursorConfig.key
      if (isTarget) {
        // isTarget是用来判断现在是点的表格的哪一列
        cursorConfig.style[propName] = value
        if (propName === 'hide') {
          // 先要进行判断，除了该列，其他所有列至少有一列没有被隐藏，否则给与用户提示：“至少要显示一列数据”
          let atLeastDisplayOneColumn = false
          for(let i = 0; i < localConfig.length; i++) {
            if (key !== localConfig[i].key) {
              if (!localConfig[i].hide) {
                atLeastDisplayOneColumn = true
                break
              }
            }
          }
          if (atLeastDisplayOneColumn) {
            cursorConfig['hide'] = cursorConfig['hide'] ? false : true
          } else {
            message.warning('至少要显示一列数据！')
          }
        }
      }
      return isTarget
    }
    localConfig.some((config) => this.traverseHeaderConfig(config, null, cb))
    this.setState({
      localConfig: [...localConfig]
    })
  }

  // 批量修改样式 弹框里的样式修改
  private styleChangeModalChange = (record: ITableHeaderConfig, propName) => (e) => {
    const value = e.target ? e.target.value : e
    const tmpObj = this.state.styleChangeDefaultConfig[0].style
    tmpObj[propName] = value
    this.setState({
      styleChangeDefaultConfig: [{
        style: tmpObj
      }],
    })
  }

  private openStyleChangeModal = () => {
    // 初始化 + 打开弹框
    this.setState({
      styleChangeDefaultConfig: [{
        style: {
          backgroundColor: "transparent",
          fontColor: "#666",
          fontFamily: "PingFang SC",
          fontSize: "12",
          fontStyle: "normal",
          fontWeight: "normal",
          justifyContent: "flex-start"
        }
      }],
      styleChangeModalVisible: true
    })
  }

  // 交换数组两个元素的位置
  private swapArray = (arr, index1, index2) => {
    // arr[index1] = arr.splice(index2, 1, arr[index1])[0];

    // 更换两项元素的seq值
    const tempSeq = arr[index1].seq
    arr[index1].seq = arr[index2].seq
    arr[index2].seq = tempSeq

    // 更换两项元素，这个切换本身是不会保存在数据中的，只是用于立即在页面上显示效果，真正的持久化的数据是各列的seq值，打开表头设置弹框时，根据seq值排序各列
    // 但因为这个操作直接改变了localConfig里的数据（可以在子组件中直接改变父组件中的对象中的属性），所以这个改变即时不保存，在关闭弹框后也还是会保留，而上面的seq属性就不会
    const tempItem = arr[index1]
    arr[index1] = arr[index2]
    arr[index2] = tempItem

    return arr;
 }

  private goUpOrDown = (key, direction) => (e) => {
    const { localConfig } = this.state
    if (localConfig.length && !localConfig[0].seq) {
      // 如果localConfig里面的seq是undefined的话，则给一个初始的顺序
      localConfig.forEach((item, index) => {
        localConfig[index].seq = index + 1
      })
    }
    const cb = (cursorConfig: ITableHeaderConfig) => {
      const isTarget = key === cursorConfig.key
      if (isTarget) {
        // 记录当前是哪一行要往上移一行
        let index = 0
        for (let i = 0; i < localConfig.length; i++) {
          if (key === localConfig[i].key) {
            index = i
            break
          }
        }
        this.swapArray(localConfig, direction === 'up' ? index - 1 : index + 1, index)
      }
      return isTarget
    }
    localConfig.some((config) => this.traverseHeaderConfig(config, null, cb))
    this.setState({
      localConfig: [...localConfig]
    })
  }

  private editHeaderName = (key: string) => () => {
    const { localConfig } = this.state
    localConfig.some((config) => (
      this.traverseHeaderConfig(config, null, (cursorConfig) => {
        const hasFound = cursorConfig.key === key
        if (hasFound) {
          this.setState({
            currentEditingConfig: cursorConfig
          }, () => {
            this.headerNameInput.current.focus()
            this.headerNameInput.current.select()
          })
        }
        return hasFound
      })
    ))
  }

  private deleteHeader = (key: string) => () => {
    const { localConfig, mapHeader, mapHeaderParent } = this.state
    localConfig.some((config) => (
      this.traverseHeaderConfig(config, null, (cursorConfig) => {
        const hasFound = cursorConfig.key === key
        if (hasFound) {
          const parent = mapHeaderParent[cursorConfig.key]
          let idx
          if (parent) {
            idx = parent.children.findIndex((c) => c.key === cursorConfig.key)
            parent.children.splice(idx, 1, ...cursorConfig.children)
          } else {
            idx = localConfig.findIndex((c) => c.key === cursorConfig.key)
            localConfig.splice(idx, 1, ...cursorConfig.children)
          }
        }
        return hasFound
      })
    ))
    const [newMapHeader, newMapHeaderParent] = this.getMapHeaderKeyAndConfig(localConfig)
    this.setState({
      mapHeader: newMapHeader,
      mapHeaderParent: newMapHeaderParent,
      localConfig
    })
  }

  private saveEditingHeaderName = (e) => {
    const value = e.target.value
    if (!value) {
      message.warning('请输入和并列名称')
      return
    }
    const { localConfig, currentEditingConfig } = this.state
    localConfig.some((config) => (
      this.traverseHeaderConfig(config, null, (cursorConfig) => {
        const hasFound = cursorConfig.key === currentEditingConfig.key
        if (hasFound) {
          cursorConfig.headerName = value
        }
        return hasFound
      })
    ))
    this.setState({
      localConfig: [...localConfig],
      currentEditingConfig: null
    })
  }

  private columns: Array<ColumnProps<any>> = [{
    title: '表格列',
    dataIndex: 'headerName',
    key: 'headerName',
    render: (_, record: ITableHeaderConfig) => {
      const { currentEditingConfig } = this.state
      const { key, headerName, alias, isGroup } = record
      if (!currentEditingConfig || currentEditingConfig.key !== key) {
        return isGroup ? (
          <span className={styles.tableEditCell}>
            <label>{alias || headerName}</label>
            <Icon type="edit" onClick={this.editHeaderName(key)} />
            <Icon type="delete" onClick={this.deleteHeader(key)} />
          </span>
        ) : (<label>{alias || headerName}</label>)
      }
      const { headerName: currentEditingHeaderName } = currentEditingConfig
      return (
        <Input
          ref={this.headerNameInput}
          className={styles.tableInput}
          defaultValue={currentEditingHeaderName}
          onBlur={this.saveEditingHeaderName}
          onPressEnter={this.saveEditingHeaderName}
        />
      )
    }
  }, {
    title: '上移',
    dataIndex: 'up',
    key: 'up',
    width: 60,
    render: (_, record: ITableHeaderConfig) => {
      const { localConfig } = this.state
      const { key } = record
      if (localConfig && localConfig.length && key !== localConfig[0].key) {
        // 说明此时不是首行
        return (
          <Row type="flex" justify="center">
            <Col>
              <Icon type="arrow-up" style={{fontSize: '18px', cursor: 'pointer'}} onClick={this.goUpOrDown(key, 'up')} />
            </Col>
          </Row>
        )
      }
      return null
    }
  }, {
    title: '下移',
    dataIndex: 'down',
    key: 'down',
    width: 60,
    render: (_, record: ITableHeaderConfig) => {
      const { localConfig } = this.state
      const { key } = record
      if (localConfig && localConfig.length && key !== localConfig[localConfig.length - 1].key) {
        // 说明此时不是最后一行
        return (
          <Row type="flex" justify="center">
            <Col>
              <Icon type="arrow-down" style={{fontSize: '18px', cursor: 'pointer'}} onClick={this.goUpOrDown(key, 'down')} />
            </Col>
          </Row>
        )
      }
      return null
    }
  }, {
    title: '是否隐藏',
    dataIndex: 'hide',
    key: 'hide',
    width: 80,
    render: (_, record: ITableHeaderConfig) => {
      const { hide } = record
      return (
        <Row type="flex" justify="center">
          <Col>
            <Checkbox checked={hide} onChange={this.propChange(record, 'hide')}></Checkbox>
          </Col>
        </Row>
      )
    }
  }, {
    title: '背景色',
    dataIndex: 'backgroundColor',
    key: 'backgroundColor',
    width: 60,
    render: (_, record: ITableHeaderConfig) => {
      const { style } = record
      const { backgroundColor } = style
      return (
        <Row type="flex" justify="center">
          <Col>
            <ColorPicker
              className={stylesConfig.color}
              value={backgroundColor}
              onChange={this.propChange(record, 'backgroundColor')}
            />
          </Col>
        </Row>
      )
    }
  }, {
    title: '字体',
    dataIndex: 'font',
    key: 'font',
    width: 285,
    render: (_, record: ITableHeaderConfig) => {
      const { style } = record
      const { fontSize, fontFamily, fontColor, fontStyle, fontWeight } = style
      return (
        <>
          <Row gutter={8} type="flex" align="middle" className={stylesConfig.rowBlock}>
            <Col span={14}>
              <Select
                size="small"
                className={stylesConfig.colControl}
                placeholder="字体"
                value={fontFamily}
                onChange={this.propChange(record, 'fontFamily')}
              >
                {fontFamilyOptions}
              </Select>
            </Col>
            <Col span={6}>
              <Select
                size="small"
                className={stylesConfig.colControl}
                placeholder="文字大小"
                value={fontSize}
                onChange={this.propChange(record, 'fontSize')}
              >
                {fontSizeOptions}
              </Select>
            </Col>
            <Col span={4}>
              <ColorPicker
                className={stylesConfig.color}
                value={fontColor}
                onChange={this.propChange(record, 'fontColor')}
              />
            </Col>
          </Row>
          <Row gutter={8} type="flex" align="middle" className={stylesConfig.rowBlock}>
            <Col span={12}>
              <Select
                size="small"
                className={stylesConfig.colControl}
                value={fontStyle}
                onChange={this.propChange(record, 'fontStyle')}
              >
                {fontStyleOptions}
              </Select>
            </Col>
            <Col span={12}>
              <Select
                size="small"
                className={stylesConfig.colControl}
                value={fontWeight}
                onChange={this.propChange(record, 'fontWeight')}
              >
                {fontWeightOptions}
              </Select>
            </Col>
          </Row>
        </>
      )
    }
  }, {
    title: '对齐',
    dataIndex: 'justifyContent',
    key: 'justifyContent',
    width: 180,
    render: (_, record: ITableHeaderConfig) => {
      const { style } = record
      const { justifyContent } = style
      return (
        <RadioGroup size="small" value={justifyContent} onChange={this.propChange(record, 'justifyContent')}>
          <RadioButton value="flex-start">左对齐</RadioButton>
          <RadioButton value="center">居中</RadioButton>
          <RadioButton value="flex-end">右对齐</RadioButton>
        </RadioGroup>
      )
    }
  }]

  private styleChangeColumns: Array<ColumnProps<any>> = [{
    title: '背景色',
    dataIndex: 'backgroundColor',
    key: 'backgroundColor',
    width: 60,
    render: (_, record: ITableHeaderConfig) => {
      const { style } = record
      const { backgroundColor } = style
      return (
        <Row type="flex" justify="center">
          <Col>
            <ColorPicker
              className={stylesConfig.color}
              value={backgroundColor}
              onChange={this.styleChangeModalChange(record, 'backgroundColor')}
            />
          </Col>
        </Row>
      )
    }
  }, {
    title: '字体',
    dataIndex: 'font',
    key: 'font',
    width: 285,
    render: (_, record: ITableHeaderConfig) => {
      const { style } = record
      const { fontSize, fontFamily, fontColor, fontStyle, fontWeight } = style
      return (
        <>
          <Row gutter={8} type="flex" align="middle" className={stylesConfig.rowBlock}>
            <Col span={14}>
              <Select
                size="small"
                className={stylesConfig.colControl}
                placeholder="字体"
                value={fontFamily}
                onChange={this.styleChangeModalChange(record, 'fontFamily')}
              >
                {fontFamilyOptions}
              </Select>
            </Col>
            <Col span={6}>
              <Select
                size="small"
                className={stylesConfig.colControl}
                placeholder="文字大小"
                value={fontSize}
                onChange={this.styleChangeModalChange(record, 'fontSize')}
              >
                {fontSizeOptions}
              </Select>
            </Col>
            <Col span={4}>
              <ColorPicker
                className={stylesConfig.color}
                value={fontColor}
                onChange={this.styleChangeModalChange(record, 'fontColor')}
              />
            </Col>
          </Row>
          <Row gutter={8} type="flex" align="middle" className={stylesConfig.rowBlock}>
            <Col span={12}>
              <Select
                size="small"
                className={stylesConfig.colControl}
                value={fontStyle}
                onChange={this.styleChangeModalChange(record, 'fontStyle')}
              >
                {fontStyleOptions}
              </Select>
            </Col>
            <Col span={12}>
              <Select
                size="small"
                className={stylesConfig.colControl}
                value={fontWeight}
                onChange={this.styleChangeModalChange(record, 'fontWeight')}
              >
                {fontWeightOptions}
              </Select>
            </Col>
          </Row>
        </>
      )
    }
  }, {
    title: '对齐',
    dataIndex: 'justifyContent',
    key: 'justifyContent',
    width: 180,
    render: (_, record: ITableHeaderConfig) => {
      const { style } = record
      const { justifyContent } = style
      return (
        <RadioGroup size="small" value={justifyContent} onChange={this.styleChangeModalChange(record, 'justifyContent')}>
          <RadioButton value="flex-start">左对齐</RadioButton>
          <RadioButton value="center">居中</RadioButton>
          <RadioButton value="flex-end">右对齐</RadioButton>
        </RadioGroup>
      )
    }
  }]

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

  private tableRowSelection: TableRowSelection<ITableHeaderConfig> = {
    hideDefaultSelections: true,
    onChange: (selectedRowKeys: string[]) => {
      this.setState({
        currentSelectedKeys: selectedRowKeys
      })
    }
    // @FIXME data columns do not allow check
    // getCheckboxProps: (record) => ({
    //   disabled: !record.isGroup
    // })
  }

  // 点击 批量修改样式 弹框的OK
  private styleChangeOk = () => {
    const tempConfig = this.state.localConfig
    tempConfig.forEach((config, index) => {
      if (this.state.currentSelectedKeys.includes(config.key)) {
        tempConfig[index].style = this.state.styleChangeDefaultConfig[0].style
      }
      this.setChildrenStyle(config)
    })
    this.setState({
      localConfig: tempConfig,
      styleChangeModalVisible: false
    })
  }

  private setChildrenStyle = (item) => {
    if (Array.isArray(item.children) && item.children.length > 0) {
      for (let i = 0; i < item.children.length; i++) {
        if (this.state.currentSelectedKeys.includes(item.children[i].key)) {
          item.children[i].style = this.state.styleChangeDefaultConfig[0].style
        }
        if (Array.isArray(item.children[i].children) && item.children[i].children.length > 0) this.setChildrenStyle(item.children[i])
      }
    }
  }

  // 点击 批量修改样式 弹框的Cancel
  private styleChangeCancel = () => {
    this.setState({styleChangeModalVisible: false})
  }

  public render () {
    const { visible } = this.props
    const { localConfig, currentSelectedKeys, styleChangeDefaultConfig } = this.state
    const rowSelection: TableRowSelection<ITableHeaderConfig> = {
      ...this.tableRowSelection,
      selectedRowKeys: currentSelectedKeys
    }
    const wrapTableCls = classnames({
      [stylesConfig.rows]: true,
      [styles.headerTable]: true
    })

    return (
      <Modal
        title="表头设置"
        width={1000}
        maskClosable={false}
        footer={this.modalFooter}
        visible={visible}
        onCancel={this.cancel}
        onOk={this.save}
      >
        <div className={stylesConfig.rows}>
          <Row gutter={8} className={stylesConfig.rowBlock} type="flex" align="middle">
            <Col span={6}>
              <Button type="primary" onClick={this.mergeColumns}>合并</Button>
              <Button type="primary" style={{marginLeft: '10px'}} disabled={currentSelectedKeys.length <= 0} onClick={this.openStyleChangeModal}>批量修改样式</Button>
            </Col>
            <Modal
              title="批量样式修改"
              visible={this.state.styleChangeModalVisible}
              onOk={this.styleChangeOk}
              onCancel={this.styleChangeCancel}
              width={800}
            >
              <Table
                bordered={true}
                pagination={false}
                columns={this.styleChangeColumns}
                dataSource={styleChangeDefaultConfig}
              />
            </Modal>
            <Col span={17}>
              {/* <Row gutter={8} type="flex" justify="end" align="middle">
                <ButtonGroup>
                  <Button onClick={this.moveUp}><Icon type="arrow-up" />上移</Button>
                  <Button onClick={this.moveDown}>下移<Icon type="arrow-down" /></Button>
                </ButtonGroup>
              </Row> */}
            </Col>
            <Col span={1}>
              <Row type="flex" justify="end">
                <Tooltip
                  title="表格数据列请在外部拖拽以更改顺序"
                >
                  <Icon type="info-circle" />
                </Tooltip>
              </Row>
            </Col>
          </Row>
        </div>
        <div className={wrapTableCls}>
          <Row gutter={8} className={stylesConfig.rowBlock}>
            <Col span={24}>
              {/* 这个Table的rowSelection逻辑是，父和子节点是单独被选中，如果选中父节点，子节点不会被同时联动选中，各个节点的选中逻辑是单独的 */}
              <Table
                bordered={true}
                pagination={false}
                columns={this.columns}
                dataSource={localConfig}
                rowSelection={rowSelection}
              />
            </Col>
          </Row>
        </div>
      </Modal>
    )
  }
}

export default HeaderConfigModal
