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

import React, { PropTypes } from 'react'
import { connect } from 'react-redux'
import { createStructuredSelector } from 'reselect'
import { Link } from 'react-router'
import classnames from 'classnames'

import Container from '../../components/Container'
import DashboardForm from './components/DashboardForm'
import Row from 'antd/lib/row'
import Col from 'antd/lib/col'
import Button from 'antd/lib/button'
import Icon from 'antd/lib/icon'
import Tooltip from 'antd/lib/tooltip'
import Modal from 'antd/lib/modal'
import Breadcrumb from 'antd/lib/breadcrumb'
import Popconfirm from 'antd/lib/popconfirm'

import { promiseDispatcher } from '../../utils/reduxPromisation'
import { loadDashboards, addDashboard, editDashboard, deleteDashboard } from './actions'
import { makeSelectDashboards } from './selectors'
import { makeSelectLoginUser } from '../App/selectors'

import utilStyles from '../../assets/less/util.less'
import styles from './Dashboard.less'

export class Dashboard extends React.Component {
  constructor (props) {
    super(props)
    this.state = {
      modalLoading: false,

      formType: '',
      formVisible: false
    }
  }

  componentWillMount () {
    this.props.onLoadDashboards()
  }

  toGrid = (dashboard) => () => {
    this.props.router.push(`/visual/report/grid/${dashboard.id}`)
  }

  showDashboardForm = (formType, dashboard) => (e) => {
    e.stopPropagation()
    this.setState({
      formType: formType,
      formVisible: true
    }, () => {
      if (dashboard) {
        this.dashboardForm.setFieldsValue(dashboard)
      }
    })
  }

  hideDashboardForm = () => {
    this.setState({
      formVisible: false,
      modalLoading: false
    }, () => {
      this.dashboardForm.resetFields()
    })
  }

  stopPPG = (e) => {
    e.stopPropagation()
  }

  onModalOk = () => {
    this.dashboardForm.validateFieldsAndScroll((err, values) => {
      if (!err) {
        this.setState({ modalLoading: true })
        if (this.state.formType === 'add') {
          this.props.onAddDashboard(Object.assign({}, values, {
            pic: `${Math.ceil(Math.random() * 19)}`
          }))
            .then(() => { this.hideDashboardForm() })
        } else {
          this.props.onEditDashboard(values)
            .then(() => { this.hideDashboardForm() })
        }
      }
    })
  }

  render () {
    const {
      dashboards,
      loginUser,
      onDeleteDashboard
    } = this.props

    const {
      modalLoading,
      formType,
      formVisible
    } = this.state

    const dashboardItems = dashboards
      ? dashboards.map(d => {
        let editButton = ''
        let deleteButton = ''

        if (loginUser.admin) {
          editButton = (
            <Tooltip title="编辑">
              <Icon className={styles.edit} type="setting" onClick={this.showDashboardForm('edit', d)} />
            </Tooltip>
          )
          deleteButton = (
            <Popconfirm
              title="确定删除？"
              placement="bottom"
              onConfirm={onDeleteDashboard(d.id)}
            >
              <Tooltip title="删除">
                <Icon className={styles.delete} type="delete" onClick={this.stopPPG} />
              </Tooltip>
            </Popconfirm>
          )
        }

        const itemClass = classnames({
          [styles.unit]: true,
          [styles.editing]: !d.publish
        })

        const editHint = !d.publish && '(编辑中…)'

        return (
          <Col
            key={d.id}
            xl={4} lg={6} md={8} sm={12} xs={24}
          >
            <div
              className={itemClass}
              style={{backgroundImage: `url(${require(`../../assets/images/bg${d.pic}.png`)})`}}
              onClick={this.toGrid(d)}
            >
              <header>
                <h3 className={styles.title}>
                  {d.name} {editHint}
                </h3>
                <p className={styles.content}>
                  {d.desc}
                </p>
              </header>
              {editButton}
              {deleteButton}
            </div>
          </Col>
        )
      })
      : ''

    const modalButtons = ([
      <Button
        key="back"
        size="large"
        onClick={this.hideDashboardForm}>
        取 消
      </Button>,
      <Button
        key="submit"
        size="large"
        type="primary"
        loading={modalLoading}
        disabled={modalLoading}
        onClick={this.onModalOk}>
        保 存
      </Button>
    ])

    const addButton = loginUser.admin
      ? (
        <Button
          size="large"
          type="primary"
          icon="plus"
          onClick={this.showDashboardForm('add')}
        >
          新 增
        </Button>
      ) : ''

    return (
      <Container>
        <Container.Title>
          <Row>
            <Col span={18}>
              <Breadcrumb className={utilStyles.breadcrumb}>
                <Breadcrumb.Item>
                  <Link>
                    Dashboard
                  </Link>
                </Breadcrumb.Item>
              </Breadcrumb>
            </Col>
            <Col span={6} className={utilStyles.textAlignRight}>
              {addButton}
            </Col>
          </Row>
        </Container.Title>
        <Container.Body>
          <Row gutter={20}>
            {dashboardItems}
          </Row>
        </Container.Body>
        <Modal
          title={`${formType === 'add' ? '新增' : '修改'}Dashboard`}
          wrapClassName="ant-modal-small"
          visible={formVisible}
          footer={modalButtons}
          onCancel={this.hideDashboardForm}
        >
          <DashboardForm
            type={formType}
            ref={(f) => { this.dashboardForm = f }}
          />
        </Modal>
      </Container>
    )
  }
}

Dashboard.propTypes = {
  dashboards: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.array
  ]),
  loginUser: PropTypes.object,
  router: PropTypes.any,
  onLoadDashboards: PropTypes.func,
  onAddDashboard: PropTypes.func,
  onEditDashboard: PropTypes.func,
  onDeleteDashboard: PropTypes.func
}

const mapStateToProps = createStructuredSelector({
  dashboards: makeSelectDashboards(),
  loginUser: makeSelectLoginUser()
})

export function mapDispatchToProps (dispatch) {
  return {
    onLoadDashboards: () => promiseDispatcher(dispatch, loadDashboards),
    onAddDashboard: (dashboard) => promiseDispatcher(dispatch, addDashboard, dashboard),
    onEditDashboard: (dashboard) => promiseDispatcher(dispatch, editDashboard, dashboard),
    onDeleteDashboard: (id) => () => promiseDispatcher(dispatch, deleteDashboard, id)
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Dashboard)
