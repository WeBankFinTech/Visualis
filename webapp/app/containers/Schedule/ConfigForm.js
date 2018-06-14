/*
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

import React, { PropTypes } from 'react'
import Form from 'antd/lib/form'
import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
import Input from 'antd/lib/input'
import Select from 'antd/lib/select'
import TreeSelect from 'antd/lib/tree-select'
const SHOW_PARENT = TreeSelect.SHOW_PARENT
const FormItem = Form.Item

import utilStyles from '../../assets/less/util.less'

export class ConfigForm extends React.PureComponent {
  render () {
    const { getFieldDecorator } = this.props.form
    const { dashboardTree, dashboardTreeValue, treeSelect, treeChange, loadTreeData } = this.props
    const commonFormItemStyle = {
      labelCol: { span: 4 },
      wrapperCol: { span: 18 }
    }
    const treeSelectProps = {
      size: 'large',
      multiple: true,
      maxHeight: 400,
      overflow: 'auto',
      treeCheckable: true,
      onChange: treeChange,
      onSelect: treeSelect,
      treeData: dashboardTree,
      value: dashboardTreeValue,
      loadData: loadTreeData,
      showCheckedStrategy: SHOW_PARENT,
      searchPlaceholder: 'Please select'
    }
    return (
      <Form>
        <Row>
          <Col>
            <FormItem
              label="主题"
              labelCol={{span: 2}}
              wrapperCol={{span: 21}}
            >
              {getFieldDecorator('subject', {
                rules: [{
                  required: true,
                  message: 'Name 不能为空'
                }]
              })(
                <Input placeholder="subject" />
              )}
            </FormItem>
          </Col>
        </Row>
        <Row>
          <Col span={12}>
            <FormItem className={utilStyles.hide}>
              {getFieldDecorator('id', {
                hidden: this.props.type === 'add'
              })(
                <Input />
              )}
            </FormItem>
            <FormItem
              label="收件人"
              {...commonFormItemStyle}
            >
              {getFieldDecorator('to', {
                rules: [{
                  required: true,
                  message: 'Name 不能为空'
                }]
              })(
                <Input placeholder="to" />
              )}
            </FormItem>
          </Col>
          <Col span={12}>
            <FormItem
              label="抄送"
              {...commonFormItemStyle}
            >
              {getFieldDecorator('cc', {

              })(
                <Input placeholder="cc" />
              )}
            </FormItem>
          </Col>
        </Row>
        <Row>
          <Col span={12}>
            <FormItem
              label="私密发送"
              {...commonFormItemStyle}
            >
              {getFieldDecorator('bcc', {
                initialValue: ''
              })(
                <Input placeholder="bcc" />
              )}
            </FormItem>
          </Col>
          <Col span={12}>
            <FormItem
              label="文件类型"
              {...commonFormItemStyle}
            >
              {getFieldDecorator('type', {
                initialValue: 'image'
              })(
                <Select>
                  <Option value="excel">excel</Option>
                  <Option value="image">image</Option>
                </Select>
              )}
            </FormItem>
          </Col>
        </Row>
        <Row>
          <div className="ant-col-2 ant-form-item-label">
            <label className="" title="发送项">发送项</label>
          </div>
          <Col span={21}>
            <TreeSelect {...treeSelectProps} />
          </Col>
        </Row>
      </Form>
    )
  }
}

ConfigForm.propTypes = {
  form: PropTypes.any,
  type: PropTypes.string,
  dashboardTree: PropTypes.array,
  treeSelect: PropTypes.func,
  treeChange: PropTypes.func,
  loadTreeData: PropTypes.func,
  dashboardTreeValue: PropTypes.any
}

export default Form.create()(ConfigForm)
