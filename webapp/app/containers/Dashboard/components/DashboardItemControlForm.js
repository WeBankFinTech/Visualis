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

import React, { PropTypes, PureComponent } from 'react'

import Form from 'antd/lib/form'
import Input from 'antd/lib/input'
import InputNumber from 'antd/lib/input-number'
import Select from 'antd/lib/select'
import DatePicker from 'antd/lib/date-picker'
import Button from 'antd/lib/button'
import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
const FormItem = Form.Item
const Option = Select.Option
const RangePicker = DatePicker.RangePicker

import styles from '../Dashboard.less'

export class DashboardItemControlForm extends PureComponent {
  constructor (props) {
    super(props)
    this.state = {
      parentSelValues: null
    }
  }

  componentWillMount () {
    this.generateParentSelValues(this.props)
  }

  componentWillUpdate (nextProps) {
    const currentControlIds = this.props.controls.map(c => c.key).join(',')
    const nextControlIds = nextProps.controls.map(c => c.key).join(',')
    if (currentControlIds !== nextControlIds) {
      this.generateParentSelValues(nextProps)
    }
  }

  generateParentSelValues = (props) => {
    this.state.parentSelValues = props.controls
      .filter(c => c.sub.length)
      .reduce((acc, c) => {
        acc[c.id] = 0
        return acc
      }, {})
  }

  generateFormComponent = (c) => {
    const {
      form
    } = this.props

    const { getFieldDecorator } = form

    switch (c.type) {
      case 'inputNumber':
        return (
          <Col
            key={c.id}
            lg={6}
            md={8}
            sm={12}
          >
            <FormItem className={styles.formItem}>
              {getFieldDecorator(`${c.id}`, {})(
                <InputNumber placeholder={c.variables[0]} />
              )}
            </FormItem>
          </Col>
        )
      case 'select':
      case 'multiSelect':
        let options = []
        let followComponents = []

        c.sub.forEach((sub, index) => {
          options.push(
            <Option key={sub.id} value={sub.value}>{sub.text}</Option>
          )

          if (sub.variables.length && this.state.parentSelValues[c.id] === index) {
            followComponents = followComponents.concat(this.generateFormComponent(Object.assign({}, sub, {
              id: `sub_${c.id}_${sub.id}`,
              type: sub.variableType
            })))
          }
        })

        let mode = c.type === 'multiSelect'
          ? {
            mode: 'multiple'
          }
          : {
            allowClear: true
          }
        let selProperties = Object.assign({
          placeholder: c.variables[0] || '请选择',
          onChange: this.parentSelectChange(c)
        }, mode)

        followComponents.unshift(
          <Col
            key={c.id}
            lg={6}
            md={8}
            sm={12}
          >
            <FormItem className={styles.formItem}>
              {getFieldDecorator(`${c.id}`, {})(
                <Select {...selProperties}>
                  {options}
                </Select>
              )}
            </FormItem>
          </Col>
        )

        return followComponents
      case 'date':
      case 'datetime':
        let dateFormat = c.type === 'datetime'
          ? {
            format: 'YYYY-MM-DD HH:mm:ss',
            showTime: true
          }
          : {
            format: 'YYYY-MM-DD'
          }
        let dateProperties = Object.assign({}, dateFormat)

        return (
          <Col
            key={c.id}
            lg={6}
            md={8}
            sm={12}
          >
            <FormItem className={styles.formItem}>
              {getFieldDecorator(`${c.id}`, {})(
                <DatePicker {...dateProperties} />
              )}
            </FormItem>
          </Col>
        )
      case 'dateRange':
      case 'datetimeRange':
        let rangeFormat = c.type === 'datetimeRange'
          ? {
            format: 'YYYY-MM-DD HH:mm:ss',
            showTime: true
          }
          : {
            format: 'YYYY-MM-DD'
          }
        let rangeProperties = Object.assign({}, rangeFormat)

        return (
          <Col
            key={c.id}
            lg={6}
            md={8}
            sm={12}
          >
            <FormItem className={styles.formItem}>
              {getFieldDecorator(`${c.id}`, {})(
                <RangePicker {...rangeProperties} />
              )}
            </FormItem>
          </Col>
        )
      default:
        return (
          <Col
            key={c.id}
            lg={6}
            md={8}
            sm={12}
          >
            <FormItem className={styles.formItem}>
              {getFieldDecorator(`${c.id}`, {})(
                <Input placeholder={c.variables[0]} />
              )}
            </FormItem>
          </Col>
        )
    }
  }

  parentSelectChange = (control) => (val) => {
    const { parentSelValues } = this.state

    if (val) {
      const selIndex = control.sub.findIndex(c => c.value === val)
      parentSelValues[control.id] = selIndex

      this.setState({
        parentSelValues: parentSelValues
      }, () => {
        this.props.form.setFieldsValue({
          [`sub_${control.id}_${control.sub[selIndex].id}`]: ''
        })
      })
    } else {
      const selIndex = parentSelValues[control.id]
      parentSelValues[control.id] = 0

      this.props.form.setFieldsValue({
        [`sub_${control.id}_${control.sub[selIndex].id}`]: ''
      })

      this.setState({
        parentSelValues: parentSelValues
      })
    }
  }

  onControlSearch = () => {
    const { controls, onSearch, onHide } = this.props

    const formValues = this.props.form.getFieldsValue()

    const params = Object.keys(formValues).reduce((arr, key) => {
      let val = formValues[key]

      let valControl

      if (key.indexOf('sub') >= 0) {
        const idArr = key.split('_')
        valControl = controls.find(c => c.id === idArr[1]).sub.find(s => s.id === idArr[2])
      } else {
        valControl = controls.find(c => c.id === key)
      }

      valControl.type = valControl.variableType || valControl.type

      if (Object.prototype.toString.call(val) === '[object Array]') {
        val = val.map(v => {
          switch (valControl.type) {
            case 'dateRange':
              return v.format('YYYY-MM-DD')
            case 'datetimeRange':
              return v.format('YYYY-MM-DD HH:mm:ss')
            default:
              return ''
          }
        })

        arr = arr.concat({
          k: valControl.variables[0],
          v: `'${val[0]}'`
        }).concat({
          k: valControl.variables[1],
          v: `'${val[1]}'`
        })
      } else {
        if (val) {
          if (valControl.variables[0]) {
            switch (valControl.type) {
              case 'date':
                val = val.format('YYYY-MM-DD')
                break
              case 'datetime':
                val = val.format('YYYY-MM-DD HH:mm:ss')
                break
              default:
                break
            }

            arr = arr.concat({
              k: valControl.variables[0],
              v: `'${val}'`
            })
          }
        }
      }
      return arr
    }, [])

    onSearch({
      params
    })
    onHide()
  }

  render () {
    const {
      controls
    } = this.props

    const controlItems = controls
      .map(c => this.generateFormComponent(c))

    return (
      <Form className={styles.controlForm}>
        <Row gutter={10}>
          {controlItems}
        </Row>
        <Row className={styles.buttonRow}>
          <Col span={24}>
            <Button type="primary" onClick={this.onControlSearch}>查询</Button>
          </Col>
        </Row>
      </Form>
    )
  }
}

DashboardItemControlForm.propTypes = {
  form: PropTypes.any,
  controls: PropTypes.array,
  onSearch: PropTypes.func,
  onHide: PropTypes.func
}

export default Form.create({withRef: true})(DashboardItemControlForm)
